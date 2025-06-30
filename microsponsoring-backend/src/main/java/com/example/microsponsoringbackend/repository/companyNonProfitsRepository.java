package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.companyNonProfits;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface companyNonProfitsRepository extends JpaRepository<companyNonProfits, UUID> {
    Optional<companyNonProfits> findByUser_UserId(UUID userId);
} 