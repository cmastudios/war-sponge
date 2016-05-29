package com.tommytony.war;

public abstract class WarConsole {
    /**
     * Send the player a message. May contain formatting characters.
     *
     * @param message message
     */
    public abstract void sendMessage(String message);
}
