package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.PaymentTransaction;
import com.example.microsponsoringbackend.model.Sponsor;
import com.example.microsponsoringbackend.service.PaymentTransactionService;
import com.example.microsponsoringbackend.service.SponsorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sample-data")
public class SampleDataController {
    
    private static final Logger logger = LoggerFactory.getLogger(SampleDataController.class);
    
    @Autowired
    private PaymentTransactionService paymentTransactionService;
    
    @Autowired
    private SponsorService sponsorService;
    
    @PostMapping("/payment-transactions/{sponsorId}")
    public ResponseEntity<String> createSamplePaymentTransactions(@PathVariable UUID sponsorId) {
        try {
            Sponsor sponsor = sponsorService.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));
            
            // Create sample transactions
            createSampleTransaction(sponsor, "Sponsorship Payment", new BigDecimal("500.00"), 
                PaymentTransaction.TransactionStatus.COMPLETED, PaymentTransaction.TransactionType.SPONSORSHIP);
            
            createSampleTransaction(sponsor, "Monthly Donation", new BigDecimal("100.00"), 
                PaymentTransaction.TransactionStatus.COMPLETED, PaymentTransaction.TransactionType.SPONSORSHIP);
            
            createSampleTransaction(sponsor, "Event Sponsorship", new BigDecimal("750.00"), 
                PaymentTransaction.TransactionStatus.PROCESSING, PaymentTransaction.TransactionType.SPONSORSHIP);
            
            createSampleTransaction(sponsor, "Refund - Cancelled Event", new BigDecimal("200.00"), 
                PaymentTransaction.TransactionStatus.COMPLETED, PaymentTransaction.TransactionType.REFUND);
            
            createSampleTransaction(sponsor, "Platform Fee", new BigDecimal("25.00"), 
                PaymentTransaction.TransactionStatus.COMPLETED, PaymentTransaction.TransactionType.FEE);
            
            logger.info("Created sample payment transactions for sponsor: {}", sponsorId);
            return ResponseEntity.ok("Sample payment transactions created successfully");
            
        } catch (Exception e) {
            logger.error("Error creating sample payment transactions for sponsor: {}", sponsorId, e);
            return ResponseEntity.internalServerError().body("Error creating sample data: " + e.getMessage());
        }
    }
    
    private void createSampleTransaction(Sponsor sponsor, String description, BigDecimal amount, 
                                      PaymentTransaction.TransactionStatus status, 
                                      PaymentTransaction.TransactionType type) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setSponsor(sponsor);
        transaction.setAmount(amount);
        transaction.setCurrency("EUR");
        transaction.setPaymentMethod("CREDIT_CARD");
        transaction.setDescription(description);
        transaction.setStatus(status);
        transaction.setType(type);
        transaction.setTransactionDate(new Date());
        transaction.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setBankReference("BANK-" + System.currentTimeMillis());
        
        if (status == PaymentTransaction.TransactionStatus.COMPLETED) {
            transaction.setProcessedDate(new Date());
        }
        
        paymentTransactionService.createTransaction(transaction);
    }
}
