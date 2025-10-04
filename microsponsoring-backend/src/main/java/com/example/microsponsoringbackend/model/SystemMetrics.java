package com.example.microsponsoringbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemMetrics {
    private double cpu;
    private double memory;
    private double disk;
    private double network;
    private String uptime;
    private String lastRestart;
    
    // Add these missing fields
    private long maxMemoryMB;
    private long memoryUsageMB;
    private double memoryUsagePercent;
    private int threadCount;
    private long uptimeMinutes;
   
}