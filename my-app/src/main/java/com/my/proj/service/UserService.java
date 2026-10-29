package com.my.proj.service;

import java.util.List;

import com.my.proj.entity.User;
import com.my.proj.exception.EmailAlreadyExistsException;
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
    public User createUser(User user) {
        if (user == null || user.email == null || user.email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (userRepository.count("email", user.email) > 0) {
            throw new EmailAlreadyExistsException(user.email);
        }
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
