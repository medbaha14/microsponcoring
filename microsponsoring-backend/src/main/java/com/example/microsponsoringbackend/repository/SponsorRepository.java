package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SponsorRepository extends JpaRepository<Sponsor, UUID> {
} 