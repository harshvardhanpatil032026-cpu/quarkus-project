package com.my.proj.service;

import io.quarkus.elytron.security.common.BcryptUtil;

/** Bcrypt hashing and verification shared by registration, login and password reset. */
final class PasswordHashing {

    /**
     * Checked against when there is no real hash (unknown email, or a user created before
     * password hashing), so every failure costs one bcrypt check and response time does not
     * reveal whether the email exists.
     */
    private static final String DUMMY_HASH = BcryptUtil.bcryptHash("dummy-password-for-timing");

    private PasswordHashing() {
    }

    /** Salted, adaptive bcrypt hash of a plain-text password. */
    static String hash(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }

    /**
     * Whether {@code plainPassword} matches {@code storedHash}. A null hash never matches, but
     * still costs one bcrypt check, so it takes as long as a wrong password.
     */
    static boolean matches(String plainPassword, String storedHash) {
        if (storedHash == null) {
            BcryptUtil.matches(plainPassword, DUMMY_HASH);
            return false;
        }
        return BcryptUtil.matches(plainPassword, storedHash);
    }
}
