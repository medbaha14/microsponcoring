package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.dto.CheckoutPaymentRequest;
import com.example.microsponsoringbackend.dto.CheckoutPaymentResponse;
import com.example.microsponsoringbackend.dto.CheckoutPaymentSessionRequest;
import com.example.microsponsoringbackend.dto.CheckoutPaymentSessionResponse;
import com.example.microsponsoringbackend.dto.PaymentRequest;
import com.example.microsponsoringbackend.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.UUID;
import java.util.Map;
import java.util.List;

@Service
public class CheckoutPaymentService {

        @Value("${checkout.secret.key:}")  // no default secret key here
        private String secretKey;
    
        @Value("${checkout.public.key:}")  // no default public key here
        private String publicKey;
    

    private final RestTemplate restTemplate;
    private static final String CHECKOUT_API_URL = "https://api.sandbox.checkout.com";

    @Autowired
    private PaymentService paymentService;

    public CheckoutPaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Process payment using Checkout.com API
     */
    public CheckoutPaymentResponse processPayment(CheckoutPaymentRequest request) {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);
        headers.set("Cko-Request-Id", UUID.randomUUID().toString());

        HttpEntity<CheckoutPaymentRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<CheckoutPaymentResponse> response = restTemplate.exchange(
                CHECKOUT_API_URL + "/payments",
                HttpMethod.POST,
                entity,
                CheckoutPaymentResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get payment details
     */
    public CheckoutPaymentResponse getPaymentDetails(String paymentId) {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<CheckoutPaymentResponse> response = restTemplate.exchange(
                CHECKOUT_API_URL + "/payments/" + paymentId,
                HttpMethod.GET,
                entity,
                CheckoutPaymentResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get payment details: " + e.getMessage(), e);
        }
    }

    public String getPublicKey() {
        return publicKey;
    }

    public CheckoutPaymentSessionResponse createPaymentSession(CheckoutPaymentSessionRequest request) {
        // For testing purposes, create a mock response
        // In production, this would call the actual Checkout.com API
        CheckoutPaymentSessionResponse mockResponse = new CheckoutPaymentSessionResponse();
        mockResponse.setId("mock_session_" + UUID.randomUUID().toString());
        mockResponse.setSession_secret("mock_secret_" + UUID.randomUUID().toString());
        mockResponse.setStatus("created");

        // Create invoice using PaymentService
        try {
            Map<String, Object> metadata = request.getMetadata();
            System.out.println("Received metadata: " + metadata);
            String sponsorIdStr = metadata.get("sponsor_id") != null ? metadata.get("sponsor_id").toString() : null;
            String companyIdStr = metadata.get("company_id") != null ? metadata.get("company_id").toString() : null;
            String benefitIdsStr = metadata.get("benefit_ids") != null ? metadata.get("benefit_ids").toString() : "";
            if (benefitIdsStr.isEmpty()) {
                System.err.println("No benefit_ids provided, skipping invoice creation.");
            } else {
                PaymentRequest paymentRequest = new PaymentRequest();
                if (sponsorIdStr != null) paymentRequest.setSponsorId(UUID.fromString(sponsorIdStr));
                if (companyIdStr != null) paymentRequest.setCompanyId(UUID.fromString(companyIdStr));
                List<UUID> benefitIdList = java.util.Arrays.stream(benefitIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(UUID::fromString)
                    .toList();
                System.out.println("Parsed benefit IDs: " + benefitIdList);
                if (!benefitIdList.isEmpty()) {
                    paymentRequest.setBenefitIds(benefitIdList);
                }
                System.out.println("About to create invoice with:");
                System.out.println("  sponsorId: " + sponsorIdStr);
                System.out.println("  companyId: " + companyIdStr);
                System.out.println("  benefitIds: " + benefitIdList);
                paymentRequest.setAmount(request.getAmount() / 100.0); // Convert pence to pounds
                paymentRequest.setPaymentMethod("CHECKOUT_MOCK");
                paymentRequest.setCurrency(request.getCurrency());
                paymentRequest.setDescription(request.getReference());
                paymentRequest.setAcceptedTerms(true);
                Invoice invoice = paymentService.processPayment(paymentRequest);
                System.out.println("Mock payment: Invoice created with ID: " + invoice.getInvoiceId());
            }
        } catch (Exception e) {
            System.err.println("Failed to create invoice in mock payment: " + e.getMessage());
        }

        System.out.println("Mock payment session created: " + mockResponse.getId());
        return mockResponse;
    }
} 