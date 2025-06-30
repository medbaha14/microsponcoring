package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.Invoice;
import com.example.microsponsoringbackend.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceService {
    @Autowired
    private InvoiceRepository repository;

    public List<Invoice> findAll() {
        return repository.findAll();
    }

    public Optional<Invoice> findById(UUID id) {
        return repository.findById(id);
    }

    public List<Invoice> findBySponsorId(UUID sponsorId) {
        return repository.findBySponsor_SponsorId(sponsorId);
    }

    public List<Invoice> findByCompanyId(UUID companyId) {
        return repository.findByCompany_CompanyId(companyId);
    }

    public Invoice save(Invoice invoice) {
        return repository.save(invoice);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
} 