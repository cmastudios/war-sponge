package com.tommytony.war.zone;

import com.tommytony.war.WarPlayer;

import java.text.MessageFormat;

public class WarDamageCause {
    private final WarPlayer victim;

    public WarDamageCause(WarPlayer victim) {
        this.victim = victim;
    }

    public String getDeathMessage() {
        return MessageFormat.format("{0} died.", victim.getName());
    }

    public static class Player extends WarDamageCause {

        private final WarPlayer victim;
        private final WarPlayer attacker;
        private final String weapon;

        public Player(WarPlayer victim, WarPlayer attacker) {
            super(victim);
            this.victim = victim;
            this.attacker = attacker;
            weapon = attacker.getItemInHand().getDisplayName();
        }

        @Override
        public String getDeathMessage() {
            return MessageFormat.format("{0} killed {1} using {2}.", attacker.getDisplayName(), victim.getDisplayName(), weapon);
        }
    }
}

