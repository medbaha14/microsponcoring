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
    
    @GetMapping("/alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getSecurityAlerts() {
        try {
            java.util.List<java.util.Map<String, Object>> alerts = new java.util.ArrayList<>();
            
            // Add some sample security alerts
            java.util.Map<String, Object> alert1 = new java.util.HashMap<>();
            alert1.put("id", "1");
            alert1.put("type", "HIGH");
            alert1.put("title", "Suspicious Login Activity");
            alert1.put("description", "Multiple failed login attempts detected from IP 192.168.1.100");
            alert1.put("timestamp", System.currentTimeMillis() - 3600000); // 1 hour ago
            alert1.put("status", "ACTIVE");
            alerts.add(alert1);
            
            java.util.Map<String, Object> alert2 = new java.util.HashMap<>();
            alert2.put("id", "2");
            alert2.put("type", "MEDIUM");
            alert2.put("title", "Unusual API Usage Pattern");
            alert2.put("description", "High volume of API requests detected from user 'testuser'");
            alert2.put("timestamp", System.currentTimeMillis() - 7200000); // 2 hours ago
            alert2.put("status", "RESOLVED");
            alerts.add(alert2);
            
            java.util.Map<String, Object> alert3 = new java.util.HashMap<>();
            alert3.put("id", "3");
            alert3.put("type", "LOW");
            alert3.put("title", "Password Policy Violation");
            alert3.put("description", "User 'newuser' created account with weak password");
            alert3.put("timestamp", System.currentTimeMillis() - 10800000); // 3 hours ago
            alert3.put("status", "ACTIVE");
            alerts.add(alert3);
            
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 