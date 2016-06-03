package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.zone.Warzone;

import java.text.MessageFormat;
import java.util.List;

public class SaveZoneCommand extends WarCommand {
    public SaveZoneCommand(ServerAPI plugin) {
        super(plugin);
    }

    @Override
    void handleCommand(WarConsole sender, String[] args) {
        if (args.length != 1) {
            throw new InvalidArgumentsError();
        }
        String zoneName = args[0];
        Warzone zone = getPlugin().getZone(zoneName);
        if (zone == null) {
            throw new CommandUserError(MessageFormat.format("Warzone {0} not found.", zoneName));
        }
        sender.sendMessage(MessageFormat.format("Saving zone {0}...", zoneName));
        zone.save();
        int affected = (int) Math.floor(zone.getCuboid().getSize());
        sender.sendMessage(MessageFormat.format("Saved {0} blocks in zone {1}.", affected, zoneName));

    }

    @Override
    List<String> handleTab(WarConsole sender, String[] args) {
        if (args.length == 0) {
            return ImmutableList.copyOf(getPlugin().getZones().keySet());
        } else if (args.length != 1) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<String> list = ImmutableList.builder();
        getPlugin().getZones().values().stream()
                .filter(zone -> zone.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                .forEach(zone -> list.add(zone.getName()));
        return list.build();
    }

    @Override
    public String getName() {
        return "savezone";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getTagline() {
        return "Save zone blocks to disk.";
    }

    @Override
    public String getDescription() {
        return "This command only adds the current state of the zone to the storage file. All other configuration changes are saved automatically.\n" +
                "Warning: this command will function during a game; make sure this is the intended behavior.\n" +
                "Currently, the plugin does not store previous versions of zones: this command will overwrite the existing save.";
    }

    @Override
    public String getUsage() {
        return "<zone>";
    }

    @Override
    public String getPermission() {
        return "war.zone.save";
    }
}
