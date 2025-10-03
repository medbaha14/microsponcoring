package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.PaymentTransaction;
import com.example.microsponsoringbackend.model.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    
    List<PaymentTransaction> findBySponsorOrderByTransactionDateDesc(Sponsor sponsor);
    
    List<PaymentTransaction> findBySponsorAndStatusOrderByTransactionDateDesc(Sponsor sponsor, PaymentTransaction.TransactionStatus status);
    
    List<PaymentTransaction> findBySponsorAndTransactionDateBetweenOrderByTransactionDateDesc(
        Sponsor sponsor, Date startDate, Date endDate);
    
    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.sponsor = :sponsor AND pt.status = :status")
    BigDecimal getTotalAmountBySponsorAndStatus(@Param("sponsor") Sponsor sponsor, @Param("status") PaymentTransaction.TransactionStatus status);
    
    @Query("SELECT COUNT(pt) FROM PaymentTransaction pt WHERE pt.sponsor = :sponsor AND pt.status = :status")
    Long getTransactionCountBySponsorAndStatus(@Param("sponsor") Sponsor sponsor, @Param("status") PaymentTransaction.TransactionStatus status);
    
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.sponsor = :sponsor AND pt.transactionDate >= :startDate ORDER BY pt.transactionDate DESC")
    List<PaymentTransaction> findRecentTransactionsBySponsor(@Param("sponsor") Sponsor sponsor, @Param("startDate") Date startDate);
    
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.sponsor = :sponsor AND pt.uploadedFileName IS NOT NULL ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findUploadedTransactionsBySponsor(@Param("sponsor") Sponsor sponsor);
    
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.sponsor = :sponsor AND pt.description LIKE %:searchTerm% ORDER BY pt.transactionDate DESC")
    List<PaymentTransaction> searchTransactionsByDescription(@Param("sponsor") Sponsor sponsor, @Param("searchTerm") String searchTerm);
}
