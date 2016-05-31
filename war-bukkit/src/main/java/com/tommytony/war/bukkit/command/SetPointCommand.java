package com.tommytony.war.bukkit.command;

import com.tommytony.war.WarPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class SetPointCommand extends com.tommytony.war.command.SetPointCommand implements TabExecutor {
    private final WarPlugin plugin;

    public SetPointCommand(WarPlugin warPlugin) {
        super(warPlugin);
        this.plugin = warPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.runCommand(plugin.getSender(sender), args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return this.tabComplete(plugin.getSender(sender), args);
    }
}
