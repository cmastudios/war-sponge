package com.tommytony.war.listener;

import com.tommytony.war.WarPlayer;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.struct.WarLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        WarPlayer defender = plugin.getWarPlayer((Player) event.getEntity());
        WarPlayer attacker = plugin.getWarPlayer((Player) event.getDamager());
        boolean b = plugin.getListener().handleCombat(attacker, defender);
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
