package com.tommytony.war.command;

import com.tommytony.war.ServerAPI;

public abstract class WarCommandManager {
    private final ServerAPI plugin;

    protected WarCommandManager(ServerAPI plugin) {
        this.plugin = plugin;
    }

    protected abstract void registerCommand(WarCommand command);

    public void registerCommands() {
        registerCommand(new TeleportZoneCommand(plugin));
        registerCommand(new LeaveCommand(plugin));
        registerCommand(new SetPointCommand(plugin));
        registerCommand(new ResetZoneCommand(plugin));
        registerCommand(new ZoneConfigCommand(plugin));
        registerCommand(new SaveZoneCommand(plugin));
        registerCommand(new DeleteZoneCommand(plugin));
        registerCommand(new SetZoneCommand(plugin));
        registerCommand(new WarConfigCommand(plugin));
    }
}
