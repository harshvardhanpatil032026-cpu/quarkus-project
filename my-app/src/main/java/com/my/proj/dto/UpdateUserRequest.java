package com.my.proj.dto;

import java.util.Locale;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Partial update of a user's details. A null field is left unchanged; at least one must be given.
 * The name is trimmed and the email trimmed and lower-cased before validation.
 */
public record UpdateUserRequest(
        @Size(min = UpdateUserRequest.MIN_NAME_LENGTH, max = UpdateUserRequest.MAX_NAME_LENGTH,
                message = "name must be " + UpdateUserRequest.MIN_NAME_LENGTH + "-" + UpdateUserRequest.MAX_NAME_LENGTH
                        + " characters")
        @Pattern(regexp = "^[\\p{L}\\p{M} .'-]+$",
                message = "name may contain only letters, spaces, hyphens, apostrophes and periods")
        String name,
        @Email(message = "email must be a valid email address") String email) {

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;

    public UpdateUserRequest {
        if (name != null) {
            name = name.strip();
        }
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }

    @AssertTrue(message = "name or email is required")
    boolean isAnyFieldPresent() {
        return name != null || email != null;
    }

    @AssertTrue(message = "email must not be blank")
    boolean isEmailNotBlank() {
        return email == null || !email.isEmpty();
    }
}
