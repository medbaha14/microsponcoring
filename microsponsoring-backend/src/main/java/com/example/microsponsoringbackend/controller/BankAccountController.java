package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.BankAccount;
import com.example.microsponsoringbackend.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/bank-accounts")
public class BankAccountController {
    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping("/user/{userId}")
    public List<BankAccount> getByUserId(@PathVariable UUID userId) {
        return bankAccountService.getByUserId(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getById(@PathVariable UUID id) {
        Optional<BankAccount> result = bankAccountService.getById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public BankAccount create(@RequestBody BankAccount bankAccount) {
        return bankAccountService.save(bankAccount);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BankAccount> update(@PathVariable UUID id, @RequestBody BankAccount bankAccount) {
        Optional<BankAccount> existing = bankAccountService.getById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        bankAccount.setId(id);
        return ResponseEntity.ok(bankAccountService.save(bankAccount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bankAccountService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 