package com.tommytony.war.command;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.World;

import java.util.List;

public class ClearEntCommand implements CommandCallable {
    private final WarPlugin plugin;

    public ClearEntCommand(WarPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        int total = 0;
        for (World world : plugin.getGame().getServer().getWorlds()) {
            int tally = 0;
            for (Entity ent : world.getEntities()) {
                ent.remove();
                tally++;
            }
            source.sendMessage(Texts.of("Cleared ", tally, " entities in world ", world));
            total += tally;
        }
        return CommandResult.builder().affectedEntities(total).build();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return ImmutableList.of();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("war.admin");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Texts.of("Clear all entities"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Texts.of("Remove all entities in every world"));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Texts.of("/clearent");
    }
}
