package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.dto.LoginRequest;
import com.example.microsponsoringbackend.dto.RegisterRequest;
import com.example.microsponsoringbackend.dto.ForgotPasswordRequest;
import com.example.microsponsoringbackend.dto.ResetPasswordRequest;
import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.model.Status;
import com.example.microsponsoringbackend.service.UserService;
import com.example.microsponsoringbackend.service.PasswordResetService;
import com.example.microsponsoringbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.info("Registration request received for username: {}", request.getUsername());
        
        try {
            // Check for existing users with same username or email
            if (userService.findAll().stream().anyMatch(u -> u.getUsername().equals(request.getUsername()) || u.getEmail().equals(request.getEmail()))) {
                logger.warn("Registration failed - username or email already exists: {}", request.getUsername());
                return ResponseEntity.badRequest().body("Username or email already exists");
            }
        } catch (Exception e) {
            logger.warn("Error checking for existing users, proceeding with registration: {}", e.getMessage());
            // Continue with registration even if we can't check for duplicates
        }
        
        try {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setUserType(request.getUserType());
            user.setStatus(Status.ACTIVE);
            user.setActive(true);
            user.setAcceptedConditions(true);
            user.setIsVerified(false);
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            
            User savedUser = userService.save(user);
            logger.info("User registered successfully: {}", savedUser.getUsername());
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.findByUsername(request.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401).body("Invalid username or password");
            }
            if (user.getActive() == null || !user.getActive()) {
                return ResponseEntity.status(403).body("User is blocked");
            }
            user.setLastLogin(new Date());
            userService.save(user);

            String token = jwtUtil.generateToken(user.getUsername());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        boolean success = passwordResetService.initiatePasswordReset(request.getEmail());
        if (success) {
            return ResponseEntity.ok().body("Password reset email sent successfully");
        } else {
            return ResponseEntity.ok().body("If the email exists, a password reset link has been sent");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        if (success) {
            return ResponseEntity.ok().body(Map.of("message", "Password reset successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        logger.info("[API] validate-reset-token appelé avec token: {}", token);
        boolean valid = passwordResetService.validateToken(token);
        if (valid) {
            return ResponseEntity.ok().body(Map.of("message", "Token is valid"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
        }
    }
} 