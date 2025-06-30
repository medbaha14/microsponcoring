package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.RecognitionBenefits;
import com.example.microsponsoringbackend.service.RecognitionBenefitsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/recognition-benefits")
public class RecognitionBenefitsController {
    private static final Logger logger = LoggerFactory.getLogger(RecognitionBenefitsController.class);
    
    @Autowired
    private RecognitionBenefitsService recognitionBenefitsService;

    @GetMapping
    public List<RecognitionBenefits> getAll() {
        return recognitionBenefitsService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecognitionBenefits> getById(@PathVariable UUID id) {
        Optional<RecognitionBenefits> result = recognitionBenefitsService.findById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/company/{companyId}")
    public List<RecognitionBenefits> getAllByCompanyId(@PathVariable UUID companyId) {
        logger.info("Received request for benefits by company ID: {}", companyId);
        List<RecognitionBenefits> benefits = recognitionBenefitsService.findAllByCompanyId(companyId);
        logger.info("Found {} benefits for company ID: {}", benefits.size(), companyId);
        return benefits;
    }

    @PostMapping
    public RecognitionBenefits create(@RequestBody RecognitionBenefits benefit) {
        Date currentDate = new Date();
        benefit.setCreatedAt(currentDate);
        benefit.setUpdatedAt(currentDate);
        return recognitionBenefitsService.save(benefit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecognitionBenefits> update(@PathVariable UUID id, @RequestBody RecognitionBenefits benefit) {
        if (!recognitionBenefitsService.existsById(id)) return ResponseEntity.notFound().build();

        // Fetch the existing benefit
        RecognitionBenefits existingBenefit = recognitionBenefitsService.findById(id).orElse(null);
        if (existingBenefit == null) return ResponseEntity.notFound().build();

        // Only update non-null fields
        if (benefit.getRewardType() != null) existingBenefit.setRewardType(benefit.getRewardType());
        if (benefit.getCurrency() != null) existingBenefit.setCurrency(benefit.getCurrency());
        if (benefit.getSponsorshipType() != null) existingBenefit.setSponsorshipType(benefit.getSponsorshipType());
        if (benefit.getShowName() != null) existingBenefit.setShowName(benefit.getShowName());
        if (benefit.getShowLogo() != null) existingBenefit.setShowLogo(benefit.getShowLogo());
        if (benefit.getLogoSize() != null) existingBenefit.setLogoSize(benefit.getLogoSize());
        if (benefit.getPlacement() != null) existingBenefit.setPlacement(benefit.getPlacement());

        // Always update the updatedAt field
        existingBenefit.setUpdatedAt(new Date());

        return ResponseEntity.ok(recognitionBenefitsService.save(existingBenefit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!recognitionBenefitsService.existsById(id)) return ResponseEntity.notFound().build();
        recognitionBenefitsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 