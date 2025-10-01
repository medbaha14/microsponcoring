package com.example.microsponsoringbackend.dto;

import lombok.Data;
import com.example.microsponsoringbackend.model.UserType;

@Data
public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private String fullName;
    private UserType userType;

    // Nested DTOs for user-type specific data
    private SponsorDto sponsor;
    private CompanyNonProfitDto companyNonProfits;
}
