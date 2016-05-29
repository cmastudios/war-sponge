package com.tommytony.war.bukkit.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneSetting;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

public class ZoneConfigCommand implements TabExecutor {
    private final WarPlugin plugin;

    public ZoneConfigCommand(WarPlugin warPlugin) {
        this.plugin = warPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            StringBuilder zones = new StringBuilder();
            boolean first = true;
            for (String zoneName : plugin.getZones().keySet()) {
                if (!first) {
                    zones.append(", ");
                }
                first = false;
                zones.append(zoneName);
            }
            sender.sendMessage("Warzones: " + zones.toString());
            StringBuilder properties = new StringBuilder();
            for (ZoneSetting setting : ZoneSetting.values()) {
                properties.append(setting.name().toLowerCase()).append("<").append(setting.getDataType().getSimpleName());
                properties.append("> (").append(setting.getDefaultValue().toString()).append(")\n");
            }
            sender.sendMessage("Available properties:\n" + properties.toString());
            return true;
        } else if (args.length == 1) {
            String zoneName = args[0];
            Warzone zone = plugin.getZone(zoneName);
            if (zone == null) {
                throw new CommandException(String.format("Can't find warzone %s.", zoneName));
            }
            StringBuilder properties = new StringBuilder();
            for (ZoneSetting setting : ZoneSetting.values()) {
                properties.append(setting.name().toLowerCase()).append(" <").append(setting.getDataType().getSimpleName());
                try {
                    properties.append("> (").append(zone.getConfig().getObject(setting).toString()).append(")\n");
                } catch (SQLException e) {
                    throw new CommandException("Failed to load properties for warzone.", e);
                }
            }
            sender.sendMessage(MessageFormat.format("Properties of warzone `{0}'':\n{1}", zone.getName(), properties.toString()));
            return true;
        } else if (args.length == 2) {
            String zoneName = args[0];
            Warzone zone = plugin.getZone(zoneName);
            if (zone == null) {
                throw new CommandException(String.format("Can't find warzone %s.", zoneName));
            }
            ZoneSetting setting = ZoneSetting.valueOf(args[1].toUpperCase());
            try {
                sender.sendMessage(MessageFormat.format("{0} <{1}> = {2}", setting.name().toLowerCase(), setting.getDataType().getSimpleName(),
                        zone.getConfig().getObject(setting).toString()));
            } catch (SQLException e) {
                throw new CommandException("Failed to load properties for warzone.", e);
            }
            return true;
        } else if (args.length == 3) {
            String zoneName = args[0];
            Warzone zone = plugin.getZone(zoneName);
            if (zone == null) {
                throw new CommandException(String.format("Can't find warzone %s.", zoneName));
            }
            ZoneSetting setting = ZoneSetting.valueOf(args[1].toUpperCase());
            String value = args[2];
            try {
                zone.getConfig().setValue(setting, value);
                sender.sendMessage(MessageFormat.format("Setting `{0}'' has been successfully set to {1}.", setting.name().toLowerCase(), value));
            } catch (SQLException e) {
                throw new CommandException("Failed to set property for warzone.", e);
            }
            return true;
        } else {
            throw new CommandException("Insufficient arguments.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return ImmutableList.of();
        } else if (args.length == 1) {
            ImmutableList.Builder<String> list = ImmutableList.builder();
            // IntelliJ suggested this. I have no understanding of it but wow
            plugin.getZones().values().stream().filter(zone -> zone.getName().toLowerCase().startsWith(args[0].toLowerCase())).forEach(zone -> list.add(zone.getName()));
            return list.build();
        } else if (args.length == 2) {
            ImmutableList.Builder<String> list = ImmutableList.builder();
            for (ZoneSetting setting : ZoneSetting.values()) {
                list.add(setting.name().toLowerCase());
            }
            return list.build();
        } else if (args.length == 3) {
            Warzone zone = plugin.getZone(args[0]);
            if (zone == null) {
                return ImmutableList.of();
            }
            try {
                ZoneSetting setting = ZoneSetting.valueOf(args[1].toUpperCase());
                return ImmutableList.of(setting.getDefaultValue().toString());
            } catch (IllegalArgumentException e) {
                return ImmutableList.of();
            }
        }
        return ImmutableList.of();
    }
}
