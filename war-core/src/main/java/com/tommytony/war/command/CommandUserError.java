package com.tommytony.war.command;

/**
 * Represents an error on the part of the user. Analogous to a 404 web error. Can be thrown if, for example, a zone is
 * not found.
 */
public class CommandUserError extends RuntimeException {
    public CommandUserError() {
    }

    public CommandUserError(String message) {
        super(message);
    }
}
