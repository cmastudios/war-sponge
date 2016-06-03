package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;
import com.tommytony.war.zone.ZoneValidator;

import java.text.MessageFormat;
import java.util.List;

public class SetZoneCommand extends WarCommand {
    private static final List<String> nameIdeas = ImmutableList.of("ziggy", "bootcamp", "nuketown", "canyon", "valley", "bridge", "2fort", "venice", "fire_spleef");

    public SetZoneCommand(ServerAPI plugin) {
        super(plugin);
    }

    @Override
    void handleCommand(WarConsole sender, String[] args) {
        if (!(sender instanceof WarPlayer)) {
            throw new NotPlayerError();
        }
        WarPlayer player = (WarPlayer) sender;
        if (args.length == 0) {
            throw new InvalidArgumentsError();
        }
        if (player.isCreatingZone() && (args[0].equalsIgnoreCase("c1") || args[0].equalsIgnoreCase("c2"))) {
            WarPlayer.ZoneCreationState state = player.getZoneCreationState();
            WarLocation location = player.getTargetBlock();
            if (location == null) {
                throw new CommandUserError("You are not pointing at a block.");
            }
            if (args[0].equalsIgnoreCase("c1")) {
                state.setPosition1(location);
                sender.sendMessage(MessageFormat.format("Set position 1 of warzone {0} to {1}.", state.getZoneName(), state.getPosition1().toString()));
                return;
            } else if (args[0].equalsIgnoreCase("c2")) {
                if (!state.isPosition1Set()) {
                    throw new CommandUserError("Please place corner 1 of the zone before placing corner 2.");
                }
                WarCuboid cuboid = new WarCuboid(state.getPosition1(), location);
                if (getPlugin().getValidator().validateDimensions(cuboid) == ZoneValidator.ValidationStatus.INVALID) {
                    throw new CommandUserError(MessageFormat.format(
                            "Failed to create warzone {0} with dimensions {1}. Make your zone larger/smaller by placing your second corner somewhere else.",
                            state.getZoneName(), cuboid.toString()));
                }
                sender.sendMessage(MessageFormat.format("Set position 2 of warzone {0} to {1}.", state.getZoneName(), location.toString()));
                Warzone zone = getPlugin().createZone(state.getZoneName());
                zone.setCuboid(cuboid);
                zone.setTeleport(player.getLocation());
                player.setZoneCreationState(null);
                sender.sendMessage(MessageFormat.format("Successfully created warzone {0} with dimensions {1}.", zone.getName(), cuboid.toString()));
                return;
            }
        }
        if (args[0].equalsIgnoreCase("c1") || args[0].equalsIgnoreCase("c2")) {
            throw new CommandUserError("Create a new warzone before setting the corners.");
        }
        if (getPlugin().getValidator().validateName(args[0]) == ZoneValidator.ValidationStatus.INVALID) {
            throw new CommandUserError(MessageFormat.format("Name `{0}'' is invalid for a warzone.", args[0]));
        }
        player.setZoneCreationState(new WarPlayer.ZoneCreationState(args[0]));
        sender.sendMessage(MessageFormat.format("You are now creating warzone {0}. Type `/setzone c1'' to place the first corner based on the block under your cursor.", args[0]));
    }

    @Override
    List<String> handleTab(WarConsole sender, String[] args) {
        if (!(sender instanceof WarPlayer)) {
            throw new NotPlayerError();
        }
        WarPlayer player = (WarPlayer) sender;
        WarPlayer.ZoneCreationState state = player.getZoneCreationState();
        if (state == null) {
            if (args.length == 0) {
                return nameIdeas;
            } else {
                ImmutableList.Builder<String> list = ImmutableList.builder();
                nameIdeas.stream().filter(i -> i.startsWith(args[0].toLowerCase())).forEach(list::add);
                return list.build();
            }
        } else if (state.isPosition1Set()) {
            return ImmutableList.of("c2");
        } else {
            return ImmutableList.of("c1");
        }
    }

    @Override
    public String getName() {
        return "setzone";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getTagline() {
        return "Creates a warzone.";
    }

    @Override
    public String getDescription() {
        return "Process of setting a zone:\n" +
                "- Find two corners; these will mark the boundaries of the zone, maximum and minimum.\n" +
                "- Use /setzone <name> to begin marking.\n" +
                "- Point your cursor at one block and use /setzone c1.\n" +
                "- Point your cursor at the second block and use /setzone c2.";
    }

    @Override
    public String getUsage() {
        return "<zone/c1/c2>";
    }

    @Override
    public String getPermission() {
        return "war.zone.create";
    }
}
