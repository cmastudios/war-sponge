package com.tommytony.war.listener;

import com.tommytony.war.WarPlayer;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.struct.WarLocation;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;

public class PlayerListener {
    private WarPlugin plugin;

    public PlayerListener(WarPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onPlayerMove(DisplaceEntityEvent.TargetPlayer event) {
        WarPlayer player = plugin.getWarPlayer(event.getTargetEntity());
        WarLocation from = plugin.getWarLocation(event.getFromTransform());
        WarLocation to = plugin.getWarLocation(event.getToTransform());
        boolean b = plugin.getListener().handlePlayerMovement(player, from, to);
        event.setCancelled(b);
    }
}
