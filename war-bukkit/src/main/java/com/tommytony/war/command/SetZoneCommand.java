package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneValidator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.List;

public class SetZoneCommand implements TabExecutor {
    private final WarPlugin plugin;

    public SetZoneCommand(WarPlugin warPlugin) {
        this.plugin = warPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sender.sendMessage("This command can't be used from console.");
            return true;
        }
        WarPlayer warPlayer = plugin.getWarPlayer(player);
        if (args.length == 0) {
            throw new CommandException("Insufficient arguments.");
        }
        if (warPlayer.isCreatingZone() && (args[0].equalsIgnoreCase("c1") || args[0].equalsIgnoreCase("c2"))) {
            WarPlayer.ZoneCreationState state = warPlayer.getZoneCreationState();
            WarLocation location = warPlayer.getTargetBlock();
            WarBlock block = plugin.getBlock(location, true);
            if (block == null) {
                throw new CommandException("You are not pointing at a block.");
            }
            if (args[0].equalsIgnoreCase("c1")) {
                state.setPosition1(location);
                sender.sendMessage(MessageFormat.format("Set position 1 of warzone {0} to {1}.", state.getZoneName(), state.getPosition1().toString()));
                return true;
            } else if (args[0].equalsIgnoreCase("c2")) {
                if (!state.isPosition1Set()) {
                    throw new CommandException("Please place corner 1 of the zone before placing corner 2.");
                }
                WarCuboid cuboid = new WarCuboid(state.getPosition1(), location);
                if (plugin.getValidator().validateDimensions(cuboid) == ZoneValidator.ValidationStatus.INVALID) {
                    throw new CommandException(ChatColor.RED + MessageFormat.format("Failed to create warzone {0} with dimensions {1}. Make your zone larger/smaller by placing your second corner somewhere else.",
                            state.getZoneName(), cuboid.toString()));
                }
                sender.sendMessage(MessageFormat.format("Set position 2 of warzone {0} to {1}.", state.getZoneName(), location.toString()));
                Warzone zone = plugin.createZone(state.getZoneName());
                zone.setCuboid(cuboid);
                zone.setTeleport(plugin.getWarLocation(player.getLocation()));
                warPlayer.setZoneCreationState(null);
                sender.sendMessage(MessageFormat.format("Successfully created warzone {0} with dimensions {1}.", zone.getName(), cuboid.toString()));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("c1") || args[0].equalsIgnoreCase("c2")) {
            throw new CommandException("Create a new warzone before setting the corners.");
        }
        if (plugin.getValidator().validateName(args[0]) == ZoneValidator.ValidationStatus.INVALID) {
            throw new CommandException(MessageFormat.format("Name `{0}'' is invalid for a warzone.", args[0]));
        }
        warPlayer.setZoneCreationState(new WarPlayer.ZoneCreationState(args[0]));
        sender.sendMessage(MessageFormat.format("You are now creating warzone {0}. Type `/setzone c1'' to place the first corner based on the block under your cursor.", args[0]));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player))
            return ImmutableList.of();
        WarPlayer.ZoneCreationState state = plugin.getWarPlayer(((Player) sender)).getZoneCreationState();
        if (state == null) {
            return ImmutableList.of();
        } else if (state.isPosition1Set()) {
            return ImmutableList.of("c2");
        } else {
            return ImmutableList.of("c1");
        }
    }
}
