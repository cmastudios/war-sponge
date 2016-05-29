package com.tommytony.war;

import org.bukkit.Bukkit;

public class BukkitWarConsole extends WarConsole {
    @Override
    public void sendMessage(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }
}
