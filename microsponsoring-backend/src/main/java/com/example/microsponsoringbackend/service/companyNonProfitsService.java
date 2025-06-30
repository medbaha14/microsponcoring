package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.companyNonProfits;
import com.example.microsponsoringbackend.repository.companyNonProfitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class companyNonProfitsService {
    @Autowired
    private companyNonProfitsRepository repository;

    public List<companyNonProfits> findAll() {
        return repository.findAll();
    }

    public Optional<companyNonProfits> findById(UUID id) {
        return repository.findById(id);
    }

    public Optional<companyNonProfits> findByUserId(UUID userId) {
        return repository.findByUser_UserId(userId);
    }

    public companyNonProfits save(companyNonProfits company) {
        return repository.save(company);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
} 