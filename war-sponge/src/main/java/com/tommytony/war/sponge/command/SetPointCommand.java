package com.tommytony.war.sponge.command;

import com.tommytony.war.WarPlugin;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public class SetPointCommand extends com.tommytony.war.command.SetPointCommand implements CommandCallable {
    private final WarPlugin plugin;

    public SetPointCommand(WarPlugin warPlugin) {
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
        return this.tabComplete(plugin.getSender(source), arguments);
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("war.zone.config");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Set a location for a zone, such as a spawn or gate."));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Text.of(
                "To set a spawn, use /setpoint <zone> spawn <teamname>.\nTo set a gate, use /setpoint <zone> gate <teamname / autoassign>.\nTo set a lobby, use /setpoint <zone> lobby."));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("<zone> <lobby/spawn/gate> [team/autoassign]");
    }
}
