package com.tommytony.war.command;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlayerState;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.World;

import java.text.MessageFormat;
import java.util.List;

public class SetZoneCommand implements CommandCallable {
    private final WarPlugin plugin;

    public SetZoneCommand(WarPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource commandSource, String s) throws CommandException {
        Player player;
        if (commandSource instanceof Player) {
            player = (Player) commandSource;
        } else {
            commandSource.sendMessage(Texts.of("This command cannot be used from console."));
            return CommandResult.empty();
        }
        String[] args = s.split(" ");
        if (args.length < 1) {
            return CommandResult.empty();
        }
        if (plugin.getState(player).isCreatingZone() && (args[0].equalsIgnoreCase("c1") || args[0].equalsIgnoreCase("c2"))) {
            WarPlayerState.ZoneCreationState state = plugin.getState(player).getZoneCreationState();
            final Optional<BlockRayHit<World>> block = BlockRay.from(player).filter(BlockRay.<World>onlyAirFilter()).end();
            if (!block.isPresent()) {
                commandSource.sendMessage(Texts.of("You are not pointing at a block."));
                return CommandResult.success();
            }
            if (args[0].equalsIgnoreCase("c1")) {
                state.setPosition1(plugin.getWarLocation(block.get().getLocation()));
                commandSource.sendMessage(Texts.of(MessageFormat.format("Set position 1 of warzone {0} to {1}.", state.getZoneName(), state.getPosition1().toString())));
                return CommandResult.success();
            } else if (args[0].equalsIgnoreCase("c2")) {
                if (!state.isPosition1Set()) {
                    commandSource.sendMessage(Texts.of("Please place corner 1 of the zone first."));
                    return CommandResult.success();
                }
                WarLocation pos2 = plugin.getWarLocation(block.get().getLocation());
                commandSource.sendMessage(Texts.of(MessageFormat.format("Set position 2 of warzone {0} to {1}.", state.getZoneName(), pos2.toString())));
                WarCuboid cuboid = new WarCuboid(state.getPosition1(), pos2);
                Warzone zone = plugin.createZone(state.getZoneName());
                zone.setCuboid(cuboid);
                plugin.getState(player).setZoneCreationState(null);
                commandSource.sendMessage(Texts.of(MessageFormat.format("Successfully created warzone {0} with dimensions {1}.", zone.getName(), cuboid.toString())));
                return CommandResult.success();
            }
        }
        if (args[0].equalsIgnoreCase("c1") || args[0].equalsIgnoreCase("c2")) {
            commandSource.sendMessage(Texts.of("Create a warzone using the command `/setzone <name>' first."));
            return CommandResult.success();
        }
        if (Warzone.zoneNameInvalid(args[0])) {
            commandSource.sendMessage(Texts.of(MessageFormat.format("Name `{0}'' is invalid for a warzone.", args[0])));
            return CommandResult.success();
        }
        plugin.getState(player).setZoneCreationState(new WarPlayerState.ZoneCreationState(args[0]));
        commandSource.sendMessage(Texts.of(MessageFormat.format("You are now creating warzone {0}. Type `/setzone c1'' to place the first corner based on the block under your cursor.", args[0])));

        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource commandSource, String s) throws CommandException {
        return ImmutableList.of();
    }

    @Override
    public boolean testPermission(CommandSource commandSource) {
        return commandSource.hasPermission("war.setzone");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource commandSource) {
        return Optional.of(Texts.of("Creates a warzone"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource commandSource) {
        return Optional.of(Texts.of("Creates a warzone. Puts the player into zone setting mode, which then allows them to select blocks."));
    }

    @Override
    public Text getUsage(CommandSource commandSource) {
        return Texts.of("<name>");
    }
}
