package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.*;
import com.example.microsponsoringbackend.repository.*;
import com.example.microsponsoringbackend.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private SponsorService sponsorService;
    
    @Autowired
    private companyNonProfitsService companyService;
    
    @Autowired
    private RecognitionBenefitsService benefitsService;
    
    @Autowired
    private BankAccountService bankAccountService;

    /**
     * Process a payment and automatically generate an invoice
     */
    @Transactional
    public Invoice processPayment(PaymentRequest paymentRequest) {
        // Validate the payment request
        validatePaymentRequest(paymentRequest);
        
        // Get sponsor and company
        Sponsor sponsor = getSponsorById(paymentRequest.getSponsorId());
        companyNonProfits company = getCompanyById(paymentRequest.getCompanyId());
        
        // Get the benefits being purchased
        List<RecognitionBenefits> benefits = getBenefitsByIds(paymentRequest.getBenefitIds());
        
        // Calculate total amount
        Double totalAmount = calculateTotalAmount(benefits);
        
        // Validate amount matches
        if (!totalAmount.equals(paymentRequest.getAmount())) {
            throw new IllegalArgumentException("Payment amount doesn't match benefits total");
        }
        
        // Create invoice
        Invoice invoice = createInvoice(sponsor, company, benefits, totalAmount, paymentRequest);
        
        // Update sponsor's total sponsorships and amount spent
        updateSponsorStats(sponsor, totalAmount);
        
        // Update company's total sponsorships and amount received
        updateCompanyStats(company, totalAmount);
        
        return invoice;
    }

    /**
     * Create invoice from payment
     */
    private Invoice createInvoice(Sponsor sponsor, companyNonProfits company, 
                                List<RecognitionBenefits> benefits, Double amount, 
                                PaymentRequest paymentRequest) {
        Invoice invoice = new Invoice();
        invoice.setAmount(amount);
        invoice.setStatus("PAID");
        invoice.setCompany(company);
        invoice.setSponsor(sponsor);
        invoice.setBenefits(benefits);
        invoice.setInvoiceDate(new Date());
        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice.setAcceptedTerms(true);
        invoice.setCreatedAt(new Date());
        invoice.setUpdatedAt(new Date());
        invoice = invoiceService.save(invoice);

        // Generate PDF
        try {
            String pdfDir = "src/main/resources/invoices/";
            String pdfFile = invoice.getInvoiceId() + ".pdf";
            String pdfPath = pdfDir + pdfFile;
            com.example.microsponsoringbackend.util.InvoicePdfGenerator.generateInvoicePdf(invoice, pdfPath);
            invoice.setGeneratedPdfUrl("/invoices/" + pdfFile);
            invoice = invoiceService.save(invoice); // update with PDF URL
        } catch (Exception e) {
            System.err.println("Failed to generate invoice PDF: " + e.getMessage());
        }

        return invoice;
    }

    /**
     * Validate payment request
     */
    private void validatePaymentRequest(PaymentRequest paymentRequest) {
        if (paymentRequest.getSponsorId() == null) {
            throw new IllegalArgumentException("Sponsor ID is required");
        }
        if (paymentRequest.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required");
        }
        if (paymentRequest.getAmount() == null || paymentRequest.getAmount() <= 0) {
            throw new IllegalArgumentException("Valid payment amount is required");
        }
        if (paymentRequest.getBenefitIds() == null || paymentRequest.getBenefitIds().isEmpty()) {
            throw new IllegalArgumentException("At least one benefit must be selected");
        }
    }

    /**
     * Get sponsor by ID
     */
    private Sponsor getSponsorById(UUID sponsorId) {
        Optional<Sponsor> sponsorOpt = sponsorService.findById(sponsorId);
        if (sponsorOpt.isEmpty()) {
            throw new IllegalArgumentException("Sponsor not found");
        }
        return sponsorOpt.get();
    }

    /**
     * Get company by ID
     */
    private companyNonProfits getCompanyById(UUID companyId) {
        Optional<companyNonProfits> companyOpt = companyService.findById(companyId);
        if (companyOpt.isPresent()) {
            return companyOpt.get();
        } else {
            // Fallback: try to find by userId
            Optional<companyNonProfits> companyByUser = companyService.findByUserId(companyId);
            if (companyByUser.isPresent()) {
                System.out.println("Found company by userId fallback: " + companyByUser.get().getCompanyId());
                return companyByUser.get();
            }
            throw new IllegalArgumentException("Company not found");
        }
    }

    /**
     * Get benefits by IDs
     */
    private List<RecognitionBenefits> getBenefitsByIds(List<UUID> benefitIds) {
        List<RecognitionBenefits> benefits = benefitsService.findAllByIds(benefitIds);
        if (benefits.size() != benefitIds.size()) {
            throw new IllegalArgumentException("Some benefits not found");
        }
        return benefits;
    }

    /**
     * Calculate total amount from benefits
     */
    private Double calculateTotalAmount(List<RecognitionBenefits> benefits) {
        return benefits.stream()
                .mapToDouble(benefit -> benefit.getCurrency() != null ? benefit.getCurrency().doubleValue() : 0.0)
                .sum();
    }

    /**
     * Update sponsor statistics
     */
    private void updateSponsorStats(Sponsor sponsor, Double amount) {
        // Update sponsorship count
        Integer currentTotal = sponsor.getTotalSponsorships() != null ? sponsor.getTotalSponsorships() : 0;
        sponsor.setTotalSponsorships(currentTotal + 1);
        
        // Update total amount spent
        Double currentAmount = sponsor.getTotalAmountSpent() != null ? sponsor.getTotalAmountSpent() : 0.0;
        sponsor.setTotalAmountSpent(currentAmount + amount);
        sponsor.setUpdatedAt(new Date());
        sponsorService.save(sponsor);
    }

    /**
     * Update company statistics
     */
    private void updateCompanyStats(companyNonProfits company, Double amount) {
        // Update sponsorship count
        Integer currentTotal = company.getTotalSponsorships() != null ? company.getTotalSponsorships() : 0;
        company.setTotalSponsorships(currentTotal + 1);
        
        // Update total amount received
        Double currentAmount = company.getTotalAmountReceived() != null ? company.getTotalAmountReceived() : 0.0;
        company.setTotalAmountReceived(currentAmount + amount);
        company.setUpdatedAt(new Date());
        companyService.save(company);
    }

    /**
     * Get invoices for a sponsor
     */
    public List<Invoice> getInvoicesBySponsor(UUID sponsorId) {
        return invoiceService.findBySponsorId(sponsorId);
    }

    /**
     * Get invoices for a company
     */
    public List<Invoice> getInvoicesByCompany(UUID companyId) {
        return invoiceService.findByCompanyId(companyId);
    }

    /**
     * Get sponsor statistics
     */
    public SponsorStats getSponsorStats(UUID sponsorId) {
        Sponsor sponsor = getSponsorById(sponsorId);
        List<Invoice> invoices = getInvoicesBySponsor(sponsorId);
        
        Double totalSpent = invoices.stream()
                .mapToDouble(invoice -> invoice.getAmount() != null ? invoice.getAmount() : 0.0)
                .sum();
        
        return new SponsorStats(
            sponsor.getTotalSponsorships() != null ? sponsor.getTotalSponsorships() : 0,
            sponsor.getTotalAmountSpent() != null ? sponsor.getTotalAmountSpent() : 0.0,
            invoices.size(),
            totalSpent
        );
    }

    /**
     * Get company statistics
     */
    public CompanyStats getCompanyStats(UUID companyId) {
        companyNonProfits company = getCompanyById(companyId);
        List<Invoice> invoices = getInvoicesByCompany(companyId);
        
        Double totalReceived = invoices.stream()
                .mapToDouble(invoice -> invoice.getAmount() != null ? invoice.getAmount() : 0.0)
                .sum();
        
        return new CompanyStats(
            company.getTotalSponsorships() != null ? company.getTotalSponsorships() : 0,
            company.getTotalAmountReceived() != null ? company.getTotalAmountReceived() : 0.0,
            invoices.size(),
            totalReceived
        );
    }

    /**
     * Sponsor statistics DTO
     */
    public static class SponsorStats {
        private final Integer totalSponsorships;
        private final Double totalAmountSpent;
        private final Integer totalInvoices;
        private final Double totalSpentFromInvoices;

        public SponsorStats(Integer totalSponsorships, Double totalAmountSpent, 
                          Integer totalInvoices, Double totalSpentFromInvoices) {
            this.totalSponsorships = totalSponsorships;
            this.totalAmountSpent = totalAmountSpent;
            this.totalInvoices = totalInvoices;
            this.totalSpentFromInvoices = totalSpentFromInvoices;
        }

        // Getters
        public Integer getTotalSponsorships() { return totalSponsorships; }
        public Double getTotalAmountSpent() { return totalAmountSpent; }
        public Integer getTotalInvoices() { return totalInvoices; }
        public Double getTotalSpentFromInvoices() { return totalSpentFromInvoices; }
    }

    /**
     * Company statistics DTO
     */
    public static class CompanyStats {
        private final Integer totalSponsorships;
        private final Double totalAmountReceived;
        private final Integer totalInvoices;
        private final Double totalReceivedFromInvoices;

        public CompanyStats(Integer totalSponsorships, Double totalAmountReceived, 
                          Integer totalInvoices, Double totalReceivedFromInvoices) {
            this.totalSponsorships = totalSponsorships;
            this.totalAmountReceived = totalAmountReceived;
            this.totalInvoices = totalInvoices;
            this.totalReceivedFromInvoices = totalReceivedFromInvoices;
        }

        // Getters
        public Integer getTotalSponsorships() { return totalSponsorships; }
        public Double getTotalAmountReceived() { return totalAmountReceived; }
        public Integer getTotalInvoices() { return totalInvoices; }
        public Double getTotalReceivedFromInvoices() { return totalReceivedFromInvoices; }
    }
} 