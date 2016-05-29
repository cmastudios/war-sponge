package com.tommytony.war.command;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.WarPlayer;

import java.util.List;

public abstract class WarCommand {
    private final ServerAPI plugin;

    public WarCommand(ServerAPI plugin) {
        this.plugin = plugin;
    }

    public void runCommand(WarConsole sender, String[] args) {
        try {
            handleCommand(sender, args);
        } catch (CommandUserError | IllegalArgumentException | IllegalStateException ex) {
            if (sender instanceof WarPlayer) {
                sender.sendMessage("\u00A7c" + ex.getMessage());
            } else {
                sender.sendMessage(ex.getMessage());
            }
        } catch (RuntimeException ex) {
            if (sender instanceof WarPlayer) {
                sender.sendMessage("\u00A7cError in War plugin, check server console for details.");
            }
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
}
