package com.tommytony.war;

import com.google.common.collect.ImmutableSet;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarLocation;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

class BukkitWarPlayer extends WarPlayer {
    private final WarPlugin plugin;

    BukkitWarPlayer(UUID playerId, WarPlugin warPlugin) {
        super(playerId, warPlugin);
        plugin = warPlugin;
    }

    private Player getPlayer() {
        return plugin.getServer().getPlayer(getPlayerId());
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    @Override
    public WarLocation getLocation() {
        return plugin.getWarLocation(getPlayer().getLocation());
    }

    @Override
    public void setLocation(WarLocation location) {
        getPlayer().teleport(plugin.getBukkitLocation(location), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public void sendMessage(String message) {
        getPlayer().sendMessage(message);
    }

    @Override
    public WarLocation getTargetBlock() {
        Block targetBlock = getPlayer().getTargetBlock(ImmutableSet.of(Material.AIR), 128);
        if (targetBlock == null || targetBlock.isEmpty())
            return null;
        return plugin.getWarLocation(targetBlock.getLocation());
    }

    @Override
    public boolean isZoneMaker() {
        return getPlayer().hasPermission("war.zone.construct");
    }

    @Override
    public void setLocalBlock(WarLocation location, WarBlock block) {
        String blockName = block.getBlockName();
        // attempt to get an acceptable name from the JSON variant. used for internal constants.
        if (blockName.startsWith("minecraft:")) {
            blockName = blockName.substring(10);
        }
        //noinspection deprecation
        getPlayer().sendBlockChange(plugin.getBukkitLocation(location), Material.matchMaterial(blockName), (byte) block.getMeta());
    }

    @Override
    public String getName() {
        return getPlayer().getName();
    }
}
