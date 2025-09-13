package com.example.microsponsoringbackend.interceptor;

import com.example.microsponsoringbackend.service.PerformanceMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.CompletableFuture;

@Component
public class PerformanceMonitoringInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringInterceptor.class);
    
    @Autowired
    private PerformanceMonitoringService performanceService;
    
    private static final String START_TIME_ATTRIBUTE = "requestStartTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Record start time
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            // Calculate response time
            Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            if (startTime != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                
                // Get request details
                String endpoint = request.getRequestURI();
                String method = request.getMethod();
                int statusCode = response.getStatus();
                String userId = getCurrentUserId();
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");
                Long requestSize = getRequestSize(request);
                Long responseSize = getResponseSize(response);
                
                // Record performance metric asynchronously
                CompletableFuture.runAsync(() -> {
                    try {
                        performanceService.recordMetric(endpoint, method, responseTime, statusCode, 
                                                     userId, ipAddress, userAgent, requestSize, responseSize);
                    } catch (Exception e) {
                        logger.error("Failed to record performance metric for {} {}", method, endpoint, e);
                    }
                });
                
                // Log slow requests
                if (responseTime > 1000) { // Log requests slower than 1 second
                    logger.warn("Slow request detected: {} {} - {}ms - {}", 
                               method, endpoint, responseTime, statusCode);
                }
            }
        } catch (Exception e) {
            logger.error("Error in performance monitoring interceptor", e);
        }
    }
    
    /**
     * Get the current user ID from security context
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            logger.debug("Could not extract user ID from security context", e);
        }
        return null;
    }
    
    /**
     * Get the client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Get request size (approximate)
     */
    private Long getRequestSize(HttpServletRequest request) {
        try {
            String contentLength = request.getHeader("Content-Length");
            if (contentLength != null && !contentLength.isEmpty()) {
                return Long.parseLong(contentLength);
            }
        } catch (Exception e) {
            logger.debug("Could not parse Content-Length header", e);
        }
        return null;
    }
    
    /**
     * Get response size (approximate)
     */
    private Long getResponseSize(HttpServletResponse response) {
        try {
            // This is a rough approximation - actual response size might be different
            // due to compression, encoding, etc.
            return null; // We'll leave this as null for now
        } catch (Exception e) {
            logger.debug("Could not determine response size", e);
        }
        return null;
    }
}
