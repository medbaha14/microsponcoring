package com.example.microsponsoringbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutPaymentResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("action_id")
    private String actionId;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("approved")
    private Boolean approved;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("auth_code")
    private String authCode;
    
    @JsonProperty("response_code")
    private String responseCode;
    
    @JsonProperty("response_summary")
    private String responseSummary;
    
    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("customer")
    private Customer customer;
    
    @JsonProperty("source")
    private Source source;
    
    @JsonProperty("metadata")
    private Metadata metadata;
    
    @JsonProperty("created_on")
    private String createdOn;
    
    @JsonProperty("processed_on")
    private String processedOn;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Customer {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("name")
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Source {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("last4")
        private String last4;
        
        @JsonProperty("fingerprint")
        private String fingerprint;
        
        @JsonProperty("bin")
        private String bin;
        
        @JsonProperty("card_type")
        private String cardType;
        
        @JsonProperty("card_category")
        private String cardCategory;
        
        @JsonProperty("issuer")
        private String issuer;
        
        @JsonProperty("issuer_country")
        private String issuerCountry;
        
        @JsonProperty("product_id")
        private String productId;
        
        @JsonProperty("product_type")
        private String productType;
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