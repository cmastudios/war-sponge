package com.tommytony.war;

import com.google.inject.Inject;
import com.tommytony.war.command.*;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneValidator;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Plugin(id = "war", name = "War", version = "2.0-SNAPSHOT")
public class WarPlugin {
    private Game game;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private File dataDir;

    private WarConfig config;
    private Map<String, Warzone> zones;
    private ZoneValidator validator;

    @Listener
    public void onConstruction(GameConstructionEvent event) throws InstantiationException {
        game = event.getGame();
        try {
            Class.forName("com.tommytony.war.sqlite.JDBC").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new InstantiationException("Failed to load SQLite database");
        }
        zones = new HashMap<>();
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) throws FileNotFoundException, SQLException {
        // register commands
        game.getCommandDispatcher().register(this, new WarzoneCommand(this), "warzone", "zone");
        game.getCommandDispatcher().register(this, new WarConfigCommand(this), "warcfg", "warconfig");
        game.getCommandDispatcher().register(this, new SetZoneCommand(this), "setzone", "zoneset");
        game.getCommandDispatcher().register(this, new ClearEntCommand(this), "clearent", "killall");
        game.getCommandDispatcher().register(this, new DeleteZoneCommand(this), "delzone", "rmzone");

        if (!dataDir.exists() && !dataDir.mkdirs())
            throw new FileNotFoundException("Failed to make War data folder at " + dataDir.getPath());
        config = new WarConfig(this, new File(dataDir, "war.sl3"));
        validator = new ZoneValidator(config);
        for (String zoneName : config.getZones()) {
            logger.info("[War] Loading zone " + zoneName + "...");
            Warzone zone = new Warzone(this, zoneName);
            zones.put(zoneName, zone);
        }
    }

    public Game getGame() {
        return game;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getDataDir() {
        return dataDir;
    }

    public WarConfig getConfig() {
        return config;
    }

    public Optional<Warzone> getZone(String zoneName) {
        if (zones.containsKey(zoneName)) {
            return Optional.of(zones.get(zoneName));
        }
        return Optional.empty();
    }

    public Warzone createZone(String zoneName) {
        try {
            config.addZone(zoneName);
            Warzone zone = new Warzone(this, zoneName);
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
}
