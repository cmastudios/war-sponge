package com.tommytony.war.zone;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarPlayer;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class WarGame {
    private final Warzone warzone;
    private final ServerAPI plugin;
    private List<WarPlayer> players;
    private List<Team> teams;
    private List<Attack> attacks;
    private int round;

    WarGame(Warzone warzone, ServerAPI plugin) {
        this.warzone = warzone;
        this.plugin = plugin;
        players = new ArrayList<>();
        teams = new ArrayList<>();
        teams.addAll(warzone.getTeams().stream().map(Team::new).collect(Collectors.toList()));
        round = 0;
    }

    public Team getTeam(String teamName) {
        return teams.stream().filter(team -> team.getName().equalsIgnoreCase(teamName)).findFirst().orElse(null);
    }

    public Team getPlayerTeam(WarPlayer player) {
        return teams.stream().filter(team -> team.players.contains(player)).findFirst().orElse(null);
    }

    public boolean isPlaying(WarPlayer player) {
        return players.contains(player);
    }

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

    public void assign(WarPlayer player, Team team) {
        if (team == null) {
            throw new IllegalStateException("Team does not exist.");
        }
        if (players.size() >= warzone.getConfig().getInt(ZoneSetting.MAXPLAYERS)) {
            throw new IllegalStateException("Warzone is full.");
        }
        players.add(player);
        team.players.add(player);
        player.setLocation(warzone.getTeamSpawn(team.name));
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

    public void broadcast(String message) {
        players.stream().forEach(p -> p.sendMessage(message));
    }

    public void onDeath(WarPlayer player) {
        Team playerTeam = getPlayerTeam(player);
        Optional<Attack> first = attacks.stream().filter(a -> a.defender == player).filter(a -> System.currentTimeMillis() - a.time < 3000).findFirst();
        if (first.isPresent()) {
            Team beneficiary = getPlayerTeam(first.get().attacker);
            beneficiary.addPoints(1);
            broadcast(MessageFormat.format("Team {0} gains 1 point.", beneficiary.getName()));
        }
        checkForEndRound();
    }

    public void onAttackByPlayer(WarPlayer attacker, WarPlayer defender) {
        for (Iterator<Attack> iterator = attacks.iterator(); iterator.hasNext(); ) {
            Attack attack = iterator.next();
            if (attack.defender == defender)
                iterator.remove();
        }
        attacks.add(new Attack(attacker, defender, System.currentTimeMillis()));
    }

    public void checkForEndRound() {
        for (Team team : teams) {
            if (team.getPoints() >= warzone.getConfig().getInt(ZoneSetting.MAXPOINTS)) {
                broadcast(MessageFormat.format("Team {0} wins!", team.getName()));
                endRound();
                return;
            }
        }
    }

    public void endRound() {
        boolean gameOver = round >= warzone.getConfig().getInt(ZoneSetting.MAXROUNDS);
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
            players.stream().forEach(this::removePlayer);
        }
        warzone.reset();
    }

    private void removePlayer(WarPlayer p) {
        getPlayerTeam(p).players.remove(p);
        players.remove(p);
        p.setLocation(warzone.getTeleport());
    }

    public class Team {
        private String name;
        private List<WarPlayer> players;
        private int points;

        public Team(String name) {
            this.name = name;
            players = new ArrayList<>();
            points = 0;
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return points;
        }

        public void addPoints(int i) {
            points += i;
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
    }
}
