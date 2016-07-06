package com.tommytony.war.zone;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.item.WarColor;
import com.tommytony.war.item.WarItem;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Active game in a warzone.
 */
public class WarGame {
    private final Warzone warzone;
    private final ServerAPI plugin;
    private List<WarPlayer> players;
    private List<Team> teams;
    private List<Attack> attacks;
    private Map<WarPlayer, WarPlayer.PlayerState> inventories;
    private int round;

    WarGame(Warzone warzone, ServerAPI plugin) {
        this.warzone = warzone;
        this.plugin = plugin;
        players = new ArrayList<>();
        teams = new ArrayList<>();
        teams.addAll(warzone.getTeams().stream().map(Team::new).collect(Collectors.toList()));
        round = 0;
        attacks = new ArrayList<>();
        inventories = new HashMap<>();
    }

    /**
     * Get team information by name.
     *
     * @param teamName team name to lookup.
     * @return team information or null if not found.
     */
    public Team getTeam(String teamName) {
        return teams.stream().filter(team -> team.getName().equalsIgnoreCase(teamName)).findFirst().orElse(null);
    }

    /**
     * Get the team a particular player is playing for.
     *
     * @param player player to search.
     * @return team information or null if not found.
     */
    public Team getPlayerTeam(WarPlayer player) {
        return teams.stream().filter(team -> team.players.contains(player)).findFirst().orElse(null);
    }

    /**
     * Check if a player is playing in this warzone.
     *
     * @param player player to search.
     * @return true if player is playing.
     */
    public boolean isPlaying(WarPlayer player) {
        return players.contains(player);
    }

    /**
     * Assign a player to a team, picking the team with the least number of players.
     *
     * @param player player to assign.
     */
    public void autoAssign(WarPlayer player) {
        if (teams.size() == 0) {
            throw new IllegalStateException("No teams in this warzone.");
        }
        Optional<Team> first = teams.stream().sorted(Comparator.comparingInt(team -> team.players.size())).findFirst();
        if (!first.isPresent()) {
            throw new IllegalStateException("No teams found.");
        }
        assign(player, first.get());
    }

    /**
     * Assign a player to a team. This then saves the player's state and brings them into the zone.
     *
     * @param player player to assign.
     * @param team   team for player.
     */
    public void assign(WarPlayer player, Team team) {
        if (team == null) {
            throw new IllegalStateException("Team does not exist.");
        }
        if (players.size() >= warzone.getConfig().getInt(ZoneSetting.MAXPLAYERS)) {
            throw new IllegalStateException("Warzone is full.");
        }
        if (isPlaying(player)) {
            throw new IllegalStateException("Already playing in this zone.");
        }
        if (player.isPlayingWar()) {
            throw new IllegalStateException("Already playing in another zone.");
        }
        players.add(player);
        team.players.add(player);
        savePlayerState(player);
        resetPlayerState(player);
        StringBuilder teamPlayers = new StringBuilder();
        List<WarPlayer> players1 = team.players;
        for (int i = 0, players1Size = players1.size(); i < players1Size; i++) {
            WarPlayer teamPlayer = players1.get(i);
            teamPlayers.append(teamPlayer.getName());
            if (i < players1Size - 1)
                teamPlayers.append(", ");
        }
        player.sendMessage(MessageFormat.format("Welcome to team {0}. Points: {1}/{2}. Teammates: {3}",
                team.getName(), team.getPoints(), warzone.getConfig().getInt(ZoneSetting.MAXPOINTS), teamPlayers.toString()));
    }

    private void savePlayerState(WarPlayer player) {
        inventories.put(player, player.getState());
    }

    void resetPlayerState(WarPlayer player) {
        if (!warzone.getGame().isPresent()) {
            return; // do nothing if the game ended (and players have already been teleported out)
        }
        Team team = getPlayerTeam(player);
        WarPlayer.PlayerState newState = new WarPlayer.PlayerState(WarPlayer.WarGameMode.SURVIVAL, new WarItem[]{},
                null, null, null, null, null, 20, 0, 0, 20, 0, 0, false);
        player.setState(newState);
        player.setLocation(warzone.getTeamSpawn(team.getName()));
    }

