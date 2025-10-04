import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { buildInfo } from '../../../../environments/build-info';
import { environment } from '../../../../environments/environment';
import { TokenHandler } from '../../../services/token-handler';

interface SecurityVulnerability {
  packageName: string;
  source: string;
  severity: string;
  count: number;
  description: string;
  cveId: string;
  fixVersion: string;
  riskScore: string;
}

interface SecurityDashboard {
  vulnerabilities: SecurityVulnerability[];
  criticalCount: number;
  highCount: number;
  moderateCount: number;
  lowCount: number;
  lastUpdate: string;
  nextScan: string;
  overallStatus: string;
}

interface SystemMetrics {
  cpu: number;
  memory: number;
  disk: number;
  network: number;
  uptime: string;
  lastRestart: string;
  maxMemoryMB: number;
  memoryUsageMB: number;
  memoryUsagePercent: number;
  threadCount: number;
  uptimeMinutes: number;
}

interface UserStats {
  activeUsers: number;
  totalUsers: number;
  recentLogins: LoginAttempt[];
}

interface LoginAttempt {
  username: string;
  timestamp: string;
  success: boolean;
  ipAddress: string;
  userAgent: string;
}

interface BuildInfo {
  version: string;
  buildTime: string;
  environment: string;
  lastUpdate: string;
}

interface AlertItem {
  id: string;
  type: string;
  title: string;
  description: string;
  timestamp: number;
  status: string;
  cveId?: string;
  package?: string;
  riskScore?: string;
}

interface SyncStatus {
  syncStatus: string;
  syncNeeded: boolean;
  lastChecked: number;
}

interface SecurityStats {
  vulnerabilityCounts: {
    critical: number;
    high: number;
    moderate: number;
    low: number;
    total: number;
  };
  overallStatus: string;
  lastUpdate: string;
  vulnerabilityStats: any[];
  ecosystemStats: any[];
  syncStatus: string;
}

@Component({
  selector: 'app-security-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './security-dashboard.component.html',
  styleUrls: ['./security-dashboard.component.css']
})
export class SecurityDashboardComponent implements OnInit, OnDestroy {
  environment = environment;
  
  // Security data
  vulnerabilities: SecurityVulnerability[] = [];
  loading = false;
  lastUpdate = '';
  nextScan = '';
  criticalCount = 0;
  highCount = 0;
  moderateCount = 0;
  lowCount = 0;
  overallStatus = '';

  // System metrics
  systemMetrics: SystemMetrics = {
    cpu: 0,
    memory: 0,
    disk: 0,
    network: 0,
    uptime: 'Loading...',
    lastRestart: 'Loading...',
    maxMemoryMB: 0,
    memoryUsageMB: 0,
    memoryUsagePercent: 0,
    threadCount: 0,
    uptimeMinutes: 0
  };

  // User statistics
  userStats: UserStats = {
    activeUsers: 0,
    totalUsers: 0,
    recentLogins: []
  };

  // Build information
  buildInfo: BuildInfo = {
    version: '1.0.0',
    buildTime: '',
    environment: 'development',
    lastUpdate: ''
  };

  // Dynamic alerts from backend
  pendingAlerts: AlertItem[] = [];

  // Sync status
  syncStatus: SyncStatus = {
    syncStatus: 'UNKNOWN',
    syncNeeded: true,
    lastChecked: 0
  };

  // Auto-refresh intervals
  private refreshInterval: any;
  private metricsRefreshInterval: any;

  // Connection status
  backendStatus: 'online' | 'offline' | 'checking' = 'checking';
  lastSuccessfulUpdate: Date | null = null;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    if (!this.checkAuthentication()) {
      return;
    }
    
    console.log('Security Dashboard initialized');
    console.log('API URL:', this.getApiUrl());
    
