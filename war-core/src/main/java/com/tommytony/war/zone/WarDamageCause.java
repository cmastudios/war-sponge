package com.tommytony.war.zone;

import com.tommytony.war.WarPlayer;

import java.text.MessageFormat;

public class WarDamageCause {
    private final WarPlayer victim;

    public WarDamageCause(WarPlayer victim) {
        this.victim = victim;
    }

    public String getDeathMessage() {
        return MessageFormat.format("{0} died.", victim.getDisplayName());
    }

    public WarPlayer getVictim() {
        return victim;
    }

    public static class Combat extends WarDamageCause {

        private final WarPlayer victim;
        private final WarPlayer attacker;
        private final String weapon;

        public Combat(WarPlayer victim, WarPlayer attacker) {
            super(victim);
            this.victim = victim;
            this.attacker = attacker;
            weapon = attacker.getItemInHand().getDisplayName();
        }

        @Override
        public String getDeathMessage() {
            return MessageFormat.format("{0} killed {1} using {2}.", attacker.getDisplayName(), victim.getDisplayName(), weapon);
        }

        @Override
        public WarPlayer getVictim() {
            return victim;
        }

        public WarPlayer getAttacker() {
            return attacker;
        }

        public String getWeapon() {
            return weapon;
        }
    }

    public static class Explosion extends WarDamageCause {

        public Explosion(WarPlayer victim) {
            super(victim);
        }

        @Override
        public String getDeathMessage() {
            return MessageFormat.format("{0} exploded.", super.victim.getDisplayName());
        }
    }

    public static class Combustion extends WarDamageCause {

        public Combustion(WarPlayer victim) {
            super(victim);
        }

        @Override
        public String getDeathMessage() {
            return MessageFormat.format("{0} burned to a crisp.", super.victim.getDisplayName());
        }
    }

    public static class Drowning extends WarDamageCause {
        public Drowning(WarPlayer victim) {
            super(victim);
        }

        @Override
        public String getDeathMessage() {
            return MessageFormat.format("{0} drowned.", super.victim.getDisplayName());
        }
    }

    public static class Falling extends WarDamageCause {

        public Falling(WarPlayer victim) {
            super(victim);
        }

        @Override
        public String getDeathMessage() {
            return MessageFormat.format("{0} fell to their death.", super.victim.getDisplayName());
        }
    }

    public static class Suicide extends WarDamageCause {

        public Suicide(WarPlayer victim) {
            super(victim);
        }

        @Override
        public String getDeathMessage() {
            return MessageFormat.format("{0} committed seppuku.", super.victim.getDisplayName());
        }
    }

    public static class Creature extends WarDamageCause {

        public Creature(WarPlayer victim) {
            super(victim);
        }
    }

}

