package com.wishlistapp.services;

import com.wishlistapp.models.User;
import com.wishlistapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Mock authentication token storage for simplicity
    private static final String MOCK_TOKEN_PREFIX = "mock_token_";

    public Optional<User> registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent() ||
            userRepository.findByUsername(user.getUsername()).isPresent()) {
            return Optional.empty(); // User already exists
        }
        // In a real app: hash password here
        user.setPasswordHash(user.getPasswordHash()); // Storing as plain for mock, hash it!
        user.setId(UUID.randomUUID().toString()); // Generate ID
        return Optional.of(userRepository.save(user));
    }

    public String loginUser(String emailOrUsername, String password) {
        Optional<User> userOptional = userRepository.findByEmail(emailOrUsername);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByUsername(emailOrUsername);
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // In a real app: compare hashed passwords
            if (user.getPasswordHash().equals(password)) { // Mock password check
                return MOCK_TOKEN_PREFIX + user.getId(); // Return a mock token
            }
        }
        return null; // Login failed
    }

    public Optional<User> getUserByToken(String token) {
        if (token != null && token.startsWith(MOCK_TOKEN_PREFIX)) {
            String userId = token.substring(MOCK_TOKEN_PREFIX.length());
            return userRepository.findById(userId);
        }
        return Optional.empty();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
 // In UserService.java
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}