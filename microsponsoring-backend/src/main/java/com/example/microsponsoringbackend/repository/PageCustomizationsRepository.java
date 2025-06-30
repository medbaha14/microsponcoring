package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.PageCustomizations;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PageCustomizationsRepository extends JpaRepository<PageCustomizations, UUID> {
} 