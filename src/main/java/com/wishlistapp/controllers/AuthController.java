package com.wishlistapp.controllers;

import com.wishlistapp.models.User;
import com.wishlistapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "https://wishlist-frontend.netlify.app"})  // Allow React app to access
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        Optional<User> registeredUser = userService.registerUser(user);
        if (registeredUser.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", registeredUser.get().getId());
            response.put("username", registeredUser.get().getUsername());
            response.put("email", registeredUser.get().getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User with this email or username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String emailOrUsername = credentials.get("emailOrUsername");
        String password = credentials.get("password");

        String token = userService.loginUser(emailOrUsername, password);
        if (token != null) {
            Optional<User> userOptional = userService.getUserByToken(token);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("userId", user.getId());
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                return ResponseEntity.ok(response);
            }
        }
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // A simple endpoint to validate a token and get user info (for frontend)
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(name = "Authorization") String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or missing token"));
        }
        String token = tokenHeader.substring(7); // Remove "Bearer " prefix

        Optional<User> userOptional = userService.getUserByToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Map<String, String> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
    }
}
