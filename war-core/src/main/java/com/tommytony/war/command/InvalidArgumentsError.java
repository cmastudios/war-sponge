package com.tommytony.war.command;

public class InvalidArgumentsError extends CommandUserError {
    public InvalidArgumentsError() {
        super("Invalid or insufficient arguments to command.");
    }
}
