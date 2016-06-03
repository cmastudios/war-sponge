package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConfig;
import com.tommytony.war.WarConsole;
import com.tommytony.war.zone.WarGame;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneSetting;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

public class ZoneConfigCommand extends WarCommand {
    public ZoneConfigCommand(ServerAPI plugin) {
        super(plugin);
    }

    @Override
    void handleCommand(WarConsole sender, String[] args) {
        if (args.length == 0) {
            StringBuilder zones = new StringBuilder();
            boolean first = true;
            for (String zoneName : getPlugin().getZones().keySet()) {
                if (!first) {
                    zones.append(", ");
                }
                first = false;
                zones.append(zoneName);
            }
            sender.sendMessage(MessageFormat.format("Zones: {0}", zones));
            StringBuilder properties = new StringBuilder("\n");
            for (ZoneSetting setting : ZoneSetting.values()) {
                properties.append(MessageFormat.format(WarConfig.DEFAULT_FORMAT,
                        setting.name().toLowerCase(), setting.getDataType().getSimpleName(), setting.getDefaultValue()))
                        .append('\n');
            }
            sender.sendMessage(MessageFormat.format("Available properties: {0}", properties.toString()));
            return;
        } else if (args.length == 1) {
            String zoneName = args[0];
            Warzone zone = getPlugin().getZone(zoneName);
            if (zone == null) {
                throw new CommandUserError(MessageFormat.format("Zone {0} not found.", zoneName));
            }
            StringBuilder properties = new StringBuilder("\n");
            for (ZoneSetting setting : ZoneSetting.values()) {
                properties.append(MessageFormat.format(WarConfig.MODIFIED_FORMAT,
                        setting.name().toLowerCase(), setting.getDataType().getSimpleName(), zone.getConfig().getObject(setting)))
                        .append('\n');
            }
            sender.sendMessage(MessageFormat.format("Properties of warzone `{0}'': {1}", zone.getName(), properties.toString()));
            return;
        } else if (args.length == 2) {
            String zoneName = args[0];
            Warzone zone = getPlugin().getZone(zoneName);
            if (zone == null) {
                throw new CommandUserError(MessageFormat.format("Zone {0} not found.", zoneName));
            }
            try {
                ZoneSetting setting = ZoneSetting.valueOf(args[1].toUpperCase());
                sender.sendMessage(MessageFormat.format(WarConfig.MODIFIED_FORMAT,
                        setting.name().toLowerCase(), setting.getDataType().getSimpleName(),
                        zone.getConfig().getObject(setting).toString()));
            } catch (IllegalArgumentException e) {
                throw new CommandUserError(MessageFormat.format("Cannot find zone property with name {0}.", args[1]));
            }
            return;
        } else if (args.length == 3) {
            String zoneName = args[0];
            Warzone zone = getPlugin().getZone(zoneName);
            if (zone == null) {
                throw new CommandUserError(MessageFormat.format("Zone {0} not found.", zoneName));
            }
            try {
                ZoneSetting setting = ZoneSetting.valueOf(args[1].toUpperCase());
                Optional<WarGame> game = zone.getGame();
                if (setting == ZoneSetting.EDITING && game.isPresent()) {
                    game.get().forceEndGame();
                }
                String value = args[2];
                zone.getConfig().setValue(setting, value);
                sender.sendMessage(MessageFormat.format("Setting `{0}'' has been successfully set to {1}.", setting.name().toLowerCase(), value));
            } catch (IllegalArgumentException e) {
                throw new CommandUserError(MessageFormat.format("Cannot find zone property with name {0}.", args[1]));
            }
            return;
        }
        throw new InvalidArgumentsError();
    }

    @Override
    List<String> handleTab(WarConsole sender, String[] args) {
        if (args.length == 0) {
            return ImmutableList.copyOf(getPlugin().getZones().keySet());
        } else if (args.length == 1) {
            ImmutableList.Builder<String> list = ImmutableList.builder();
            getPlugin().getZones().values().stream()
                    .filter(zone -> zone.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                    .forEach(zone -> list.add(zone.getName()));
            return list.build();
        } else if (args.length == 2) {
            ImmutableList.Builder<String> list = ImmutableList.builder();
            for (ZoneSetting setting : ZoneSetting.values()) {
                list.add(setting.name().toLowerCase());
            }
            return list.build();
        } else if (args.length == 3) {
            Warzone zone = getPlugin().getZone(args[0]);
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

    @Override
    public String getName() {
        return "zonecfg";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("zoneconfig");
    }

    @Override
    public String getTagline() {
        return "View or set zone properties.";
    }

    @Override
    public String getDescription() {
        return "With no arguments, lists zones and properties.\n" +
                "With one argument, displays properties of the specified zone.\n" +
                "With two arguments, displays the value of one property of one zone.\n" +
                "With three arguments, sets a property in a zone to a value.";
    }

    @Override
    public String getUsage() {
        return "<zone> [setting] [value]";
    }

    @Override
    public String getPermission() {
        return "war.zone.config";
    }
}