    this.loadAllData();
    this.startAutoRefresh();
    this.loadBuildInfo();
    this.startMetricsAutoRefresh();
    this.loadSyncStatus();
  }

  ngOnDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
    if (this.metricsRefreshInterval) {
      clearInterval(this.metricsRefreshInterval);
    }
  }

  loadAllData() {
    this.loading = true;
    this.backendStatus = 'checking';
    
    Promise.all([
      this.loadSecurityData(),
      this.loadSystemMetrics(),
      this.loadUserStats(),
      this.loadPendingAlerts(),
      this.loadSyncStatus()
    ]).finally(() => {
      this.loading = false;
      this.lastSuccessfulUpdate = new Date();
    });
  }

  loadSecurityData(): Promise<void> {
    return new Promise((resolve) => {
      const apiUrl = this.getApiUrl();
      // Change from /security/dashboard to /vulnerabilities
      this.http.get<SecurityVulnerability[]>(`${apiUrl}/security/vulnerabilities`)
        .subscribe({
          next: (data) => {
            // Transform the response to match your interface
            this.vulnerabilities = Array.isArray(data) ? data : [];
            
            // Calculate counts based on severity
            this.criticalCount = this.vulnerabilities.filter(v => 
              v.severity?.toLowerCase() === 'critical').length;
            this.highCount = this.vulnerabilities.filter(v => 
              v.severity?.toLowerCase() === 'high').length;
            this.moderateCount = this.vulnerabilities.filter(v => 
              v.severity?.toLowerCase() === 'moderate' || v.severity?.toLowerCase() === 'medium').length;
            this.lowCount = this.vulnerabilities.filter(v => 
              v.severity?.toLowerCase() === 'low').length;
            
            this.lastUpdate = new Date().toISOString();
            this.overallStatus = this.calculateOverallStatus();
            this.backendStatus = 'online';
            resolve();
          },
          error: (error) => {
            console.error('Error loading security data:', error);
            this.backendStatus = 'offline';
            resolve();
          }
        });
    });
  }
  
  // Add this helper method to calculate overall status
  private calculateOverallStatus(): string {
    if (this.criticalCount > 0) return 'Critical';
    if (this.highCount > 0) return 'High';
    if (this.moderateCount > 0) return 'Moderate';
    if (this.lowCount > 0) return 'Low';
    return 'Healthy';
  }

  loadSystemMetrics(): Promise<void> {
    return new Promise((resolve) => {
      const apiUrl = this.getApiUrl();
      this.http.get<any>(`${apiUrl}/system/metrics`)
        .subscribe({
          next: (data) => {
            // Calculate missing fields if not provided by backend
            const runtime = this.calculateRuntimeMetrics();
            
            this.systemMetrics = {
              cpu: this.sanitizeMetric(data?.cpu),
              memory: this.sanitizeMetric(data?.memory),
              disk: this.sanitizeMetric(data?.disk),
              network: this.sanitizeMetric(data?.network),
              uptime: data?.uptime || 'Unknown',
              lastRestart: data?.lastRestart || new Date().toISOString(),
              
              // Use backend data or calculate locally
              maxMemoryMB: data?.maxMemoryMB || runtime.maxMemoryMB,
              memoryUsageMB: data?.memoryUsageMB || runtime.memoryUsageMB,
              memoryUsagePercent: this.sanitizeMetric(
                data?.memoryUsagePercent || runtime.memoryUsagePercent
              ),
              threadCount: data?.threadCount || runtime.threadCount,
              uptimeMinutes: data?.uptimeMinutes || this.parseUptimeToMinutes(data?.uptime)
            };
            this.backendStatus = 'online';
            resolve();
          },
          error: (error) => {
            console.error('Error loading system metrics:', error);
            // Set default values on error
            const runtime = this.calculateRuntimeMetrics();
            this.systemMetrics = {
              cpu: 0,
              memory: 0,
              disk: 0,
              network: 0,
              uptime: 'Unknown',
              lastRestart: new Date().toISOString(),
              maxMemoryMB: runtime.maxMemoryMB,
              memoryUsageMB: runtime.memoryUsageMB,
              memoryUsagePercent: runtime.memoryUsagePercent,
              threadCount: runtime.threadCount,
              uptimeMinutes: 0
            };
            this.backendStatus = 'offline';
            resolve();
          }
        });
    });
  }
  
  // Add these helper methods to calculate runtime metrics
  private calculateRuntimeMetrics() {
    // These are browser approximations since we can't get real JVM metrics in frontend
    const memory = (performance as any).memory;
    const maxMemoryMB = memory ? Math.round(memory.jsHeapSizeLimit / (1024 * 1024)) : 512;
    const usedMemoryMB = memory ? Math.round(memory.usedJSHeapSize / (1024 * 1024)) : 128;
    const memoryUsagePercent = maxMemoryMB > 0 ? (usedMemoryMB / maxMemoryMB) * 100 : 25;
    
    return {
      maxMemoryMB,
      memoryUsageMB: usedMemoryMB,
      memoryUsagePercent,
      threadCount: navigator.hardwareConcurrency || 4
    };
  }
  
  private parseUptimeToMinutes(uptimeString: string): number {
    if (!uptimeString) return 0;
    
    try {
      // Parse "6 days, 16 hours, 4 minutes" to minutes
      let totalMinutes = 0;
      
      const daysMatch = uptimeString.match(/(\d+)\s*days?/);
      const hoursMatch = uptimeString.match(/(\d+)\s*hours?/);
      const minutesMatch = uptimeString.match(/(\d+)\s*minutes?/);
      
      if (daysMatch) totalMinutes += parseInt(daysMatch[1]) * 24 * 60;
      if (hoursMatch) totalMinutes += parseInt(hoursMatch[1]) * 60;
      if (minutesMatch) totalMinutes += parseInt(minutesMatch[1]);
      
      return totalMinutes;
    } catch (e) {
      return 0;
    }
  }
  loadUserStats(): Promise<void> {
    return new Promise((resolve) => {
      const apiUrl = this.getApiUrl();
      this.http.get<UserStats>(`${apiUrl}/users/stats`)
        .subscribe({
          next: (data) => {
            this.userStats = {
              activeUsers: data?.activeUsers || 0,
              totalUsers: data?.totalUsers || 0,
              recentLogins: data?.recentLogins || []
            };
            this.backendStatus = 'online';
            resolve();
          },
          error: (error) => {
            console.error('Error loading user stats:', error);
            this.backendStatus = 'offline';
            resolve();
          }
        });
    });
  }

  loadPendingAlerts(): Promise<void> {
    return new Promise((resolve) => {
      const apiUrl = this.getApiUrl();
      this.http.get<AlertItem[]>(`${apiUrl}/security/alerts`)
        .subscribe({
          next: (data) => {
            this.pendingAlerts = Array.isArray(data) ? data : [];
            console.log('Loaded alerts from backend:', this.pendingAlerts);
            this.backendStatus = 'online';
            resolve();
          },
          error: (error) => {
            console.error('Error loading security alerts:', error);
            this.backendStatus = 'offline';
            resolve();
          }
        });
    });
  }

  loadSyncStatus(): Promise<void> {
    return new Promise((resolve) => {
      const apiUrl = this.getApiUrl();
      this.http.get<SyncStatus>(`${apiUrl}/security/sync-status`)
        .subscribe({
          next: (data) => {
            this.syncStatus = data || { syncStatus: 'UNKNOWN', syncNeeded: true, lastChecked: 0 };
            this.backendStatus = 'online';
            resolve();
          },
          error: (error) => {
            console.error('Error loading sync status:', error);
            this.backendStatus = 'offline';
            resolve();
          }
        });
    });
  }

  loadBuildInfo() {
    this.buildInfo = {
      version: buildInfo.version || '1.0.0',
      buildTime: buildInfo.buildTime || new Date().toISOString(),
      environment: buildInfo.environment || 'development',
      lastUpdate: buildInfo.lastUpdate || new Date().toISOString()
    };
  }

  startAutoRefresh() {
    // Refresh main data every 5 minutes
    this.refreshInterval = setInterval(() => {
      this.loadAllData();
    }, 300000); // 5 minutes
  }

  startMetricsAutoRefresh() {
    // Refresh system metrics more frequently (every 30 seconds)
    this.metricsRefreshInterval = setInterval(() => {
      this.loadSystemMetrics();
    }, 30000); // 30 seconds
  }

  // Improved metric sanitization
  private sanitizeMetric(value: any): number {
    if (value === null || value === undefined || isNaN(value)) {
      return 0;
    }
    return Math.max(0, Math.min(100, Number(value))); // Ensure between 0-100
  }

  // Helper to format uptime from minutes for backward compatibility
  private formatUptimeFromMinutes(minutes: number): string {
    if (!minutes) return 'Unknown';
    
    const days = Math.floor(minutes / (24 * 60));
    const hours = Math.floor((minutes % (24 * 60)) / 60);
    const mins = minutes % 60;
    
    if (days > 0) {
      return `${days} days, ${hours} hours, ${mins} minutes`;
    } else if (hours > 0) {
      return `${hours} hours, ${mins} minutes`;
    } else {
      return `${mins} minutes`;
    }
  }

  // Security actions
  runSecurityScan() {
    this.loading = true;
    const apiUrl = this.getApiUrl();
    
    this.http.post<string>(`${apiUrl}/security/scan`, {})
      .subscribe({
        next: (result) => {
          window.alert('Security scan completed: ' + result);
          this.loadSecurityData();
          this.loadSyncStatus();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error running security scan:', error);
          window.alert('Error running security scan: ' + error.message);
          this.loading = false;
        }
      });
  }

  fixVulnerabilities() {
    const apiUrl = this.getApiUrl();
    this.http.post<string>(`${apiUrl}/security/fix`, {})
      .subscribe({
        next: (result) => {
          window.alert(result);
          this.loadSecurityData();
        },
        error: (error) => {
          console.error('Error fixing vulnerabilities:', error);
          window.alert('Error fixing vulnerabilities: ' + error.message);
        }
      });
  }

  forceUpdate() {
    if (window.confirm('This will force update all packages and may cause breaking changes. Continue?')) {
      const apiUrl = this.getApiUrl();
      this.http.post<string>(`${apiUrl}/security/force-update`, {})
        .subscribe({
          next: (result) => {
            window.alert(result);
            this.loadSecurityData();
          },
          error: (error) => {
            console.error('Error forcing update:', error);
            window.alert('Error forcing update: ' + error.message);
          }
        });
    }
  }

  exportReport() {
    const apiUrl = this.getApiUrl();
    this.http.get<string>(`${apiUrl}/security/export-report`)
      .subscribe({
        next: (reportUrl) => {
          window.alert('Security report exported to: ' + reportUrl);
          window.open(reportUrl, '_blank');
        },
        error: (error) => {
          console.error('Error exporting report:', error);
          window.alert('Error exporting report: ' + error.message);
        }
      });
  }

  syncNVDVulnerabilities() {
    this.loading = true;
    const apiUrl = this.getApiUrl();
    this.http.post<{status: string, message: string}>(`${apiUrl}/security/sync-nvd`, {})
      .subscribe({
        next: (response) => {
          window.alert('NVD sync: ' + response.message);
          this.loadSecurityData();
          this.loadSyncStatus();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error syncing NVD vulnerabilities:', error);
          window.alert('Error syncing NVD vulnerabilities: ' + error.message);
          this.loading = false;
        }
      });
  }

  quickSecurityScan() {
    const apiUrl = this.getApiUrl();
    this.http.get<SecurityDashboard>(`${apiUrl}/security/quick-scan`)
      .subscribe({
        next: (dashboard) => {
          this.vulnerabilities = dashboard.vulnerabilities || [];
          this.criticalCount = dashboard.criticalCount || 0;
          this.highCount = dashboard.highCount || 0;
          this.moderateCount = dashboard.moderateCount || 0;
          this.lowCount = dashboard.lowCount || 0;
          this.lastUpdate = dashboard.lastUpdate || new Date().toISOString();
          this.overallStatus = dashboard.overallStatus || 'Unknown';
          window.alert('Quick scan completed!');
        },
        error: (error) => {
          console.error('Error running quick scan:', error);
          window.alert('Error running quick scan: ' + error.message);
        }
      });
  }

  // Enhanced backend connection test
  testBackendConnection() {
    const apiUrl = this.getApiUrl();
    console.log('Testing backend connection to:', apiUrl);
    
    const endpoints = [
      `${apiUrl}/system/metrics`,
      `${apiUrl}/security/dashboard`,
      `${apiUrl}/security/alerts`,
      `${apiUrl}/security/sync-status`
    ];
    
    let successCount = 0;
    const results: {endpoint: string, status: string}[] = [];
    
    endpoints.forEach(endpoint => {
      this.http.get(endpoint).subscribe({
        next: () => {
          successCount++;
          results.push({endpoint, status: '✅ Online'});
          this.checkAllResults(successCount, endpoints.length, results);
        },
        error: (error) => {
          console.error(`Endpoint ${endpoint} failed:`, error);
          results.push({endpoint, status: '❌ Offline'});
          this.checkAllResults(successCount, endpoints.length, results);
        }
      });
    });
  }

  private checkAllResults(successCount: number, totalCount: number, results: {endpoint: string, status: string}[]) {
    if (results.length === totalCount) {
      const resultMessage = results.map(r => `${r.endpoint}: ${r.status}`).join('\n');
      window.alert(`Backend Connection Test:\n\n${resultMessage}\n\n✅ ${successCount}/${totalCount} endpoints accessible`);
      this.backendStatus = successCount > 0 ? 'online' : 'offline';
    }
  }

  // Utility methods
  getApiUrl(): string {
    return this.environment?.apiUrl || 'http://localhost:8080/api';
  }

  getEnvironmentDisplayName(): string {
    if (!this.environment) return 'Unknown';
    return this.environment.production ? 'Production' : 'Development';
  }

  isEnvironmentLoaded(): boolean {
    return !!this.environment && !!this.environment.apiUrl;
  }

  isAuthenticated(): boolean {
    const token = TokenHandler.getToken();
    const user = TokenHandler.getUser();
    return !!(token && user && user.userType === 'ADMIN');
  }

  checkAuthentication(): boolean {
    const token = TokenHandler.getToken();
    const user = TokenHandler.getUser();
    
    if (!token) {
      console.error('No authentication token found');
      this.router.navigate(['/login']);
      return false;
    }
    
    if (!user || user.userType !== 'ADMIN') {
      console.error('User does not have ADMIN role');
      window.alert('Access denied. Admin role required.');
      this.router.navigate(['/']);
      return false;
    }
    
    console.log('Authentication check passed. User:', user.username, 'Role:', user.userType);
    return true;
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  getCurrentUserInfo(): string {
    const user = TokenHandler.getUser();
    if (user) {
      return `${user.username} (${user.userType})`;
    }
    return 'Unknown';
  }

  // Template helper methods
  get hasVulnerabilities(): boolean {
    return this.vulnerabilities && this.vulnerabilities.length > 0;
  }

  get hasRecentLogins(): boolean {
    return this.userStats && this.userStats.recentLogins && this.userStats.recentLogins.length > 0;
  }

  get hasPendingAlerts(): boolean {
    return this.pendingAlerts && this.pendingAlerts.length > 0;
  }

  get backendStatusIcon(): string {
    switch (this.backendStatus) {
      case 'online': return '🟢';
      case 'offline': return '🔴';
      case 'checking': return '🟡';
      default: return '⚪';
    }
  }

  get backendStatusText(): string {
    switch (this.backendStatus) {
      case 'online': return 'Backend Online';
      case 'offline': return 'Backend Offline';
      case 'checking': return 'Checking Connection';
      default: return 'Unknown Status';
    }
  }

  get syncStatusIcon(): string {
    switch (this.syncStatus.syncStatus) {
      case 'SYNCED': return '🟢';
      case 'NEEDS_SYNC': return '🟡';
      case 'SYNC_STARTED': return '🟠';
      case 'SYNC_FAILED': return '🔴';
      default: return '⚪';
    }
  }

  // Utility methods
  getSeverityColor(severity: string): string {
    switch (severity?.toLowerCase()) {
      case 'critical': return '#dc3545';
      case 'high': return '#fd7e14';
      case 'moderate': return '#ffc107';
      case 'medium': return '#ffc107';
      case 'low': return '#28a745';
      default: return '#6c757d';
    }
  }

  getSeverityIcon(severity: string): string {
    switch (severity?.toLowerCase()) {
      case 'critical': return '🔴';
      case 'high': return '🟠';
      case 'moderate': return '🟡';
      case 'medium': return '🟡';
      case 'low': return '🟢';
      default: return '⚪';
    }
  }

  getMetricColor(value: number): string {
    if (value < 50) return '#28a745';
    if (value < 80) return '#ffc107';
    return '#dc3545';
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleString();
    } catch {
      return dateString;
    }
  }

  formatTimestamp(timestamp: number): string {
    if (!timestamp) return 'N/A';
    try {
      const date = new Date(timestamp);
      return date.toLocaleString();
    } catch {
      return 'Invalid Date';
    }
  }

  getStatusIcon(status: string): string {
    switch (status?.toLowerCase()) {
      case 'healthy': return '🟢';
      case 'warning': return '🟡';
      case 'critical': return '🔴';
      default: return '⚪';
    }
  }

  // For thread count visualization
  getThreadUsagePercent(threadCount: number): number {
    const maxThreads = 200;
    return Math.min((threadCount / maxThreads) * 100, 100);
  }

  // Uptime formatting methods
  formatUptime(minutes: number): string {
    if (!minutes) return '0m';
    
    const days = Math.floor(minutes / (24 * 60));
    const hours = Math.floor((minutes % (24 * 60)) / 60);
    const mins = minutes % 60;
    
    if (days > 0) {
      return `${days}d ${hours}h ${mins}m`;
    } else if (hours > 0) {
      return `${hours}h ${mins}m`;
    } else {
      return `${mins}m`;
    }
  }

  getUptimeDays(minutes: number): number {
    return Math.floor(minutes / (24 * 60));
  }

  getUptimeHours(minutes: number): number {
    return Math.floor((minutes % (24 * 60)) / 60);
  }

  getUptimeMinutes(minutes: number): number {
    return minutes % 60;
  }

  // Metric status helpers
  getMetricStatus(value: number): string {
    if (value < 50) return 'optimal';
    if (value < 80) return 'warning';
    return 'critical';
  }

  getMetricStatusText(value: number): string {
    if (value < 50) return 'Optimal';
    if (value < 80) return 'Warning';
    return 'Critical';
  }

  // Format time since last update
  getTimeSinceLastUpdate(): string {
    if (!this.lastSuccessfulUpdate) return 'Never';
    
    const now = new Date();
    const diffMs = now.getTime() - this.lastSuccessfulUpdate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins === 1) return '1 minute ago';
    if (diffMins < 60) return `${diffMins} minutes ago`;
    
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours === 1) return '1 hour ago';
    if (diffHours < 24) return `${diffHours} hours ago`;
    
    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays} days ago`;
  }
}