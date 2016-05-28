package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarConfig;
import com.tommytony.war.WarPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class WarConfigCommand implements TabExecutor {
    public WarConfigCommand(WarPlugin warPlugin) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<String> list = ImmutableList.builder();
        for (WarConfig.WarSetting setting : WarConfig.WarSetting.values()) {
            if (setting.name().toLowerCase().startsWith(args[0].toLowerCase()))
                list.add(setting.name().toLowerCase() + ":");
        }
        return list.build();
    }
}
