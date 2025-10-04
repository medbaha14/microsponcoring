package com.example.microsponsoringbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.microsponsoringbackend.model.Alert;
import com.example.microsponsoringbackend.service.AlertService;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertService alertService;

    // Get pending alerts from database
    @GetMapping("/pending")
    public ResponseEntity<List<Alert>> getPendingAlerts() {
        try {
            List<Alert> pendingAlerts = alertService.findPendingAlerts();
            return ResponseEntity.ok(pendingAlerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get all alerts with pagination - FIXED
    @GetMapping
    public ResponseEntity<Page<Alert>> getAllAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Alert> alerts = alertService.findAllAlerts(pageable);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Alternative simplified version without pagination
    @GetMapping("/all")
    public ResponseEntity<List<Alert>> getAllAlertsSimple() {
        try {
            List<Alert> alerts = alertService.findAllAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Mark alert as resolved
    @PostMapping("/{alertId}/resolve")
    public ResponseEntity<String> resolveAlert(@PathVariable String alertId) {
        try {
            alertService.resolveAlert(alertId);
            return ResponseEntity.ok("Alert resolved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error resolving alert: " + e.getMessage());
        }
    }

    // Acknowledge alert
    @PostMapping("/{alertId}/acknowledge")
    public ResponseEntity<String> acknowledgeAlert(@PathVariable String alertId) {
        try {
            alertService.acknowledgeAlert(alertId);
            return ResponseEntity.ok("Alert acknowledged");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error acknowledging alert: " + e.getMessage());
        }
    }

    // Create a new alert
    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
        try {
            Alert savedAlert = alertService.createAlert(alert);
            return ResponseEntity.ok(savedAlert);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get alerts by severity
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Alert>> getAlertsBySeverity(@PathVariable String severity) {
        try {
            List<com.example.microsponsoringbackend.model.Alert> alerts = alertService.findAlertsBySeverity(severity);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}