package com.tommytony.war;

import com.google.inject.Inject;
import com.tommytony.war.command.*;
import com.tommytony.war.item.WarEntity;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneValidator;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(id = "war", name = "War", version = "2.0-SNAPSHOT")
public class WarPlugin implements ServerAPI {
    @Inject
    private Game game;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private File dataDir;

    private WarConfig config;
    private Map<String, Warzone> zones;
    private ZoneValidator validator;
    private YamlTranslator translator;
    private HashMap<UUID, SpongeWarPlayer> players;

    @Listener
    public void onConstruction(GameConstructionEvent event) throws InstantiationException {
        try {
            Class.forName("com.tommytony.war.sqlite.JDBC").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new InstantiationException("Failed to load SQLite database");
        }
        zones = new HashMap<>();
        translator = new YamlTranslator();
        dataDir = dataDir.getParentFile();
        players = new HashMap<>();
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) throws FileNotFoundException, SQLException {
        // register commands
        game.getCommandManager().register(this, new WarzoneCommand(this), "warzone", "zone");
        game.getCommandManager().register(this, new WarConfigCommand(this), "warcfg", "warconfig");
        game.getCommandManager().register(this, new SetZoneCommand(this), "setzone", "zoneset");
        game.getCommandManager().register(this, new DeleteZoneCommand(this), "delzone", "rmzone", "deletezone");
        game.getCommandManager().register(this, new ZoneConfigCommand(this), "zonecfg", "zoneconfig", "zc");
        game.getCommandManager().register(this, new SaveZoneCommand(this), "savezone", "zonesave", "zs");
        game.getCommandManager().register(this, new ResetZoneCommand(this), "resetzone", "reloadzone", "zr");

        if (!dataDir.exists() && !dataDir.mkdirs())
            throw new FileNotFoundException("Failed to make War data folder at " + dataDir.getPath());
        config = new WarConfig(new File(dataDir, "war.sl3"));
        validator = new ZoneValidator(config);
        for (String zoneName : config.getZones()) {
            logger.info("Loading zone " + zoneName + "...");
            Warzone zone = new Warzone(zoneName, this);
            zones.put(zoneName, zone);
        }
    }

