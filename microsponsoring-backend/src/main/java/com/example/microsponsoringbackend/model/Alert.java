package com.example.microsponsoringbackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alerts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String severity; // 'high', 'medium', 'low'

    @Column(nullable = false)
    private String category; // 'system', 'security', 'performance'

    @Column(nullable = false)
    private String status = "pending"; // 'pending', 'acknowledged', 'resolved'

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private LocalDateTime resolvedAt;

 
}