package com.example.microsponsoringbackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page_customizations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageCustomizations {

    @Id
    @GeneratedValue
    private UUID id;

    @Column
    private String backgroundColor;

    @Column
    private String primaryColor;

    @Column
    private String secondaryColor;

    @Column
    private String fontStyle;

    @Column(columnDefinition = "VARCHAR(500)")
    private String logoUrl;

    @Column(columnDefinition = "VARCHAR(500)")
    private String bannerImageUrl;

    @Column(columnDefinition = "VARCHAR(500)")
    private String backgroundImageUrl;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;

    @OneToOne
    @JoinColumn(name = "company_id")
    @JsonBackReference
    private companyNonProfits company;
}
