package com.example.microsponsoringbackend.model;

public class SystemMetrics {
    private double cpu;
    private double memory;
    private double disk;
    private double network;
    private String uptime;
    private String lastRestart;
    
    // Constructors
    public SystemMetrics() {}
    
    public SystemMetrics(double cpu, double memory, double disk, double network, String uptime, String lastRestart) {
        this.cpu = cpu;
        this.memory = memory;
        this.disk = disk;
        this.network = network;
        this.uptime = uptime;
        this.lastRestart = lastRestart;
    }
    
    // Getters and Setters
    public double getCpu() { return cpu; }
    public void setCpu(double cpu) { this.cpu = cpu; }
    
    public double getMemory() { return memory; }
    public void setMemory(double memory) { this.memory = memory; }
    
    public double getDisk() { return disk; }
    public void setDisk(double disk) { this.disk = disk; }
    
    public double getNetwork() { return network; }
    public void setNetwork(double network) { this.network = network; }
    
    public String getUptime() { return uptime; }
    public void setUptime(String uptime) { this.uptime = uptime; }
    
    public String getLastRestart() { return lastRestart; }
    public void setLastRestart(String lastRestart) { this.lastRestart = lastRestart; }
    
    @Override
    public String toString() {
        return String.format(
            "SystemMetrics{cpu=%.1f%%, memory=%.1f%%, disk=%.1f%%, network=%.1f%%, uptime='%s', lastRestart='%s'}",
            cpu, memory, disk, network, uptime, lastRestart
        );
    }
}