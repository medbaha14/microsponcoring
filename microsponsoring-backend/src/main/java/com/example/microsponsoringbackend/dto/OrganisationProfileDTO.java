package com.example.microsponsoringbackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

import com.example.microsponsoringbackend.model.RecognitionBenefits;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationProfileDTO {
    // User fields
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String profilePicture;
    private String websiteUrl;

    // companyNonProfits fields
    private String activityType;
    private String details;
    private Integer totalSponsorships;
    private UUID companyId;

    // PageCustomizations fields
    private String backgroundColor;
    private String primaryColor;
    private String secondaryColor;
    private String fontStyle;
    private String logoUrl;
    private String bannerImageUrl;
    private String backgroundImageUrl;
    private List<RecognitionBenefits> recognitionBenefits;
} 