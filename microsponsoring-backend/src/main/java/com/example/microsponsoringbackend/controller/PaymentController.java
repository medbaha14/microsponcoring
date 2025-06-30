package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.dto.PaymentRequest;
import com.example.microsponsoringbackend.model.Invoice;
import com.example.microsponsoringbackend.service.PaymentService;
import com.example.microsponsoringbackend.dto.CheckoutPaymentSessionRequest;
import com.example.microsponsoringbackend.dto.CheckoutPaymentSessionResponse;
import com.example.microsponsoringbackend.service.CheckoutPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CheckoutPaymentService checkoutPaymentService;

    /**
     * Process a payment and generate invoice
     */
    @PostMapping("/process")
    public ResponseEntity<Invoice> processPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            Invoice invoice = paymentService.processPayment(paymentRequest);
            return ResponseEntity.ok(invoice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get invoices for a sponsor
     */
    @GetMapping("/sponsor/{sponsorId}/invoices")
    public ResponseEntity<List<Invoice>> getInvoicesBySponsor(@PathVariable UUID sponsorId) {
        try {
            List<Invoice> invoices = paymentService.getInvoicesBySponsor(sponsorId);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get invoices for a company
     */
    @GetMapping("/company/{companyId}/invoices")
    public ResponseEntity<List<Invoice>> getInvoicesByCompany(@PathVariable UUID companyId) {
        try {
            List<Invoice> invoices = paymentService.getInvoicesByCompany(companyId);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get sponsor statistics
     */
    @GetMapping("/sponsor/{sponsorId}/stats")
    public ResponseEntity<PaymentService.SponsorStats> getSponsorStats(@PathVariable UUID sponsorId) {
        try {
            PaymentService.SponsorStats stats = paymentService.getSponsorStats(sponsorId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get company statistics
     */
    @GetMapping("/company/{companyId}/stats")
    public ResponseEntity<PaymentService.CompanyStats> getCompanyStats(@PathVariable UUID companyId) {
        try {
            PaymentService.CompanyStats stats = paymentService.getCompanyStats(companyId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/checkout-session")
    public ResponseEntity<CheckoutPaymentSessionResponse> createCheckoutSession(
            @RequestBody CheckoutPaymentSessionRequest request) {
        CheckoutPaymentSessionResponse response = checkoutPaymentService.createPaymentSession(request);
        return ResponseEntity.ok(response);
    }
} 