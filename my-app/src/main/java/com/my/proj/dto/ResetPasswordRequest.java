package com.my.proj.dto;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import jakarta.validation.constraints.AssertTrue;
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

    /** bcrypt uses only the first 72 bytes of a password, so a longer one would be silently cut short. */
    public static final int MAX_PASSWORD_BYTES = 72;

    /** Emails are stored trimmed and lower-cased, so normalise before validation and lookup. */
    public ResetPasswordRequest {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }

    @AssertTrue(message = "newPassword must be at most " + MAX_PASSWORD_BYTES + " bytes")
    boolean isNewPasswordWithinBcryptLimit() {
        return newPassword == null || newPassword.getBytes(StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES;
    }

    // Never expose either password, even if the request object is logged.
    @Override
    public String toString() {
        return "ResetPasswordRequest[email=" + email + ", currentPassword=***, newPassword=***]";
    }
}
