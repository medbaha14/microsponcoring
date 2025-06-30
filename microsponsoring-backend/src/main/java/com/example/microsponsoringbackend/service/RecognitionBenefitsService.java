package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.RecognitionBenefits;
import com.example.microsponsoringbackend.repository.RecognitionBenefitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecognitionBenefitsService {
    @Autowired
    private RecognitionBenefitsRepository repository;

    public List<RecognitionBenefits> findAll() {
        return repository.findAll();
    }

    public Optional<RecognitionBenefits> findById(UUID id) {
        return repository.findById(id);
    }

    public List<RecognitionBenefits> findAllByIds(List<UUID> ids) {
        return repository.findAllById(ids);
    }

    public List<RecognitionBenefits> findAllByCompanyId(UUID companyId) {
        return repository.findByCompanyNonProfits_CompanyId(companyId);
    }

    public RecognitionBenefits save(RecognitionBenefits benefit) {
        return repository.save(benefit);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
} 