    private void restorePlayerState(WarPlayer player) {
        player.setState(inventories.get(player));
    }

    /**
     * Send a message to all players in the warzone.
     *
     * @param message message to broadcast.
     */
    public void broadcast(String message) {
        players.stream().forEach(p -> p.sendMessage(message));
    }

    void checkForEndRound() {
        for (Team team : teams) {
            if (team.getPoints() >= warzone.getConfig().getInt(ZoneSetting.MAXPOINTS)) {
                broadcast(MessageFormat.format("Team {0} wins!", team.getName()));
                endRound();
                return;
            }
        }
    }

    /**
     * End the current round in the warzone. This reloads all blocks and resets all team points. If the round counter
     * has exceeded the maximum rounds for this warzone, then all players are removed.
     */
    public void endRound() {
        boolean gameOver = round++ >= warzone.getConfig().getInt(ZoneSetting.MAXROUNDS);
        StringBuilder builder = new StringBuilder();
        if (gameOver) {
            builder.append("Game over!");
        } else {
            builder.append("Round over!");
        }
        builder.append(' ');
        teams.stream().sorted(Comparator.comparingInt(Team::getPoints))
                .forEach(t -> builder.append(MessageFormat.format("{0} has {1} points.", t.getName(), t.getPoints())).append(' '));
        if (!gameOver) {
            builder.append("Resetting warzone for next match...");
        }
        broadcast(builder.toString());
        if (gameOver) {
            ImmutableList.copyOf(players).forEach(this::removePlayer);
        } else {
            players.forEach(this::resetPlayerState);
        }
        warzone.reset();
    }

    /**
     * Publicly remove a player from a warzone, restoring their state and bringing them to the lobby.
     *
     * @param player player to remove
     */
    public void removePlayer(WarPlayer player) {
        String playerName = player.getName();
        String teamName = getPlayerTeam(player).getName();
        removePlayerSilent(player);
        this.broadcast(MessageFormat.format("Player {0} left team {1}.", playerName, teamName));
    }

    private void removePlayerSilent(WarPlayer player) {
        getPlayerTeam(player).players.remove(player);
        players.remove(player);
        player.setLocation(warzone.getTeleport());
        restorePlayerState(player);
        if (players.isEmpty()) {
            warzone.setGame(null); // kill ourselves
        }
    }

    /**
     * End the current game and remove all players, resetting the warzone additionally.
     */
    public void forceEndGame() {
        round = Integer.MAX_VALUE;
        endRound();
    }

    List<Attack> getAttacks() {
        return attacks;
    }

    void addAttack(WarPlayer attacker, WarPlayer defender) {
        for (Iterator<WarGame.Attack> iterator = this.getAttacks().iterator(); iterator.hasNext(); ) {
            WarGame.Attack attack = iterator.next();
            if (attack.getDefender() == defender)
                iterator.remove();
        }
        attacks.add(new Attack(attacker, defender, System.currentTimeMillis()));
    }

    public class Team {
        private String name;
        private List<WarPlayer> players;
        private int points;

        Team(String name) {
            this.name = name;
            players = new ArrayList<>();
            points = 0;
        }

        public String getName() {
            return name;
        }

        int getPoints() {
            return points;
        }

        void addPoints(int i) {
            points += i;
        }

        /**
         * Get the color code for the team, if the name of the team matches a known color.
         *
         * @return color code, or nothing.
         */
        public String getColor() {
            try {
                return WarColor.valueOf(name.toUpperCase()).toString();
            } catch (IllegalArgumentException e) {
                return "";
            }
        }

        public String getDisplayName() {
            return getColor() + getName() + WarColor.WHITE;
        }
    }

    class Attack {
        private WarPlayer attacker, defender;
        private long time;

        Attack(WarPlayer attacker, WarPlayer defender, long time) {
            this.attacker = attacker;
            this.defender = defender;
            this.time = time;
        }

        WarPlayer getDefender() {
            return defender;
        }

        long getTime() {
            return time;
        }

        WarPlayer getAttacker() {
            return attacker;
        }
    }

}
