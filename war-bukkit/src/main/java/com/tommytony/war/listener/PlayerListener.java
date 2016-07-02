package com.tommytony.war.listener;

import com.tommytony.war.WarPlayer;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.WarDamageCause;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {

    private final WarPlugin plugin;

    public PlayerListener(WarPlugin warPlugin) {
        this.plugin = warPlugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        WarPlayer player = plugin.getWarPlayer(event.getPlayer());
        WarLocation from = plugin.getWarLocation(event.getFrom());
        WarLocation to = plugin.getWarLocation(event.getTo());
        boolean b = plugin.getListener().handlePlayerMovement(player, from, to);
        event.setCancelled(b);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        WarPlayer defender = plugin.getWarPlayer((Player) event.getEntity());
        double damage = event.getFinalDamage();
        WarDamageCause cause;
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edee = (EntityDamageByEntityEvent) event;
            if (edee.getDamager() instanceof TNTPrimed) {
                cause = new WarDamageCause.Explosion(defender);
            } else if (edee.getDamager() instanceof Creature) {
                cause = new WarDamageCause.Creature(defender);
            } else if (edee.getDamager() instanceof Player) {
                WarPlayer attacker = plugin.getWarPlayer((Player) edee.getDamager());
                cause = new WarDamageCause.Combat(defender, attacker);
            } else {
                cause = new WarDamageCause(defender);
            }
        } else if (event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                || event.getCause() == EntityDamageEvent.DamageCause.LAVA || event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            cause = new WarDamageCause.Combustion(defender);
        } else if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            cause = new WarDamageCause.Drowning(defender);
        } else if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            cause = new WarDamageCause.Falling(defender);
        } else {
            cause = new WarDamageCause(defender);
        }
        boolean b = plugin.getListener().handleDamage(defender, damage, cause);
        event.setCancelled(b);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        WarPlayer player = plugin.getWarPlayer(event.getEntity());
        boolean b = plugin.getListener().handleDeath(player, event.getDeathMessage());
        if (b) {
            event.setDeathMessage("");
        }
    }
}
