package com.tommytony.war.command;

/**
 * Thrown when the command requires a player instead of the console. This may be thrown depending on the arguments, for
 * example some commands offer an enumeration of zones with no arguments but perform a teleportation depending on the
 * input. Therefore, commands may throw this error under varying circumstances.
 */
public class NotPlayerError extends CommandUserError {
    public NotPlayerError() {
        super("This command cannot be used by console.");
    }
}
