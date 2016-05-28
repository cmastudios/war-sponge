package com.tommytony.war.zone;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class WarGame {
    private final Warzone warzone;
    private final ServerAPI plugin;
    private List<WarPlayer> players;
    private List<Team> teams;

    WarGame(Warzone warzone, ServerAPI plugin) {
        this.warzone = warzone;
        this.plugin = plugin;
        players = new ArrayList<>();
        teams = new ArrayList<>();
        teams.addAll(warzone.getTeams().stream().map(Team::new).collect(Collectors.toList()));
    }

    public Team getTeam(String teamName) {
        return teams.stream().filter(team -> team.getName().equalsIgnoreCase(teamName)).findFirst().orElse(null);
    }

    public boolean isPlaying(WarPlayer player) {
        return players.contains(player);
    }

    public class Team {
        private String name;
        private List<WarPlayer> players;

        public Team(String name) {
            this.name = name;
            players = new ArrayList<>();
        }

        public String getName() {
            return name;
        }
    }
}
