package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.zone.Warzone;

import java.text.MessageFormat;
import java.util.List;

public class SetInventoryCommand extends WarCommand {
    public SetInventoryCommand(ServerAPI plugin) {
        super(plugin);
    }

    @Override
    void handleCommand(WarConsole sender, String[] args) {
        if (!(sender instanceof WarPlayer)) {
            throw new NotPlayerError();
        }
        WarPlayer player = (WarPlayer) sender;
        if (args.length < 1) {
            throw new InvalidArgumentsError();
        }
        String zoneName = args[0];
        Warzone zone = getPlugin().getZone(zoneName);
        if (zone == null) {
            throw new CommandUserError(MessageFormat.format("Warzone {0} not found.", zoneName));
        }
        String name = "default";
        if (args.length > 1) {
            name = args[1];
        }
        zone.saveInventory(name, player.getState().getInventory());
        player.sendMessage(MessageFormat.format("Inventory {0} for zone {1} set to the contents of your inventory.",
                name, zoneName));
    }

    @Override
    List<String> handleTab(WarConsole sender, String[] args) {
        return null;
    }

    @Override
    public String getName() {
        return "setinventory";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("inventory", "loadout");
    }

    @Override
    public String getTagline() {
        return "Set a loadout for a zone.";
    }

    @Override
    public String getDescription() {
        return "Upon execution, this command will save a copy of the player's current inventory. Whenever a player respawns in the warzone, their inventory will be populated with the saved contents.\n" +
                "The one saved with the name 'default' will be the automatically applied inventory, and the rest will be accessible by pressing the shift key in spawn.";
    }

    @Override
    public String getUsage() {
        return "<zone> [name]";
    }

    @Override
    public String getPermission() {
        return "war.zone.config";
    }
}
