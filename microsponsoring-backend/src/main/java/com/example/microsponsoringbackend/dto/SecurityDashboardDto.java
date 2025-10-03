package com.example.microsponsoringbackend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityDashboardDto {
    private List<SecurityVulnerabilityDto> vulnerabilities;
    private int criticalCount;
    private int highCount;
    private int moderateCount;
    private int lowCount;
    private String lastUpdate;
    private String nextScan;
    private String overallStatus;
}
