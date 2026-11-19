package com.my.proj.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jboss.logging.Logger;

import com.my.proj.entity.User;
import com.my.proj.exception.EmailAlreadyExistsException;
import com.my.proj.exception.InvalidCredentialsException;
import com.my.proj.exception.UserNotFoundException;
import com.my.proj.repository.UserRepository;
import com.my.proj.repository.UserRoleRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class);

    private static final String EMAIL = "email";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.listAll();
    }

    public User getUserById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }

    /**
     * Finds the user registered with this email. The lookup is case-insensitive: the email is
     * trimmed and lower-cased the same way {@link #createUser} stores it.
     *
     * @throws IllegalArgumentException if the email is blank
     * @throws UserNotFoundException if no user has this email
     */
    public User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        String normalised = email.trim().toLowerCase(Locale.ROOT);
        String domain = normalised.substring(normalised.indexOf('@') + 1);
        User user = userRepository.find(EMAIL, normalised).firstResult();
        if (user == null) {
            throw new UserNotFoundException(normalised);
        }
        return user;
    }

    @Transactional
    public User createUser(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.count(EMAIL, email) > 0) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        // Salted, adaptive bcrypt hash; the plain-text password is never stored or logged.
        user.setPasswordHash(PasswordHashing.hash(password));
        userRepository.persist(user);
        return user;
    }

    /**
     * Replaces the user's password hash once the current password is confirmed. Runs in one
     * transaction: the managed entity's new hash is flushed on commit, or not at all.
     *
     * @throws IllegalArgumentException if a field is blank or the new password equals the current one
     * @throws InvalidCredentialsException if the email is unknown, the user has no stored hash, or
     *         the current password is wrong (the same exception for all three)
     */
    @Transactional
    public void resetPassword(String email, String currentPassword, String newPassword) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password cannot be null or empty");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }
        if (newPassword.equals(currentPassword)) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        User user = userRepository.find(EMAIL, email.trim().toLowerCase(Locale.ROOT)).firstResult();
        // A missing user or hash still costs one bcrypt check, so timing reveals nothing.
        boolean matches = PasswordHashing.matches(currentPassword, user == null ? null : user.getPasswordHash());
        if (user == null || !matches) {
            throw InvalidCredentialsException.forPasswordReset();
        }
        user.setPasswordHash(PasswordHashing.hash(newPassword));
    }

    /**
     * Changes the user's name and/or email. A null argument leaves that field unchanged; the name
     * is expected trimmed and the email normalised (see UpdateUserRequest).
     *
     * @throws UserNotFoundException if no user has {@code email}
     * @throws EmailAlreadyExistsException if {@code newEmail} belongs to another user
     */
    @Transactional
    public User updateUser(String email, String newName, String newEmail) {
        User user = getUserByEmail(email);
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (userRepository.count(EMAIL, newEmail) > 0) {
                throw new EmailAlreadyExistsException(newEmail);
            }
            user.setEmail(newEmail);
        }
        if (newName != null) {
            user.setName(newName);
        }
        return user;
    }

    /**
     * Replaces the password of the user with this email once the current password is confirmed.
     * Every rule is checked here, so a refusal lists all the broken rules at once. Rules that need
     * the user's details are checked only after the current password is confirmed, so nothing
     * about the account is revealed to a caller who does not know it.
     *
     * @throws IllegalArgumentException listing every broken rule (400)
     * @throws InvalidCredentialsException if the user is unknown or the current password is wrong (401)
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            List<String> problems = new ArrayList<>();
            problems.add("currentPassword is required");
            problems.addAll(PasswordPolicy.formatProblems(newPassword));
            throw new IllegalArgumentException(String.join("; ", problems));
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("newPassword is required");
        }

        User user = email == null ? null : userRepository.find(EMAIL, email.trim().toLowerCase(Locale.ROOT)).firstResult();
        // A missing user or hash still costs one bcrypt check, so timing reveals nothing.
        boolean matches = PasswordHashing.matches(currentPassword, user == null ? null : user.getPasswordHash());
        if (user == null || !matches) {
            throw InvalidCredentialsException.forPasswordReset();
        }

        List<String> problems = PasswordPolicy.problems(newPassword, currentPassword, user.getEmail(), user.getName());
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", problems));
        }

        user.setPasswordHash(PasswordHashing.hash(newPassword));
        // Log the user id only: never the password or its hash.
        LOG.infof("Password changed for user %s", user.id);
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        String normalised = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.find(EMAIL, normalised).firstResult();
        if (user == null) {
            throw new UserNotFoundException(normalised);
        }
        // Role grants reference the user, so they go first.
        userRoleRepository.deleteForUser(user.id);
        userRepository.delete(user);
    }
}
