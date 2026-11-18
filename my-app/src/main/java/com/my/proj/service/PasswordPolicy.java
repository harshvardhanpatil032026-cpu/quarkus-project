package com.my.proj.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The rules a new password must meet (DEMO-91). Every broken rule is returned, so a refusal can
 * list them all at once.
 */
final class PasswordPolicy {

    static final int MIN_LENGTH = 8;
    static final int MAX_LENGTH = 64;

    /** bcrypt uses only the first 72 bytes of a password, so a longer one would be silently cut short. */
    static final int MAX_BYTES = 72;

    private static final Pattern UPPERCASE = Pattern.compile("\\p{Lu}");
    private static final Pattern LOWERCASE = Pattern.compile("\\p{Ll}");
    private static final Pattern DIGIT = Pattern.compile("\\p{Nd}");
    private static final Pattern SPECIAL = Pattern.compile("[^\\p{L}\\p{N}\\s]");

    private PasswordPolicy() {
    }

    /** The rules that need nothing but the password itself. */
    static List<String> formatProblems(String password) {
        List<String> problems = new ArrayList<>();
        if (password == null || password.isBlank()) {
            problems.add("newPassword is required");
            return problems;
        }
        int length = password.codePointCount(0, password.length());
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            problems.add("newPassword must be " + MIN_LENGTH + "-" + MAX_LENGTH + " characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            problems.add("newPassword must be at most " + MAX_BYTES + " bytes");
        }
        if (!UPPERCASE.matcher(password).find()) {
            problems.add("newPassword must contain an uppercase letter");
        }
        if (!LOWERCASE.matcher(password).find()) {
            problems.add("newPassword must contain a lowercase letter");
        }
        if (!DIGIT.matcher(password).find()) {
            problems.add("newPassword must contain a digit");
        }
        if (!SPECIAL.matcher(password).find()) {
            problems.add("newPassword must contain a special character");
        }
        return problems;
    }

    /** Every rule, including those that need the user's details and current password. */
    static List<String> problems(String password, String currentPassword, String email, String name) {
        List<String> problems = formatProblems(password);
        if (password == null || password.isBlank()) {
            return problems;
        }
        String lowered = password.toLowerCase(Locale.ROOT);
        if (email != null && !email.isBlank() && lowered.contains(email.trim().toLowerCase(Locale.ROOT))) {
            problems.add("newPassword must not contain your email");
        }
        if (name != null && !name.isBlank() && lowered.contains(name.trim().toLowerCase(Locale.ROOT))) {
            problems.add("newPassword must not contain your name");
        }
        if (password.equals(currentPassword)) {
            problems.add("newPassword must be different from the current password");
        }
        return problems;
    }
}
