package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.PageCustomizations;
import com.example.microsponsoringbackend.service.PageCustomizationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/page-customizations")
public class PageCustomizationsController {
    @Autowired
    private PageCustomizationsService pageCustomizationsService;

    @GetMapping
    public List<PageCustomizations> getAll() {
        return pageCustomizationsService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageCustomizations> getById(@PathVariable UUID id) {
        Optional<PageCustomizations> result = pageCustomizationsService.findById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public PageCustomizations create(@RequestBody PageCustomizations pageCustomizations) {
        Date currentDate = new Date();
        pageCustomizations.setCreatedAt(currentDate);
        pageCustomizations.setUpdatedAt(currentDate);
        return pageCustomizationsService.save(pageCustomizations);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PageCustomizations> update(@PathVariable UUID id, @RequestBody PageCustomizations pageCustomizations) {
        if (!pageCustomizationsService.existsById(id)) return ResponseEntity.notFound().build();

        // Fetch the existing page customization
        PageCustomizations existingCustomization = pageCustomizationsService.findById(id).orElse(null);
        if (existingCustomization == null) return ResponseEntity.notFound().build();

        // Only update non-null fields
        if (pageCustomizations.getBackgroundColor() != null) existingCustomization.setBackgroundColor(pageCustomizations.getBackgroundColor());
        if (pageCustomizations.getPrimaryColor() != null) existingCustomization.setPrimaryColor(pageCustomizations.getPrimaryColor());
        if (pageCustomizations.getSecondaryColor() != null) existingCustomization.setSecondaryColor(pageCustomizations.getSecondaryColor());
        if (pageCustomizations.getFontStyle() != null) existingCustomization.setFontStyle(pageCustomizations.getFontStyle());
        if (pageCustomizations.getLogoUrl() != null) existingCustomization.setLogoUrl(pageCustomizations.getLogoUrl());
        if (pageCustomizations.getBannerImageUrl() != null) existingCustomization.setBannerImageUrl(pageCustomizations.getBannerImageUrl());
        if (pageCustomizations.getBackgroundImageUrl() != null) existingCustomization.setBackgroundImageUrl(pageCustomizations.getBackgroundImageUrl());
        if (pageCustomizations.getCompany() != null) existingCustomization.setCompany(pageCustomizations.getCompany());

        // Always update the updatedAt field
        existingCustomization.setUpdatedAt(new Date());

        return ResponseEntity.ok(pageCustomizationsService.save(existingCustomization));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!pageCustomizationsService.existsById(id)) return ResponseEntity.notFound().build();
        pageCustomizationsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 