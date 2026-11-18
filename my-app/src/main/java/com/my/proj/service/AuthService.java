package com.my.proj.service;

import org.jboss.logging.Logger;

import com.my.proj.dto.LoginResponse;
import com.my.proj.entity.User;
import com.my.proj.exception.InvalidCredentialsException;
import com.my.proj.repository.UserRepository;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

/** Verifies login credentials and issues a token on success. */
@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    /**
     * Compared against when there is no real hash (unknown email, or a user created before
     * password hashing), so every failure path costs one bcrypt check and timing does not
     * reveal whether the email exists.
     */
    private static final String DUMMY_HASH = BcryptUtil.bcryptHash("dummy-password-for-timing");

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public LoginResponse login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("email and password are required");
        }
        User user = userRepository.find("email", email.trim().toLowerCase()).firstResult();

        if (user == null || user.passwordHash == null) {
            BcryptUtil.matches(password, DUMMY_HASH);
            // Log the reason, but never the email, password or hash.
            LOG.debugf("Login rejected: %s", user == null ? "unknown account" : "account has no password hash");
            throw new InvalidCredentialsException();
        }
        if (!BcryptUtil.matches(password, user.passwordHash)) {
            LOG.debugf("Login rejected for user id %d: password mismatch", user.id);
            throw new InvalidCredentialsException();
        }

        LOG.debugf("Login succeeded for user id %d", user.id);
        return LoginResponse.bearer(tokenService.issueToken(user), tokenService.lifespanSeconds());
    }
}
