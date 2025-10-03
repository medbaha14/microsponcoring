package com.example.microsponsoringbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit_log")
public class SecurityAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(nullable = false)
    private String endpoint;
    
    @Column(name = "http_method", nullable = false)
    private String httpMethod;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String status;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "session_id")
    private String sessionId;
    
    // Constructors
    public SecurityAuditLog() {}
    
    public SecurityAuditLog(String userId, String ipAddress, String endpoint, 
                           String httpMethod, String action, String status, 
                           String details, String userAgent, String sessionId) {
        this.timestamp = LocalDateTime.now();
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.action = action;
        this.status = status;
        this.details = details;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
