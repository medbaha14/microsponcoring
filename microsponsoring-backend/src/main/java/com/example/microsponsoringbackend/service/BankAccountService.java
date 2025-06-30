package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.BankAccount;
import com.example.microsponsoringbackend.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankAccountService {
    @Autowired
    private BankAccountRepository bankAccountRepository;

    public List<BankAccount> getByUserId(UUID userId) {
        return bankAccountRepository.findByUserId(userId);
    }

    public Optional<BankAccount> getById(UUID id) {
        return bankAccountRepository.findById(id);
    }

    public BankAccount save(BankAccount bankAccount) {
        return bankAccountRepository.save(bankAccount);
    }

    public void delete(UUID id) {
        bankAccountRepository.deleteById(id);
    }
} 