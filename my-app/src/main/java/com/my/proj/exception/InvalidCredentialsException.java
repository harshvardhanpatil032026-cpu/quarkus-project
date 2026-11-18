package com.my.proj.exception;

/**
 * Thrown for every login failure: unknown email, wrong password, or a user with no stored
 * password hash. The message is deliberately generic so callers cannot tell which it was.
 */
public class InvalidCredentialsException extends RuntimeException {

    public static final String MESSAGE = "Invalid credentials";

    public InvalidCredentialsException() {
        super(MESSAGE, null, false, false);
    }
}
