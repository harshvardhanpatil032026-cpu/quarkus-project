package com.my.proj.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "email is required") @Email(message = "email must be a valid email address") String email,
        @NotBlank(message = "password is required") String password) {

    /** Emails are stored trimmed and lower-cased, so normalise before validation and lookup. */
    public LoginRequest {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }

    // Never expose the plain-text password, even if the request object is logged.
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=***]";
    }
}
