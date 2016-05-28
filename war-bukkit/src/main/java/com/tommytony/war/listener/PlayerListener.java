package com.tommytony.war.listener;

import com.tommytony.war.WarPlayer;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.struct.WarLocation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
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
    public void onPlayerInteract(PlayerInteractEvent event) {
    }
}
