package com.tommytony.war;

import com.tommytony.war.bukkit.command.*;
import com.tommytony.war.item.WarEntity;
import com.tommytony.war.listener.PlayerListener;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneValidator;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.*;

public final class WarPlugin extends JavaPlugin implements ServerAPI {
    private WarConfig config;
    private ZoneValidator validator;
    private HashMap<String, Warzone> zones;
    private HashMap<UUID, BukkitWarPlayer> players;
    private WarListener listener;

    @Override
    public void onDisable() {
        super.onDisable();
        players.clear();
    }

    @Override
    public void onEnable() {
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            this.setEnabled(false);
            throw new RuntimeException("Failed to load SQLite database");
        }
        if (!this.getDataDir().exists() && !this.getDataDir().mkdirs()) {
            this.setEnabled(false);
            throw new RuntimeException("Failed to make War data folder at " + this.getDataDir().getPath());
        }
        try {
            config = new WarConfig(new File(this.getDataDir(), "war.sl3"));
        } catch (FileNotFoundException | SQLException e) {
            this.setEnabled(false);
            throw new RuntimeException(e);
        }
        validator = new ZoneValidator(config);
        zones = new HashMap<>();
        players = new HashMap<>();
        listener = new WarListener(this);
        this.getCommand("warzone").setExecutor(new WarzoneCommand(this));
        this.getCommand("warcfg").setExecutor(new WarConfigCommand(this));
        this.getCommand("setzone").setExecutor(new SetZoneCommand(this));
        this.getCommand("delzone").setExecutor(new com.tommytony.war.bukkit.command.DeleteZoneCommand(this));
        this.getCommand("zonecfg").setExecutor(new ZoneConfigCommand(this));
        this.getCommand("savezone").setExecutor(new SaveZoneCommand(this));
        this.getCommand("resetzone").setExecutor(new ResetZoneCommand(this));
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        try {
            for (String zoneName : config.getZones()) {
                this.logInfo("Loading zone " + zoneName + "...");
                Warzone zone = new Warzone(zoneName, this);
                zones.put(zoneName, zone);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WarBlock getBlock(WarLocation location, boolean cheap) {
        if (location == null) {
            return null;
        }
        Location loc = this.getBukkitLocation(location);
        Block block = loc.getBlock();
        BlockState state = block.getState();
        String serialized = "";
        if (!cheap) {
            if (state instanceof Sign) {
                serialized = StringUtils.join(((Sign) block.getState()).getLines(), "\n");
            } else if (state instanceof InventoryHolder) {
                List<ItemStack> items = Arrays.asList(((InventoryHolder) block.getState()).getInventory().getContents());
                YamlConfiguration config = new YamlConfiguration();
                // Serialize to config, then store config in database
                config.set("items", items);
                serialized = config.saveToString();
            } else if (state instanceof NoteBlock) {
                Note note = ((NoteBlock) block.getState()).getNote();
                serialized = note.getTone().toString() + '\n' + note.getOctave() + '\n' + note.isSharped();
            } else if (state instanceof Jukebox) {
                serialized = ((Jukebox) block.getState()).getPlaying().toString();
            } else if (state instanceof Skull) {
                serialized = String.format("%s\n%s\n%s",
                        ((Skull) block.getState()).hasOwner() ? ((Skull) block.getState()).getOwner() : "",
                        ((Skull) block.getState()).getSkullType().toString(),
                        ((Skull) block.getState()).getRotation().toString());
            } else if (state instanceof CommandBlock) {
                serialized = ((CommandBlock) block.getState()).getName()
                        + "\n" + ((CommandBlock) block.getState()).getCommand();
            } else if (state instanceof CreatureSpawner) {
                serialized = ((CreatureSpawner) block.getState()).getSpawnedType().toString();
            }
        }
        return new WarBlock(block.getType().name(), new HashMap<>(), serialized, state.getData().toItemStack().getDurability());
    }

    public Location getBukkitLocation(WarLocation location) {
        World world = this.getServer().getWorld(location.getWorld());
        return new Location(world, location.getX(), location.getY(), location.getZ(), (float) location.getPitch(), (float) location.getYaw());
    }

    @Override
    public void setBlock(WarLocation location, WarBlock block) {
        Location bukkitLoc = this.getBukkitLocation(location);
        BlockState modify = bukkitLoc.getBlock().getState();
        String blockName = block.getBlockName();
        // attempt to get an acceptable name from the JSON variant. used for internal constants.
        if (blockName.startsWith("minecraft:")) {
            blockName = blockName.substring(10);
        }
        ItemStack data = new ItemStack(
                Material.matchMaterial(blockName), // this particular line of code will cause an error if the user loads a warzone from sponge
                0, block.getMeta());
        if (modify.getType() != data.getType() || !modify.getData().equals(data.getData())) {
            // Update the type & data if it has changed
            modify.setType(data.getType());
            modify.setData(data.getData());
            modify.update(true, false); // No-physics update, preventing the need for deferring blocks
            modify = bukkitLoc.getBlock().getState(); // Grab a new instance
        }
        if (!block.getSerialized().isEmpty()) {
            if (modify instanceof Sign) {
                final String[] lines = block.getSerialized().split("\n");
                for (int i = 0; i < lines.length; i++) {
                    ((Sign) modify).setLine(i, lines[i]);
                }
                modify.update(true, false);
            }
            // Containers
            if (modify instanceof InventoryHolder) {
                YamlConfiguration config = new YamlConfiguration();
                try {
                    config.loadFromString(block.getSerialized());
                } catch (InvalidConfigurationException e) {
                    throw new RuntimeException(e);
                }
                ((InventoryHolder) modify).getInventory().clear();
                for (Object obj : config.getList("items")) {
                    if (obj instanceof ItemStack) {
                        ((InventoryHolder) modify).getInventory().addItem((ItemStack) obj);
                    }
                }
                modify.update(true, false);
            }
            // Notes
            if (modify instanceof NoteBlock) {
                String[] split = block.getSerialized().split("\n");
                Note note = new Note(Integer.parseInt(split[1]), Note.Tone.valueOf(split[0]), Boolean.parseBoolean(split[2]));
                ((NoteBlock) modify).setNote(note);
                modify.update(true, false);
            }
            // Records
            if (modify instanceof Jukebox) {
                ((Jukebox) modify).setPlaying(Material.valueOf(block.getSerialized()));
                modify.update(true, false);
            }
            // Skulls
            if (modify instanceof Skull) {
                String[] opts = block.getSerialized().split("\n");
                if (!opts[0].isEmpty()) {
                    ((Skull) modify).setOwner(opts[0]);
                }
                ((Skull) modify).setSkullType(SkullType.valueOf(opts[1]));
                ((Skull) modify).setRotation(BlockFace.valueOf(opts[2]));
                modify.update(true, false);
            }
            // Command blocks
            if (modify instanceof CommandBlock) {
                final String[] commandArray = block.getSerialized().split("\n");
                ((CommandBlock) modify).setName(commandArray[0]);
                ((CommandBlock) modify).setCommand(commandArray[1]);
                modify.update(true, false);
            }
            // Creature spawner
            if (modify instanceof CreatureSpawner) {
                ((CreatureSpawner) modify).setSpawnedType(EntityType.valueOf(block.getSerialized()));
                modify.update(true, false);
            }
        }
    }

    @Override
    public void removeEntity(WarCuboid cuboid, WarEntity type) {
        World world = this.getBukkitLocation(cuboid.getCorner1()).getWorld();
        for (Entity entity : world.getEntities()) {
            WarLocation location = this.getWarLocation(entity.getLocation());
            if (!cuboid.contains(location)) {
                continue;
            }
            WarEntity thisType = WarEntity.UNKNOWN;
            if (entity.getType() == EntityType.DROPPED_ITEM) {
                thisType = WarEntity.ITEM;
            }
            if (entity.getType() == EntityType.PRIMED_TNT) {
                thisType = WarEntity.TNT;
            }
            if (entity.getType() == EntityType.ARMOR_STAND || entity.getType() == EntityType.PAINTING
                    || entity.getType() == EntityType.ITEM_FRAME) {
                thisType = WarEntity.PROP;
            }
            if (thisType == type) {
                entity.remove();
            }
        }
    }

    public WarLocation getWarLocation(Location location) {
        return new WarLocation(location.getX(), location.getY(), location.getZ(), location.getWorld().getName(), location.getPitch(), location.getYaw());
    }

    @Override
    public File getDataDir() {
        return this.getDataFolder();
    }

    @Override
    public WarConfig getWarConfig() {
        return this.config;
    }

    @Override
    public void logInfo(String message) {
        this.getLogger().info(message);
    }

    @Override
    public Map<String, Warzone> getZones() {
        return zones;
    }

    @Override
    public Warzone getZone(String zoneName) {
        return zones.get(zoneName);
    }

    public WarPlayer getWarPlayer(Player player) {
        if (players.containsKey(player.getUniqueId())) {
            return players.get(player.getUniqueId());
        } else {
            BukkitWarPlayer wp = new BukkitWarPlayer(player.getUniqueId(), this);
            players.put(player.getUniqueId(), wp);
            return wp;
        }
    }

    public ZoneValidator getValidator() {
        return validator;
    }

    @Override
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

    @Override
    public String deleteZone(String zoneName) {
        try {
            Warzone zone = zones.remove(zoneName);
            config.deleteZone(zone.getName());
            zone.close();
            File trashDir = new File(this.getDataDir(), "trash");
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
        long delayTicks = (long) (delay * 20.0);
        long intervalTicks = (long) (interval * 20.0);
        this.getServer().getScheduler().runTaskTimer(this, runnable, delayTicks, intervalTicks);
    }

    @Override
    public void delayTask(double delay, Runnable runnable) {
        long delayTicks = (long) (delay * 20.0);
        this.getServer().getScheduler().runTaskLater(this, runnable, delayTicks);
    }

    public WarListener getListener() {
        return listener;
    }

    public WarConsole getSender(CommandSender source) {
        if (source instanceof Player) {
            return getWarPlayer((Player) source);
        } else {
            return new BukkitWarConsole();
        }
    }

}
