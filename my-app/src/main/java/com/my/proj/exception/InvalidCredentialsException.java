package com.my.proj.exception;

/**
 * Thrown when an email and password do not identify a user: unknown email, wrong password, or a
 * user with no stored password hash. Each operation uses one fixed message for all three cases,
 * so callers cannot tell which emails are registered.
 */
public class InvalidCredentialsException extends RuntimeException {

    /** Login failure message (DEMO-90). */
    public static final String LOGIN_MESSAGE = "Invalid credentials";

    /** Password-reset failure message (DEMO-99). */
    public static final String PASSWORD_RESET_MESSAGE = "Invalid email or password";

    private InvalidCredentialsException(String message) {
        super(message, null, false, false);
    }

    public static InvalidCredentialsException forLogin() {
        return new InvalidCredentialsException(LOGIN_MESSAGE);
    }

    public static InvalidCredentialsException forPasswordReset() {
        return new InvalidCredentialsException(PASSWORD_RESET_MESSAGE);
    }
}
