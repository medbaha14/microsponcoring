package com.example.microsponsoringbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recognition_benefits")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecognitionBenefits {

    @Id
    @GeneratedValue
    private UUID benefitId;

    @Column
    private String rewardType;
    @Column
    private Long currency;
    @Column(name = "currency_type")
    private String currencyType;
    @Column
    private String sponsorshipType;
    @Column
    private String showName;
    @Column
    private String showLogo;
    @Column
    private String logoSize;
    @Column
    private String placement;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonBackReference
    private companyNonProfits companyNonProfits;

    @ManyToMany(mappedBy = "benefits")
    @JsonIgnore
    private List<Invoice> invoices;
}
