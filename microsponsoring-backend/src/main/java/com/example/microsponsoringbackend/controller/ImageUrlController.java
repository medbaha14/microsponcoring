package com.example.microsponsoringbackend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.model.PageCustomizations;
import com.example.microsponsoringbackend.repository.UserRepository;
import com.example.microsponsoringbackend.repository.PageCustomizationsRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/image-url")
public class ImageUrlController {

    private static final Logger logger = LoggerFactory.getLogger(ImageUrlController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PageCustomizationsRepository pageCustomizationsRepository;

    @PostMapping("/profile-picture")
    public ResponseEntity<?> updateProfilePicture(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String imageUrl = request.get("imageUrl");

            if (userId == null || imageUrl == null) {
                return ResponseEntity.badRequest().body("userId and imageUrl are required");
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userOpt.get();
            user.setProfilePicture(imageUrl);
            userRepository.save(user);

            logger.info("Profile picture URL updated for user {}: {}", userId, imageUrl);

            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("imageUrl", imageUrl);
            response.put("message", "Profile picture updated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to update profile picture: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to update profile picture: " + e.getMessage());
        }
    }

    @PostMapping("/organisation-logo")
    public ResponseEntity<?> updateOrganisationLogo(@RequestBody Map<String, String> request) {
        return updatePageCustomizationImage(request, "logo");
    }

    @PostMapping("/organisation-banner")
    public ResponseEntity<?> updateOrganisationBanner(@RequestBody Map<String, String> request) {
        return updatePageCustomizationImage(request, "banner");
    }

    @PostMapping("/organisation-background")
    public ResponseEntity<?> updateOrganisationBackground(@RequestBody Map<String, String> request) {
        return updatePageCustomizationImage(request, "background");
    }

    private ResponseEntity<?> updatePageCustomizationImage(Map<String, String> request, String imageType) {
        try {
            String userId = request.get("userId");
            String imageUrl = request.get("imageUrl");

            if (userId == null || imageUrl == null) {
                return ResponseEntity.badRequest().body("userId and imageUrl are required");
            }

            // Find or create PageCustomizations for the user
            PageCustomizations customizations = pageCustomizationsRepository.findByUserId(userId)
                    .orElse(new PageCustomizations());
            
            if (customizations.getUserId() == null) {
                customizations.setUserId(userId);
                customizations.setCreatedAt(new Date());
            }

            // Update the appropriate field based on image type
            switch (imageType) {
                case "logo":
                    customizations.setLogoUrl(imageUrl);
                    break;
                case "banner":
                    customizations.setBannerImageUrl(imageUrl);
                    break;
                case "background":
                    customizations.setBackgroundImageUrl(imageUrl);
                    break;
                default:
                    return ResponseEntity.badRequest().body("Invalid image type: " + imageType);
            }

            customizations.setUpdatedAt(new Date());
            pageCustomizationsRepository.save(customizations);

            logger.info("{} image URL updated for user {}: {}", imageType, userId, imageUrl);

            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("imageUrl", imageUrl);
            response.put("message", imageType + " image updated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to update {} image: {}", imageType, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to update " + imageType + " image: " + e.getMessage());
        }
    }
}
