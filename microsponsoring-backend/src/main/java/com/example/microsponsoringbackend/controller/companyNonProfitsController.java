package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.companyNonProfits;
import com.example.microsponsoringbackend.service.companyNonProfitsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies-non-profits")
public class companyNonProfitsController {
    private static final Logger logger = LoggerFactory.getLogger(companyNonProfitsController.class);
    
    @Autowired
    private companyNonProfitsService companyNonProfitsService;

    @GetMapping
    public List<companyNonProfits> getAll() {
        return companyNonProfitsService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<companyNonProfits> getById(@PathVariable UUID id) {
        Optional<companyNonProfits> result = companyNonProfitsService.findById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<companyNonProfits> getByUserId(@PathVariable UUID userId) {
        Optional<companyNonProfits> result = companyNonProfitsService.findByUserId(userId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public companyNonProfits create(@RequestBody companyNonProfits company) {
        Date currentDate = new Date();
        company.setCreatedAt(currentDate);
        company.setUpdatedAt(currentDate);
        return companyNonProfitsService.save(company);
    }

    @PutMapping("/{id}")
    public ResponseEntity<companyNonProfits> update(@PathVariable UUID id, @RequestBody companyNonProfits company) {
        logger.info("=== PUT /api/companies-non-profits/{} - UPDATE REQUEST ===", id);
        
        // Log authentication details
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.info("Authenticated user: {}", auth.getName());
            logger.info("User authorities: {}", auth.getAuthorities());
            logger.info("Authentication details: {}", auth.getDetails());
        } else {
            logger.warn("No authentication found in security context!");
        }
        
        // Log incoming data
        logger.info("Incoming company data:");
        logger.info("  - Activity Type: {}", company.getActivityType());
        logger.info("  - Details: {}", company.getDetails());
        logger.info("  - Total Sponsorships: {}", company.getTotalSponsorships());
        logger.info("  - User: {}", company.getUser());
        
        // Check if company exists
        logger.info("Checking if company exists with ID: {}", id);
        if (!companyNonProfitsService.existsById(id)) {
            logger.warn("Company with ID {} not found!", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Company with ID {} exists, proceeding with update", id);

        // Fetch the existing company
        logger.info("Fetching existing company from database...");
        companyNonProfits existingCompany = companyNonProfitsService.findById(id).orElse(null);
        if (existingCompany == null) {
            logger.error("Failed to fetch existing company with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        logger.info("Existing company data:");
        logger.info("  - Activity Type: {}", existingCompany.getActivityType());
        logger.info("  - Details: {}", existingCompany.getDetails());
        logger.info("  - Total Sponsorships: {}", existingCompany.getTotalSponsorships());
        logger.info("  - Created At: {}", existingCompany.getCreatedAt());
        logger.info("  - Updated At: {}", existingCompany.getUpdatedAt());

        // Only update non-null fields
        logger.info("Updating fields...");
        boolean hasChanges = false;
        
        if (company.getActivityType() != null && !company.getActivityType().equals(existingCompany.getActivityType())) {
            logger.info("Updating Activity Type: {} -> {}", existingCompany.getActivityType(), company.getActivityType());
            existingCompany.setActivityType(company.getActivityType());
            hasChanges = true;
        }
        
        if (company.getDetails() != null && !company.getDetails().equals(existingCompany.getDetails())) {
            logger.info("Updating Details: {} -> {}", existingCompany.getDetails(), company.getDetails());
            existingCompany.setDetails(company.getDetails());
            hasChanges = true;
        }
        
        if (company.getTotalSponsorships() != null && !company.getTotalSponsorships().equals(existingCompany.getTotalSponsorships())) {
            logger.info("Updating Total Sponsorships: {} -> {}", existingCompany.getTotalSponsorships(), company.getTotalSponsorships());
            existingCompany.setTotalSponsorships(company.getTotalSponsorships());
            hasChanges = true;
        }
        
        if (company.getUser() != null && company.getUser().getUserId() != null && 
            !company.getUser().getUserId().equals(existingCompany.getUser().getUserId())) {
            logger.info("Updating User: {} -> {}", existingCompany.getUser().getUserId(), company.getUser().getUserId());
            existingCompany.setUser(company.getUser());
            hasChanges = true;
        }

        if (!hasChanges) {
            logger.info("No changes detected, returning existing company");
            return ResponseEntity.ok(existingCompany);
        }

        // Always update the updatedAt field
        Date updateTime = new Date();
        existingCompany.setUpdatedAt(updateTime);
        logger.info("Updated timestamp to: {}", updateTime);

        // Save to database
        logger.info("Saving updated company to database...");
        try {
            companyNonProfits savedCompany = companyNonProfitsService.save(existingCompany);
            logger.info("Company successfully updated in database!");
            logger.info("Final company data:");
            logger.info("  - Activity Type: {}", savedCompany.getActivityType());
            logger.info("  - Details: {}", savedCompany.getDetails());
            logger.info("  - Total Sponsorships: {}", savedCompany.getTotalSponsorships());
            logger.info("  - Updated At: {}", savedCompany.getUpdatedAt());
            logger.info("=== UPDATE COMPLETED SUCCESSFULLY ===");
            return ResponseEntity.ok(savedCompany);
        } catch (Exception e) {
            logger.error("Error saving company to database: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!companyNonProfitsService.existsById(id)) return ResponseEntity.notFound().build();
        companyNonProfitsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 