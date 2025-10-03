package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.PerformanceMetric;
import com.example.microsponsoringbackend.repository.PerformanceMetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PerformanceMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringService.class);
    
    @Autowired
    private PerformanceMetricRepository performanceMetricRepository;
    
    /**
     * Record a performance metric asynchronously
     */
    @Async
    public CompletableFuture<PerformanceMetric> recordMetric(String endpoint, String httpMethod, 
                                                           Long responseTimeMs, Integer statusCode, 
                                                           String userId, String ipAddress, 
                                                           String userAgent, Long requestSize, 
                                                           Long responseSize) {
        try {
            PerformanceMetric metric = new PerformanceMetric(endpoint, httpMethod, responseTimeMs, 
                                                          statusCode, userId, ipAddress, userAgent, 
                                                          requestSize, responseSize);
            
            PerformanceMetric savedMetric = performanceMetricRepository.save(metric);
            logger.debug("Recorded performance metric: {} {} - {}ms - {}", 
                        httpMethod, endpoint, responseTimeMs, statusCode);
            
            return CompletableFuture.completedFuture(savedMetric);
        } catch (Exception e) {
            logger.error("Error recording performance metric: {} {}", endpoint, httpMethod, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Get performance summary for the last N hours
     */
    public Map<String, Object> getPerformanceSummary(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Get endpoint performance summary
            List<Object[]> endpointSummary = performanceMetricRepository.getEndpointPerformanceSummary(startTime);
            summary.put("endpointPerformance", endpointSummary);
            
            // Get response time statistics
            summary.put("responseTimeStats", getResponseTimeStats(startTime));
            
            // Get status code distribution
            summary.put("statusCodeDistribution", getStatusCodeDistribution(startTime));
            
            // Get system metrics
            summary.put("systemMetrics", getSystemMetrics());
            
            // Get recent metrics
            List<PerformanceMetric> recentMetrics = performanceMetricRepository.findByTimestampAfter(startTime);
            summary.put("recentMetrics", recentMetrics);
            
        } catch (Exception e) {
            logger.error("Error getting performance summary", e);
            summary.put("error", "Failed to retrieve performance data");
        }
        
        return summary;
    }
    
    /**
     * Get response time statistics
     */
    private Map<String, Object> getResponseTimeStats(LocalDateTime startTime) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get average response time for all endpoints
            List<PerformanceMetric> metrics = performanceMetricRepository.findByTimestampAfter(startTime);
            
            if (!metrics.isEmpty()) {
                double avgResponseTime = metrics.stream()
                    .mapToLong(PerformanceMetric::getResponseTimeMs)
                    .average()
                    .orElse(0.0);
                
                long maxResponseTime = metrics.stream()
                    .mapToLong(PerformanceMetric::getResponseTimeMs)
                    .max()
                    .orElse(0);
                
                long minResponseTime = metrics.stream()
                    .mapToLong(PerformanceMetric::getResponseTimeMs)
                    .min()
                    .orElse(0);
                
                stats.put("averageResponseTime", avgResponseTime);
                stats.put("maxResponseTime", maxResponseTime);
                stats.put("minResponseTime", minResponseTime);
                stats.put("totalRequests", metrics.size());
            }
            
        } catch (Exception e) {
            logger.error("Error calculating response time stats", e);
        }
        
        return stats;
    }
    
    /**
     * Get status code distribution
     */
    private Map<String, Object> getStatusCodeDistribution(LocalDateTime startTime) {
        Map<String, Object> distribution = new HashMap<>();
        
        try {
            // Count by status code
            Long successCount = performanceMetricRepository.countByStatusCodeAndTimestampAfter(200, startTime);
            Long clientErrorCount = performanceMetricRepository.countByStatusCodeAndTimestampAfter(400, startTime);
            Long serverErrorCount = performanceMetricRepository.countByStatusCodeAndTimestampAfter(500, startTime);
            
            distribution.put("success", successCount != null ? successCount : 0);
            distribution.put("clientErrors", clientErrorCount != null ? clientErrorCount : 0);
            distribution.put("serverErrors", serverErrorCount != null ? serverErrorCount : 0);
            
        } catch (Exception e) {
            logger.error("Error calculating status code distribution", e);
        }
        
        return distribution;
    }
    
    /**
     * Get current system metrics
     */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Memory usage
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            metrics.put("memoryUsageMB", usedMemory / (1024 * 1024));
            metrics.put("maxMemoryMB", maxMemory / (1024 * 1024));
            metrics.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);
            
            // Thread count
            int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();
            metrics.put("threadCount", threadCount);
            
            // Uptime
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            metrics.put("uptimeMinutes", uptime / (1000 * 60));
            
        } catch (Exception e) {
            logger.error("Error getting system metrics", e);
        }
        
        return metrics;
    }
    
    /**
     * Get performance metrics for a specific endpoint
     */
    public Map<String, Object> getEndpointPerformance(String endpoint, int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        
        Map<String, Object> performance = new HashMap<>();
        
        try {
            List<PerformanceMetric> metrics = performanceMetricRepository.findByEndpointAndTimestampAfter(endpoint, startTime);
            
            if (!metrics.isEmpty()) {
                double avgResponseTime = metrics.stream()
                    .mapToLong(PerformanceMetric::getResponseTimeMs)
                    .average()
                    .orElse(0.0);
                
                long totalRequests = metrics.size();
                long errorCount = metrics.stream()
                    .filter(m -> m.getStatusCode() >= 400)
                    .count();
                
                performance.put("endpoint", endpoint);
                performance.put("averageResponseTime", Math.round(avgResponseTime * 100.0) / 100.0);
                performance.put("totalRequests", totalRequests);
                performance.put("errorCount", errorCount);
                performance.put("successRate", Math.round((double) (totalRequests - errorCount) / totalRequests * 10000.0) / 100.0);
                performance.put("metrics", metrics);
            }
            
        } catch (Exception e) {
            logger.error("Error getting endpoint performance: {}", endpoint, e);
            performance.put("error", "Failed to retrieve endpoint performance data");
        }
        
        return performance;
    }
    
    /**
     * Clean up old performance metrics (older than N days)
     */
    @Async
    public CompletableFuture<Long> cleanupOldMetrics(int days) {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
            // This would require a custom repository method to delete old records
            // For now, we'll just log the cleanup request
            logger.info("Cleanup requested for performance metrics older than {} days", days);
            return CompletableFuture.completedFuture(0L);
        } catch (Exception e) {
            logger.error("Error during metrics cleanup", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
