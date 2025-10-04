package com.example.microsponsoringbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.microsponsoringbackend.model.Alert;
import com.example.microsponsoringbackend.repository.AlertRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    public List<Alert> findPendingAlerts() {
        return alertRepository.findByStatusOrderByTimestampDesc("pending");
    }

    // FIXED: Use Pageable interface
    public Page<Alert> findAllAlerts(Pageable pageable) {
        return alertRepository.findAll(pageable);
    }

    // Alternative without pagination
    public List<Alert> findAllAlerts() {
        return alertRepository.findAllByOrderByTimestampDesc();
    }

    public void resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + alertId));
        alert.setStatus("resolved");
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }

    public void acknowledgeAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + alertId));
        alert.setStatus("acknowledged");
        alertRepository.save(alert);
    }

    public Alert createAlert(Alert alert) {
        if (alert.getTimestamp() == null) {
            alert.setTimestamp(LocalDateTime.now());
        }
        if (alert.getStatus() == null) {
            alert.setStatus("pending");
        }
        return alertRepository.save(alert);
    }

    public List<Alert> findAlertsBySeverity(String severity) {
        return alertRepository.findBySeverityOrderByTimestampDesc(severity);
    }

    public List<Alert> findCriticalAlerts() {
        return alertRepository.findBySeverityAndStatus("high", "pending");
    }

    // Get alert statistics
    public AlertStatistics getAlertStatistics() {
        long totalAlerts = alertRepository.count();
        long pendingAlerts = alertRepository.countByStatus("pending");
        long highSeverityAlerts = alertRepository.countBySeverityAndStatus("high", "pending");
        
        return new AlertStatistics(totalAlerts, pendingAlerts, highSeverityAlerts);
    }

    // Inner class for statistics
    public static class AlertStatistics {
        private long totalAlerts;
        private long pendingAlerts;
        private long highSeverityAlerts;

        public AlertStatistics(long totalAlerts, long pendingAlerts, long highSeverityAlerts) {
            this.totalAlerts = totalAlerts;
            this.pendingAlerts = pendingAlerts;
            this.highSeverityAlerts = highSeverityAlerts;
        }

        // Getters and setters
        public long getTotalAlerts() { return totalAlerts; }
        public void setTotalAlerts(long totalAlerts) { this.totalAlerts = totalAlerts; }
        public long getPendingAlerts() { return pendingAlerts; }
        public void setPendingAlerts(long pendingAlerts) { this.pendingAlerts = pendingAlerts; }
        public long getHighSeverityAlerts() { return highSeverityAlerts; }
        public void setHighSeverityAlerts(long highSeverityAlerts) { this.highSeverityAlerts = highSeverityAlerts; }
    }
}