    Game getGame() {
        return game;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public File getDataDir() {
        return dataDir;
    }

    public WarConfig getWarConfig() {
        return config;
    }

    @Override
    public void logInfo(String message) {
        logger.info(message);
    }

    public Warzone getZone(String zoneName) {
        if (zones.containsKey(zoneName)) {
            return zones.get(zoneName);
        }
        return null;
    }

    public Warzone createZone(String zoneName) {
        try {
            config.addZone(zoneName);
            Warzone zone = new Warzone(zoneName, this);
            zones.put(zoneName, zone);
            return zone;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteZone(String zoneName) {
        try {
            Warzone zone = zones.remove(zoneName);
            config.deleteZone(zone.getName());
            zone.close();
            File trashDir = new File(dataDir, "trash");
            if (!trashDir.exists() && !trashDir.mkdirs()) {
                throw new RuntimeException("Failed to create trash folder on the server. Warzone only removed from config.");
            }
            File dataFile = zone.getDataFile();
            File outputFile = new File(trashDir, dataFile.getName());
            if (!dataFile.renameTo(outputFile)) {
                throw new RuntimeException("Failed to remove zone data file for zone being removed: " + zoneName);
            }
            return outputFile.getPath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void scheduleTask(double delay, double interval, Runnable runnable) {
        long delayMs = (long) (delay * 1000);
        long intervalMs = (long) (interval * 1000);
        Sponge.getScheduler().createTaskBuilder().execute(runnable)
                .delay(delayMs, TimeUnit.MILLISECONDS).interval(intervalMs, TimeUnit.MILLISECONDS)
                .name("War Repeating Task - " + interval + "s").submit(this);
    }

    @Override
    public void delayTask(double delay, Runnable runnable) {
        long delayMs = (long) (delay * 1000);
        Sponge.getScheduler().createTaskBuilder().execute(runnable)
                .delay(delayMs, TimeUnit.MILLISECONDS)
                .name("War Delayed Task - " + delay + "s").submit(this);
    }

    public Map<String, Warzone> getZones() {
        return zones;
    }

    public ZoneValidator getValidator() {
        return validator;
    }

    public Location<World> getSpongeLocation(WarLocation location) {
        Optional<World> world = this.getGame().getServer().getWorld(location.getWorld());
        if (!world.isPresent()) {
            throw new IllegalStateException("Can't find world with name " + location.getWorld());
        }
        return world.get().getLocation(location.getX(), location.getY(), location.getZ());
    }

    public WarLocation getWarLocation(Location<World> location) {
        return new WarLocation(location.getX(), location.getY(), location.getZ(), location.getExtent().getName());
    }

    public WarPlayer getWarPlayer(Player player) {
        if (players.containsKey(player.getUniqueId())) {
            return players.get(player.getUniqueId());
        } else {
            SpongeWarPlayer wp = new SpongeWarPlayer(player.getUniqueId(), this);
            players.put(player.getUniqueId(), wp);
            return wp;
        }
    }

    @Override
    public WarBlock getBlock(WarLocation location, boolean cheap) {
        String serialized = "";
        Map<String, Object> data = new HashMap<>();
        Location<World> sloc = getSpongeLocation(location);
        BlockState block = sloc.getBlock();
        short meta = 0;
        if (!cheap) {
            // TODO test serialized interop with Bukkit
            serialized = translator.translateData(sloc.createSnapshot().toContainer());
        }
        String name = block.getType().getName();
        return new WarBlock(name, data, serialized, meta);
    }

    @Override
    public void setBlock(WarLocation location, WarBlock block) {
        Location<World> sloc = getSpongeLocation(location);
        String blockName = block.getBlockName();
        // attempt to make blocks from Bukkit variant usable.
        if (!blockName.contains(":")) {
            blockName = "minecraft:" + blockName.toLowerCase();
        }
        Optional<BlockType> type = game.getRegistry().getType(BlockType.class, blockName);
        if (!type.isPresent()) {
            throw new IllegalStateException("Failed to get block type for block " + blockName);
        }
        if (block.getData() == null && !block.getSerialized().isEmpty()) {
            DataContainer container = translator.translateFrom(block.getSerialized());
            Optional<BlockSnapshot> build = BlockSnapshot.builder().build(container);
            if (build.isPresent()) {
                build.get().withLocation(sloc).restore(true, false);
            }
//                    .position(sloc.getBlockPosition())
//                    .world(sloc.getExtent().getProperties())
//                    .blockState(BlockState.builder().blockType(type.get()).build())
//                    .build().withContainer(container).restore(true, false);
        }
//        sloc.setBlockType(type.get(), false);
    }

    @Override
    public void removeEntity(WarCuboid cuboid, WarEntity type) {
        Location<World> pos1 = getSpongeLocation(cuboid.getCorner1());
        for (Entity entity : pos1.getExtent().getEntities()) {
            WarLocation loc = getWarLocation(entity.getLocation());
            if (!cuboid.contains(loc)) {
                continue;
            }
            WarEntity thisType = WarEntity.UNKNOWN;
            if (entity.getType() == EntityTypes.ITEM) {
                thisType = WarEntity.ITEM;
            }
            if (entity.getType() == EntityTypes.PRIMED_TNT) {
                thisType = WarEntity.TNT;
            }
            if (entity.getType() == EntityTypes.ARMOR_STAND || entity.getType() == EntityTypes.PAINTING
                    || entity.getType() == EntityTypes.ITEM_FRAME) {
                thisType = WarEntity.PROP;
            }
            if (thisType == type) {
                entity.remove();
            }
        }
    }

    YamlTranslator getTranslator() {
        return translator;
    }
}
