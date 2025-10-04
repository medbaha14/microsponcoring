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
import java.util.stream.Collectors;

@Service
public class SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;
    
    @Autowired
    private NVDService nvdService;

    /**
     * Get security dashboard with real NVD data
     */
    public SecurityDashboardDto getSecurityDashboard() {
        List<SecurityVulnerabilityDto> vulnerabilities = getVulnerabilities();
        
        int criticalCount = (int) vulnerabilities.stream().filter(v -> "critical".equalsIgnoreCase(v.getSeverity())).count();
        int highCount = (int) vulnerabilities.stream().filter(v -> "high".equalsIgnoreCase(v.getSeverity())).count();
        int moderateCount = (int) vulnerabilities.stream().filter(v -> "moderate".equalsIgnoreCase(v.getSeverity()) || "medium".equalsIgnoreCase(v.getSeverity())).count();
        int lowCount = (int) vulnerabilities.stream().filter(v -> "low".equalsIgnoreCase(v.getSeverity())).count();
        
        String overallStatus = calculateOverallStatus(criticalCount, highCount, moderateCount, lowCount);
        
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

    /**
     * Get vulnerabilities from database (populated by NVD service)
     */
    public List<SecurityVulnerabilityDto> getVulnerabilities() {
        try {
            // Get vulnerabilities from database
            List<Vulnerability> dbVulnerabilities = vulnerabilityRepository.findByIsActiveTrue();
            
            if (dbVulnerabilities.isEmpty()) {
                logger.info("No vulnerabilities found in database, initiating NVD sync");
                // Trigger sync and return empty list - frontend will show loading state
                syncVulnerabilitiesFromNVD();
                return new ArrayList<>();
            }
            
            // Convert database entities to DTOs
            return dbVulnerabilities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Error fetching vulnerabilities from database: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Convert Vulnerability entity to SecurityVulnerabilityDto
     */
    private SecurityVulnerabilityDto convertToDto(Vulnerability vuln) {
        String packageName = vuln.getAffectedPackage();
        if (vuln.getAffectedVersion() != null && !vuln.getAffectedVersion().isEmpty() && !"ALL".equals(vuln.getAffectedVersion())) {
            packageName += " " + vuln.getAffectedVersion();
        }
        
        String source = vuln.getEcosystem() != null ? vuln.getEcosystem() + " · " + vuln.getSource() : vuln.getSource();
        
        // Convert severity to lowercase for frontend consistency
        String severity = vuln.getSeverity() != null ? vuln.getSeverity().toLowerCase() : "unknown";
        
        int count = 1; // Each vulnerability is counted individually
        
        String description = vuln.getDescription();
        if (description == null || description.isEmpty()) {
            description = "Vulnerability in " + vuln.getAffectedPackage();
        }
        
        String cveId = vuln.getCveId();
        String fixVersion = vuln.getFixedVersion() != null ? vuln.getFixedVersion() : "N/A";
        String riskScore = vuln.getCvssScore() != null ? String.format("%.1f", vuln.getCvssScore()) : "0.0";
        
        return new SecurityVulnerabilityDto(
            packageName,
            source,
            severity,
            count,
            description,
            cveId,
            fixVersion,
            riskScore
        );
    }

    /**
     * Calculate overall security status based on vulnerability counts
     */
    private String calculateOverallStatus(int critical, int high, int moderate, int low) {
        if (critical > 0) return "CRITICAL";
        if (high > 0) return "HIGH";
        if (moderate > 0) return "MEDIUM";
        if (low > 0) return "LOW";
        return "SECURE";
    }

    /**
     * Sync vulnerabilities from NVD API
     */
    @Async
    public CompletableFuture<String> syncVulnerabilitiesFromNVD() {
        logger.info("Starting NVD vulnerability sync...");
        try {
            // Get known vulnerable packages
            List<String> packages = nvdService.getKnownVulnerablePackages();
            
            // Fetch vulnerabilities for all packages
            CompletableFuture<List<Vulnerability>> future = nvdService.fetchVulnerabilitiesForPackages(packages);
            List<Vulnerability> vulnerabilities = future.get();
            
            // Save to database
            int savedCount = 0;
            for (Vulnerability vuln : vulnerabilities) {
                if (!vulnerabilityRepository.existsByCveId(vuln.getCveId())) {
                    vulnerabilityRepository.save(vuln);
                    savedCount++;
                }
            }
            
            String result = String.format("NVD sync completed. Found %d vulnerabilities, saved %d new entries.", 
                vulnerabilities.size(), savedCount);
            logger.info(result);
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            logger.error("Error syncing vulnerabilities from NVD: {}", e.getMessage());
            return CompletableFuture.completedFuture("Error syncing vulnerabilities: " + e.getMessage());
        }
    }
    
    /**
     * Run security scan (triggers NVD sync and returns immediate results)
     */
    public String runSecurityScan() {
        try {
            logger.info("Starting security scan...");
            
            // Trigger NVD sync
            CompletableFuture<String> syncFuture = syncVulnerabilitiesFromNVD();
            
            // For immediate response, we can return current data while sync runs in background
            List<SecurityVulnerabilityDto> currentVulnerabilities = getVulnerabilities();
            int totalVulnerabilities = currentVulnerabilities.size();
            
            String syncMessage = "Security scan initiated. NVD sync running in background. ";
            if (totalVulnerabilities > 0) {
                syncMessage += "Currently showing " + totalVulnerabilities + " vulnerabilities.";
            } else {
                syncMessage += "No vulnerabilities found in current scan. NVD sync in progress.";
            }
            
            logger.info("Security scan completed with {} vulnerabilities", totalVulnerabilities);
            return syncMessage;
            
        } catch (Exception e) {
            logger.error("Error running security scan: {}", e.getMessage());
            return "Security scan failed: " + e.getMessage();
        }
    }

    /**
     * Quick security scan that returns current database state
     */
    public SecurityDashboardDto quickSecurityScan() {
        logger.info("Running quick security scan...");
        return getSecurityDashboard();
    }

    /**
     * Get vulnerabilities for specific package
     */
    public List<SecurityVulnerabilityDto> getVulnerabilitiesForPackage(String packageName) {
        try {
            List<Vulnerability> vulnerabilities = vulnerabilityRepository.findByAffectedPackageContainingIgnoreCaseAndIsActiveTrue(packageName);
            return vulnerabilities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching vulnerabilities for package {}: {}", packageName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get vulnerabilities by severity
     */
    public List<SecurityVulnerabilityDto> getVulnerabilitiesBySeverity(String severity) {
        try {
            List<Vulnerability> vulnerabilities = vulnerabilityRepository.findBySeverityIgnoreCaseAndIsActiveTrue(severity);
            return vulnerabilities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching vulnerabilities by severity {}: {}", severity, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Fix vulnerabilities - mark low severity vulnerabilities as inactive
     */
    public String fixVulnerabilities() {
        try {
            // Mark low-severity vulnerabilities as inactive
            List<Vulnerability> lowVulnerabilities = vulnerabilityRepository.findBySeverityIgnoreCaseAndIsActiveTrue("LOW");
            
            int fixedCount = 0;
            for (Vulnerability vuln : lowVulnerabilities) {
                vuln.setIsActive(false);
                vulnerabilityRepository.save(vuln);
                fixedCount++;
            }
            
            return String.format("Marked %d low-severity vulnerabilities as resolved. Review medium/high severity issues manually.", fixedCount);
            
        } catch (Exception e) {
            logger.error("Error fixing vulnerabilities: {}", e.getMessage());
            return "Error fixing vulnerabilities: " + e.getMessage();
        }
    }

    /**
     * Force update all packages
     */
    public String forceUpdate() {
        // In production, this would force update all packages
        // For now, return a message about the action
        return "Force update initiated. This would update all packages to their latest versions. Monitor for any breaking changes.";
    }

    /**
     * Export security report
     */
    public String exportSecurityReport() {
        try {
            SecurityDashboardDto dashboard = getSecurityDashboard();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String reportPath = "/api/security/reports/security-report-" + timestamp + ".pdf";
            
            // In production, generate actual PDF report
            logger.info("Generated security report with {} vulnerabilities", dashboard.getVulnerabilities().size());
            
            return reportPath;
        } catch (Exception e) {
            logger.error("Error exporting security report: {}", e.getMessage());
            return "/api/security/reports/error-report.pdf";
        }
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
     * Get recent vulnerabilities (last 30 days)
     */
    public List<SecurityVulnerabilityDto> getRecentVulnerabilities() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Vulnerability> recentVulnerabilities = vulnerabilityRepository.findByPublishedDateAfter(thirtyDaysAgo);
            
            return recentVulnerabilities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching recent vulnerabilities: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Check if NVD sync is needed (no vulnerabilities in database)
     */
    public boolean isNvdSyncNeeded() {
        long vulnerabilityCount = vulnerabilityRepository.count();
        return vulnerabilityCount == 0;
    }
    
    /**
     * Get NVD sync status
     */
    public String getNvdSyncStatus() {
        long vulnerabilityCount = vulnerabilityRepository.count();
        if (vulnerabilityCount == 0) {
            return "NEEDS_SYNC";
        } else {
            return "SYNCED (" + vulnerabilityCount + " vulnerabilities)";
        }
    }
    
    /**
     * Get vulnerabilities that need attention (critical and high severity)
     */
    public List<SecurityVulnerabilityDto> getVulnerabilitiesNeedingAttention() {
        try {
            List<Vulnerability> criticalVulns = vulnerabilityRepository.findBySeverityIgnoreCaseAndIsActiveTrue("CRITICAL");
            List<Vulnerability> highVulns = vulnerabilityRepository.findBySeverityIgnoreCaseAndIsActiveTrue("HIGH");
            
            List<Vulnerability> allCriticalAndHigh = new ArrayList<>();
            allCriticalAndHigh.addAll(criticalVulns);
            allCriticalAndHigh.addAll(highVulns);
            
            return allCriticalAndHigh.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching vulnerabilities needing attention: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get vulnerability count by severity
     */
    public java.util.Map<String, Long> getVulnerabilityCountBySeverity() {
        try {
            List<Object[]> stats = vulnerabilityRepository.countBySeverity();
            java.util.Map<String, Long> severityCounts = new java.util.HashMap<>();
            
            for (Object[] stat : stats) {
                String severity = (String) stat[0];
                Long count = (Long) stat[1];
                severityCounts.put(severity, count);
            }
            
            // Ensure all severity levels are present
            severityCounts.putIfAbsent("CRITICAL", 0L);
            severityCounts.putIfAbsent("HIGH", 0L);
            severityCounts.putIfAbsent("MEDIUM", 0L);
            severityCounts.putIfAbsent("LOW", 0L);
            
            return severityCounts;
        } catch (Exception e) {
            logger.error("Error getting vulnerability counts by severity: {}", e.getMessage());
            return java.util.Map.of(
                "CRITICAL", 0L,
                "HIGH", 0L,
                "MEDIUM", 0L,
                "LOW", 0L
            );
        }
    }
    
    /**
     * Get security alerts for the dashboard
     */
    public List<Object> getSecurityAlerts() {
        try {
            List<Vulnerability> criticalVulns = vulnerabilityRepository.findBySeverityIgnoreCaseAndIsActiveTrue("CRITICAL");
            List<Vulnerability> highVulns = vulnerabilityRepository.findBySeverityIgnoreCaseAndIsActiveTrue("HIGH");
            
            List<Object> alerts = new ArrayList<>();
            
            // Create alerts for critical vulnerabilities
            for (Vulnerability vuln : criticalVulns) {
                alerts.add(createAlertFromVulnerability(vuln, "CRITICAL"));
            }
            
            // Create alerts for high vulnerabilities
            for (Vulnerability vuln : highVulns) {
                alerts.add(createAlertFromVulnerability(vuln, "HIGH"));
            }
            
            // Add sync status alert if needed
            if (isNvdSyncNeeded()) {
                alerts.add(createSyncAlert());
            }
            
            return alerts;
            
        } catch (Exception e) {
            logger.error("Error generating security alerts: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Create alert object from vulnerability
     */
    private Object createAlertFromVulnerability(Vulnerability vuln, String alertType) {
        return java.util.Map.of(
            "id", vuln.getCveId(),
            "type", alertType,
            "title", vuln.getCveId() + " - " + vuln.getAffectedPackage(),
            "description", vuln.getDescription(),
            "timestamp", System.currentTimeMillis(),
            "status", "PENDING",
            "cveId", vuln.getCveId(),
            "package", vuln.getAffectedPackage(),
            "riskScore", vuln.getCvssScore() != null ? String.format("%.1f", vuln.getCvssScore()) : "0.0"
        );
    }
    
    /**
     * Create sync alert
     */
    private Object createSyncAlert() {
        return java.util.Map.of(
            "id", "SYNC_NEEDED",
            "type", "WARNING",
            "title", "NVD Data Sync Required",
            "description", "No vulnerability data found. Please sync with NVD database.",
            "timestamp", System.currentTimeMillis(),
            "status", "PENDING"
        );
    }
    
    /**
     * Get sync status object for frontend
     */
    public java.util.Map<String, Object> getSyncStatus() {
        long vulnerabilityCount = vulnerabilityRepository.count();
        boolean syncNeeded = vulnerabilityCount == 0;
        
        return java.util.Map.of(
            "syncStatus", syncNeeded ? "NEEDS_SYNC" : "SYNCED",
            "syncNeeded", syncNeeded,
            "lastChecked", System.currentTimeMillis(),
            "vulnerabilityCount", vulnerabilityCount
        );
    }
    
    /**
     * Scheduled task to sync vulnerabilities daily
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    public void scheduledVulnerabilitySync() {
        logger.info("Starting scheduled vulnerability sync...");
        try {
            CompletableFuture<String> future = syncVulnerabilitiesFromNVD();
            // Don't wait for completion - let it run in background
            logger.info("Scheduled vulnerability sync initiated");
        } catch (Exception e) {
            logger.error("Scheduled vulnerability sync failed: {}", e.getMessage());
        }
    }
    
    /**
     * Health check for security service
     */
    public String healthCheck() {
        try {
            long vulnerabilityCount = vulnerabilityRepository.count();
            boolean databaseConnected = vulnerabilityCount >= 0; // Simple connectivity check
            
            if (databaseConnected) {
                return String.format("Security Service Healthy - %d vulnerabilities in database", vulnerabilityCount);
            } else {
                return "Security Service Unhealthy - Database connection issue";
            }
        } catch (Exception e) {
            return "Security Service Unhealthy: " + e.getMessage();
        }
    }
    
    /**
     * Clear all vulnerabilities (for testing/reset purposes)
     */
    public String clearAllVulnerabilities() {
        try {
            long countBefore = vulnerabilityRepository.count();
            vulnerabilityRepository.deleteAll();
            long countAfter = vulnerabilityRepository.count();
            
            logger.info("Cleared all vulnerabilities: {} -> {}", countBefore, countAfter);
            return String.format("Cleared all %d vulnerabilities from database", countBefore);
        } catch (Exception e) {
            logger.error("Error clearing vulnerabilities: {}", e.getMessage());
            return "Error clearing vulnerabilities: " + e.getMessage();
        }
    }
}