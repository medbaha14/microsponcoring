package com.example.microsponsoringbackend.controller;

import com.example.microsponsoringbackend.service.PerformanceMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class SystemController {

    @Autowired
    private PerformanceMonitoringService performanceService;

    /**
     * Get system metrics
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
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
