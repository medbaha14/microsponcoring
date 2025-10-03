package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.PaymentTransaction;
import com.example.microsponsoringbackend.model.Sponsor;
import com.example.microsponsoringbackend.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class PaymentTransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentTransactionService.class);
    private static final String UPLOAD_DIR = "src/main/resources/transactions/";
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    public List<PaymentTransaction> getTransactionsBySponsor(Sponsor sponsor) {
        return paymentTransactionRepository.findBySponsorOrderByTransactionDateDesc(sponsor);
    }
    
    public List<PaymentTransaction> getTransactionsBySponsorAndStatus(Sponsor sponsor, PaymentTransaction.TransactionStatus status) {
        return paymentTransactionRepository.findBySponsorAndStatusOrderByTransactionDateDesc(sponsor, status);
    }
    
    public List<PaymentTransaction> getTransactionsByDateRange(Sponsor sponsor, Date startDate, Date endDate) {
        return paymentTransactionRepository.findBySponsorAndTransactionDateBetweenOrderByTransactionDateDesc(sponsor, startDate, endDate);
    }
    
    public PaymentTransaction createTransaction(PaymentTransaction transaction) {
        logger.info("Creating payment transaction for sponsor: {}", transaction.getSponsor().getSponsorId());
        return paymentTransactionRepository.save(transaction);
    }
    
    public PaymentTransaction updateTransaction(UUID transactionId, PaymentTransaction updatedTransaction) {
        Optional<PaymentTransaction> existingTransaction = paymentTransactionRepository.findById(transactionId);
        if (existingTransaction.isPresent()) {
            PaymentTransaction transaction = existingTransaction.get();
            transaction.setAmount(updatedTransaction.getAmount());
            transaction.setDescription(updatedTransaction.getDescription());
            transaction.setStatus(updatedTransaction.getStatus());
            transaction.setNotes(updatedTransaction.getNotes());
            return paymentTransactionRepository.save(transaction);
        }
        throw new RuntimeException("Transaction not found with id: " + transactionId);
    }
    
    public void deleteTransaction(UUID transactionId) {
        paymentTransactionRepository.deleteById(transactionId);
    }
    
    public Map<String, Object> getTransactionSummary(Sponsor sponsor) {
        Map<String, Object> summary = new HashMap<>();
        
        BigDecimal totalAmount = paymentTransactionRepository.getTotalAmountBySponsorAndStatus(sponsor, PaymentTransaction.TransactionStatus.COMPLETED);
        Long totalCount = paymentTransactionRepository.getTransactionCountBySponsorAndStatus(sponsor, PaymentTransaction.TransactionStatus.COMPLETED);
        
        BigDecimal pendingAmount = paymentTransactionRepository.getTotalAmountBySponsorAndStatus(sponsor, PaymentTransaction.TransactionStatus.PENDING);
        Long pendingCount = paymentTransactionRepository.getTransactionCountBySponsorAndStatus(sponsor, PaymentTransaction.TransactionStatus.PENDING);
        
        summary.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        summary.put("totalCount", totalCount != null ? totalCount : 0);
        summary.put("pendingAmount", pendingAmount != null ? pendingAmount : BigDecimal.ZERO);
        summary.put("pendingCount", pendingCount != null ? pendingCount : 0);
        
        // Get recent transactions (last 30 days)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        List<PaymentTransaction> recentTransactions = paymentTransactionRepository.findRecentTransactionsBySponsor(sponsor, cal.getTime());
        summary.put("recentTransactions", recentTransactions);
        
        return summary;
    }
    
    public PaymentTransaction uploadTransactionFile(UUID transactionId, MultipartFile file) throws IOException {
        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transaction not found with id: " + transactionId);
        }
        
        PaymentTransaction transaction = transactionOpt.get();
        
        // Create upload directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFilename);
        Files.copy(file.getInputStream(), filePath);
        
        // Update transaction with file info
        transaction.setUploadedFileName(originalFilename);
        transaction.setUploadedFilePath(filePath.toString());
        transaction.setUploadedFileType(file.getContentType());
        transaction.setUploadedFileSize(file.getSize());
        
        logger.info("Uploaded transaction file: {} for transaction: {}", originalFilename, transactionId);
        
        return paymentTransactionRepository.save(transaction);
    }
    
    public List<PaymentTransaction> searchTransactions(Sponsor sponsor, String searchTerm) {
        return paymentTransactionRepository.searchTransactionsByDescription(sponsor, searchTerm);
    }
    
    public List<PaymentTransaction> getUploadedTransactions(Sponsor sponsor) {
        return paymentTransactionRepository.findUploadedTransactionsBySponsor(sponsor);
    }
    
    public byte[] downloadTransactionFile(UUID transactionId) throws IOException {
        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty() || transactionOpt.get().getUploadedFilePath() == null) {
            throw new RuntimeException("Transaction file not found");
        }
        
        PaymentTransaction transaction = transactionOpt.get();
        Path filePath = Paths.get(transaction.getUploadedFilePath());
        return Files.readAllBytes(filePath);
    }
}
