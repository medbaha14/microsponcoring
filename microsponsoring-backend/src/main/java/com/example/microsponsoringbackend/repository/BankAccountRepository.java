package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
    List<BankAccount> findByUserId(UUID userId);
} 