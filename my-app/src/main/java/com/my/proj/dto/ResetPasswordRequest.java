package com.my.proj.dto;

import java.util.Locale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "email is required") @Email(message = "email must be a valid email address") String email,
        @NotBlank(message = "currentPassword is required") String currentPassword,
        @NotBlank(message = "newPassword is required")
        @Size(min = ResetPasswordRequest.MIN_PASSWORD_LENGTH,
                message = "newPassword must be at least " + ResetPasswordRequest.MIN_PASSWORD_LENGTH + " characters")
        String newPassword) {

    /** Minimum length of a new password, per the product owner's answer on DEMO-99. */
    public static final int MIN_PASSWORD_LENGTH = 10;

    /** Emails are stored trimmed and lower-cased, so normalise before validation and lookup. */
    public ResetPasswordRequest {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }

    // Never expose either password, even if the request object is logged.
    @Override
    public String toString() {
        return "ResetPasswordRequest[email=" + email + ", currentPassword=***, newPassword=***]";
    }
}
