package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.SecurityRule;
import com.example.microsponsoringbackend.repository.SecurityRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class DatabaseDrivenSecurityService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseDrivenSecurityService.class);
    
    @Autowired
    private SecurityRuleRepository securityRuleRepository;
    
    // In-memory cache for performance
    private final ConcurrentHashMap<String, SecurityRule> ruleCache = new ConcurrentHashMap<>();
    
    /**
     * Check if an endpoint is public (no authentication required)
     */
    @Cacheable(value = "securityRules", key = "#endpoint + '_' + #method")
    public boolean isPublicEndpoint(String endpoint, String method) {
        try {
            SecurityRule rule = findMatchingRule(endpoint, method);
            return rule != null && rule.getIsPublic();
        } catch (Exception e) {
            logger.error("Error checking if endpoint is public: {} {}", endpoint, method, e);
            return false; // Default to secure
        }
    }
    
    /**
     * Get the required role for an endpoint
     */
    @Cacheable(value = "securityRules", key = "#endpoint + '_' + #method + '_role'")
    public String getRequiredRole(String endpoint, String method) {
        try {
            SecurityRule rule = findMatchingRule(endpoint, method);
            return rule != null ? rule.getRequiredRole() : null;
        } catch (Exception e) {
            logger.error("Error getting required role for endpoint: {} {}", endpoint, method, e);
            return null;
        }
    }
    
    /**
     * Find the matching security rule for an endpoint and method
     */
    private SecurityRule findMatchingRule(String endpoint, String method) {
        // Check cache first
        String cacheKey = endpoint + "_" + method;
        SecurityRule cachedRule = ruleCache.get(cacheKey);
        if (cachedRule != null) {
            return cachedRule;
        }
        
        // Load from database
        List<SecurityRule> activeRules = securityRuleRepository.findAllActiveOrderByPriority();
        
        for (SecurityRule rule : activeRules) {
            if (matchesPattern(endpoint, rule.getEndpointPattern()) && 
                method.equalsIgnoreCase(rule.getHttpMethod())) {
                // Cache the rule
                ruleCache.put(cacheKey, rule);
                return rule;
            }
        }
        
        return null;
    }
    
    /**
     * Check if an endpoint matches a pattern (supports wildcards)
     */
    private boolean matchesPattern(String endpoint, String pattern) {
        if (pattern == null || endpoint == null) {
            return false;
        }
        
        // Convert pattern to regex
        String regexPattern = pattern
            .replace("**", ".*")  // ** matches any sequence
            .replace("*", "[^/]*"); // * matches any sequence not containing /
        
        return Pattern.matches(regexPattern, endpoint);
    }
    
    /**
     * Get all active security rules
     */
    public List<SecurityRule> getAllActiveRules() {
        return securityRuleRepository.findAllActiveOrderByPriority();
    }
    
    /**
     * Get all public endpoints
     */
    public List<SecurityRule> getPublicEndpoints() {
        return securityRuleRepository.findAllPublicEndpoints();
    }
    
    /**
     * Add a new security rule
     */
    @CacheEvict(value = "securityRules", allEntries = true)
    public SecurityRule addSecurityRule(SecurityRule rule) {
        logger.info("Adding new security rule: {} {} -> {}", 
                   rule.getHttpMethod(), rule.getEndpointPattern(), 
                   rule.getIsPublic() ? "PUBLIC" : rule.getRequiredRole());
        
        SecurityRule savedRule = securityRuleRepository.save(rule);
        clearCache();
        return savedRule;
    }
    
    /**
     * Update an existing security rule
     */
    @CacheEvict(value = "securityRules", allEntries = true)
    public SecurityRule updateSecurityRule(Long id, SecurityRule rule) {
        logger.info("Updating security rule: {} {} -> {}", 
                   rule.getHttpMethod(), rule.getEndpointPattern(), 
                   rule.getIsPublic() ? "PUBLIC" : rule.getRequiredRole());
        
        rule.setId(id);
        SecurityRule savedRule = securityRuleRepository.save(rule);
        clearCache();
        return savedRule;
    }
    
    /**
     * Delete a security rule
     */
    @CacheEvict(value = "securityRules", allEntries = true)
    public void deleteSecurityRule(Long id) {
        logger.info("Deleting security rule with ID: {}", id);
        securityRuleRepository.deleteById(id);
        clearCache();
    }
    
    /**
     * Toggle the active status of a security rule
     */
    @CacheEvict(value = "securityRules", allEntries = true)
    public SecurityRule toggleRuleStatus(Long id) {
        SecurityRule rule = securityRuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Security rule not found: " + id));
        
        rule.setIsActive(!rule.getIsActive());
        logger.info("Toggling security rule status: {} {} -> {}", 
                   rule.getHttpMethod(), rule.getEndpointPattern(), 
                   rule.getIsActive() ? "ACTIVE" : "INACTIVE");
        
        SecurityRule savedRule = securityRuleRepository.save(rule);
        clearCache();
        return savedRule;
    }
    
    /**
     * Clear the in-memory cache
     */
    private void clearCache() {
        ruleCache.clear();
        logger.info("Security rules cache cleared");
    }
    
    /**
     * Reload all rules from database (useful for testing)
     */
    @CacheEvict(value = "securityRules", allEntries = true)
    public void reloadRules() {
        clearCache();
        logger.info("Security rules reloaded from database");
    }
}
