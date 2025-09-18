package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.PaymentTransaction;
import com.example.microsponsoringbackend.model.Sponsor;
import com.example.microsponsoringbackend.service.PaymentTransactionService;
import com.example.microsponsoringbackend.service.SponsorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-transactions")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PaymentTransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentTransactionController.class);
    
    @Autowired
    private PaymentTransactionService paymentTransactionService;
    
    @Autowired
    private SponsorService sponsorService;
    
    @GetMapping("/sponsor/{sponsorId}")
    public ResponseEntity<List<PaymentTransaction>> getTransactionsBySponsor(@PathVariable UUID sponsorId) {
        try {
            Sponsor sponsor = sponsorService.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));
            
            List<PaymentTransaction> transactions = paymentTransactionService.getTransactionsBySponsor(sponsor);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error getting transactions for sponsor: {}", sponsorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/sponsor/{sponsorId}/summary")
    public ResponseEntity<Map<String, Object>> getTransactionSummary(@PathVariable UUID sponsorId) {
        try {
            Sponsor sponsor = sponsorService.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));
            
            Map<String, Object> summary = paymentTransactionService.getTransactionSummary(sponsor);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error getting transaction summary for sponsor: {}", sponsorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/sponsor/{sponsorId}/status/{status}")
    public ResponseEntity<List<PaymentTransaction>> getTransactionsByStatus(
            @PathVariable UUID sponsorId, 
            @PathVariable PaymentTransaction.TransactionStatus status) {
        try {
            Sponsor sponsor = sponsorService.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));
            
            List<PaymentTransaction> transactions = paymentTransactionService.getTransactionsBySponsorAndStatus(sponsor, status);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error getting transactions by status for sponsor: {}", sponsorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/sponsor/{sponsorId}/date-range")
    public ResponseEntity<List<PaymentTransaction>> getTransactionsByDateRange(
            @PathVariable UUID sponsorId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            Sponsor sponsor = sponsorService.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));
            
            Date start = new Date(Long.parseLong(startDate));
            Date end = new Date(Long.parseLong(endDate));
            
            List<PaymentTransaction> transactions = paymentTransactionService.getTransactionsByDateRange(sponsor, start, end);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error getting transactions by date range for sponsor: {}", sponsorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<PaymentTransaction> createTransaction(@RequestBody PaymentTransaction transaction) {
        try {
            // Get current user's sponsor profile
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Find sponsor by username (assuming user has a sponsor profile)
            // This would need to be implemented based on your user-sponsor relationship
            Sponsor sponsor = sponsorService.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Sponsor profile not found for user: " + username));
            
            transaction.setSponsor(sponsor);
            transaction.setTransactionDate(new Date());
            
            PaymentTransaction createdTransaction = paymentTransactionService.createTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
        } catch (Exception e) {
            logger.error("Error creating transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{transactionId}")
    public ResponseEntity<PaymentTransaction> updateTransaction(
            @PathVariable UUID transactionId, 
            @RequestBody PaymentTransaction updatedTransaction) {
        try {
            PaymentTransaction transaction = paymentTransactionService.updateTransaction(transactionId, updatedTransaction);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            logger.error("Error updating transaction: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID transactionId) {
        try {
            paymentTransactionService.deleteTransaction(transactionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting transaction: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{transactionId}/upload")
    public ResponseEntity<PaymentTransaction> uploadTransactionFile(
            @PathVariable UUID transactionId,
            @RequestParam("file") MultipartFile file) {
        try {
            PaymentTransaction transaction = paymentTransactionService.uploadTransactionFile(transactionId, file);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            logger.error("Error uploading file for transaction: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{transactionId}/download")
    public ResponseEntity<byte[]> downloadTransactionFile(@PathVariable UUID transactionId) {
        try {
            byte[] fileContent = paymentTransactionService.downloadTransactionFile(transactionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "transaction_file");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
        } catch (Exception e) {
            logger.error("Error downloading file for transaction: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/sponsor/{sponsorId}/search")
    public ResponseEntity<List<PaymentTransaction>> searchTransactions(
            @PathVariable UUID sponsorId,
            @RequestParam String searchTerm) {
        try {
            Sponsor sponsor = sponsorService.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));
            
            List<PaymentTransaction> transactions = paymentTransactionService.searchTransactions(sponsor, searchTerm);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error searching transactions for sponsor: {}", sponsorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/sponsor/{sponsorId}/uploaded")
    public ResponseEntity<List<PaymentTransaction>> getUploadedTransactions(@PathVariable UUID sponsorId) {
        try {
            Sponsor sponsor = sponsorService.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));
            
            List<PaymentTransaction> transactions = paymentTransactionService.getUploadedTransactions(sponsor);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Error getting uploaded transactions for sponsor: {}", sponsorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
