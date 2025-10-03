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
import com.example.microsponsoringbackend.model.Sponsor;
import com.example.microsponsoringbackend.model.companyNonProfits;
import com.example.microsponsoringbackend.model.PageCustomizations;
import com.example.microsponsoringbackend.service.SponsorService;
import com.example.microsponsoringbackend.service.companyNonProfitsService;
import com.example.microsponsoringbackend.service.PageCustomizationsService;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private companyNonProfitsService companyNonProfitsService;
    @Autowired
    private PageCustomizationsService pageCustomizationsService;
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
        logger.info("=== UserController.create called ===");
        logger.info("Username: {}", user.getUsername());
        logger.info("UserType: {}", user.getUserType());
        logger.info("Password provided: {}", user.getPassword() != null ? "YES" : "NO");
        
        Date currentDate = new Date();
        user.setCreatedAt(currentDate);
        user.setUpdatedAt(currentDate);
        
        // Hash the password before saving
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            logger.info("Password hashed successfully");
        } else {
            logger.warn("No password provided for user creation!");
        }
        
        // Save the user first
        User savedUser = userService.save(user);
        logger.info("User created successfully: {} with type: {}", savedUser.getUsername(), savedUser.getUserType());
        
        // Create related profile based on user type
        try {
            createRelatedProfile(savedUser);
            logger.info("Related profile created successfully for user: {}", savedUser.getUsername());
        } catch (Exception e) {
            logger.error("Error creating related profile for user {}: {}", savedUser.getUsername(), e.getMessage(), e);
            // Don't fail the user creation if profile creation fails
        }
        
        return savedUser;
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

        if (user.getSponsor() != null && user.getUserType() == UserType.SPONSOR) {
        	Sponsor sponsor = existingUser.getSponsor() == null ? new Sponsor() : existingUser.getSponsor();
        	sponsor.setPaymentMethod(user.getSponsor().getPaymentMethod());
        	sponsor.setSponcerCat(user.getSponsor().getSponcerCat());
        	sponsorService.save(sponsor);
            existingUser.setSponsor(sponsor);
        } else if (user.getCompanyNonProfits() != null && user.getUserType() == UserType.ORGANISATION_NONPROFIT) {
        	companyNonProfits companyNonProfits = existingUser.getCompanyNonProfits() == null ? new companyNonProfits() : existingUser.getCompanyNonProfits();
        	companyNonProfits.setActivityType(user.getCompanyNonProfits().getActivityType());
        	companyNonProfitsService.save(companyNonProfits);
            existingUser.setCompanyNonProfits(companyNonProfits);
        }

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
    
    @PostMapping("/{id}/initialize-profiles")
    public ResponseEntity<?> initializeProfiles(@PathVariable UUID id) {
        logger.info("Initialize profiles request for user ID: {}", id);
        
        try {
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            logger.info("Found user: {} with type: {}", user.getUsername(), user.getUserType());
            
            // Check if user already has the required profiles
            if (user.getUserType() == UserType.ORGANISATION_NONPROFIT) {
                // Check if company profile exists
                if (user.getCompanyNonProfits() == null) {
                    logger.info("Creating missing organization profiles for user: {}", user.getUsername());
                    createRelatedProfile(user);
                    return ResponseEntity.ok().body(Map.of("message", "Organization profiles created successfully"));
                } else {
                    return ResponseEntity.ok().body(Map.of("message", "Organization profiles already exist"));
                }
            } else if (user.getUserType() == UserType.SPONSOR) {
                // Check if sponsor profile exists
                if (user.getSponsor() == null) {
                    logger.info("Creating missing sponsor profile for user: {}", user.getUsername());
                    createRelatedProfile(user);
                    return ResponseEntity.ok().body(Map.of("message", "Sponsor profile created successfully"));
                } else {
                    return ResponseEntity.ok().body(Map.of("message", "Sponsor profile already exists"));
                }
            } else {
                return ResponseEntity.ok().body(Map.of("message", "No additional profiles needed for this user type"));
            }
            
        } catch (Exception e) {
            logger.error("Error initializing profiles for user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to initialize profiles: " + e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getUserStats() {
        try {
            List<User> allUsers = userService.findAll();
            
            long totalUsers = allUsers.size();
            long activeUsers = allUsers.stream().filter(User::getActive).count();
            long adminUsers = allUsers.stream().filter(u -> u.getUserType() == UserType.ADMIN).count();
            long sponsorUsers = allUsers.stream().filter(u -> u.getUserType() == UserType.SPONSOR).count();
            long organisationUsers = allUsers.stream().filter(u -> u.getUserType() == UserType.ORGANISATION_NONPROFIT).count();
            
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", totalUsers - activeUsers);
            stats.put("adminUsers", adminUsers);
            stats.put("sponsorUsers", sponsorUsers);
            stats.put("organisationUsers", organisationUsers);
            stats.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting user stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Creates related profiles based on user type after user creation
     * ADMIN: No additional profiles needed
     * SPONSOR: Creates empty Sponsor profile
     * ORGANISATION_NONPROFIT: Creates empty Company and PageCustomizations profiles
     */
    private void createRelatedProfile(User user) {
        Date currentDate = new Date();
        
        if (user.getUserType() == UserType.SPONSOR) {
            // Create empty Sponsor profile
            Sponsor sponsor = new Sponsor();
            sponsor.setSponsorId(UUID.randomUUID());
            sponsor.setUser(user);
            sponsor.setPaymentMethod("CREDIT_CARD"); // Default payment method
            sponsor.setSponcerCat("GENERAL"); // Default category
            sponsor.setTotalAmountSpent(0.0);
            sponsor.setTotalSponsorships(0);
            sponsor.setCreatedAt(currentDate);
            sponsor.setUpdatedAt(currentDate);
            if (user.getSponsor() != null) {
                sponsor.setPaymentMethod(user.getSponsor().getPaymentMethod());
                sponsor.setSponcerCat(user.getSponsor().getSponcerCat());
            }
            
            sponsorService.save(sponsor);
            logger.info("Created Sponsor profile for user: {}", user.getUsername());
            
        } else if (user.getUserType() == UserType.ORGANISATION_NONPROFIT) {
            // Create empty Company profile
            companyNonProfits company = new companyNonProfits();
            company.setCompanyId(UUID.randomUUID());
            company.setUser(user);
            company.setActivityType("GENERAL"); // Default activity type
            company.setDetails(""); // Empty details
            company.setTotalAmountReceived(0.0);
            company.setTotalSponsorships(0);
            company.setCreatedAt(currentDate);
            company.setUpdatedAt(currentDate);
            if (user.getCompanyNonProfits() != null) {
                company.setActivityType(user.getCompanyNonProfits().getActivityType());
            }
            
            companyNonProfitsService.save(company);
            logger.info("Created Company profile for user: {}", user.getUsername());
            
            // Create empty PageCustomizations profile
            PageCustomizations customizations = new PageCustomizations();
            customizations.setId(UUID.randomUUID());
            customizations.setCompany(company);
            customizations.setBackgroundColor("#FFFFFF"); // Default white background
            customizations.setPrimaryColor("#222831"); // Default dark teal
            customizations.setSecondaryColor("#393E46"); // Default medium teal
            customizations.setFontStyle("Arial"); // Default font
            customizations.setLogoUrl(""); // Empty logo URL
            customizations.setBannerImageUrl(""); // Empty banner URL
            customizations.setBackgroundImageUrl(""); // Empty background image URL
            customizations.setCreatedAt(currentDate);
            customizations.setUpdatedAt(currentDate);
            
            pageCustomizationsService.save(customizations);
            logger.info("Created PageCustomizations profile for user: {}", user.getUsername());
            
        } else if (user.getUserType() == UserType.ADMIN) {
            logger.info("Admin user created, no additional profiles needed: {}", user.getUsername());
        } else {
            logger.warn("Unknown user type: {} for user: {}", user.getUserType(), user.getUsername());
        }
    }
}
