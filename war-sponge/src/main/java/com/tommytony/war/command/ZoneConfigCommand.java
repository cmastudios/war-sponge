package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneSetting;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Command to change the properties of an individual zone.
 */
public class ZoneConfigCommand implements CommandCallable {
    private final WarPlugin plugin;

    public ZoneConfigCommand(WarPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        String argv[] = arguments.trim().split(" ");
        if (arguments.trim().length() == 0) {
            StringBuilder zones = new StringBuilder();
            boolean first = true;
            for (String zoneName : plugin.getZones().keySet()) {
                if (!first) {
                    zones.append(", ");
                }
                first = false;
                zones.append(zoneName);
            }
            source.sendMessage(Texts.of("Warzones: ", zones.toString()));
            StringBuilder properties = new StringBuilder();
            for (ZoneSetting setting : ZoneSetting.values()) {
                properties.append(setting.name().toLowerCase()).append("<").append(setting.getDataType().getSimpleName());
                properties.append("> (").append(setting.getDefaultValue().toString()).append(")\n");
            }
            source.sendMessage(Texts.of("Available properties:\n", properties.toString()));
            return CommandResult.empty();
        } else if (argv.length == 1) {
            String zoneName = argv[0];
            Warzone zone = plugin.getZone(zoneName);
            if (zone == null) {
                source.sendMessage(Texts.of("Can't find warzone ", zoneName));
                return CommandResult.empty();
            }
            StringBuilder properties = new StringBuilder();
            for (ZoneSetting setting : ZoneSetting.values()) {
                properties.append(setting.name().toLowerCase()).append(" <").append(setting.getDataType().getSimpleName());
                try {
                    properties.append("> (").append(zone.getConfig().getObject(setting).toString()).append(")\n");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            source.sendMessage(Texts.of("Properties of warzone `", zone.getName(), "':\n", properties.toString()));
            return CommandResult.success();
        } else if (argv.length == 2) {
            String zoneName = argv[0];
            Warzone zone = plugin.getZone(zoneName);
            if (zone == null) {
                source.sendMessage(Texts.of("Can't find warzone ", zoneName));
                return CommandResult.empty();
            }
            ZoneSetting setting = ZoneSetting.valueOf(argv[1].toUpperCase());
            try {
                source.sendMessage(Texts.of(setting.name().toLowerCase(), " <", setting.getDataType().getSimpleName(), "> = ",
                        zone.getConfig().getObject(setting).toString()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return CommandResult.empty();
        } else if (argv.length == 3) {
            String zoneName = argv[0];
            Warzone zone = plugin.getZone(zoneName);
            if (zone == null) {
                source.sendMessage(Texts.of("Can't find warzone ", zoneName));
                return CommandResult.empty();
            }
            ZoneSetting setting = ZoneSetting.valueOf(argv[1].toUpperCase());
            String value = argv[2];
            try {
                zone.getConfig().setValue(setting, value);
                source.sendMessage(Texts.of("Setting `", setting.name().toLowerCase(), "' has been successfully set to ", value));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return CommandResult.success();
        } else {
            source.sendMessage(Texts.of("Usage: /zonecfg ", this.getUsage(source)));
            return CommandResult.empty();
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        String argv[] = arguments.trim().split(" ");
        if (arguments.trim().isEmpty()) {
            return ImmutableList.of();
        } else if (argv.length == 1) {
            ImmutableList.Builder<String> list = ImmutableList.builder();
            // IntelliJ suggested this. I have no understanding of it but wow
            plugin.getZones().values().stream().filter(zone -> zone.getName().toLowerCase().startsWith(argv[0].toLowerCase())).forEach(zone -> list.add(zone.getName()));
            return list.build();
        } else if (argv.length == 2) {
            ImmutableList.Builder<String> list = ImmutableList.builder();
            for (ZoneSetting setting : ZoneSetting.values()) {
                list.add(setting.name().toLowerCase());
            }
            return list.build();
        } else if (argv.length == 3) {
            Warzone zone = plugin.getZone(argv[0]);
            if (zone == null) {
                return ImmutableList.of();
            }
            try {
                ZoneSetting setting = ZoneSetting.valueOf(argv[1].toUpperCase());
                return ImmutableList.of(setting.getDefaultValue().toString());
            } catch (IllegalArgumentException e) {
                return ImmutableList.of();
            }
        }
        return ImmutableList.of();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("war.zonemaker");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Texts.of("View or set properties of warzone"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Texts.of("With no arguments: displays a list of warzones and properties\n" +
                "With one argument: lists properties of the warzone\n" +
                "With two arguments: prints value of property of the warzone\n" +
                "With three arguments: sets value of property of the warzone to value"));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Texts.of("[zone] [option] [value]");
    }
}
