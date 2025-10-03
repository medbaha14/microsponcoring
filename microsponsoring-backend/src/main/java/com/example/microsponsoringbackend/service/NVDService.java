package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.Vulnerability;
import com.example.microsponsoringbackend.repository.VulnerabilityRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class NVDService {
    
    private static final Logger logger = LoggerFactory.getLogger(NVDService.class);
    private static final String NVD_API_BASE_URL = "https://services.nvd.nist.gov/rest/json/cves/2.0";
    private static final String NVD_API_KEY = "YOUR_NVD_API_KEY"; // Get from environment variable
    
    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Fetch vulnerabilities from NVD API for specific packages
     */
    @Async
    public CompletableFuture<List<Vulnerability>> fetchVulnerabilitiesForPackages(List<String> packageNames) {
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        
        for (String packageName : packageNames) {
            try {
                List<Vulnerability> packageVulns = fetchVulnerabilitiesForPackage(packageName);
                vulnerabilities.addAll(packageVulns);
                
                // Save to database
                for (Vulnerability vuln : packageVulns) {
                    if (!vulnerabilityRepository.existsByCveId(vuln.getCveId())) {
                        vulnerabilityRepository.save(vuln);
                    }
                }
                
                // Rate limiting - NVD allows 5 requests per 30 seconds
                Thread.sleep(7000);
                
            } catch (Exception e) {
                logger.error("Error fetching vulnerabilities for package {}: {}", packageName, e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(vulnerabilities);
    }
    
    /**
     * Fetch vulnerabilities for a specific package from NVD
     */
    @Cacheable(value = "nvd-vulnerabilities", key = "#packageName")
    public List<Vulnerability> fetchVulnerabilitiesForPackage(String packageName) {
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        
        try {
            String url = NVD_API_BASE_URL + "?keywordSearch=" + packageName + "&resultsPerPage=50";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Microsponsoring-Security-Scanner/1.0");
            if (!NVD_API_KEY.equals("YOUR_NVD_API_KEY")) {
                headers.set("apiKey", NVD_API_KEY);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode vulnerabilitiesNode = root.path("vulnerabilities");
                
                for (JsonNode vulnNode : vulnerabilitiesNode) {
                    Vulnerability vuln = parseVulnerabilityFromNVD(vulnNode, packageName);
                    if (vuln != null) {
                        vulnerabilities.add(vuln);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error fetching NVD data for package {}: {}", packageName, e.getMessage());
        }
        
        return vulnerabilities;
    }
    
    /**
     * Parse NVD JSON response into Vulnerability entity
     */
    private Vulnerability parseVulnerabilityFromNVD(JsonNode vulnNode, String packageName) {
        try {
            JsonNode cve = vulnNode.path("cve");
            String cveId = cve.path("id").asText();
            
            // Get description
            String description = "";
            JsonNode descriptions = cve.path("descriptions");
            if (descriptions.isArray() && descriptions.size() > 0) {
                for (JsonNode desc : descriptions) {
                    if ("en".equals(desc.path("lang").asText())) {
                        description = desc.path("value").asText();
                        break;
                    }
                }
            }
            
            // Get CVSS score and severity
            String severity = "LOW";
            Double cvssScore = 0.0;
            String cvssVector = "";
            
            JsonNode metrics = cve.path("metrics");
            if (metrics.has("cvssMetricV31")) {
                JsonNode cvss31 = metrics.path("cvssMetricV31").get(0);
                JsonNode cvssData = cvss31.path("cvssData");
                cvssScore = cvssData.path("baseScore").asDouble();
                cvssVector = cvssData.path("vectorString").asText();
                severity = mapCvssScoreToSeverity(cvssScore);
            } else if (metrics.has("cvssMetricV30")) {
                JsonNode cvss30 = metrics.path("cvssMetricV30").get(0);
                JsonNode cvssData = cvss30.path("cvssData");
                cvssScore = cvssData.path("baseScore").asDouble();
                cvssVector = cvssData.path("vectorString").asText();
                severity = mapCvssScoreToSeverity(cvssScore);
            } else if (metrics.has("cvssMetricV2")) {
                JsonNode cvss2 = metrics.path("cvssMetricV2").get(0);
                JsonNode cvssData = cvss2.path("cvssData");
                cvssScore = cvssData.path("baseScore").asDouble();
                cvssVector = cvssData.path("vectorString").asText();
                severity = mapCvssScoreToSeverity(cvssScore);
            }
            
            // Get published and last modified dates
            LocalDateTime publishedDate = null;
            LocalDateTime lastModifiedDate = null;
            
            String publishedStr = cve.path("published").asText();
            if (!publishedStr.isEmpty()) {
                publishedDate = LocalDateTime.parse(publishedStr.substring(0, 19));
            }
            
            String lastModifiedStr = cve.path("lastModified").asText();
            if (!lastModifiedStr.isEmpty()) {
                lastModifiedDate = LocalDateTime.parse(lastModifiedStr.substring(0, 19));
            }
            
            // Get references
            StringBuilder references = new StringBuilder();
            JsonNode referencesNode = cve.path("references");
            if (referencesNode.isArray()) {
                for (JsonNode ref : referencesNode) {
                    if (references.length() > 0) references.append("; ");
                    references.append(ref.path("url").asText());
                }
            }
            
            Vulnerability vulnerability = new Vulnerability(
                cveId,
                cve.path("id").asText(),
                description,
                severity,
                cvssScore,
                packageName,
                "NVD"
            );
            
            vulnerability.setCvssVector(cvssVector);
            vulnerability.setPublishedDate(publishedDate);
            vulnerability.setLastModifiedDate(lastModifiedDate);
            vulnerability.setReferences(references.toString());
            vulnerability.setSource("NVD");
            
            return vulnerability;
            
        } catch (Exception e) {
            logger.error("Error parsing NVD vulnerability: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Map CVSS score to severity level
     */
    private String mapCvssScoreToSeverity(Double score) {
        if (score >= 9.0) return "CRITICAL";
        if (score >= 7.0) return "HIGH";
        if (score >= 4.0) return "MEDIUM";
        return "LOW";
    }
    
    /**
     * Get known vulnerable packages for our project
     */
    public List<String> getKnownVulnerablePackages() {
        List<String> packages = new ArrayList<>();
        
        // Frontend packages (NPM)
        packages.add("angular");
        packages.add("typescript");
        packages.add("rxjs");
        packages.add("zone.js");
        packages.add("webpack");
        packages.add("esbuild");
        
        // Backend packages (Maven)
        packages.add("spring-boot");
        packages.add("spring-security");
        packages.add("mysql-connector-java");
        packages.add("jackson-databind");
        packages.add("logback");
        packages.add("hibernate");
        
        // Docker base images
        packages.add("openjdk");
        packages.add("node");
        packages.add("nginx");
        packages.add("alpine");
        
        return packages;
    }
    
    /**
     * Sync vulnerabilities from NVD
     */
    @Async
    public CompletableFuture<String> syncVulnerabilitiesFromNVD() {
        try {
            List<String> packages = getKnownVulnerablePackages();
            CompletableFuture<List<Vulnerability>> future = fetchVulnerabilitiesForPackages(packages);
            List<Vulnerability> vulnerabilities = future.get();
            
            logger.info("Synced {} vulnerabilities from NVD", vulnerabilities.size());
            return CompletableFuture.completedFuture("Successfully synced " + vulnerabilities.size() + " vulnerabilities from NVD");
            
        } catch (Exception e) {
            logger.error("Error syncing vulnerabilities from NVD: {}", e.getMessage());
            return CompletableFuture.completedFuture("Error syncing vulnerabilities: " + e.getMessage());
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
}
