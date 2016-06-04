package com.tommytony.war.zone;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.struct.WarLocation;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Optional;

/**
 * Handles events that occur in relation to a particular zone.
 */
public class ZoneListener {
    private final Warzone warzone;
    private final ServerAPI plugin;

    ZoneListener(Warzone warzone, ServerAPI plugin) {
        this.warzone = warzone;
        this.plugin = plugin;
    }

    public boolean handlePlayerMovementInWarzone(WarPlayer player, WarLocation from, WarLocation to) {
        return false;
    }

    public boolean handlePlayerLeaveZone(WarPlayer player, WarLocation from, WarLocation to) {
        Optional<WarGame> game = warzone.getGame();
        if (game.isPresent() && game.get().isPlaying(player)) {
            player.sendMessage("Please use /warleave to exit the game before leaving.");
            warzone.mask(player);
            plugin.delayTask(3, () -> warzone.unmask(player));
            return true;
        }
        return false;
    }

    public boolean handlePlayerEnterZone(WarPlayer player, WarLocation from, WarLocation to) {
        Optional<WarGame> game = warzone.getGame();
        if (game.isPresent() && !game.get().isPlaying(player) || !player.isZoneMaker()) {
            player.sendMessage("Please join a team first.");
            player.setLocation(warzone.getTeleport());
            warzone.mask(player);
            plugin.delayTask(3, () -> warzone.unmask(player));
            return false;
        }
        return false;
    }

    public boolean handleCombat(WarPlayer attacker, WarPlayer defender) {
        WarGame game = warzone.getGame().orElseThrow(IllegalStateException::new);
        WarGame.Team attackerTeam = game.getPlayerTeam(attacker);
        WarGame.Team defenderTeam = game.getPlayerTeam(defender);
        if (attackerTeam == defenderTeam) {
            attacker.sendMessage("Do not target your own team!");
            return true; // no friendly fire
        }
        for (Iterator<WarGame.Attack> iterator = game.getAttacks().iterator(); iterator.hasNext(); ) {
            WarGame.Attack attack = iterator.next();
            if (attack.getDefender() == defender)
                iterator.remove();
        }
        game.addAttack(attacker, defender);
        return false;
    }

    public boolean handleDeath(WarPlayer player, String deathMessage) {
        WarGame game = warzone.getGame().orElseThrow(IllegalStateException::new);
        game.broadcast(deathMessage);
        Optional<WarGame.Attack> first = game.getAttacks().stream()
                .filter(a -> a.getDefender() == player)
                .filter(a -> System.currentTimeMillis() - a.getTime() < 3000)
                .findFirst();
        if (first.isPresent()) {
            WarGame.Team beneficiary = game.getPlayerTeam(first.get().getAttacker());
            beneficiary.addPoints(1);
            game.broadcast(MessageFormat.format("Team {0} gains 1 point.", beneficiary.getName()));
        }
        game.checkForEndRound();
        player.setLocation(warzone.getTeamSpawn(game.getPlayerTeam(player).getName()));
        plugin.delayTask(0.1, () -> {
            if (player.isPlayingWar()) game.resetPlayerState(player);
        });
        return true;
    }
}
