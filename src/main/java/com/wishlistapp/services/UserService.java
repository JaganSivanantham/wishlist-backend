package com.wishlistapp.services;

import com.wishlistapp.models.User;
import com.wishlistapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
     @Autowired // <-- Inject BCryptPasswordEncoder
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // Mock authentication token storage for simplicity
    private static final String MOCK_TOKEN_PREFIX = "mock_token_";

    public Optional<User> registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent() ||
            userRepository.findByUsername(user.getUsername()).isPresent()) {
            return Optional.empty(); // User already exists
        }
        // In a real app: hash password here
       user.setPasswordHash(bCryptPasswordEncoder.encode(user.getPasswordHash()));
        user.setId(UUID.randomUUID().toString()); // Generate ID
        return Optional.of(userRepository.save(user));
    }

   // Inside UserService.java, within your loginUser method
public String loginUser(String emailOrUsername, String password) {
    System.out.println("--- Login Attempt Start ---");
    System.out.println("Attempting login for: " + emailOrUsername);

    Optional<User> userOptional = userRepository.findByEmail(emailOrUsername);
    if (userOptional.isEmpty()) {
        userOptional = userRepository.findByUsername(emailOrUsername);
    }

    if (userOptional.isEmpty()) {
        System.out.println("User NOT found in database for: " + emailOrUsername);
        System.out.println("--- Login Attempt End (User Not Found) ---");
        return null; // User not found
    }

    User user = userOptional.get();
    System.out.println("User found: " + user.getUsername());
    // For debugging, you can print the provided and stored passwords.
    // REMOVE THESE LINES AFTER DEBUGGING FOR SECURITY!
    System.out.println("Provided password (plain): " + password);
    System.out.println("Stored password (hashed): " + user.getPasswordHash());

    if (bCryptPasswordEncoder.matches(password, user.getPasswordHash())) {
        System.out.println("Password MATCHED for user: " + user.getUsername());
        String mockToken = MOCK_TOKEN_PREFIX + user.getId();
        System.out.println("Generated mock token: " + mockToken);
        System.out.println("--- Login Attempt End (Success) ---");
        return mockToken; // Return a mock token
    } else {
        System.out.println("Password MISMATCH for user: " + user.getUsername());
        System.out.println("--- Login Attempt End (Password Mismatch) ---");
        return null; // Password does not match
    }
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
