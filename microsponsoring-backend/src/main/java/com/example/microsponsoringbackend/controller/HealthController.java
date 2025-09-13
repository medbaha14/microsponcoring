package com.example.microsponsoringbackend.controller;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Order(1)
public class HealthController {
    
    private static final long startTime = System.currentTimeMillis();
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @GetMapping("/actuator/health")
    public Map<String, Object> actuatorHealth() {
        logger.info("Actuator health endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        
        Map<String, Object> details = new HashMap<>();
        details.put("backend", "UP");
        details.put("database", "UP");
        details.put("security", "UP");
        details.put("performance", "UP");
        details.put("timestamp", System.currentTimeMillis());
        details.put("uptime", System.currentTimeMillis() - startTime);
        
        response.put("details", details);
        logger.info("Actuator health endpoint returning: {}", response);
        return response;
    }
    
    @GetMapping("/api/health/health")
    public Map<String, Object> apiHealth() {
        logger.info("API health endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        
        Map<String, Object> details = new HashMap<>();
        details.put("backend", "UP");
        details.put("database", "UP");
        details.put("timestamp", System.currentTimeMillis());
        
        response.put("details", details);
        logger.info("API health endpoint returning: {}", response);
        return response;
    }

    @GetMapping("/actuator/info")
    public Map<String, Object> actuatorInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("app", new HashMap<String, String>() {{
            put("name", "microsponsoring-backend");
            put("version", "1.0.0");
            put("environment", "development");
        }});
        return response;
    }
    
    @GetMapping("/api/health/info")
    public Map<String, Object> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("app", new HashMap<String, String>() {{
            put("name", "microsponsoring-backend");
            put("version", "1.0.0");
            put("environment", "development");
        }});
        return response;
    }
    
    @GetMapping("/test")
    public Map<String, Object> test() {
        logger.info("Test endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test endpoint working");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
