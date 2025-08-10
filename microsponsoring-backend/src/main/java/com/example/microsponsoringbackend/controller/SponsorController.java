package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.Sponsor;
import com.example.microsponsoringbackend.service.SponsorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/sponsors")
public class SponsorController {
    @Autowired
    private SponsorService sponsorService;

    @GetMapping
    public List<Sponsor> getAll() {
        return sponsorService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sponsor> getById(@PathVariable UUID id) {
        Optional<Sponsor> result = sponsorService.findById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Sponsor> getByUserId(@PathVariable UUID userId) {
        Optional<Sponsor> result = sponsorService.findByUserId(userId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Sponsor create(@RequestBody Sponsor sponsor) {
        Date currentDate = new Date();
        sponsor.setCreatedAt(currentDate);
        sponsor.setUpdatedAt(currentDate);
        return sponsorService.save(sponsor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sponsor> update(@PathVariable UUID id, @RequestBody Sponsor sponsor) {
        if (!sponsorService.existsById(id)) return ResponseEntity.notFound().build();

        // Fetch the existing sponsor
        Sponsor existingSponsor = sponsorService.findById(id).orElse(null);
        if (existingSponsor == null) return ResponseEntity.notFound().build();

        // Only update non-null fields
        if (sponsor.getSponcerCat() != null) existingSponsor.setSponcerCat(sponsor.getSponcerCat());
        if (sponsor.getTotalSponsorships() != null) existingSponsor.setTotalSponsorships(sponsor.getTotalSponsorships());
        if (sponsor.getPaymentMethod() != null) existingSponsor.setPaymentMethod(sponsor.getPaymentMethod());
        if (sponsor.getUser() != null) existingSponsor.setUser(sponsor.getUser());

        // Always update the updatedAt field
        existingSponsor.setUpdatedAt(new Date());

        return ResponseEntity.ok(sponsorService.save(existingSponsor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!sponsorService.existsById(id)) return ResponseEntity.notFound().build();
        sponsorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 