package com.example.microsponsoringbackend.service;

import java.io.File;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.microsponsoringbackend.model.SystemMetrics;

// Remove this import:
// import ch.qos.logback.core.util.SystemInfo;

// Add these OSHI imports:
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;

@Service
public class SystemMetricsService {
    
    private final SystemInfo systemInfo = new SystemInfo();
    private NetworkIF previousNetworkInterface;
    private long previousNetworkTime;
    private long previousNetworkBytes;
    
    public SystemMetrics getSystemMetrics() {
        SystemMetrics metrics = new SystemMetrics();
        
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        
        // CPU Usage
        metrics.setCpu(getCpuUsage(hardware));
        
        // Memory Usage
        metrics.setMemory(getMemoryUsage(hardware));
        
        // Disk Usage
        metrics.setDisk(getDiskUsage(hardware));
        
        // Network Usage
        metrics.setNetwork(getNetworkUsage(hardware));
        
        // System Info
        metrics.setUptime(getSystemUptime());
        metrics.setLastRestart(getLastRestartTime());
        
        return metrics;
    }
    
    private double getCpuUsage(HardwareAbstractionLayer hardware) {
        CentralProcessor processor = hardware.getProcessor();
        
        // Get CPU ticks and wait a second for accurate measurement
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0.0;
        }
        
        long[] ticks = processor.getSystemCpuLoadTicks();
        long total = 0;
        for (int i = 0; i < ticks.length; i++) {
            total += ticks[i] - prevTicks[i];
        }
        
        // Calculate CPU usage
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - 
                   prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        
        if (total > 0) {
            double cpuUsage = (1.0 - (double) idle / total) * 100;
            return Math.round(cpuUsage * 100.0) / 100.0;
        }
        
        return 0.0;
    }
    
    private double getMemoryUsage(HardwareAbstractionLayer hardware) {
        GlobalMemory memory = hardware.getMemory();
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;
        
        if (totalMemory > 0) {
            double memoryUsage = (double) usedMemory / totalMemory * 100;
            return Math.round(memoryUsage * 100.0) / 100.0;
        }
        
        return 0.0;
    }
    
    private double getDiskUsage(HardwareAbstractionLayer hardware) {
        List<HWDiskStore> diskStores = hardware.getDiskStores();
        if (diskStores.isEmpty()) {
            return 0.0;
        }
        
        long totalSpace = 0;
        long usedSpace = 0;
        
        // Use the operating system's file system to get disk usage
        List<OSFileStore> fileStores = systemInfo.getOperatingSystem().getFileSystem().getFileStores();
        for (OSFileStore fs : fileStores) {
            totalSpace += fs.getTotalSpace();
            usedSpace += (fs.getTotalSpace() - fs.getFreeSpace());
        }
        
        if (totalSpace > 0) {
            double diskUsage = (double) usedSpace / totalSpace * 100;
            return Math.round(diskUsage * 100.0) / 100.0;
        }
        
        return 0.0;
    }
    
    private double getNetworkUsage(HardwareAbstractionLayer hardware) {
        List<NetworkIF> networkIFs = hardware.getNetworkIFs();
        if (networkIFs.isEmpty()) {
            return 0.0;
        }
        
        // Refresh network stats
        networkIFs.get(0).updateAttributes();
        NetworkIF networkInterface = networkIFs.get(0);
        
        long currentTime = System.currentTimeMillis();
        long currentBytes = networkInterface.getBytesRecv() + networkInterface.getBytesSent();
        
        if (previousNetworkInterface != null && previousNetworkTime > 0) {
            long timeDiff = currentTime - previousNetworkTime;
            long bytesDiff = currentBytes - previousNetworkBytes;
            
            if (timeDiff > 0) {
                // Calculate bytes per second
                double bytesPerSecond = (double) bytesDiff / (timeDiff / 1000.0);
                
                // Convert to percentage (assuming 1 Gbps max speed)
                double maxSpeedBytes = 125000000; // 1 Gbps in bytes per second
                double networkUsage = Math.min((bytesPerSecond / maxSpeedBytes) * 100, 100);
                
                previousNetworkTime = currentTime;
                previousNetworkBytes = currentBytes;
                
                return Math.round(networkUsage * 100.0) / 100.0;
            }
        }
        
        previousNetworkInterface = networkInterface;
        previousNetworkTime = currentTime;
        previousNetworkBytes = currentBytes;
        
        return 0.0;
    }
    
    private String getSystemUptime() {
        long uptimeSeconds = systemInfo.getOperatingSystem().getSystemUptime();
        return formatUptime(uptimeSeconds);
    }
    
    private String getLastRestartTime() {
        long bootTime = systemInfo.getOperatingSystem().getSystemBootTime();
        return Instant.ofEpochSecond(bootTime).toString();
    }
    
    private String formatUptime(long seconds) {
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        
        return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
    }
}