package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.PageCustomizations;
import com.example.microsponsoringbackend.model.companyNonProfits;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PageCustomizationsRepository extends JpaRepository<PageCustomizations, UUID> {
    Optional<PageCustomizations> findByCompany(companyNonProfits company);
}
