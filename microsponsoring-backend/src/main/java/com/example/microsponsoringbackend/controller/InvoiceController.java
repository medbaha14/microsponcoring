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
        String pdfPath = "src/main/resources/invoices/" + id + ".pdf";
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
} 