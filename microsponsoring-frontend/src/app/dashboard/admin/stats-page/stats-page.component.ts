import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../services/user.service';
import { InvoiceService } from '../../../services/invoice.service';
import { PaymentService } from '../../../services/payment.service';
import { ThemeService } from '../../../services/theme.service';
import { User } from '../../../models/user.model';
import { Invoice } from '../../../models/invoice.model';

interface UserRoleStats {
  role: string;
  count: number;
  percentage: number;
}

interface UserCreationStats {
  month: string;
  count: number;
}

interface PaymentStats {
  totalPayments: number;
  totalAmount: number;
  averageAmount: number;
  monthlyPayments: { month: string; amount: number }[];
}

@Component({
  selector: 'app-stats-page',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule],
  templateUrl: './stats-page.component.html',
  styleUrls: ['./stats-page.component.css']
})
export class StatsPageComponent implements OnInit {
  // User statistics
  totalUsers: number = 0;
  activeUsers: number = 0;
  inactiveUsers: number = 0;
  
  // Role distribution
  userRoleStats: UserRoleStats[] = [];
  
  // User creation by date
  userCreationStats: UserCreationStats[] = [];
  
  // Payment statistics
  paymentStats: PaymentStats = {
    totalPayments: 0,
    totalAmount: 0,
    averageAmount: 0,
    monthlyPayments: []
  };
  
  // Recent activity
  recentUsers: User[] = [];
  recentInvoices: Invoice[] = [];
  
  // Loading states
  isLoading: boolean = true;
  isLoadingUsers: boolean = true;
  isLoadingPayments: boolean = true;

  // Date range filters
  startDate: string = '';
  endDate: string = '';
  selectedDateRange: string = 'all';

  // Real-time updates
  private refreshInterval: any;
  autoRefresh: boolean = false;
  refreshIntervalSeconds: number = 30;

  // Performance metrics
  pageLoadTime: number = 0;
  userEngagement: number = 0;

  // Theme mode
  isDarkMode: boolean = false;

  constructor(
    private userService: UserService,
    private invoiceService: InvoiceService,
    private paymentService: PaymentService,
    private themeService: ThemeService
  ) {}

  ngOnInit() {
    // Subscribe to theme service
    this.themeService.theme$.subscribe(theme => {
      this.isDarkMode = theme === 'dark';
    });
    
    this.measurePageLoadTime();
    this.loadAllStats();
    this.setupAutoRefresh();
  }

  ngOnDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  loadAllStats() {
    this.isLoading = true;
    this.loadUserStats();
    this.loadPaymentStats();
  }

  loadUserStats() {
    this.isLoadingUsers = true;
    this.userService.getUserStats().subscribe({
      next: (stats: any) => {
        // Use the stats from the API directly
        this.totalUsers = stats.totalUsers || 0;
        this.activeUsers = stats.activeUsers || 0;
        this.inactiveUsers = stats.inactiveUsers || 0;
        
        // Create role stats from the API data
        this.userRoleStats = [
          { role: 'ADMIN', count: stats.adminUsers || 0, percentage: 0 },
          { role: 'SPONSOR', count: stats.sponsorUsers || 0, percentage: 0 },
          { role: 'ORGANISATION_NONPROFIT', count: stats.organisationUsers || 0, percentage: 0 }
        ];
        
        // Calculate percentages
        this.userRoleStats.forEach(role => {
          role.percentage = this.totalUsers > 0 ? (role.count / this.totalUsers) * 100 : 0;
        });
        
        // Still need to load individual users for recent users and creation stats
        this.userService.getAll().subscribe({
          next: (users: User[]) => {
            this.processUserStats(users);
            this.isLoadingUsers = false;
            this.checkLoadingComplete();
          },
          error: (error) => {
            console.error('Error loading users for detailed stats:', error);
            this.isLoadingUsers = false;
            this.checkLoadingComplete();
          }
        });
      },
      error: (error) => {
        console.error('Error loading user stats:', error);
        // Fallback to loading all users
        this.userService.getAll().subscribe({
          next: (users: User[]) => {
            this.processUserStats(users);
            this.isLoadingUsers = false;
            this.checkLoadingComplete();
          },
          error: (fallbackError) => {
            console.error('Error loading users (fallback):', fallbackError);
            this.isLoadingUsers = false;
            this.checkLoadingComplete();
          }
        });
      }
    });
  }

  loadPaymentStats() {
    this.isLoadingPayments = true;
    this.invoiceService.getAll().subscribe({
      next: (invoices: Invoice[]) => {
        this.processPaymentStats(invoices);
        this.isLoadingPayments = false;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading payment stats:', error);
        this.isLoadingPayments = false;
        this.checkLoadingComplete();
      }
    });
  }

