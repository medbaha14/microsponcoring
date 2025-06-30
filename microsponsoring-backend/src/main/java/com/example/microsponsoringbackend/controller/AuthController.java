package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.dto.LoginRequest;
import com.example.microsponsoringbackend.dto.RegisterRequest;
import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.model.Status;
import com.example.microsponsoringbackend.service.UserService;
import com.example.microsponsoringbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userService.findAll().stream().anyMatch(u -> u.getUsername().equals(request.getUsername()) || u.getEmail().equals(request.getEmail()))) {
            return ResponseEntity.badRequest().body("Username or email already exists");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setUserType(request.getUserType());
        user.setStatus(Status.ACTIVE);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userService.save(user);
        return ResponseEntity.ok(user);
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
} 