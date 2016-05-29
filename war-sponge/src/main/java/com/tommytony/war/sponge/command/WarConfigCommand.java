package com.tommytony.war.sponge.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarConfig;
import com.tommytony.war.WarPlugin;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public class WarConfigCommand implements CommandCallable {

    private final Optional<Text> desc = Optional.of((Text) Text.of("View/modify War config"));
    private final Optional<Text> help = Optional.of((Text) Text.of("Allows viewing of the server config or changing various settings."));
    private final Text usage = (Text) Text.of("[-p] setting value");

    private final WarPlugin plugin;

    public WarConfigCommand(WarPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        ImmutableList.Builder<String> list = ImmutableList.builder();
        for (WarConfig.WarSetting setting : WarConfig.WarSetting.values()) {
            if (setting.name().toLowerCase().startsWith(arguments.toLowerCase()))
                list.add(setting.name().toLowerCase() + ":");
        }
        return list.build();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("war.config");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return desc;
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return help;
    }

    @Override
    public Text getUsage(CommandSource source) {
        return usage;
    }
}
