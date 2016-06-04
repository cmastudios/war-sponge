package com.tommytony.war.command;

import com.tommytony.war.ServerAPI;

/**
 * Manages the War abstract command system.
 */
public abstract class WarCommandManager {
    private final ServerAPI plugin;

    protected WarCommandManager(ServerAPI plugin) {
        this.plugin = plugin;
    }

    protected abstract void registerCommand(WarCommand command);

    /**
     * Registers all the official War plugin commands in the respective server software.
     * Note: multiple execution under Bukkit does not cause any issues.
     */
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
