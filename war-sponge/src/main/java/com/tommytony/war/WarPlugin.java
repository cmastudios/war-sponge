package com.tommytony.war;

import com.google.inject.Inject;
import com.tommytony.war.command.*;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneValidator;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Plugin(id = "war", name = "War", version = "2.0-SNAPSHOT")
public class WarPlugin implements ServerAPI {
    private Game game;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private File dataDir;

    private WarConfig config;
    private Map<String, Warzone> zones;
    private ZoneValidator validator;
    private Yaml yaml;

    @Listener
    public void onConstruction(GameConstructionEvent event) throws InstantiationException {
        game = event.getGame();
        try {
            Class.forName("com.tommytony.war.sqlite.JDBC").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new InstantiationException("Failed to load SQLite database");
        }
        zones = new HashMap<>();
        yaml = new Yaml();
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) throws FileNotFoundException, SQLException {
        // register commands
        game.getCommandDispatcher().register(this, new WarzoneCommand(this), "warzone", "zone");
        game.getCommandDispatcher().register(this, new WarConfigCommand(this), "warcfg", "warconfig");
        game.getCommandDispatcher().register(this, new SetZoneCommand(this), "setzone", "zoneset");
        game.getCommandDispatcher().register(this, new ClearEntCommand(this), "clearent", "killall");
        game.getCommandDispatcher().register(this, new DeleteZoneCommand(this), "delzone", "rmzone");
        game.getCommandDispatcher().register(this, new ZoneConfigCommand(this), "zonecfg", "zoneconfig");
        game.getCommandDispatcher().register(this, new SaveZoneCommand(this), "savezone", "zonesave");

        if (!dataDir.exists() && !dataDir.mkdirs())
            throw new FileNotFoundException("Failed to make War data folder at " + dataDir.getPath());
        config = new WarConfig(new File(dataDir, "war.sl3"));
        validator = new ZoneValidator(config);
        for (String zoneName : config.getZones()) {
            logger.info("[War] Loading zone " + zoneName + "...");
            Warzone zone = new Warzone(zoneName, this);
            zones.put(zoneName, zone);
        }
    }

    public Game getGame() {
        return game;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public File getDataDir() {
        return dataDir;
    }

    public WarConfig getConfig() {
        return config;
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

    public WarPlayerState getState(Player player) {
        return WarPlayerState.getState(player.getUniqueId());
    }

    @Override
    public WarBlock getBlock(WarLocation location) {
        String name, serialized;
        Map<String, Object> data = new HashMap<>();
        Location<World> sloc = getSpongeLocation(location);
        Map<DataQuery, Object> values = sloc.getBlock().toContainer().getValues(true);
        for (DataQuery q : values.keySet()) {
            data.put(q.asString('.'), values.get(q));
        }
        name = sloc.getBlock().getType().getName();
        // TODO test serialized interop with Bukkit
        serialized = yaml.dump(data);
        return new WarBlock(name, data, serialized);
    }

    @Override
    public void setBlock(WarLocation location, WarBlock block) {
        Location<World> sloc = getSpongeLocation(location);
        Optional<BlockType> type = game.getRegistry().<BlockType>getType(BlockType.class, block.getBlockName());
        if (!type.isPresent()) {
            throw new IllegalStateException("Failed to get block type for block " + block.getBlockName());
        }
        if (block.getData() == null && !block.getSerialized().isEmpty()) {
            Object load = yaml.load(block.getSerialized());
            if (!(load instanceof Map)) {
                throw new IllegalStateException("Serialized data is not valid for block\n" + block.getSerialized());
            }
            block.setData((Map<String, Object>) load);
        }
        if (block.getData() != null) {
            DataContainer dataContainer = sloc.toContainer();
            for (String s : block.getData().keySet()) {
                dataContainer.set(DataQuery.of('.', s), block.getData().get(s));
            }
            sloc.setRawData(dataContainer);
        }
        sloc.setBlockType(type.get(), false);
    }
}
