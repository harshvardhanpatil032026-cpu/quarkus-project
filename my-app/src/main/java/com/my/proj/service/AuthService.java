package com.my.proj.service;

import java.util.Locale;

import org.jboss.logging.Logger;

import com.my.proj.dto.LoginResponse;
import com.my.proj.entity.User;
import com.my.proj.exception.InvalidCredentialsException;
import com.my.proj.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/** Verifies login credentials and issues a token on success. */
@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

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
        User user = userRepository.find("email", email.trim().toLowerCase(Locale.ROOT)).firstResult();

        // A missing user or hash still costs one bcrypt check, so timing reveals nothing.
        boolean matches = PasswordHashing.matches(password, user == null ? null : user.getPasswordHash());
        if (user == null || !matches) {
            // Log the reason with fixed messages only: nothing from the request or the account.
            if (user == null) {
                LOG.debug("Login rejected: unknown account");
            } else if (user.getPasswordHash() == null) {
                LOG.debug("Login rejected: account has no password hash");
            } else {
                LOG.debug("Login rejected: password mismatch");
            }
            throw InvalidCredentialsException.forLogin();
        }

        LOG.debug("Login succeeded");
        return LoginResponse.bearer(tokenService.issueToken(user), tokenService.lifespanSeconds());
    }
}
