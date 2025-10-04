package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.dto.SecurityVulnerabilityDto;
import com.example.microsponsoringbackend.dto.SecurityDashboardDto;
import com.example.microsponsoringbackend.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/security")
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

    @GetMapping("/vulnerabilities/package/{packageName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityVulnerabilityDto>> getVulnerabilitiesByPackage(@PathVariable String packageName) {
        try {
            List<SecurityVulnerabilityDto> vulnerabilities = securityService.getVulnerabilitiesForPackage(packageName);
            return ResponseEntity.ok(vulnerabilities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/vulnerabilities/severity/{severity}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityVulnerabilityDto>> getVulnerabilitiesBySeverity(@PathVariable String severity) {
        try {
            List<SecurityVulnerabilityDto> vulnerabilities = securityService.getVulnerabilitiesBySeverity(severity);
            return ResponseEntity.ok(vulnerabilities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/vulnerabilities/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityVulnerabilityDto>> getRecentVulnerabilities() {
        try {
            List<SecurityVulnerabilityDto> vulnerabilities = securityService.getRecentVulnerabilities();
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

    @GetMapping("/quick-scan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SecurityDashboardDto> quickSecurityScan() {
        try {
            SecurityDashboardDto dashboard = securityService.quickSecurityScan();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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
    public ResponseEntity<List<Map<String, Object>>> getSecurityAlerts() {
        try {
            // Generate dynamic alerts based on current vulnerability data
            List<SecurityVulnerabilityDto> vulnerabilities = securityService.getVulnerabilities();
            List<Map<String, Object>> alerts = new java.util.ArrayList<>();
            
            // Create alerts for high and critical vulnerabilities
            for (SecurityVulnerabilityDto vuln : vulnerabilities) {
                if ("critical".equalsIgnoreCase(vuln.getSeverity()) || "high".equalsIgnoreCase(vuln.getSeverity())) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("id", vuln.getCveId());
                    alert.put("type", vuln.getSeverity().toUpperCase());
                    alert.put("title", "Security Vulnerability: " + vuln.getPackageName());
                    alert.put("description", vuln.getDescription());
                    alert.put("timestamp", System.currentTimeMillis());
                    alert.put("status", "ACTIVE");
                    alert.put("cveId", vuln.getCveId());
                    alert.put("package", vuln.getPackageName());
                    alert.put("riskScore", vuln.getRiskScore());
                    alerts.add(alert);
                }
            }
            
            // If no high/critical vulnerabilities, create an informational alert
            if (alerts.isEmpty()) {
                Map<String, Object> infoAlert = new HashMap<>();
                infoAlert.put("id", "info-1");
                infoAlert.put("type", "INFO");
                infoAlert.put("title", "Security Status: Normal");
                infoAlert.put("description", "No critical or high severity vulnerabilities detected");
                infoAlert.put("timestamp", System.currentTimeMillis());
                infoAlert.put("status", "ACTIVE");
                alerts.add(infoAlert);
            }
            
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/sync-nvd")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> syncNVDVulnerabilities() {
        try {
            CompletableFuture<String> future = securityService.syncVulnerabilitiesFromNVD();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SYNC_STARTED");
            response.put("message", "NVD vulnerability sync initiated in background");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "SYNC_FAILED");
            errorResponse.put("message", "Error syncing NVD vulnerabilities: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/sync-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        try {
            String syncStatus = securityService.getNvdSyncStatus();
            boolean syncNeeded = securityService.isNvdSyncNeeded();
            
            Map<String, Object> status = new HashMap<>();
            status.put("syncStatus", syncStatus);
            status.put("syncNeeded", syncNeeded);
            status.put("lastChecked", System.currentTimeMillis());
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSecurityStats() {
        try {
            SecurityDashboardDto dashboard = securityService.getSecurityDashboard();
            List<Object[]> vulnerabilityStats = securityService.getVulnerabilityStats();
            List<Object[]> ecosystemStats = securityService.getEcosystemStats();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("vulnerabilityCounts", Map.of(
                "critical", dashboard.getCriticalCount(),
                "high", dashboard.getHighCount(),
                "moderate", dashboard.getModerateCount(),
                "low", dashboard.getLowCount(),
                "total", dashboard.getVulnerabilities().size()
            ));
            stats.put("overallStatus", dashboard.getOverallStatus());
            stats.put("lastUpdate", dashboard.getLastUpdate());
            stats.put("vulnerabilityStats", vulnerabilityStats);
            stats.put("ecosystemStats", ecosystemStats);
            stats.put("syncStatus", securityService.getNvdSyncStatus());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            String healthStatus = securityService.healthCheck();
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "HEALTHY");
            health.put("message", healthStatus);
            health.put("timestamp", System.currentTimeMillis());
            health.put("service", "Security Service");
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UNHEALTHY");
            health.put("message", "Security service health check failed: " + e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            health.put("service", "Security Service");
            return ResponseEntity.internalServerError().body(health);
        }
    }
    
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSecuritySummary() {
        try {
            SecurityDashboardDto dashboard = securityService.getSecurityDashboard();
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalVulnerabilities", dashboard.getVulnerabilities().size());
            summary.put("criticalCount", dashboard.getCriticalCount());
            summary.put("highCount", dashboard.getHighCount());
            summary.put("moderateCount", dashboard.getModerateCount());
            summary.put("lowCount", dashboard.getLowCount());
            summary.put("overallStatus", dashboard.getOverallStatus());
            summary.put("lastScan", dashboard.getLastUpdate());
            summary.put("nextScan", dashboard.getNextScan());
            summary.put("syncStatus", securityService.getNvdSyncStatus());
            summary.put("needsAttention", dashboard.getCriticalCount() + dashboard.getHighCount() > 0);
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}