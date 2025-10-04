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
  // Add these new properties from your backend
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
  id: number;
  type: string;
  title: string;
  description: string;
  message: string;
  severity: string;
  timestamp: string;
  status?: string;
  category?: string;
  source?: string;
  details?: string;
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

  // System metrics - updated with ALL required properties
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

  pendingAlerts: AlertItem[] = [];

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
      this.loadPendingAlerts()
    ]).finally(() => {
      this.loading = false;
      this.lastSuccessfulUpdate = new Date();
    });
  }

  loadSecurityData(): Promise<void> {
    return new Promise((resolve) => {
      const apiUrl = this.getApiUrl();
      this.http.get<SecurityDashboard>(`${apiUrl}/security/dashboard`)
        .subscribe({
          next: (data) => {
            this.vulnerabilities = data.vulnerabilities || [];
            this.criticalCount = data.criticalCount || 0;
            this.highCount = data.highCount || 0;
            this.moderateCount = data.moderateCount || 0;
            this.lowCount = data.lowCount || 0;
            this.lastUpdate = data.lastUpdate || new Date().toISOString();
            this.nextScan = data.nextScan || '';
            this.overallStatus = data.overallStatus || 'Unknown';
            this.backendStatus = 'online';
            resolve();
          },
          error: (error) => {
            console.error('Error loading security data:', error);
            this.loadSampleData();
            this.backendStatus = 'offline';
            resolve();
          }
        });
    });
  }

  loadSystemMetrics(): Promise<void> {
    return new Promise((resolve) => {
      const apiUrl = this.getApiUrl();
      this.http.get<SystemMetrics>(`${apiUrl}/system/metrics`)
        .subscribe({
          next: (data) => {
            // Handle both old and new format for backward compatibility
            this.systemMetrics = {
              cpu: this.sanitizeMetric(data?.cpu),
              memory: this.sanitizeMetric(data?.memory),
              disk: this.sanitizeMetric(data?.disk),
              network: this.sanitizeMetric(data?.network),
              uptime: data?.uptime || this.formatUptimeFromMinutes(data?.uptimeMinutes),
              lastRestart: data?.lastRestart || new Date().toISOString(),
              // New fields from backend
              maxMemoryMB: data?.maxMemoryMB || 0,
              memoryUsageMB: data?.memoryUsageMB || 0,
              memoryUsagePercent: this.sanitizeMetric(data?.memoryUsagePercent),
              threadCount: data?.threadCount || 0,
              uptimeMinutes: data?.uptimeMinutes || 0
            };
            this.backendStatus = 'online';
            resolve();
          },
          error: (error) => {
            console.error('Error loading system metrics:', error);
            this.loadSampleSystemMetrics();
            this.backendStatus = 'offline';
            resolve();
          }
        });
    });
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
            this.loadSampleUserStats();
            this.backendStatus = 'offline';
            resolve();
          }
        });
    });
  }

  loadPendingAlerts(): Promise<void> {
    return new Promise((resolve) => {
      const apiUrl = this.getApiUrl();
      console.log('Loading pending alerts from:', `${apiUrl}/alerts/pending`);
      
      this.http.get<AlertItem[]>(`${apiUrl}/alerts/pending`)
        .subscribe({
          next: (data) => {
            this.pendingAlerts = Array.isArray(data) ? data : [];
            console.log('Loaded alerts from backend:', this.pendingAlerts);
            this.backendStatus = 'online';
            resolve();
          },
          error: (error) => {
            console.error('Error loading from /alerts/pending:', error);
            this.backendStatus = 'offline';
            this.tryAlternativeAlertEndpoints().finally(() => resolve());
          }
        });
    });
  }

  private tryAlternativeAlertEndpoints(): Promise<void> {
    return new Promise((resolve) => {
      const apiUrl = this.getApiUrl();
      const endpoints = [
        `${apiUrl}/alerts/all`,
        `${apiUrl}/alerts`,
        `${apiUrl}/security/alerts`
      ];
      
      let attempts = 0;
      
      const tryNextEndpoint = () => {
        if (attempts >= endpoints.length) {
          resolve();
          return;
        }
        
        const endpoint = endpoints[attempts];
        console.log(`Trying alternative endpoint: ${endpoint}`);
        
        this.http.get<AlertItem[]>(endpoint).subscribe({
          next: (data) => {
            if (endpoint.includes('/all') || endpoint === `${apiUrl}/alerts`) {
              this.pendingAlerts = Array.isArray(data) 
                ? data.filter(alert => alert.status === 'pending')
                : [];
            } else {
              this.pendingAlerts = Array.isArray(data) ? data : [];
            }
            console.log(`Successfully loaded alerts from ${endpoint}:`, this.pendingAlerts);
            this.backendStatus = 'online';
            resolve();
          },
          error: (error) => {
            console.error(`Endpoint ${endpoint} failed:`, error);
            attempts++;
            tryNextEndpoint();
          }
        });
      };
      
      tryNextEndpoint();
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

  // Improved sample data with more realistic values
  loadSampleData() {
    this.vulnerabilities = [
      {
        packageName: 'mysql:mysql-connector-java 8.0.33',
        source: 'Maven · microsponsoring-backend/pom.xml',
        severity: 'high',
        count: 1,
        description: 'MySQL Connector vulnerability',
        cveId: 'CVE-2023-12345',
        fixVersion: '8.0.35',
        riskScore: '8.5'
      },
      {
        packageName: 'log4j:log4j-core 2.14.1',
        source: 'Maven · microsponsoring-backend/pom.xml',
        severity: 'critical',
        count: 1,
        description: 'Log4Shell vulnerability',
        cveId: 'CVE-2021-44228',
        fixVersion: '2.17.0',
        riskScore: '10.0'
      }
    ];
    this.criticalCount = 1;
    this.highCount = 1;
    this.moderateCount = 2;
    this.lowCount = 3;
    this.lastUpdate = new Date().toISOString();
    this.overallStatus = 'Warning';
  }

  loadSampleSystemMetrics() {
    // More realistic sample data with ALL properties
    const now = new Date();
    this.systemMetrics = {
      cpu: Math.floor(Math.random() * 80 + 10), // 10-90%
      memory: Math.floor(Math.random() * 70 + 20), // 20-90%
      disk: Math.floor(Math.random() * 60 + 15), // 15-75%
      network: Math.floor(Math.random() * 50), // 0-50%
      uptime: '5 days, 12 hours, 30 minutes',
      lastRestart: new Date(now.getTime() - 5 * 24 * 60 * 60 * 1000).toISOString(), // 5 days ago
      // New sample data
      maxMemoryMB: 16312,
      memoryUsageMB: 64,
      memoryUsagePercent: 0.4,
      threadCount: 38,
      uptimeMinutes: 191
    };
  }

  loadSampleUserStats() {
    this.userStats = {
      activeUsers: 23,
      totalUsers: 156,
      recentLogins: [
        {
          username: 'admin',
          timestamp: new Date().toISOString(),
          success: true,
          ipAddress: '192.168.1.100',
          userAgent: 'Chrome/120.0.0.0'
        },
        {
          username: 'user123',
          timestamp: new Date(Date.now() - 5 * 60 * 1000).toISOString(), // 5 minutes ago
          success: false,
          ipAddress: '192.168.1.150',
          userAgent: 'Firefox/119.0'
        }
      ]
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

  markAlertAsResolved(alert: AlertItem) {
    const apiUrl = this.getApiUrl();
    
    this.http.post(`${apiUrl}/alerts/${alert.id}/resolve`, {})
      .subscribe({
        next: (response) => {
          console.log('Alert marked as resolved:', response);
          this.pendingAlerts = this.pendingAlerts.filter(a => a.id !== alert.id);
          window.alert('Alert resolved successfully!');
        },
        error: (error) => {
          console.error('Error resolving alert:', error);
          this.pendingAlerts = this.pendingAlerts.filter(a => a.id !== alert.id);
          window.alert('Alert resolved locally. There was an issue communicating with the server.');
        }
      });
  }

  acknowledgeAlert(alert: AlertItem) {
    const apiUrl = this.getApiUrl();
    
    this.http.post(`${apiUrl}/alerts/${alert.id}/acknowledge`, {})
      .subscribe({
        next: (response) => {
          console.log('Alert acknowledged:', response);
          alert.status = 'acknowledged';
          window.alert('Alert acknowledged!');
        },
        error: (error) => {
          console.error('Error acknowledging alert:', error);
          window.alert('Error acknowledging alert. Please try again.');
        }
      });
  }

  loadAlertHistory() {
    const apiUrl = this.getApiUrl();
    this.http.get<AlertItem[]>(`${apiUrl}/alerts/history`)
      .subscribe({
        next: (history) => {
          console.log('Alert history:', history);
          window.alert(`Loaded ${history.length} historical alerts`);
        },
        error: (error) => {
          console.error('Error loading alert history:', error);
          window.alert('Error loading alert history.');
        }
      });
  }

  // Enhanced security actions with better feedback
  runSecurityScan() {
    this.loading = true;
    const apiUrl = this.getApiUrl();
    
    this.http.post<string>(`${apiUrl}/security/scan`, {})
      .subscribe({
        next: (result) => {
          window.alert('Security scan completed: ' + result);
          this.loadSecurityData();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error running security scan:', error);
          window.alert('Error running security scan. Using sample data instead.');
          this.loadSampleData();
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
          this.loadSecurityData(); // Refresh vulnerabilities after fix
        },
        error: (error) => {
          console.error('Error fixing vulnerabilities:', error);
          window.alert('Error fixing vulnerabilities. Check console for details.');
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
            this.loadSecurityData(); // Refresh data after update
          },
          error: (error) => {
            console.error('Error forcing update:', error);
            window.alert('Error forcing update. Check console for details.');
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
          // Optionally open the report in a new window
          window.open(reportUrl, '_blank');
        },
        error: (error) => {
          console.error('Error exporting report:', error);
          window.alert('Error exporting report. Check console for details.');
        }
      });
  }

  // Enhanced admin actions
  forceLogoutAllUsers() {
    if (window.confirm('This will force logout all currently active users. Continue?')) {
      const apiUrl = this.getApiUrl();
      this.http.post<string>(`${apiUrl}/admin/force-logout-all`, {})
        .subscribe({
          next: (result) => {
            window.alert(result);
            this.loadUserStats();
          },
          error: (error) => {
            console.error('Error forcing logout:', error);
            window.alert('Error forcing logout. Check console for details.');
          }
        });
    }
  }

  refreshSystem() {
    const apiUrl = this.getApiUrl();
    this.http.post<string>(`${apiUrl}/admin/refresh-system`, {})
      .subscribe({
        next: (result) => {
          window.alert(result);
          this.loadAllData();
        },
        error: (error) => {
          console.error('Error refreshing system:', error);
          window.alert('Error refreshing system. Check console for details.');
        }
      });
  }

  // Enhanced backend connection test
  testBackendConnection() {
    const apiUrl = this.getApiUrl();
    console.log('Testing backend connection to:', apiUrl);
    
    const endpoints = [
      `${apiUrl}/system/metrics`,
      `${apiUrl}/alerts/pending`,
      `${apiUrl}/security/dashboard`,
      `${apiUrl}/users/stats`
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

  syncNVDVulnerabilities() {
    this.loading = true;
    const apiUrl = this.getApiUrl();
    this.http.post(`${apiUrl}/security/sync-nvd`, {})
      .subscribe({
        next: (response: any) => {
          console.log('NVD sync successful:', response);
          window.alert('NVD vulnerabilities synchronized successfully!');
          this.loadAllData();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error syncing NVD vulnerabilities:', error);
          window.alert('Error syncing NVD vulnerabilities. Check console for details.');
          this.loading = false;
        }
      });
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

  getStatusIcon(status: string): string {
    switch (status?.toLowerCase()) {
      case 'healthy': return '🟢';
      case 'warning': return '🟡';
      case 'critical': return '🔴';
      default: return '⚪';
    }
  }

  // For thread count visualization (assuming a reasonable max of 200 threads)
  getThreadUsagePercent(threadCount: number): number {
    const maxThreads = 200; // Adjust this based on your typical maximum
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