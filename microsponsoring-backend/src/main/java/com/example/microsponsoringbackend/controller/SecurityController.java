package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.dto.SecurityVulnerabilityDto;
import com.example.microsponsoringbackend.dto.SecurityDashboardDto;
import com.example.microsponsoringbackend.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class SecurityController {

    @Autowired
    private SecurityService securityService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SecurityDashboardDto> getSecurityDashboard() {
        try {
            SecurityDashboardDto dashboard = securityService.getSecurityDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/vulnerabilities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityVulnerabilityDto>> getVulnerabilities() {
        try {
            List<SecurityVulnerabilityDto> vulnerabilities = securityService.getVulnerabilities();
            return ResponseEntity.ok(vulnerabilities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/scan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> runSecurityScan() {
        try {
            String result = securityService.runSecurityScan();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error running security scan: " + e.getMessage());
        }
    }

    @PostMapping("/fix")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> fixVulnerabilities() {
        try {
            String result = securityService.fixVulnerabilities();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fixing vulnerabilities: " + e.getMessage());
        }
    }

    @PostMapping("/force-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> forceUpdate() {
        try {
            String result = securityService.forceUpdate();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error forcing update: " + e.getMessage());
        }
    }

    @GetMapping("/export-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportSecurityReport() {
        try {
            String reportUrl = securityService.exportSecurityReport();
            return ResponseEntity.ok(reportUrl);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error exporting report: " + e.getMessage());
        }
    }
} 