package com.tommytony.war;

/**
 * Command sender representing the server console.
 */
public abstract class WarConsole {
    /**
     * Send the recipient a message. May contain formatting characters.
     *
     * @param message message
     */
    public abstract void sendMessage(String message);
}
