package com.example.microsponsoringbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.microsponsoringbackend.model.Alert;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    // Find pending alerts ordered by timestamp (newest first)
    List<Alert> findByStatusOrderByTimestampDesc(String status);
    
    // Find alerts by severity ordered by timestamp
    List<Alert> findBySeverityOrderByTimestampDesc(String severity);
    
    // Find alerts by severity and status
    List<Alert> findBySeverityAndStatus(String severity, String status);
    
    // Find all alerts ordered by timestamp (without pagination)
    List<Alert> findAllByOrderByTimestampDesc();
    
    // Find alerts by category
    List<Alert> findByCategoryOrderByTimestampDesc(String category);
    
    // Count methods for statistics
    long countByStatus(String status);
    long countBySeverityAndStatus(String severity, String status);
    
    // Paginated queries
    Page<Alert> findByStatus(String status, Pageable pageable);
    Page<Alert> findBySeverity(String severity, Pageable pageable);
    
    // Custom query for high priority alerts
    @Query("SELECT a FROM Alert a WHERE a.status = 'pending' AND a.severity IN ('high', 'critical') ORDER BY a.timestamp DESC")
    List<Alert> findHighPriorityAlerts();
}