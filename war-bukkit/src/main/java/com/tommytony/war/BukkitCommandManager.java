package com.tommytony.war;

import com.tommytony.war.command.WarCommand;
import com.tommytony.war.command.WarCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.List;

class BukkitCommandManager extends WarCommandManager {
    private final WarPlugin plugin;
    private CommandMap commandMap;

    BukkitCommandManager(WarPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void registerCommand(WarCommand command) {
        ReflectCommand cmd = new ReflectCommand(command.getName());
        cmd.setAliases(command.getAliases());
        cmd.setDescription(command.getTagline() + "\n" + command.getDescription());
        cmd.setUsage("/<command> " + command.getUsage());
        cmd.setPermission(command.getPermission());
        cmd.setPermissionMessage(ChatColor.RED + "You do not have permission to execute this command.");
        getCommandMap().register("war", cmd);
        cmd.setExecutor(command);
    }

    private CommandMap getCommandMap() {
        if (commandMap == null) {
            try {
                final Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                field.setAccessible(true);
                commandMap = (CommandMap) field.get(Bukkit.getServer());
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        return commandMap;
    }

    private final class ReflectCommand extends Command implements PluginIdentifiableCommand {
        private WarCommand executor = null;

        ReflectCommand(String command) {
            super(command);
        }

        void setExecutor(WarCommand exe) {
            this.executor = exe;
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (executor != null) {
                executor.runCommand(plugin.getSender(sender), args);
            }
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            if (executor != null) {
                return executor.tabComplete(plugin.getSender(sender), args);
            }
            return null;
        }

        @Override
        public Plugin getPlugin() {
            return plugin;
        }
    }
}
