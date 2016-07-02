package com.tommytony.war.zone;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.struct.WarLocation;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * Handles events that occur in relation to a particular zone.
 */
public class ZoneListener {
    /**
     * When a player dies, the last attack from another player within this time interval will be treated as the cause of
     * the death, and logged for points. For example, when a player is heavily damaged by another and then jumps off a
     * cliff to escape arrow fire, the player who did the most recent damage will receive credit.
     */
    private static final int LAST_ATTACK_DELAY_MS = 3000;
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

    public boolean handleDeath(WarPlayer player, String deathMessage) {
        WarGame game = warzone.getGame().orElseThrow(IllegalStateException::new);
        plugin.delayTask(3, () -> {
            if (player.isPlayingWar()) game.resetPlayerState(player);
        });
        return true;
    }

    public boolean handleDamage(WarPlayer defender, double damage, WarDamageCause cause) {
        WarGame game = warzone.getGame().orElseThrow(IllegalStateException::new);
        if (cause instanceof WarDamageCause.Combat) {
            WarPlayer attacker = ((WarDamageCause.Combat) cause).getAttacker();
            if (game.getPlayerTeam(defender) == game.getPlayerTeam(attacker)) {
                attacker.sendMessage("Do not target your own team!");
                return true;
            }
            game.addAttack(attacker, defender);
        }
        // prevent skeletons from shooting players in zones
        if (cause instanceof WarDamageCause.Creature) {
            return true;
        }
        // player would die by this blow
        if (defender.getHealth() - damage < 1) {
            // send the death message for the most recent damage cause
            game.broadcast(cause.getDeathMessage());
            // find a recent attack from a player
            Optional<WarGame.Attack> first = game.getAttacks().stream()
                    .filter(a -> a.getDefender() == defender)
                    .filter(a -> System.currentTimeMillis() - a.getTime() < LAST_ATTACK_DELAY_MS)
                    .findFirst();
            if (first.isPresent()) {
                WarGame.Team beneficiary = game.getPlayerTeam(first.get().getAttacker());
                beneficiary.addPoints(1);
                game.broadcast(MessageFormat.format("Team {0} gains 1 point.", beneficiary.getName()));
            }
            game.checkForEndRound();
            game.resetPlayerState(defender);
            game.resetPlayerState(defender);
            return true;
        }
        return false;
    }
}
