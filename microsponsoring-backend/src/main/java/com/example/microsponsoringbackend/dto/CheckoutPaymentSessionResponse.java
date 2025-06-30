package com.example.microsponsoringbackend.dto;

import lombok.Data;

@Data
public class CheckoutPaymentSessionResponse {
    private String id;
    private String session_secret;
    private String status;
    // Add more fields as needed based on the API response
} 