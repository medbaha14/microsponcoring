package com.example.microsponsoringbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutPaymentRequest {
    
    @JsonProperty("source")
    private PaymentSource source;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("customer")
    private Customer customer;
    
    @JsonProperty("metadata")
    private Metadata metadata;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentSource {
        @JsonProperty("type")
        private String type = "card";
        
        @JsonProperty("number")
        private String number;
        
        @JsonProperty("expiry_month")
        private Integer expiryMonth;
        
        @JsonProperty("expiry_year")
        private Integer expiryYear;
        
        @JsonProperty("cvv")
        private String cvv;
        
        @JsonProperty("name")
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Customer {
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("name")
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Metadata {
        @JsonProperty("sponsor_id")
        private String sponsorId;
        
        @JsonProperty("company_id")
        private String companyId;
        
        @JsonProperty("benefit_ids")
        private String benefitIds;
    }
} 