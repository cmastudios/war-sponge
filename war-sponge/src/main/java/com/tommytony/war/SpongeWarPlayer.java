package com.tommytony.war;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.tommytony.war.item.WarItem;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarLocation;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

class SpongeWarPlayer extends WarPlayer {
    private final WarPlugin plugin;

    SpongeWarPlayer(UUID playerId, WarPlugin warPlugin) {
        super(playerId, warPlugin);
        plugin = warPlugin;
    }

    private Optional<Player> getPlayer() {
        return plugin.getGame().getServer().getPlayer(getPlayerId());
    }

    @Override
    public boolean isOnline() {
        Optional<Player> player = getPlayer();
        return player.isPresent() && player.get().isOnline();
    }

    @Override
    public WarLocation getLocation() {
        Optional<Player> player = getPlayer();
        if (!player.isPresent()) {
            return null;
        }
        Location<World> location = player.get().getLocation();
        Vector3d rotation = player.get().getRotation();
        return new WarLocation(location.getX(), location.getY(), location.getZ(),
                location.getExtent().getName(), rotation.getX(), rotation.getY());
    }

    @Override
    public void setLocation(WarLocation location) {
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            player.get().setLocationAndRotation(plugin.getSpongeLocation(location),
                    new Vector3d(location.getPitch(), location.getYaw(), 0));
        }
    }

    @Override
    public void sendMessage(String message) {
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            player.get().sendMessage(Text.of(message));
        }
    }

    @Override
    public WarLocation getTargetBlock() {
        Optional<Player> player = getPlayer();
        if (!player.isPresent()) {
            return null;
        }
        Optional<BlockRayHit<World>> block = BlockRay.from(player.get()).filter(BlockRay.onlyAirFilter()).end();
        if (block.isPresent()) {
            return plugin.getWarLocation(block.get().getLocation());
        }
        return null;
    }

    @Override
    public boolean isZoneMaker() {
        Optional<Player> player = getPlayer();
        return player.isPresent() && player.get().hasPermission("war.zone.construct");
    }

    @Override
    public void setLocalBlock(WarLocation location, WarBlock block) {
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            BlockState state;
            BlockType type;
            String blockName = block.getBlockName();
            // attempt to make blocks from Bukkit variant usable.
            if (!blockName.contains(":")) {
                blockName = "minecraft:" + blockName.toLowerCase();
            }
            Optional<BlockType> typeo = plugin.getGame().getRegistry().getType(BlockType.class, blockName);
            if (!typeo.isPresent()) {
                throw new IllegalStateException("Failed to get block type for block " + block.getBlockName());
            }
            type = typeo.get();
            state = BlockState.builder().blockType(type).build();
            if (block.getData() == null && !block.getSerialized().isEmpty()) {
                DataContainer container = plugin.getTranslator().translateFrom(block.getSerialized());
                state = BlockState.builder().blockType(type).build(container).orElse(state);
            }
            player.get().sendBlockChange(location.getBlockX(), location.getBlockY(), location.getBlockZ(), state);
        }
    }

    @Override
    public String getName() {
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            return player.get().getName();
        }
        return "MISSINGNO";
    }

    @Override
    public PlayerState getState() {
        Player player = getPlayer().orElseThrow(IllegalStateException::new);
        ImmutableList.Builder<WarItem> builder = ImmutableList.builder();
        for (Inventory inventory : player.getInventory().slots()) {
            Optional<ItemStack> peek = inventory.peek();
            if (peek.isPresent()) {
                builder.add(plugin.getWarItem(peek.get()));
            }
        }
        int gameMode = 0;
        GameMode gameMode1 = player.getGameModeData().getValue(Keys.GAME_MODE).orElseThrow(IllegalStateException::new).get();
        if (gameMode1 == GameModes.CREATIVE)
            gameMode = WarGameMode.CREATIVE;
//        return new PlayerState(gameMode, (WarItem[]) builder.build().toArray(),
//                null, null, null, null,
//                player.getHealthData().getValue(Keys.HEALTH).orElseThrow(IllegalStateException::new).get(),
//                player.getS);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setState(PlayerState state) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WarItem getItemInHand() {
        return plugin.getWarItem(getPlayer().orElseThrow(IllegalStateException::new).getItemInHand().orElse(null));
    }
}
