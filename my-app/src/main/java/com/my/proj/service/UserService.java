package com.my.proj.service;

import java.util.List;

import com.my.proj.entity.User;
import com.my.proj.exception.EmailAlreadyExistsException;
import com.my.proj.exception.UserNotFoundException;
import com.my.proj.repository.UserRepository;

import io.quarkus.elytron.security.common.BcryptUtil;
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
        email = email.trim().toLowerCase();
        if (userRepository.count("email", email) > 0) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = new User();
        user.name = name;
        user.email = email;
        // Salted, adaptive bcrypt hash; the plain-text password is never stored or logged.
        user.passwordHash = BcryptUtil.bcryptHash(password);
        userRepository.persist(user);
        return user;
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
