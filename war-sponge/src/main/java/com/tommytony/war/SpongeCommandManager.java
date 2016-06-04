package com.tommytony.war;

import com.tommytony.war.command.WarCommand;
import com.tommytony.war.command.WarCommandManager;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

class SpongeCommandManager extends WarCommandManager {
    private final WarPlugin plugin;

    SpongeCommandManager(WarPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void registerCommand(WarCommand command) {
        plugin.getGame().getCommandManager().register(plugin, new ReflectCommand(command), command.getAliases());
    }

    private final class ReflectCommand implements CommandCallable {
        private final WarCommand executor;

        ReflectCommand(WarCommand executor) {
            this.executor = executor;
        }

        @Override
        public CommandResult process(CommandSource source, String arguments) throws CommandException {
            String[] args = new String[0];
            if (!arguments.isEmpty()) {
                args = arguments.trim().split(" ");
            }
            executor.runCommand(plugin.getSender(source), args);
            return CommandResult.empty();
        }

        @Override
        public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
            String[] args = new String[0];
            if (!arguments.isEmpty()) {
                args = arguments.trim().split(" ");
            }
            return executor.tabComplete(plugin.getSender(source), args);
        }

        @Override
        public boolean testPermission(CommandSource source) {
            return source.hasPermission(executor.getPermission());
        }

        @Override
        public Optional<? extends Text> getShortDescription(CommandSource source) {
            return Optional.of(Text.of(executor.getTagline()));
        }

        @Override
        public Optional<? extends Text> getHelp(CommandSource source) {
            return Optional.of(Text.of(executor.getDescription()));
        }

        @Override
        public Text getUsage(CommandSource source) {
            return Text.of(executor.getUsage());
        }
    }
}
