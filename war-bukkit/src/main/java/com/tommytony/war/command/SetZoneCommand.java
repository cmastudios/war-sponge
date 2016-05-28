package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.tommytony.war.WarPlayerState;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneValidator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
        if (args.length == 0) {
            throw new CommandException("Insufficient arguments.");
        }
        if (plugin.getState(player).isCreatingZone() && (args[0].equalsIgnoreCase("c1") || args[0].equalsIgnoreCase("c2"))) {
            WarPlayerState.ZoneCreationState state = plugin.getState(player).getZoneCreationState();
            Block block = player.getTargetBlock(ImmutableSet.of(Material.AIR), 128);
            if (block == null || block.isEmpty()) {
                throw new CommandException("You are not pointing at a block.");
            }
            if (args[0].equalsIgnoreCase("c1")) {
                state.setPosition1(plugin.getWarLocation(block.getLocation()));
                sender.sendMessage(MessageFormat.format("Set position 1 of warzone {0} to {1}.", state.getZoneName(), state.getPosition1().toString()));
                return true;
            } else if (args[0].equalsIgnoreCase("c2")) {
                if (!state.isPosition1Set()) {
                    throw new CommandException("Please place corner 1 of the zone before placing corner 2.");
                }
                WarLocation pos2 = plugin.getWarLocation(block.getLocation());
                WarCuboid cuboid = new WarCuboid(state.getPosition1(), pos2);
                if (plugin.getValidator().validateDimensions(cuboid) == ZoneValidator.ValidationStatus.INVALID) {
                    throw new CommandException(ChatColor.RED + MessageFormat.format("Failed to create warzone {0} with dimensions {1}. Make your zone larger/smaller by placing your second corner somewhere else.",
                            state.getZoneName(), cuboid.toString()));
                }
                sender.sendMessage(MessageFormat.format("Set position 2 of warzone {0} to {1}.", state.getZoneName(), pos2.toString()));
                Warzone zone = plugin.createZone(state.getZoneName());
                zone.setCuboid(cuboid);
                zone.setTeleport(plugin.getWarLocation(player.getLocation()));
                plugin.getState(player).setZoneCreationState(null);
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
        plugin.getState(player).setZoneCreationState(new WarPlayerState.ZoneCreationState(args[0]));
        sender.sendMessage(MessageFormat.format("You are now creating warzone {0}. Type `/setzone c1'' to place the first corner based on the block under your cursor.", args[0]));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player))
            return ImmutableList.of();
        WarPlayerState.ZoneCreationState state = plugin.getState(((Player) sender)).getZoneCreationState();
        if (state == null) {
            return ImmutableList.of();
        } else if (state.isPosition1Set()) {
            return ImmutableList.of("c2");
        } else {
            return ImmutableList.of("c1");
        }
    }
}
