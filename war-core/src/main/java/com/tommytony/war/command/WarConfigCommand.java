package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConfig;
import com.tommytony.war.WarConsole;
import com.tommytony.war.zone.ZoneSetting;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class WarConfigCommand extends WarCommand {
    public WarConfigCommand(ServerAPI plugin) {
        super(plugin);
    }

    @Override
    void handleCommand(WarConsole sender, String[] args) {
        if (args.length == 0) {
            StringBuilder properties = new StringBuilder("\n");
            for (WarConfig.WarSetting setting : WarConfig.WarSetting.values()) {
                properties.append(MessageFormat.format(WarConfig.MODIFIED_FORMAT,
                        setting.name().toLowerCase(), setting.getDataType().getSimpleName(),
                        getPlugin().getWarConfig().getObject(setting))).append('\n');
            }
            properties.append("----------------------------").append('\n');
            for (ZoneSetting setting : ZoneSetting.values()) {
                properties.append(MessageFormat.format(WarConfig.MODIFIED_FORMAT,
                        setting.name().toLowerCase(), setting.getDataType().getSimpleName(),
                        getPlugin().getWarConfig().getZoneDefaults().getObject(setting))).append('\n');
            }
            sender.sendMessage(MessageFormat.format("Available properties: {0}", properties.toString()));
            return;
        } else if (args.length == 1) {
            try {
                WarConfig.WarSetting setting = WarConfig.WarSetting.valueOf(args[0].toUpperCase());
                sender.sendMessage(MessageFormat.format(WarConfig.MODIFIED_FORMAT, setting.name().toLowerCase(), setting.getDataType().getSimpleName(),
                        getPlugin().getWarConfig().getObject(setting)));
            } catch (IllegalArgumentException e) {
                try {
                    ZoneSetting setting = ZoneSetting.valueOf(args[0].toUpperCase());
                    sender.sendMessage(MessageFormat.format(WarConfig.MODIFIED_FORMAT, setting.name().toLowerCase(), setting.getDataType().getSimpleName(),
                            getPlugin().getWarConfig().getZoneDefaults().getObject(setting)));
                } catch (IllegalArgumentException e1) {
                    throw new CommandUserError(MessageFormat.format("Cannot find zone property with name {0}.", args[0]));
                }
            }
            return;
        } else if (args.length == 2) {
            try {
                WarConfig.WarSetting setting = WarConfig.WarSetting.valueOf(args[0].toUpperCase());
                getPlugin().getWarConfig().setValue(setting, args[1]);
                sender.sendMessage(MessageFormat.format("Setting `{0}'' has been successfully set to {1}.",
                        setting.name().toLowerCase(), args[1]));
            } catch (IllegalArgumentException e) {
                try {
                    ZoneSetting setting = ZoneSetting.valueOf(args[0].toUpperCase());
                    getPlugin().getWarConfig().getZoneDefaults().setValue(setting, args[1]);
                    sender.sendMessage(MessageFormat.format("Setting `{0}'' has been successfully set to {1}.",
                            setting.name().toLowerCase(), args[1]));
                } catch (IllegalArgumentException e1) {
                    throw new CommandUserError(MessageFormat.format("Cannot find zone property with name {0}.", args[0]));
                }
            }
        }
        throw new InvalidArgumentsError();
    }

    @Override
    List<String> handleTab(WarConsole sender, String[] args) {
        if (args.length == 0) {
            ImmutableList.Builder<String> list = ImmutableList.builder();
            Arrays.stream(WarConfig.WarSetting.values()).forEach(s -> list.add(s.name().toLowerCase()));
            Arrays.stream(ZoneSetting.values()).forEach(s -> list.add(s.name().toLowerCase()));
            return list.build();
        } else if (args.length == 1) {
            ImmutableList.Builder<String> list = ImmutableList.builder();
            Arrays.stream(WarConfig.WarSetting.values()).filter(s -> s.name().toLowerCase().startsWith(args[0].toLowerCase())).forEach(s -> list.add(s.name().toLowerCase()));
            Arrays.stream(ZoneSetting.values()).filter(s -> s.name().toLowerCase().startsWith(args[0].toLowerCase())).forEach(s -> list.add(s.name().toLowerCase()));
            return list.build();
        } else if (args.length == 2) {
            try {
                WarConfig.WarSetting setting = WarConfig.WarSetting.valueOf(args[1].toUpperCase());
                return ImmutableList.of(setting.getDefaultValue().toString());
            } catch (IllegalArgumentException e) {
                try {
                    ZoneSetting setting = ZoneSetting.valueOf(args[1].toUpperCase());
                    return ImmutableList.of(setting.getDefaultValue().toString());
                } catch (IllegalArgumentException e1) {
                    return ImmutableList.of();
                }
            }
        }
        return ImmutableList.of();
    }

    @Override
    public String getName() {
        return "warcfg";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("warconfig");
    }

    @Override
    public String getTagline() {
        return "View or set War global settings.";
    }

    @Override
    public String getDescription() {
        return "With no arguments, lists all War settings and zone defaults with their current value.\n" +
                "With one argument, displays the value of the War setting or zone default.\n" +
                "With two arguments, updates the value of the War setting or zone default to the provided argument.";
    }

    @Override
    public String getUsage() {
        return "[setting] [value]";
    }

    @Override
    public String getPermission() {
        return "war.config";
    }
}
