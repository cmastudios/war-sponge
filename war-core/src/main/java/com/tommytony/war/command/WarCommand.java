package com.tommytony.war.command;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.item.WarColor;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for all commands in the War plugin's abstract command system. This level handles command execution, error
 * processing, and arguments processing, serving as the entry-point to each child.
 */
public abstract class WarCommand {
    private final ServerAPI plugin;

    /**
     * Initializes the command.
     *
     * @param plugin instance of War plugin.
     */
    public WarCommand(ServerAPI plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempts to run a command. This method returns and throws nothing, but it may have side effects. This also
     * manages error and exception handling before passing control to the command itself.
     *
     * @param sender either console or the player who executed the command.
     * @param args   arguments after the command.
     */
    public void runCommand(WarConsole sender, String[] args) {
        try {
            // call the command run function
            handleCommand(sender, args);
        } catch (InvalidArgumentsError ex) {
            // custom exception representing insufficient or too many arguments.
            if (sender instanceof WarPlayer) {
                sender.sendMessage(WarColor.RED + ex.getMessage());
            } else {
                sender.sendMessage(ex.getMessage());
            }
            sender.sendMessage(WarColor.YELLOW + "Usage: " + this.getUsage());
        } catch (CommandUserError | IllegalArgumentException | IllegalStateException ex) {
            // thrown from the command or parts of the War plugin
            if (sender instanceof WarPlayer) {
                sender.sendMessage(WarColor.RED + ex.getMessage());
            } else {
                sender.sendMessage(ex.getMessage());
            }
        } catch (RuntimeException ex) {
            // error/bug in the plugin
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

    /**
     * Performs tab completion based on the arguments typed so far.
     *
     * @param sender player typing the command.
     * @param args arguments so far.
     * @return list of suggestions
     */
    public List<String> tabComplete(WarConsole sender, String[] args) {
        return handleTab(sender, args);
    }

    abstract void handleCommand(WarConsole sender, String[] args);

    abstract List<String> handleTab(WarConsole sender, String[] args);

    protected ServerAPI getPlugin() {
        return plugin;
    }

    /**
     * Gets the executable name of the command. e.g. /name
     *
     * @return name.
     */
    public abstract String getName();

    /**
     * Gets alternative names for the command which can be used by a sender.
     *
     * @return aliases.
     */
    public abstract List<String> getAliases();

    /**
     * Gets a short description of the command's function. This is displayed first in the help menu.
     *
     * @return tagline.
     */
    public abstract String getTagline();

    /**
     * Gets the main help string, displayed after the tagline.
     *
     * @return description.
     */
    public abstract String getDescription();

    /**
     * Gets format of command usage.
     *
     * @return usage.
     */
    public abstract String getUsage();

    /**
     * Gets permission required to use command. Will be checked by the server.
     *
     * @return permission.
     */
    public abstract String getPermission();
}
