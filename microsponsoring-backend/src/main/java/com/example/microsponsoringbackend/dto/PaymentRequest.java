package com.example.microsponsoringbackend.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class PaymentRequest {
    private UUID sponsorId;
    private UUID companyId;
    private List<UUID> benefitIds;
    private Double amount;
    private String paymentMethod; // PAYPAL, STRIPE, BANK_TRANSFER, etc.
    private String currency;
    private String description;
    private Boolean acceptedTerms;
} 