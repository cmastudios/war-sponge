package com.tommytony.war;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.tommytony.war.item.WarItem;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarLocation;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
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

    @Override
    public PlayerState getState() {
        ImmutableList.Builder<WarItem> builder = ImmutableList.builder();
        Arrays.stream(getPlayer().getInventory().getContents()).filter(itemStack -> itemStack != null).forEach(itemStack -> builder.add(plugin.getWarItem(itemStack)));
        int gameMode = 0;
        switch (getPlayer().getGameMode()) {
            case CREATIVE:
                gameMode = WarGameMode.CREATIVE;
                break;
            case SURVIVAL:
                gameMode = WarGameMode.SURVIVAL;
                break;
            case ADVENTURE:
                gameMode = WarGameMode.ADVENTURE;
                break;
            case SPECTATOR:
                gameMode = WarGameMode.SURVIVAL;
                break;
        }
        return new PlayerState(gameMode,
                builder.build().toArray(new WarItem[0]),
                null, null, null, null,
                getPlayer().getHealth(),
                getPlayer().getExhaustion(),
                getPlayer().getSaturation(),
                getPlayer().getFoodLevel(),
                getPlayer().getLevel(),
                getPlayer().getExp(),
                getPlayer().getAllowFlight());
    }

    @Override
    public void setState(PlayerState state) {
        getPlayer().closeInventory();
        switch (state.getGameMode()) {
            case WarGameMode.SURVIVAL:
                getPlayer().setGameMode(GameMode.SURVIVAL);
                break;
            case WarGameMode.CREATIVE:
                getPlayer().setGameMode(GameMode.CREATIVE);
                break;
            case WarGameMode.ADVENTURE:
                getPlayer().setGameMode(GameMode.ADVENTURE);
                break;
            default:
                break;
        }
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        Arrays.stream(state.getInventory()).forEach(warItem -> builder.add(plugin.getBukkitItem(warItem)));
        getPlayer().getInventory().clear();
        getPlayer().getInventory().setContents(builder.build().toArray(new ItemStack[0]));
        getPlayer().setHealth(state.getHealth());
        getPlayer().setExhaustion((float) state.getExhaustion());
        getPlayer().setSaturation((float) state.getSaturation());
        getPlayer().setFoodLevel((int) state.getHunger());
        getPlayer().setLevel((int) state.getLevel());
        getPlayer().setExp((float) state.getExperience());
        getPlayer().setAllowFlight(state.isFlying());
        getPlayer().setFireTicks(0);
        getPlayer().setRemainingAir(getPlayer().getMaximumAir());
    }

    @Override
    public WarItem getItemInHand() {
        return plugin.getWarItem(getPlayer().getInventory().getItemInMainHand());
    }

    @Override
    public double getHealth() {
        return getPlayer().getHealth();
    }

}
