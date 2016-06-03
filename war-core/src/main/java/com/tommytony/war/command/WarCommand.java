package com.tommytony.war.command;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.item.WarColor;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public abstract class WarCommand {
    private final ServerAPI plugin;

    public WarCommand(ServerAPI plugin) {
        this.plugin = plugin;
    }

    public void runCommand(WarConsole sender, String[] args) {
        try {
            handleCommand(sender, args);
        } catch (InvalidArgumentsError ex) {
            if (sender instanceof WarPlayer) {
                sender.sendMessage(WarColor.RED + ex.getMessage());
            } else {
                sender.sendMessage(ex.getMessage());
            }
            sender.sendMessage(WarColor.YELLOW + "Usage: " + this.getUsage());
        } catch (CommandUserError | IllegalArgumentException | IllegalStateException ex) {
            if (sender instanceof WarPlayer) {
                sender.sendMessage(WarColor.RED + ex.getMessage());
            } else {
                sender.sendMessage(ex.getMessage());
            }
        } catch (RuntimeException ex) {
            if (sender instanceof WarPlayer) {
                sender.sendMessage(WarColor.RED + "Error in War plugin, check server console for details.");
            }
            StringBuilder invocation = new StringBuilder();
            Arrays.stream(args).forEach(a -> invocation.append(a).append(' '));
            System.out.println(MessageFormat.format("War Error: user {0}, command: /{1} {2}",
                    sender instanceof WarPlayer ? ((WarPlayer) sender).getName() : "Console",
                    this.getName(), invocation));
            ex.printStackTrace();
        }
    }

    public void runCommand(WarConsole sender, String arg) {
        if (arg.isEmpty()) {
            runCommand(sender, new String[]{});
        } else {
            String[] args = arg.trim().split(" ");
            runCommand(sender, args);
        }
    }

    public List<String> tabComplete(WarConsole sender, String[] args) {
        return handleTab(sender, args);
    }

    public List<String> tabComplete(WarConsole sender, String arg) {
        if (arg.isEmpty()) {
            return tabComplete(sender, new String[]{});
        } else {
            String[] args = arg.trim().split(" ");
            return tabComplete(sender, args);
        }
    }

    abstract void handleCommand(WarConsole sender, String[] args);

    abstract List<String> handleTab(WarConsole sender, String[] args);

    public ServerAPI getPlugin() {
        return plugin;
    }

    public abstract String getName();

    public abstract List<String> getAliases();

    public abstract String getTagline();

    public abstract String getDescription();

    public abstract String getUsage();

    public abstract String getPermission();
}
