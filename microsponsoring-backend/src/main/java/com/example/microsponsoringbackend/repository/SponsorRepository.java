package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface SponsorRepository extends JpaRepository<Sponsor, UUID> {
    Optional<Sponsor> findByUser_UserId(UUID userId);
} 