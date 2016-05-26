package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

public class DeleteZoneCommand implements CommandCallable {
    private final WarPlugin plugin;

    public DeleteZoneCommand(WarPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        String[] args = arguments.trim().split(" ");
        if (args.length == 0 || args[0].length() == 0) {
            throw new CommandException(Text.of("Insufficient arguments."));
        }
        String zone = args[0].trim();
        if (!plugin.getZones().containsKey(zone)) {
            throw new CommandException(Text.of("Failed to find warzone ", zone));
        }
        String output = plugin.deleteZone(zone);
        source.sendMessage(Text.of(MessageFormat.format("Deleted warzone {0}, moved data file to {1}.", zone, output)));
        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return ImmutableList.of();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("war.zone.delete");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Delete a warzone"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("Delete a zone. Only permitted through permissions."));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("<zone>");
    }
}
