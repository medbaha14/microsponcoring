package com.example.microsponsoringbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CheckoutPaymentSessionRequest {
    private Long amount;
    private String currency;
    private String reference;
    private String processingChannelId;
    private AddressContainer shipping;
    private AddressContainer billing;
    @JsonProperty("three_ds")
    private ThreeDS threeDs;
    @JsonProperty("enabled_payment_methods")
    private List<String> enabledPaymentMethods;
    @JsonProperty("success_url")
    private String successUrl;
    @JsonProperty("failure_url")
    private String failureUrl;
    private Map<String, Object> metadata;

    @Data
    public static class AddressContainer {
        private Address address;
    }

    @Data
    public static class Address {
        private String address_line1;
        private String address_line2;
        private String city;
        private String state;
        private String zip;
        private String country;
    }

    @Data
    public static class ThreeDS {
        private boolean enabled;
        @JsonProperty("attempt_n3d")
        private boolean attemptN3d;
        @JsonProperty("challenge_indicator")
        private String challengeIndicator;
        private String exemption;
        @JsonProperty("allow_upgrade")
        private boolean allowUpgrade;
    }
} 