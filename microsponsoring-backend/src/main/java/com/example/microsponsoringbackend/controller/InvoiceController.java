package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.Invoice;
import com.example.microsponsoringbackend.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public List<Invoice> getAll() {
        return invoiceService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getById(@PathVariable UUID id) {
        Optional<Invoice> result = invoiceService.findById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Invoice create(@RequestBody Invoice invoice) {
        Date currentDate = new Date();
        invoice.setCreatedAt(currentDate);
        invoice.setUpdatedAt(currentDate);
        return invoiceService.save(invoice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Invoice> update(@PathVariable UUID id, @RequestBody Invoice invoice) {
        if (!invoiceService.existsById(id)) return ResponseEntity.notFound().build();

        // Fetch the existing invoice
        Invoice existingInvoice = invoiceService.findById(id).orElse(null);
        if (existingInvoice == null) return ResponseEntity.notFound().build();

        // Only update non-null fields
        if (invoice.getAmount() != null) existingInvoice.setAmount(invoice.getAmount());
        if (invoice.getStatus() != null) existingInvoice.setStatus(invoice.getStatus());
        if (invoice.getCompany() != null) existingInvoice.setCompany(invoice.getCompany());
        if (invoice.getSponsor() != null) existingInvoice.setSponsor(invoice.getSponsor());
        if (invoice.getInvoiceDate() != null) existingInvoice.setInvoiceDate(invoice.getInvoiceDate());
        if (invoice.getPaymentStatus() != null) existingInvoice.setPaymentStatus(invoice.getPaymentStatus());
        if (invoice.getAcceptedTerms() != null) existingInvoice.setAcceptedTerms(invoice.getAcceptedTerms());
        if (invoice.getGeneratedPdfUrl() != null) existingInvoice.setGeneratedPdfUrl(invoice.getGeneratedPdfUrl());
        if (invoice.getBenefits() != null) existingInvoice.setBenefits(invoice.getBenefits());

        // Always update the updatedAt field
        existingInvoice.setUpdatedAt(new Date());

        return ResponseEntity.ok(invoiceService.save(existingInvoice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!invoiceService.existsById(id)) return ResponseEntity.notFound().build();
        invoiceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> downloadInvoicePdf(@PathVariable UUID id) {
        // Use different paths for local vs deployed environment
        String pdfPath;
        
        // Check if we're running in a container (Kubernetes) or locally
        if (System.getProperty("user.dir").contains("/app")) {
            // Running in Kubernetes container
            pdfPath = "/app/invoices/" + id + ".pdf";
        } else {
            // Running locally
            pdfPath = "src/main/resources/invoices/" + id + ".pdf";
        }
        
        try {
            Resource file = new UrlResource(new java.io.File(pdfPath).toURI());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".pdf\"")
                .body(file);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/regenerate-pdf")
    public ResponseEntity<String> regeneratePdf(@PathVariable UUID id) {
        try {
            Optional<Invoice> invoiceOpt = invoiceService.findById(id);
            if (invoiceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Invoice invoice = invoiceOpt.get();
            
            // Use different paths for local vs deployed environment
            String pdfDir;
            String pdfUrlPrefix;
            
            // Check if we're running in a container (Kubernetes) or locally
            if (System.getProperty("user.dir").contains("/app")) {
                // Running in Kubernetes container
                pdfDir = "/app/invoices/";
                pdfUrlPrefix = "/invoices/";
            } else {
                // Running locally
                pdfDir = "src/main/resources/invoices/";
                pdfUrlPrefix = "/invoices/";
            }
            
            // Create directory if it doesn't exist
            java.io.File dir = new java.io.File(pdfDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // Generate PDF
            String pdfFile = invoice.getInvoiceId() + ".pdf";
            String pdfPath = pdfDir + pdfFile;
            com.example.microsponsoringbackend.util.InvoicePdfGenerator.generateInvoicePdf(invoice, pdfPath);
            
            // Update invoice with PDF URL
            invoice.setGeneratedPdfUrl(pdfUrlPrefix + pdfFile);
            invoiceService.save(invoice);
            
            return ResponseEntity.ok("PDF regenerated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to regenerate PDF: " + e.getMessage());
        }
    }
}
