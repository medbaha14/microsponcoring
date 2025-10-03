package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.SecurityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityRuleRepository extends JpaRepository<SecurityRule, Long> {
    
    @Query("SELECT sr FROM SecurityRule sr WHERE sr.isActive = true ORDER BY sr.priority DESC")
    List<SecurityRule> findAllActiveOrderByPriority();
    
    @Query("SELECT sr FROM SecurityRule sr WHERE sr.endpointPattern = :pattern AND sr.httpMethod = :method AND sr.isActive = true")
    Optional<SecurityRule> findByEndpointAndMethod(@Param("pattern") String pattern, @Param("method") String method);
    
    @Query("SELECT sr FROM SecurityRule sr WHERE sr.endpointPattern LIKE %:pattern% AND sr.isActive = true")
    List<SecurityRule> findByEndpointPatternContaining(@Param("pattern") String pattern);
    
    @Query("SELECT sr FROM SecurityRule sr WHERE sr.isPublic = true AND sr.isActive = true")
    List<SecurityRule> findAllPublicEndpoints();
    
    @Query("SELECT sr FROM SecurityRule sr WHERE sr.requiredRole = :role AND sr.isActive = true")
    List<SecurityRule> findByRequiredRole(@Param("role") String role);
    
    @Query("SELECT sr FROM SecurityRule sr WHERE sr.isActive = true AND sr.priority >= :minPriority ORDER BY sr.priority DESC")
    List<SecurityRule> findByMinPriority(@Param("minPriority") Integer minPriority);
}
