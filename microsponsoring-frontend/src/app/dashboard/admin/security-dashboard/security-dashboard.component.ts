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

@Component({
  selector: 'app-security-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './security-dashboard.component.html',
  styleUrls: ['./security-dashboard.component.css']
})
export class SecurityDashboardComponent implements OnInit, OnDestroy {
  // Make environment accessible to template
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
    uptime: '',
    lastRestart: ''
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

  // Pending alerts
  pendingAlerts: string[] = [];

  // Auto-refresh interval
  private refreshInterval: any;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    // Check authentication and admin role
    if (!this.checkAuthentication()) {
      return;
    }
    
    // Debug: Log environment and API configuration
    console.log('Security Dashboard initialized');
    console.log('Environment object:', environment);
    console.log('Component environment:', this.environment);
    console.log('API URL:', this.getApiUrl());
    console.log('Environment display:', this.getEnvironmentDisplayName());
    
    this.loadAllData();
    this.startAutoRefresh();
    this.loadBuildInfo();
  }

  ngOnDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  loadAllData() {
    this.loading = true;
    
    // Load security data
    this.loadSecurityData();
    
    // Load system metrics
    this.loadSystemMetrics();
    
    // Load user statistics
    this.loadUserStats();
    
    // Load pending alerts
    this.loadPendingAlerts();
  }

  loadSecurityData() {
    const apiUrl = environment?.apiUrl || 'http://localhost:8080/api';
    console.log('Loading security data from:', `${apiUrl}/security/dashboard`);
    
    this.http.get<SecurityDashboard>(`${apiUrl}/security/dashboard`)
      .subscribe({
        next: (data) => {
          this.vulnerabilities = data.vulnerabilities;
          this.criticalCount = data.criticalCount;
          this.highCount = data.highCount;
          this.moderateCount = data.moderateCount;
          this.lowCount = data.lowCount;
          this.lastUpdate = data.lastUpdate;
          this.nextScan = data.nextScan;
          this.overallStatus = data.overallStatus;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading security data:', error);
          console.error('API URL attempted:', `${this.getApiUrl()}/security/dashboard`);
          console.error('Error status:', error.status);
          console.error('Error message:', error.message);
          this.loadSampleData();
        }
      });
  }

  loadSystemMetrics() {
    const apiUrl = this.getApiUrl();
    this.http.get<SystemMetrics>(`${apiUrl}/system/metrics`)
      .subscribe({
        next: (data) => {
          this.systemMetrics = data;
        },
        error: (error) => {
          console.error('Error loading system metrics:', error);
          this.loadSampleSystemMetrics();
        }
      });
  }

  loadUserStats() {
    const apiUrl = this.getApiUrl();
    this.http.get<UserStats>(`${apiUrl}/users/stats`)
      .subscribe({
        next: (data) => {
          this.userStats = data;
        },
        error: (error) => {
          console.error('Error loading user stats:', error);
          this.loadSampleUserStats();
        }
      });
  }

  loadPendingAlerts() {
    const apiUrl = this.getApiUrl();
    this.http.get<string[]>(`${apiUrl}/security/alerts`)
      .subscribe({
        next: (data) => {
          this.pendingAlerts = data;
        },
        error: (error) => {
          console.error('Error loading alerts:', error);
          this.loadSampleAlerts();
        }
      });
  }

  loadBuildInfo() {
    // Use the build info from the build-info.ts file
    this.buildInfo = {
      version: buildInfo.version,
      buildTime: buildInfo.buildTime,
      environment: buildInfo.environment,
      lastUpdate: buildInfo.lastUpdate
    };
  }

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
        packageName: 'webpack-dev-server 5.0.4',
        source: 'npm · microsponsoring-frontend/package-lock.json',
        severity: 'moderate',
        count: 2,
        description: 'Webpack dev server vulnerabilities',
        cveId: 'CVE-2023-67890',
        fixVersion: '5.0.5',
        riskScore: '6.2'
      }
    ];
    this.highCount = 1;
    this.moderateCount = 2;
    this.loading = false;
  }

  loadSampleSystemMetrics() {
    this.systemMetrics = {
      cpu: 45,
      memory: 67,
      disk: 23,
      network: 12,
      uptime: '15 days, 8 hours, 32 minutes',
      lastRestart: '2024-01-15 08:30:00'
    };
  }

  loadSampleUserStats() {
    this.userStats = {
      activeUsers: 23,
      totalUsers: 156,
      recentLogins: [
        {
          username: 'admin',
          timestamp: '2024-01-30 14:25:30',
          success: true,
          ipAddress: '192.168.1.100',
          userAgent: 'Chrome/120.0.0.0'
        },
        {
          username: 'user123',
          timestamp: '2024-01-30 14:20:15',
          success: false,
          ipAddress: '192.168.1.101',
          userAgent: 'Firefox/121.0'
        }
      ]
    };
  }

  loadSampleAlerts() {
    this.pendingAlerts = [
      'High CPU usage detected (85%)',
      'Database connection pool at 90% capacity',
      'SSL certificate expires in 30 days',
      'Failed login attempts from suspicious IP'
    ];
  }

  startAutoRefresh() {
    this.refreshInterval = setInterval(() => {
      this.loadAllData();
    }, 300000); // Refresh every 5 minutes
  }

  // Security actions
  runSecurityScan() {
    this.loading = true;
    const apiUrl = this.getApiUrl();
    
    this.http.post<string>(`${apiUrl}/security/scan`, {})
      .subscribe({
        next: (result) => {
          alert('Security scan completed: ' + result);
          this.loadSecurityData();
        },
        error: (error) => {
          console.error('Error running security scan:', error);
          alert('Error running security scan. Check console for details.');
          this.loading = false;
        }
      });
  }

  fixVulnerabilities() {
    const apiUrl = this.getApiUrl();
    this.http.post<string>(`${apiUrl}/security/fix`, {})
      .subscribe({
        next: (result) => {
          alert(result);
        },
        error: (error) => {
          console.error('Error fixing vulnerabilities:', error);
          alert('Error fixing vulnerabilities. Check console for details.');
        }
      });
  }

  forceUpdate() {
    if (confirm('This will force update all packages and may cause breaking changes. Continue?')) {
      const apiUrl = this.getApiUrl();
      this.http.post<string>(`${apiUrl}/security/force-update`, {})
        .subscribe({
          next: (result) => {
            alert(result);
          },
          error: (error) => {
            console.error('Error forcing update:', error);
            alert('Error forcing update. Check console for details.');
          }
        });
    }
  }

  exportReport() {
    const apiUrl = this.getApiUrl();
    this.http.get<string>(`${apiUrl}/security/export-report`)
      .subscribe({
        next: (reportUrl) => {
          alert('Security report exported to: ' + reportUrl);
        },
        error: (error) => {
          console.error('Error exporting report:', error);
          alert('Error exporting report. Check console for details.');
        }
      });
  }

  // Admin actions
  forceLogoutAllUsers() {
    if (confirm('This will force logout all currently active users. Continue?')) {
      const apiUrl = this.getApiUrl();
      this.http.post<string>(`${apiUrl}/admin/force-logout-all`, {})
        .subscribe({
          next: (result) => {
            alert(result);
            this.loadUserStats();
          },
          error: (error) => {
            console.error('Error forcing logout:', error);
            alert('Error forcing logout. Check console for details.');
          }
        });
    }
  }

  refreshSystem() {
    const apiUrl = this.getApiUrl();
    this.http.post<string>(`${apiUrl}/admin/refresh-system`, {})
      .subscribe({
        next: (result) => {
          alert(result);
          this.loadAllData();
        },
        error: (error) => {
          console.error('Error refreshing system:', error);
          alert('Error refreshing system. Check console for details.');
        }
      });
  }

  // Test backend connection
  testBackendConnection() {
    const apiUrl = this.getApiUrl();
    console.log('Testing backend connection to:', apiUrl);
    
    // Simple health check
    this.http.get(`${apiUrl.replace('/api', '')}/actuator/health`, { responseType: 'text' })
      .subscribe({
        next: (response) => {
          console.log('Backend connection successful:', response);
          alert('Backend connection successful!');
        },
        error: (error) => {
          console.error('Backend connection failed:', error);
          alert(`Backend connection failed: ${error.status} - ${error.message}`);
        }
      });
  }

  // Get API URL with fallback
  getApiUrl(): string {
    return this.environment?.apiUrl || 'http://localhost:8080/api';
  }

  // Get environment display name
  getEnvironmentDisplayName(): string {
    if (!this.environment) return 'Unknown';
    return this.environment.production ? 'Production' : 'Development';
  }

  // Check if environment is properly loaded
  isEnvironmentLoaded(): boolean {
    return !!this.environment && !!this.environment.apiUrl;
  }

  // Check if user is authenticated and has admin role
  isAuthenticated(): boolean {
    const token = TokenHandler.getToken();
    const user = TokenHandler.getUser();
    return !!(token && user && user.userType === 'ADMIN');
  }

  // Check authentication and admin role
  checkAuthentication(): boolean {
    const token = TokenHandler.getToken();
    const user = TokenHandler.getUser();
    
    if (!token) {
      console.error('No authentication token found');
      return false;
    }
    
    if (!user || user.userType !== 'ADMIN') {
      console.error('User does not have ADMIN role');
      return false;
    }
    
    console.log('Authentication check passed. User:', user.username, 'Role:', user.userType);
    return true;
  }

  // Navigate to login page
  goToLogin() {
    this.router.navigate(['/login']);
  }

  // Get current user info for display
  getCurrentUserInfo(): string {
    const user = TokenHandler.getUser();
    if (user) {
      return `${user.username} (${user.userType})`;
    }
    return 'Unknown';
  }

  // Check if backend is accessible
  checkBackendAccess() {
    const apiUrl = this.getApiUrl();
    console.log('Checking backend access to:', apiUrl);
    
    // Try to access a simple endpoint
    this.http.get(`${apiUrl.replace('/api', '')}/actuator/health`, { responseType: 'text' })
      .subscribe({
        next: (response) => {
          console.log('Backend is accessible:', response);
          alert('✅ Backend is accessible!\n\nResponse: ' + response.substring(0, 100) + '...');
        },
        error: (error) => {
          console.error('Backend access failed:', error);
          alert(`❌ Backend access failed!\n\nStatus: ${error.status}\nMessage: ${error.message}\n\nPlease check:\n1. Backend is running on port 8080\n2. No firewall blocking the connection\n3. Backend has actuator endpoints enabled`);
        }
      });
  }

  // Utility methods
  getSeverityColor(severity: string): string {
    switch (severity) {
      case 'critical': return '#dc3545';
      case 'high': return '#fd7e14';
      case 'moderate': return '#ffc107';
      case 'low': return '#28a745';
      default: return '#6c757d';
    }
  }

  getSeverityIcon(severity: string): string {
    switch (severity) {
      case 'critical': return '🔴';
      case 'high': return '🟠';
      case 'moderate': return '🟡';
      case 'low': return '🟢';
      default: return '⚪';
    }
  }

  getMetricColor(value: number): string {
    if (value < 50) return '#28a745'; // Green
    if (value < 80) return '#ffc107'; // Yellow
    return '#dc3545'; // Red
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
    switch (status.toLowerCase()) {
      case 'healthy': return '🟢';
      case 'warning': return '🟡';
      case 'critical': return '🔴';
      default: return '⚪';
    }
  }
} 