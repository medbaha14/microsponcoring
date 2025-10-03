package com.example.microsponsoringbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID transactionId;
    
    @ManyToOne
    @JoinColumn(name = "sponsor_id")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
    @JsonBackReference
    private Sponsor sponsor;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
    private companyNonProfits company;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String currency = "EUR";
    
    @Column(nullable = false)
    private String paymentMethod;
    
    @Column
    private String transactionReference;
    
    @Column
    private String bankReference;
    
    @Column
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type = TransactionType.SPONSORSHIP;
    
    @Column
    private Date transactionDate;
    
    @Column
    private Date processedDate;
    
    @Column
    private String uploadedFileName;
    
    @Column
    private String uploadedFilePath;
    
    @Column
    private String uploadedFileType;
    
    @Column
    private Long uploadedFileSize;
    
    @Column
    private String notes;
    
    @Column
    private Date createdAt;
    
    @Column
    private Date updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (transactionDate == null) {
            transactionDate = new Date();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
    
    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED
    }
    
    public enum TransactionType {
        SPONSORSHIP,
        REFUND,
        ADJUSTMENT,
        FEE,
        OTHER
    }
}
