package com.tommytony.war.command;

/**
 * Thrown from a command when the user does not specify enough arguments, too many arguments, or incorrect arguments.
 */
public class InvalidArgumentsError extends CommandUserError {
    public InvalidArgumentsError() {
        super("Invalid or insufficient arguments to command.");
    }
}
