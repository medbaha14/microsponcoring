import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-debug',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="debug-panel">
      <h3>Environment Debug Information</h3>
      <div class="debug-content">
        <p><strong>Environment:</strong> {{ environment.production ? 'Production' : 'Development' }}</p>
        <p><strong>API URL:</strong> {{ environment.apiUrl }}</p>
        <p><strong>Base URL:</strong> {{ environment.baseUrl }}</p>
        <p><strong>Current URL:</strong> {{ currentUrl }}</p>
        <p><strong>User Agent:</strong> {{ userAgent }}</p>
        <p><strong>Timestamp:</strong> {{ timestamp }}</p>
      </div>
      
      <h4>Available Routes:</h4>
      <ul>
        <li><a routerLink="/dashboard/admin/user-list">User List</a></li>
        <li><a routerLink="/dashboard/admin/stats-page">Stats</a></li>
        <li><a routerLink="/dashboard/admin/invoices">Invoices</a></li>
        <li><a routerLink="/dashboard/admin/security">Security</a></li>
      </ul>
      
      <h4>Console Logs:</h4>
      <div class="console-logs">
        <p>Check browser console for additional debug information</p>
      </div>
    </div>
  `,
  styles: [`
    .debug-panel {
      padding: 20px;
      background: #f5f5f5;
      border-radius: 8px;
      margin: 20px;
    }
    .debug-content {
      background: white;
      padding: 15px;
      border-radius: 5px;
      margin: 10px 0;
    }
    .console-logs {
      background: #e9ecef;
      padding: 10px;
      border-radius: 5px;
      font-family: monospace;
    }
    ul {
      list-style: none;
      padding: 0;
    }
    li {
      margin: 5px 0;
    }
    a {
      color: #007bff;
      text-decoration: none;
    }
    a:hover {
      text-decoration: underline;
    }
  `]
})
export class DebugComponent implements OnInit {
  environment = environment;
  currentUrl = '';
  userAgent = '';
  timestamp = '';

  ngOnInit() {
    this.currentUrl = window.location.href;
    this.userAgent = navigator.userAgent;
    this.timestamp = new Date().toISOString();
    
    // Log environment information to console
    console.log('=== DEBUG COMPONENT INITIALIZED ===');
    console.log('Environment:', environment);
    console.log('Current URL:', this.currentUrl);
    console.log('User Agent:', this.userAgent);
    console.log('Timestamp:', this.timestamp);
    console.log('===================================');
  }
}


