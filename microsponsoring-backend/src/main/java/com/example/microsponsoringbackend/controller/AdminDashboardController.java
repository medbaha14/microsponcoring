package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.model.SecurityRule;
import com.example.microsponsoringbackend.service.DatabaseDrivenSecurityService;
import com.example.microsponsoringbackend.service.PerformanceMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    
    @Autowired
    private DatabaseDrivenSecurityService securityService;
    
    @Autowired
    private PerformanceMonitoringService performanceService;
    
    /**
     * Get comprehensive dashboard overview
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            // Security overview
            List<SecurityRule> activeRules = securityService.getAllActiveRules();
            List<SecurityRule> publicEndpoints = securityService.getPublicEndpoints();
            
            Map<String, Object> securityOverview = new HashMap<>();
            securityOverview.put("totalRules", activeRules.size());
            securityOverview.put("publicEndpoints", publicEndpoints.size());
            securityOverview.put("activeRules", activeRules);
            dashboard.put("security", securityOverview);
            
            // Performance overview (last 24 hours)
            Map<String, Object> performanceOverview = performanceService.getPerformanceSummary(24);
            dashboard.put("performance", performanceOverview);
            
            // System health
            Map<String, Object> systemHealth = new HashMap<>();
            systemHealth.put("status", "UP");
            systemHealth.put("timestamp", System.currentTimeMillis());
            systemHealth.put("version", "1.0.0");
            systemHealth.put("environment", "development");
            dashboard.put("system", systemHealth);
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve dashboard data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get all security rules
     */
    @GetMapping("/security/rules")
    public ResponseEntity<List<SecurityRule>> getAllSecurityRules() {
        try {
            List<SecurityRule> rules = securityService.getAllActiveRules();
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Add a new security rule
     */
    @PostMapping("/security/rules")
    public ResponseEntity<SecurityRule> addSecurityRule(@RequestBody SecurityRule rule) {
        try {
            SecurityRule savedRule = securityService.addSecurityRule(rule);
            return ResponseEntity.ok(savedRule);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update an existing security rule
     */
    @PutMapping("/security/rules/{id}")
    public ResponseEntity<SecurityRule> updateSecurityRule(@PathVariable Long id, @RequestBody SecurityRule rule) {
        try {
            SecurityRule updatedRule = securityService.updateSecurityRule(id, rule);
            return ResponseEntity.ok(updatedRule);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Delete a security rule
     */
    @DeleteMapping("/security/rules/{id}")
    public ResponseEntity<Void> deleteSecurityRule(@PathVariable Long id) {
        try {
            securityService.deleteSecurityRule(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Toggle security rule status
     */
    @PatchMapping("/security/rules/{id}/toggle")
    public ResponseEntity<SecurityRule> toggleSecurityRule(@PathVariable Long id) {
        try {
            SecurityRule toggledRule = securityService.toggleRuleStatus(id);
            return ResponseEntity.ok(toggledRule);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Reload security rules from database
     */
    @PostMapping("/security/rules/reload")
    public ResponseEntity<Map<String, String>> reloadSecurityRules() {
        try {
            securityService.reloadRules();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Security rules reloaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reload security rules: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get performance metrics for the last N hours
     */
    @GetMapping("/performance/summary")
    public ResponseEntity<Map<String, Object>> getPerformanceSummary(
            @RequestParam(defaultValue = "24") int hours) {
        try {
            Map<String, Object> summary = performanceService.getPerformanceSummary(hours);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve performance data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get performance metrics for a specific endpoint
     */
    @GetMapping("/performance/endpoint/{endpoint}")
    public ResponseEntity<Map<String, Object>> getEndpointPerformance(
            @PathVariable String endpoint,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            Map<String, Object> performance = performanceService.getEndpointPerformance(endpoint, hours);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve endpoint performance: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Clean up old performance metrics
     */
    @DeleteMapping("/performance/cleanup")
    public ResponseEntity<Map<String, String>> cleanupOldMetrics(
            @RequestParam(defaultValue = "30") int days) {
        try {
            performanceService.cleanupOldMetrics(days);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cleanup scheduled for metrics older than " + days + " days");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to schedule cleanup: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get system metrics
     */
    @GetMapping("/system/metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        try {
            Map<String, Object> metrics = performanceService.getSystemMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve system metrics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
