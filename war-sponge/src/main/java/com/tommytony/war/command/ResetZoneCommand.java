package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.zone.Warzone;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

import java.util.List;
import java.util.Optional;

public class ResetZoneCommand implements CommandCallable {
    private final WarPlugin plugin;

    public ResetZoneCommand(WarPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        String argv[] = arguments.trim().split(" ");
        if (!arguments.trim().isEmpty() && argv.length == 1) {
            String zoneName = argv[0];
            Warzone zone = plugin.getZone(zoneName);
            if (zone == null) {
                throw new CommandException(Texts.of(String.format("Can't find zone %s", zoneName)));
            }
            source.sendMessage(Texts.of(String.format("Reloading zone %s...", zoneName)));
            zone.reset();
            int affected = (int) Math.floor(zone.getCuboid().getSize());
            source.sendMessage(Texts.of(String.format("Reloaded %d blocks in zone %s.", affected, zoneName)));
            return CommandResult.builder().affectedBlocks(affected).build();
        }
        throw new CommandException(Texts.of("Insufficient arguments."));
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        ImmutableList.Builder<String> list = ImmutableList.builder();
        plugin.getZones().values().stream().filter(zone -> zone.getName().toLowerCase().startsWith(arguments.toLowerCase())).forEach(zone -> list.add(zone.getName()));
        return list.build();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("war.zonemaker");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Texts.of("Reset and reload a zone"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Texts.of("Ends the current game in the zone, then proceeds to reload all blocks from disk."));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Texts.of("<zone>");
    }
}