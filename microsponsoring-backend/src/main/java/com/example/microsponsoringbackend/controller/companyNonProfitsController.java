package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.companyNonProfits;
import com.example.microsponsoringbackend.service.companyNonProfitsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies-non-profits")
public class companyNonProfitsController {
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
        if (!companyNonProfitsService.existsById(id)) return ResponseEntity.notFound().build();

        // Fetch the existing company
        companyNonProfits existingCompany = companyNonProfitsService.findById(id).orElse(null);
        if (existingCompany == null) return ResponseEntity.notFound().build();

        // Only update non-null fields
        if (company.getActivityType() != null) existingCompany.setActivityType(company.getActivityType());
        if (company.getDetails() != null) existingCompany.setDetails(company.getDetails());
        if (company.getTotalSponsorships() != null) existingCompany.setTotalSponsorships(company.getTotalSponsorships());
        if (company.getUser() != null) existingCompany.setUser(company.getUser());

        // Always update the updatedAt field
        existingCompany.setUpdatedAt(new Date());

        return ResponseEntity.ok(companyNonProfitsService.save(existingCompany));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!companyNonProfitsService.existsById(id)) return ResponseEntity.notFound().build();
        companyNonProfitsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 