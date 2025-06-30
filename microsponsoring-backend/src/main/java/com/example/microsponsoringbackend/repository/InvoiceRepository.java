package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findBySponsor_SponsorId(UUID sponsorId);
    List<Invoice> findByCompany_CompanyId(UUID companyId);
} 