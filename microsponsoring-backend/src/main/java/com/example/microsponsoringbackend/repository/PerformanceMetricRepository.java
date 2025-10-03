package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.PerformanceMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PerformanceMetricRepository extends JpaRepository<PerformanceMetric, Long> {
    
    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.timestamp >= :startTime ORDER BY pm.timestamp DESC")
    List<PerformanceMetric> findByTimestampAfter(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.endpoint = :endpoint AND pm.timestamp >= :startTime ORDER BY pm.timestamp DESC")
    List<PerformanceMetric> findByEndpointAndTimestampAfter(@Param("endpoint") String endpoint, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(pm.responseTimeMs) FROM PerformanceMetric pm WHERE pm.endpoint = :endpoint AND pm.timestamp >= :startTime")
    Double getAverageResponseTime(@Param("endpoint") String endpoint, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(pm) FROM PerformanceMetric pm WHERE pm.statusCode = :statusCode AND pm.timestamp >= :startTime")
    Long countByStatusCodeAndTimestampAfter(@Param("statusCode") Integer statusCode, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT pm.endpoint, AVG(pm.responseTimeMs) as avgResponseTime, COUNT(pm) as requestCount " +
           "FROM PerformanceMetric pm WHERE pm.timestamp >= :startTime GROUP BY pm.endpoint ORDER BY avgResponseTime DESC")
    List<Object[]> getEndpointPerformanceSummary(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.userId = :userId ORDER BY pm.timestamp DESC")
    List<PerformanceMetric> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.ipAddress = :ipAddress ORDER BY pm.timestamp DESC")
    List<PerformanceMetric> findByIpAddress(@Param("ipAddress") String ipAddress);
}