  processUserStats(users: User[]) {
    // Total users
    this.totalUsers = users.length;
    this.activeUsers = users.filter(u => u.active).length;
    this.inactiveUsers = users.filter(u => !u.active).length;
    
    // Recent users (last 5)
    this.recentUsers = users
      .filter(user => user.createdAt)
      .sort((a, b) => new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime())
      .slice(0, 5);
    
    // Role distribution
    const roleCounts = users.reduce((acc, user) => {
      const userType = user.userType || 'UNKNOWN';
      acc[userType] = (acc[userType] || 0) + 1;
      return acc;
    }, {} as { [key: string]: number });
    
    this.userRoleStats = Object.entries(roleCounts).map(([role, count]) => ({
      role,
      count,
      percentage: (count / this.totalUsers) * 100
    }));
    
    // User creation by month
    const creationByMonth = users.reduce((acc, user) => {
      if (user.createdAt) {
        const date = new Date(user.createdAt);
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        acc[monthKey] = (acc[monthKey] || 0) + 1;
      }
      return acc;
    }, {} as { [key: string]: number });
    
    this.userCreationStats = Object.entries(creationByMonth)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([month, count]) => ({
        month: this.formatMonth(month),
        count
      }));

    // Calculate user engagement
    this.calculateUserEngagement();
  }

  processPaymentStats(invoices: Invoice[]) {
    // Total payments
    this.paymentStats.totalPayments = invoices.length;
    this.paymentStats.totalAmount = invoices.reduce((sum, invoice) => sum + (invoice.amount || 0), 0);
    this.paymentStats.averageAmount = this.paymentStats.totalPayments > 0 
      ? this.paymentStats.totalAmount / this.paymentStats.totalPayments 
      : 0;
    
    // Monthly payments
    const monthlyPayments = invoices.reduce((acc, invoice) => {
      if (invoice.createdAt) {
        const date = new Date(invoice.createdAt);
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        acc[monthKey] = (acc[monthKey] || 0) + (invoice.amount || 0);
      }
      return acc;
    }, {} as { [key: string]: number });
    
    this.paymentStats.monthlyPayments = Object.entries(monthlyPayments)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([month, amount]) => ({
        month: this.formatMonth(month),
        amount
      }));
    
    // Recent invoices
    this.recentInvoices = invoices
      .filter(invoice => invoice.createdAt)
      .sort((a, b) => new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime())
      .slice(0, 5);
  }

  formatMonth(monthKey: string): string {
    const [year, month] = monthKey.split('-');
    const date = new Date(parseInt(year), parseInt(month) - 1);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long' });
  }

  checkLoadingComplete() {
    if (!this.isLoadingUsers && !this.isLoadingPayments) {
      this.isLoading = false;
    }
  }

  getRoleColor(role: string): string {
    const colors: { [key: string]: string } = {
      'ADMIN': '#FF6B6B',
      'SPONSOR': '#4ECDC4',
      'ORGANISATION_NONPROFIT': '#45B7D1',
      'ORGANISATION_PROFIT': '#96CEB4'
    };
    return colors[role] || '#95A5A6';
  }

  getRoleDisplayName(role: string): string {
    const displayNames: { [key: string]: string } = {
      'ADMIN': 'Administrators',
      'SPONSOR': 'Sponsors',
      'ORGANISATION_NONPROFIT': 'Non-Profit Organizations',
      'ORGANISATION_PROFIT': 'For-Profit Organizations'
    };
    return displayNames[role] || role;
  }

  getTimelineHeight(count: number): number {
    if (this.userCreationStats.length === 0) return 0;
    const maxCount = Math.max(...this.userCreationStats.map(m => m.count));
    return maxCount > 0 ? (count / maxCount) * 100 : 0;
  }

  getPaymentHeight(amount: number): number {
    if (this.paymentStats.monthlyPayments.length === 0) return 0;
    const maxAmount = Math.max(...this.paymentStats.monthlyPayments.map(p => p.amount));
    return maxAmount > 0 ? (amount / maxAmount) * 100 : 0;
  }

  // Performance Metrics
  measurePageLoadTime() {
    const startTime = performance.now();
    setTimeout(() => {
      this.pageLoadTime = performance.now() - startTime;
    }, 100);
  }

  // Auto Refresh
  setupAutoRefresh() {
    if (this.autoRefresh) {
      this.refreshInterval = setInterval(() => {
        this.loadAllStats();
      }, this.refreshIntervalSeconds * 1000);
    }
  }

  toggleAutoRefresh() {
    this.autoRefresh = !this.autoRefresh;
    if (this.autoRefresh) {
      this.setupAutoRefresh();
    } else if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  // Date Range Filters
  onDateRangeChange() {
    this.loadAllStats();
  }

  // Export Functionality
  exportToPDF() {
    // Implementation for PDF export
    console.log('Exporting to PDF...');
    // You can use libraries like jsPDF or html2pdf
  }

  exportToExcel() {
    // Implementation for Excel export
    console.log('Exporting to Excel...');
    // You can use libraries like xlsx
  }

  // Advanced Analytics
  calculateUserEngagement() {
    // Calculate engagement based on user activity
    const activeUsers = this.activeUsers;
    const totalUsers = this.totalUsers;
    this.userEngagement = totalUsers > 0 ? (activeUsers / totalUsers) * 100 : 0;
  }

}
