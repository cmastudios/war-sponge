package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.zone.Warzone;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;

import java.util.List;
import java.util.Optional;

/**
 * Teleport to warzone.
 */
public class WarzoneCommand implements CommandCallable {

    private final Optional<Text> desc = Optional.of((Text) Texts.of("Teleport to a zone"));
    private final Optional<Text> help = Optional.of((Text) Texts.of("Teleport to a warzone, or join automatically."));
    private final Text usage = (Text) Texts.of("<zone>");

    private WarPlugin plugin;

    public WarzoneCommand(WarPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource commandSource, String s) throws CommandException {
        if (!(commandSource instanceof Player)) {
            return CommandResult.empty();
        }
        String[] argv = s.trim().split(" ");
        if (argv.length == 0 || argv[0].length() == 0) {
            StringBuilder zones = new StringBuilder();
            boolean first = true;
            for (String zoneName : plugin.getZones().keySet()) {
                if (!first) {
                    zones.append(", ");
                }
                first = false;
                zones.append(zoneName);
            }
            commandSource.sendMessage(Texts.of("Warzones: ", zones.toString()));
            return CommandResult.success();
        }
        String zoneName = argv[0];
        Warzone zone = plugin.getZone(zoneName);
        if (zone == null) {
            return CommandResult.empty();
        }
        Player player = (Player) commandSource;
        player.setLocation(plugin.getSpongeLocation(zone.getTeleport()));

        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource commandSource, String s) throws CommandException {
        ImmutableList.Builder<String> list = ImmutableList.builder();
        for (Warzone zone : plugin.getZones().values()) {
            if (zone.getName().toLowerCase().startsWith(s.toLowerCase())) {
                list.add(zone.getName());
            }
        }
        return list.build();
    }

    @Override
    public boolean testPermission(CommandSource commandSource) {
        return commandSource.hasPermission("war.teleport");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource commandSource) {
        return desc;
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource commandSource) {
        return help;
    }

    @Override
    public Text getUsage(CommandSource commandSource) {
        return usage;
    }
}