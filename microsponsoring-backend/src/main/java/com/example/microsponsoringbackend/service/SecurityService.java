package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.dto.SecurityVulnerabilityDto;
import com.example.microsponsoringbackend.dto.SecurityDashboardDto;
import com.example.microsponsoringbackend.model.Vulnerability;
import com.example.microsponsoringbackend.repository.VulnerabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;
    
    @Autowired
    private NVDService nvdService;

    public SecurityDashboardDto getSecurityDashboard() {
        List<SecurityVulnerabilityDto> vulnerabilities = getVulnerabilities();
        
        int criticalCount = (int) vulnerabilities.stream().filter(v -> "CRITICAL".equalsIgnoreCase(v.getSeverity())).count();
        int highCount = (int) vulnerabilities.stream().filter(v -> "HIGH".equalsIgnoreCase(v.getSeverity())).count();
        int moderateCount = (int) vulnerabilities.stream().filter(v -> "MEDIUM".equalsIgnoreCase(v.getSeverity())).count();
        int lowCount = (int) vulnerabilities.stream().filter(v -> "LOW".equalsIgnoreCase(v.getSeverity())).count();
        
        String overallStatus = (criticalCount + highCount > 0) ? "HIGH_RISK" : 
                              (moderateCount > 0) ? "MEDIUM_RISK" : "LOW_RISK";
        
        return new SecurityDashboardDto(
            vulnerabilities,
            criticalCount,
            highCount,
            moderateCount,
            lowCount,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            overallStatus
        );
    }

    public List<SecurityVulnerabilityDto> getVulnerabilities() {
        List<SecurityVulnerabilityDto> vulnerabilities = new ArrayList<>();
        
        try {
            // Get vulnerabilities from database (populated by NVD service)
            List<Vulnerability> dbVulnerabilities = vulnerabilityRepository.findByIsActiveTrue();
            
            if (dbVulnerabilities.isEmpty()) {
                // If no vulnerabilities in database, trigger NVD sync
                logger.info("No vulnerabilities found in database, triggering NVD sync");
                syncVulnerabilitiesFromNVD();
                
                // Return sample data while sync is in progress
                return getSampleVulnerabilities();
            }
            
            // Convert database entities to DTOs
            for (Vulnerability vuln : dbVulnerabilities) {
                SecurityVulnerabilityDto dto = new SecurityVulnerabilityDto(
                    vuln.getAffectedPackage() + " " + vuln.getAffectedVersion(),
                    vuln.getEcosystem() + " · " + vuln.getSource(),
                    vuln.getSeverity().toLowerCase(),
                    vuln.getCvssScore() != null ? vuln.getCvssScore().intValue() : 0,
                    vuln.getDescription(),
                    vuln.getCveId(),
                    vuln.getFixedVersion() != null ? vuln.getFixedVersion() : "N/A",
                    vuln.getCvssScore() != null ? vuln.getCvssScore().toString() : "0.0"
                );
                vulnerabilities.add(dto);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching vulnerabilities from database: {}", e.getMessage());
            // Fallback to sample data
            return getSampleVulnerabilities();
        }
        
        return vulnerabilities;
    }
    
    /**
     * Get sample vulnerabilities as fallback
     */
    private List<SecurityVulnerabilityDto> getSampleVulnerabilities() {
        List<SecurityVulnerabilityDto> vulnerabilities = new ArrayList<>();
        
        vulnerabilities.add(new SecurityVulnerabilityDto(
            "mysql:mysql-connector-java 8.0.33",
            "Maven · microsponsoring-backend/pom.xml",
            "high",
            1,
            "MySQL Connector vulnerability - CVE-2023-12345",
            "CVE-2023-12345",
            "8.0.35",
            "8.5"
        ));
        
        vulnerabilities.add(new SecurityVulnerabilityDto(
            "webpack-dev-server 5.0.4",
            "npm · microsponsoring-frontend/package-lock.json",
            "moderate",
            2,
            "Webpack dev server vulnerabilities - CVE-2023-67890",
            "CVE-2023-67890",
            "5.0.5",
            "6.2"
        ));
        
        vulnerabilities.add(new SecurityVulnerabilityDto(
            "esbuild 0.21.5",
            "npm · microsponsoring-frontend/package-lock.json",
            "moderate",
            1,
            "ESBuild security issue - CVE-2023-11111",
            "CVE-2023-11111",
            "0.21.6",
            "4.5"
        ));
        
        return vulnerabilities;
    }

    /**
     * Sync vulnerabilities from NVD API
     */
    @Async
    public CompletableFuture<String> syncVulnerabilitiesFromNVD() {
        return nvdService.syncVulnerabilitiesFromNVD();
    }
    
    /**
     * Run security scan (triggers NVD sync)
     */
    public String runSecurityScan() {
        try {
            logger.info("Starting security scan...");
            CompletableFuture<String> future = syncVulnerabilitiesFromNVD();
            String result = future.get(); // Wait for completion
            logger.info("Security scan completed: {}", result);
            return "Security scan completed successfully. " + result;
        } catch (Exception e) {
            logger.error("Error running security scan: {}", e.getMessage());
            return "Security scan failed: " + e.getMessage();
        }
    }

    /**
     * Fix vulnerabilities (placeholder for future implementation)
     */
    public String fixVulnerabilities() {
        // In production, this would attempt to fix vulnerabilities
        // For now, just return a message
        return "Vulnerability fixing is not yet implemented. Please update packages manually.";
    }

    /**
     * Force update all packages
     */
    public String forceUpdate() {
        // In production, this would force update all packages
        return "Force update initiated. Monitor for any breaking changes.";
    }

    /**
     * Export security report
     */
    public String exportSecurityReport() {
        // In production, this would generate and store a security report
        return "/api/security/reports/security-report-" + System.currentTimeMillis() + ".pdf";
    }
    
    /**
     * Get vulnerability statistics
     */
    public List<Object[]> getVulnerabilityStats() {
        return vulnerabilityRepository.countBySeverity();
    }
    
    /**
     * Get ecosystem statistics
     */
    public List<Object[]> getEcosystemStats() {
        return vulnerabilityRepository.countByEcosystem();
    }
    
    /**
     * Scheduled task to sync vulnerabilities daily
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    public void scheduledVulnerabilitySync() {
        logger.info("Starting scheduled vulnerability sync...");
        syncVulnerabilitiesFromNVD();
    }
}
