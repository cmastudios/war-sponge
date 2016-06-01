package com.tommytony.war.sponge.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public class LeaveCommand extends com.tommytony.war.command.LeaveCommand implements CommandCallable {
    private final WarPlugin plugin;

    public LeaveCommand(WarPlugin warPlugin) {
        super(warPlugin);
        this.plugin = warPlugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        this.runCommand(plugin.getSender(source), arguments);
        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return ImmutableList.of();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return true;
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Leave an active game"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("Leave an active game"));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("");
    }
}
