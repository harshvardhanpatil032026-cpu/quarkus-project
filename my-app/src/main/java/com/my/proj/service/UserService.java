package com.my.proj.service;

import java.util.List;
import java.util.Locale;

import com.my.proj.entity.User;
import com.my.proj.exception.EmailAlreadyExistsException;
import com.my.proj.exception.InvalidCredentialsException;
import com.my.proj.exception.UserNotFoundException;
import com.my.proj.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

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
        if (userRepository.count("email", email) > 0) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = new User();
        user.name = name;
        user.email = email;
        // Salted, adaptive bcrypt hash; the plain-text password is never stored or logged.
        user.passwordHash = PasswordHashing.hash(password);
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

        User user = userRepository.find("email", email.trim().toLowerCase(Locale.ROOT)).firstResult();
        // A missing user or hash still costs one bcrypt check, so timing reveals nothing.
        if (!PasswordHashing.matches(currentPassword, user == null ? null : user.passwordHash)) {
            throw InvalidCredentialsException.forPasswordReset();
        }
        user.passwordHash = PasswordHashing.hash(newPassword);
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (userRepository.delete("email", email) == 0) {
            throw new UserNotFoundException(email);
        }
    }
}
