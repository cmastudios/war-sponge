package com.tommytony.war.command;

public class NotPlayerError extends CommandUserError {
    public NotPlayerError() {
        super("This command cannot be used by console.");
    }
}
