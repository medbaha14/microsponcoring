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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class StaticUploadController {

    private static final Logger logger = LoggerFactory.getLogger(StaticUploadController.class);

    @Value("${file.static-dir:src/main/resources/static/uploads}")
    private String staticDir;

    @PostMapping("/static")
    public ResponseEntity<?> uploadToStatic(
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename,
            @RequestParam("userId") String userId,
            @RequestParam("imageType") String imageType) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("File must be an image");
            }

            // Validate file size (10MB max)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File too large. Maximum size: 10MB");
            }

            // Ensure static directory exists
            Path staticPath = Paths.get(staticDir);
            if (!Files.exists(staticPath)) {
                Files.createDirectories(staticPath);
                logger.info("Created static directory: {}", staticPath);
            }

            // Save file to static directory
            Path filePath = staticPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("Static file saved: {}", filename);

            // Return the public URL
            String publicUrl = "/uploads/" + filename;
            
            Map<String, String> response = new HashMap<>();
            response.put("url", publicUrl);
            response.put("filename", filename);
            response.put("message", "File uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("Failed to upload static file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during static file upload: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/static/{filename}")
    public ResponseEntity<?> getStaticFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(staticDir).resolve(filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            // Return file info
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("filename", filename);
            fileInfo.put("url", "/uploads/" + filename);
            fileInfo.put("size", Files.size(filePath));
            fileInfo.put("exists", true);

            return ResponseEntity.ok(fileInfo);

        } catch (IOException e) {
            logger.error("Failed to get static file info: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}
