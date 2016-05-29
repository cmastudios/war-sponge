package com.tommytony.war.command;

public class CommandUserError extends RuntimeException {
    public CommandUserError() {
    }

    public CommandUserError(String message) {
        super(message);
    }
}
