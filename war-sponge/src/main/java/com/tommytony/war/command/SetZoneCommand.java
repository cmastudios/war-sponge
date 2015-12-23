package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlayerState;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneValidator;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.world.World;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

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
        String[] args = s.trim().split(" ");
        if (args.length == 0 || args[0].length() == 0) {
            throw new CommandException(Texts.of("Insufficient arguments."));
        }
        if (plugin.getState(player).isCreatingZone() && (args[0].equalsIgnoreCase("c1") || args[0].equalsIgnoreCase("c2"))) {
            WarPlayerState.ZoneCreationState state = plugin.getState(player).getZoneCreationState();
            final Optional<BlockRayHit<World>> block = BlockRay.from(player).filter(BlockRay.<World>onlyAirFilter()).end();
            if (!block.isPresent()) {
                throw new CommandException(Texts.of("You are not pointing at a block."));
            }
            if (args[0].equalsIgnoreCase("c1")) {
                state.setPosition1(plugin.getWarLocation(block.get().getLocation()));
                commandSource.sendMessage(Texts.of(MessageFormat.format("Set position 1 of warzone {0} to {1}.", state.getZoneName(), state.getPosition1().toString())));
                return CommandResult.success();
            } else if (args[0].equalsIgnoreCase("c2")) {
                if (!state.isPosition1Set()) {
                    throw new CommandException(Texts.of("Please place corner 1 of the zone before placing corner 2."));
                }
                WarLocation pos2 = plugin.getWarLocation(block.get().getLocation());
                WarCuboid cuboid = new WarCuboid(state.getPosition1(), pos2);
                if (plugin.getValidator().validateDimensions(cuboid) == ZoneValidator.ValidationStatus.INVALID) {
                    throw new CommandException(Texts.of(TextColors.RED, MessageFormat.format("Failed to create warzone {0} with dimensions {1}. Make your zone larger/smaller by placing your second corner somewhere else.",
                            state.getZoneName(), cuboid.toString())));
                }
                commandSource.sendMessage(Texts.of(MessageFormat.format("Set position 2 of warzone {0} to {1}.", state.getZoneName(), pos2.toString())));
                Warzone zone = plugin.createZone(state.getZoneName());
                zone.setCuboid(cuboid);
                zone.setTeleport(plugin.getWarLocation(player.getLocation()));
                plugin.getState(player).setZoneCreationState(null);
                commandSource.sendMessage(Texts.of(MessageFormat.format("Successfully created warzone {0} with dimensions {1}.", zone.getName(), cuboid.toString())));
                return CommandResult.success();
            }
        }
        if (args[0].equalsIgnoreCase("c1") || args[0].equalsIgnoreCase("c2")) {
            throw new CommandException(Texts.of("Create a new warzone before setting the corners."));
        }
        if (plugin.getValidator().validateName(args[0]) == ZoneValidator.ValidationStatus.INVALID) {
            throw new CommandException(Texts.of(MessageFormat.format("Name `{0}'' is invalid for a warzone.", args[0])));
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
        return Texts.of("<name / c1 / c2>");
    }
}
