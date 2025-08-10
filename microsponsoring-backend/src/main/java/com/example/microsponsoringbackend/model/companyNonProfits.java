package com.example.microsponsoringbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "companies_non_profits")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class companyNonProfits {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID companyId;

    @Column
    private String activityType;

    @Column
    private String details;

    @Column
    private Integer totalSponsorships;

    @Column
    private Double totalAmountReceived;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
    @JsonBackReference
    private User user;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private PageCustomizations pageCustomizations;
}
