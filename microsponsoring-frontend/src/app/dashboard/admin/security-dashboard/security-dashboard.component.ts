import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

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

@Component({
  selector: 'app-security-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './security-dashboard.component.html',
  styleUrls: ['./security-dashboard.component.css']
})
export class SecurityDashboardComponent implements OnInit {
  vulnerabilities: SecurityVulnerability[] = [];
  loading = false;
  lastUpdate = new Date();
  nextScan = new Date(Date.now() + 24 * 60 * 60 * 1000);
  criticalCount = 0;
  highCount = 0;
  moderateCount = 0;
  lowCount = 0;
  overallStatus = '';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadSecurityData();
    this.startAutoRefresh();
  }

  loadSecurityData() {
    this.loading = true;
    
    this.http.get<SecurityDashboard>('/api/security/dashboard')
      .subscribe({
        next: (data) => {
          this.vulnerabilities = data.vulnerabilities;
          this.criticalCount = data.criticalCount;
          this.highCount = data.highCount;
          this.moderateCount = data.moderateCount;
          this.lowCount = data.lowCount;
          this.lastUpdate = new Date(data.lastUpdate).toLocaleString();
          this.nextScan = new Date(data.nextScan).toLocaleString();
          this.overallStatus = data.overallStatus;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading security data:', error);
          this.loading = false;
          // Fallback to sample data
          this.loadSampleData();
        }
      });
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
  }

  startAutoRefresh() {
    setInterval(() => {
      this.loadSecurityData();
    }, 300000); // Refresh every 5 minutes
  }

  runSecurityScan() {
    this.loading = true;
    
    this.http.post<string>('/api/security/scan', {})
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
    this.http.post<string>('/api/security/fix', {})
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
      this.http.post<string>('/api/security/force-update', {})
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
    this.http.get<string>('/api/security/export-report')
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
} 