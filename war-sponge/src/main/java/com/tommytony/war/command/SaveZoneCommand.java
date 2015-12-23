package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;

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
                throw new CommandException(Texts.of(String.format("Can't find warzone %s", zoneName)));
            }
            zone.save();
            int affected = (int) Math.floor(zone.getCuboid().getSize());
            source.sendMessage(Texts.of(String.format("Saved %d blocks in warzone %s.", affected, zoneName)));
            return CommandResult.builder().affectedBlocks(affected).build();
        }
        if (argv.length == 2 && argv[1].equals("tdog")) {
            String zoneName = argv[0];
            Warzone zone = plugin.getZone(zoneName);
            if (zone == null) {
                throw new CommandException(Texts.of(String.format("Can't find warzone %s", zoneName)));
            }
            for (WarLocation loc : zone.getCuboid()) {
                plugin.setBlock(loc, new WarBlock("minecraft:glass", null, ""));
            }
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
        return Optional.of(Texts.of("Saves the warzone to disk."));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Texts.of("Saves the warzone to disk. Deletes the previous saved copy of the warzone.",
                "Will result in an error message if there is a problem with any block."));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Texts.of("<zone>");
    }
}
