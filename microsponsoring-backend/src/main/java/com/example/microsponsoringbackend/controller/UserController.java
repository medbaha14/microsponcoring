package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.microsponsoringbackend.dto.OrganisationProfileDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.example.microsponsoringbackend.model.UserType;
import com.example.microsponsoringbackend.service.SponsorService;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private SponsorService sponsorService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public List<User> getAll(Authentication authentication) {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable UUID id) {
        Optional<User> result = userService.findById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/organisation-profile")
    public ResponseEntity<OrganisationProfileDTO> getOrganisationProfile(@PathVariable UUID id) {
        OrganisationProfileDTO profile = userService.getOrganisationProfile(id);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{id}/organisation-profile")
    public ResponseEntity<Void> updateOrganisationProfile(@PathVariable UUID id, @RequestBody OrganisationProfileDTO profileDto) {
        // Add debugging information
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("User attempting to update organisation profile: {}", auth.getName());
        logger.info("User authorities: {}", auth.getAuthorities());
        logger.info("Target user ID: {}", id);
        
        userService.updateOrganisationProfile(id, profileDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        Date currentDate = new Date();
        user.setCreatedAt(currentDate);
        user.setUpdatedAt(currentDate);
        return userService.save(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable UUID id, @RequestBody User user) {
        if (!userService.existsById(id)) return ResponseEntity.notFound().build();

        // Fetch the existing user
        User existingUser = userService.findById(id).orElse(null);
        if (existingUser == null) return ResponseEntity.notFound().build();

        // Only update non-null fields
        if (user.getEmail() != null) existingUser.setEmail(user.getEmail());
        if (user.getUsername() != null) existingUser.setUsername(user.getUsername());
        if (user.getPassword() != null) existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getFullName() != null) existingUser.setFullName(user.getFullName());
        if (user.getActive() != null) existingUser.setActive(user.getActive());
        if (user.getUserType() != null) existingUser.setUserType(user.getUserType());
        if (user.getStatus() != null) existingUser.setStatus(user.getStatus());
        if (user.getAcceptedConditions() != null) existingUser.setAcceptedConditions(user.getAcceptedConditions());
        if (user.getLastLogin() != null) existingUser.setLastLogin(user.getLastLogin());
        if (user.getProfilePicture() != null) existingUser.setProfilePicture(user.getProfilePicture());
        if (user.getBio() != null) existingUser.setBio(user.getBio());
        if (user.getLocation() != null) existingUser.setLocation(user.getLocation());
        if (user.getWebsiteUrl() != null) existingUser.setWebsiteUrl(user.getWebsiteUrl());
        if (user.getIsVerified() != null) existingUser.setIsVerified(user.getIsVerified());

        // Always update the updatedAt field
        existingUser.setUpdatedAt(new Date());

        return ResponseEntity.ok(userService.save(existingUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!userService.existsById(id)) return ResponseEntity.notFound().build();
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<User> block(@PathVariable UUID id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        user.setActive(false);
        user.setUpdatedAt(new Date());
        return ResponseEntity.ok(userService.save(user));
    }

    @PutMapping("/{id}/deblock")
    public ResponseEntity<User> deblock(@PathVariable UUID id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        user.setActive(true);
        user.setUpdatedAt(new Date());
        return ResponseEntity.ok(userService.save(user));
    }

    @GetMapping("/role/{role}")
    public List<User> getAllByRole(@PathVariable String role) {
        UserType userType = UserType.valueOf(role.toUpperCase());
        return userService.findAllByUserType(userType);
    }

    @GetMapping("/sponsor/{sponsorId}")
    public ResponseEntity<User> getUserBySponsorId(@PathVariable UUID sponsorId) {
        return sponsorService.findById(sponsorId)
            .map(Sponsor -> Sponsor.getUser())
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/by-sponsor-ids")
    public ResponseEntity<java.util.Map<UUID, User>> getUsersBySponsorIds(@RequestBody List<UUID> sponsorIds) {
        java.util.Map<UUID, User> result = sponsorIds.stream()
            .map(id -> sponsorService.findById(id).orElse(null))
            .filter(sponsor -> sponsor != null && sponsor.getUser() != null)
            .collect(Collectors.toMap(
                sponsor -> sponsor.getSponsorId(),
                sponsor -> sponsor.getUser()
            ));
        return ResponseEntity.ok(result);
    }
} 