package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.PageCustomizations;
import com.example.microsponsoringbackend.repository.PageCustomizationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PageCustomizationsService {
    @Autowired
    private PageCustomizationsRepository repository;

    public List<PageCustomizations> findAll() {
        return repository.findAll();
    }

    public Optional<PageCustomizations> findById(UUID id) {
        return repository.findById(id);
    }

    public PageCustomizations save(PageCustomizations pageCustomizations) {
        return repository.save(pageCustomizations);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
} 