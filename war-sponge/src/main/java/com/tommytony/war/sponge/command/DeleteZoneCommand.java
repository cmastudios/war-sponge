package com.tommytony.war.sponge.command;

import com.tommytony.war.WarPlugin;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public class DeleteZoneCommand extends com.tommytony.war.command.DeleteZoneCommand implements CommandCallable {

    private final WarPlugin plugin;

    public DeleteZoneCommand(WarPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        this.runCommand(plugin.getSender(source), arguments);
        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return this.tabComplete(plugin.getSender(source), arguments);
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
