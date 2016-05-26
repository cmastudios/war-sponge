package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.zone.Warzone;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public class SaveZoneCommand implements CommandCallable {
    private final WarPlugin plugin;

    public SaveZoneCommand(WarPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        String argv[] = arguments.trim().split(" ");
        if (!arguments.trim().isEmpty() && argv.length == 1) {
            String zoneName = argv[0];
            Warzone zone = plugin.getZone(zoneName);
            if (zone == null) {
                throw new CommandException(Text.of(String.format("Can't find zone %s", zoneName)));
            }
            source.sendMessage(Text.of(String.format("Saving zone %s...", zoneName)));
            zone.save();
            int affected = (int) Math.floor(zone.getCuboid().getSize());
            source.sendMessage(Text.of(String.format("Saved %d blocks in zone %s.", affected, zoneName)));
            return CommandResult.builder().affectedBlocks(affected).build();
        }
        throw new CommandException(Text.of("Insufficient arguments."));
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
        return Optional.of(Text.of("Saves the zone to disk."));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("Saves the zone to disk. Deletes the previous saved copy of the zone.",
                "Will result in an error message if there is a problem with any block."));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("<zone>");
    }
}
