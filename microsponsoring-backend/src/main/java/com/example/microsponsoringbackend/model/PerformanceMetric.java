package com.example.microsponsoringbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_metrics")
public class PerformanceMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String endpoint;
    
    @Column(name = "http_method", nullable = false)
    private String httpMethod;
    
    @Column(name = "response_time_ms", nullable = false)
    private Long responseTimeMs;
    
    @Column(name = "status_code", nullable = false)
    private Integer statusCode;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "request_size")
    private Long requestSize;
    
    @Column(name = "response_size")
    private Long responseSize;
    
    // Constructors
    public PerformanceMetric() {}
    
    public PerformanceMetric(String endpoint, String httpMethod, Long responseTimeMs, 
                           Integer statusCode, String userId, String ipAddress, 
                           String userAgent, Long requestSize, Long responseSize) {
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.responseTimeMs = responseTimeMs;
        this.statusCode = statusCode;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = LocalDateTime.now();
        this.requestSize = requestSize;
        this.responseSize = responseSize;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Long getRequestSize() { return requestSize; }
    public void setRequestSize(Long requestSize) { this.requestSize = requestSize; }
    
    public Long getResponseSize() { return responseSize; }
    public void setResponseSize(Long responseSize) { this.responseSize = responseSize; }
}
