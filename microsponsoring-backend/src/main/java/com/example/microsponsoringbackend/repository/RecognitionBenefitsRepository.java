package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.RecognitionBenefits;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RecognitionBenefitsRepository extends JpaRepository<RecognitionBenefits, UUID> {
    List<RecognitionBenefits> findByCompanyNonProfits_CompanyId(UUID companyId);
} 