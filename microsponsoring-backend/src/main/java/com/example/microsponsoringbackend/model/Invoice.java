package com.example.microsponsoringbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue
    private UUID invoiceId;

    @Column
    private Double amount;

    @Column
    private String status;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private companyNonProfits company;

    @ManyToOne
    @JoinColumn(name = "sponsor_id")
    private Sponsor sponsor;

    @Column
    private Date invoiceDate;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    @Column
    private Boolean acceptedTerms;
    
    @Column
    private String generatedPdfUrl;
    
    @ManyToMany
    @JoinTable(
        name = "InvoiceBenefits",
        joinColumns = @JoinColumn(name = "invoiceId", columnDefinition = "BINARY(16)"),
        inverseJoinColumns = @JoinColumn(name = "benefitId", columnDefinition = "BINARY(16)")
    )
    @JsonBackReference
    private List<RecognitionBenefits> benefits;
}
