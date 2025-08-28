package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.dto.SecurityVulnerabilityDto;
import com.example.microsponsoringbackend.dto.SecurityDashboardDto;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityService {

    public SecurityDashboardDto getSecurityDashboard() {
        List<SecurityVulnerabilityDto> vulnerabilities = getVulnerabilities();
        
        int criticalCount = (int) vulnerabilities.stream().filter(v -> "critical".equals(v.getSeverity())).count();
        int highCount = (int) vulnerabilities.stream().filter(v -> "high".equals(v.getSeverity())).count();
        int moderateCount = (int) vulnerabilities.stream().filter(v -> "moderate".equals(v.getSeverity())).count();
        int lowCount = (int) vulnerabilities.stream().filter(v -> "low".equals(v.getSeverity())).count();
        
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
        
        // Sample vulnerabilities - in production, this would come from actual security scans
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

    public String runSecurityScan() {
        // In production, this would run actual security tools like:
        // - npm audit for frontend
        // - OWASP dependency check for backend
        // - Trivy for Docker images
        
        try {
            // Simulate running security scan
            Thread.sleep(2000);
            return "Security scan completed successfully. Found " + getVulnerabilities().size() + " vulnerabilities.";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Security scan interrupted.";
        }
    }

    public String fixVulnerabilities() {
        // In production, this would attempt to fix vulnerabilities
        return "Attempting to fix safe vulnerabilities... This may take several minutes.";
    }

    public String forceUpdate() {
        // In production, this would force update all packages
        return "Force update initiated. Monitor for any breaking changes.";
    }

    public String exportSecurityReport() {
        // In production, this would generate and store a security report
        return "/api/security/reports/security-report-" + System.currentTimeMillis() + ".pdf";
    }
} 