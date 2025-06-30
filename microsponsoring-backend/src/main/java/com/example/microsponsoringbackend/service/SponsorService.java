package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.Sponsor;
import com.example.microsponsoringbackend.repository.SponsorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SponsorService {
    @Autowired
    private SponsorRepository repository;

    public List<Sponsor> findAll() {
        return repository.findAll();
    }

    public Optional<Sponsor> findById(UUID id) {
        return repository.findById(id);
    }

    public Sponsor save(Sponsor sponsor) {
        return repository.save(sponsor);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
} 