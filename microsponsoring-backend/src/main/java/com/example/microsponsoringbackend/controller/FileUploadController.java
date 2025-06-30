package com.example.microsponsoringbackend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.Optional;

import com.example.microsponsoringbackend.repository.UserRepository;
import com.example.microsponsoringbackend.repository.PageCustomizationsRepository;
import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.model.UserType;
import com.example.microsponsoringbackend.model.companyNonProfits;
import com.example.microsponsoringbackend.model.PageCustomizations;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final PageCustomizationsRepository pageCustomizationsRepository;

    public FileUploadController(UserRepository userRepository, PageCustomizationsRepository pageCustomizationsRepository) {
        this.userRepository = userRepository;
        this.pageCustomizationsRepository = pageCustomizationsRepository;
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("userId") UUID userId) {
        logger.info("Profile picture upload request received for userId: {}", userId);
        
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty for userId: {}", userId);
                return ResponseEntity.badRequest().body("File is required");
            }

            // Fetch user
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                logger.error("User not found for userId: {}", userId);
                return ResponseEntity.badRequest().body("User not found");
            }
            User user = userOpt.get();
            logger.info("User found: {} (type: {})", user.getUsername(), user.getUserType());

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }

            // Delete old profile picture if it exists
            String oldProfilePictureUrl = user.getProfilePicture();
            if (oldProfilePictureUrl != null && !oldProfilePictureUrl.isEmpty()) {
                // Extract filename from URL
                String oldFilename = oldProfilePictureUrl.substring(oldProfilePictureUrl.lastIndexOf("/") + 1);
                Path oldFilePath = uploadPath.resolve(oldFilename);
                try {
                    Files.deleteIfExists(oldFilePath);
                    logger.info("Deleted old profile picture: {}", oldFilename);
                } catch (IOException e) {
                    logger.warn("Failed to delete old profile picture: {}", e.getMessage());
                }
            }

            // Extract file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                logger.error("Invalid filename: {}", originalFilename);
                return ResponseEntity.badRequest().body("Invalid file format");
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // Build new filename: userId-profile-timestamp.extension
            long timestamp = System.currentTimeMillis();
            String newFilename = userId + "-profile-" + timestamp + extension;

            // Save file (overwrite if exists)
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("Profile picture saved: {}", newFilename);

            // Build file URL
            String fileUrl = "/api/images/" + newFilename;

            // Update user profilePicture in DB
            user.setProfilePicture(fileUrl);
            userRepository.save(user);
            logger.info("Profile picture URL updated in database: {}", fileUrl);

            return ResponseEntity.ok(fileUrl);

        } catch (IOException e) {
            logger.error("Failed to upload profile picture for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during profile picture upload for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/organisation-logo")
    public ResponseEntity<?> uploadOrganisationLogo(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("userId") UUID userId) {
        logger.info("Organisation logo upload request received for userId: {}", userId);
        return uploadOrganisationImage(file, userId, "logo");
    }

    @PostMapping("/organisation-banner")
    public ResponseEntity<?> uploadOrganisationBanner(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("userId") UUID userId) {
        logger.info("Organisation banner upload request received for userId: {}", userId);
        return uploadOrganisationImage(file, userId, "banner");
    }

    @PostMapping("/organisation-background")
    public ResponseEntity<?> uploadOrganisationBackground(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("userId") UUID userId) {
        logger.info("Organisation background upload request received for userId: {}", userId);
        return uploadOrganisationImage(file, userId, "background");
    }

    private ResponseEntity<?> uploadOrganisationImage(MultipartFile file, UUID userId, String imageType) {
        logger.info("Processing organisation image upload - type: {}, userId: {}", imageType, userId);
        
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty for userId: {} and imageType: {}", userId, imageType);
                return ResponseEntity.badRequest().body("File is required");
            }

            // Fetch user
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                logger.error("User not found for userId: {}", userId);
                return ResponseEntity.badRequest().body("User not found");
            }
            User user = userOpt.get();
            logger.info("User found: {} (type: {})", user.getUsername(), user.getUserType());

            // Check if user is an organisation
            if (user.getUserType() != UserType.ORGANISATION_NONPROFIT) {
                logger.error("User {} is not an organisation (type: {})", user.getUsername(), user.getUserType());
                return ResponseEntity.badRequest().body("User is not an organisation. User type: " + user.getUserType());
            }

            // Get company and customizations
            companyNonProfits company = user.getCompanyNonProfits();
            if (company == null) {
                logger.error("Company profile not found for user: {}", user.getUsername());
                return ResponseEntity.badRequest().body("Company profile not found. Please complete your organisation profile first.");
            }
            logger.info("Company profile found for user: {}", user.getUsername());

            PageCustomizations customizations = company.getPageCustomizations();
            if (customizations == null) {
                logger.info("Page customizations not found for user: {}. Creating new page customizations.", user.getUsername());
                // Create new page customizations
                customizations = new PageCustomizations();
                customizations.setCompany(company);
                customizations.setCreatedAt(new Date());
                customizations.setUpdatedAt(new Date());
                
                // Set default values
                customizations.setBackgroundColor("#ffffff");
                customizations.setPrimaryColor("#007bff");
                customizations.setSecondaryColor("#6c757d");
                customizations.setFontStyle("Arial, sans-serif");
                
                // Save the new customizations
                customizations = pageCustomizationsRepository.save(customizations);
                logger.info("Created new page customizations for user: {}", user.getUsername());
            } else {
                logger.info("Page customizations found for user: {}", user.getUsername());
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }

            // Delete old image if it exists
            String oldImageUrl = null;
            switch (imageType) {
                case "logo":
                    oldImageUrl = customizations.getLogoUrl();
                    break;
                case "banner":
                    oldImageUrl = customizations.getBannerImageUrl();
                    break;
                case "background":
                    oldImageUrl = customizations.getBackgroundImageUrl();
                    break;
            }

            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                // Extract filename from URL
                String oldFilename = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
                Path oldFilePath = uploadPath.resolve(oldFilename);
                try {
                    Files.deleteIfExists(oldFilePath);
                    logger.info("Deleted old {} image: {}", imageType, oldFilename);
                } catch (IOException e) {
                    logger.warn("Failed to delete old {} image: {}", imageType, e.getMessage());
                }
            }

            // Extract file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                logger.error("Invalid filename: {}", originalFilename);
                return ResponseEntity.badRequest().body("Invalid file format");
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // Build new filename: userId-imageType-timestamp.extension
            long timestamp = System.currentTimeMillis();
            String newFilename = userId + "-" + imageType + "-" + timestamp + extension;

            // Save file (overwrite if exists)
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("{} image saved: {}", imageType, newFilename);

            // Build file URL
            String fileUrl = "/api/images/" + newFilename;

            // Update the appropriate field in PageCustomizations
            switch (imageType) {
                case "logo":
                    customizations.setLogoUrl(fileUrl);
                    break;
                case "banner":
                    customizations.setBannerImageUrl(fileUrl);
                    break;
                case "background":
                    customizations.setBackgroundImageUrl(fileUrl);
                    break;
            }

            // Update the updatedAt timestamp
            customizations.setUpdatedAt(new Date());

            // Save the updated customizations
            pageCustomizationsRepository.save(customizations);
            logger.info("{} image URL updated in database: {}", imageType, fileUrl);

            return ResponseEntity.ok(fileUrl);

        } catch (IOException e) {
            logger.error("Failed to upload {} image for userId {}: {}", imageType, userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during {} image upload for userId {}: {}", imageType, userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Unexpected error: " + e.getMessage());
        }
    }
} 