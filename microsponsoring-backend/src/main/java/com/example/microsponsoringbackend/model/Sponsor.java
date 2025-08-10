package com.example.microsponsoringbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "sponsors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sponsor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID sponsorId;

    @Column
    private String sponcerCat;

    @Column
    private String paymentMethod;

    @Column
    private Integer totalSponsorships;

    @Column
    private Double totalAmountSpent;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
    @JsonBackReference
    private User user;
}
