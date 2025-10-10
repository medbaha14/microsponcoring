import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaymentTransactionService } from '../../../services/payment-transaction.service';
import { PaymentService } from '../../../services/payment.service';
import { InvoiceService } from '../../../services/invoice.service';
import { PaymentTransaction, TransactionSummary, TransactionFilters, TransactionStatus, TransactionType } from '../../../models/payment-transaction.model';
import { ThemeService } from '../../../services/theme.service';
import { UserService } from '../../../services/user.service';
import { User } from '../../../models/user.model';
import { SponsorService } from '../../../services/sponsor.service';
import { Sponsor } from '../../../models/sponsor.model';
import { Invoice } from '../../../models/invoice.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-payment-tracking',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule],
  templateUrl: './payment-tracking.component.html',
  styleUrls: ['./payment-tracking.component.css']
})
export class PaymentTrackingComponent implements OnInit, OnDestroy {
  transactions: PaymentTransaction[] = [];
  filteredTransactions: PaymentTransaction[] = [];
  summary: TransactionSummary | null = null;
  currentUser: User | null = null;
  currentSponsor: Sponsor | null = null;
  
  // Make Math available in template
  Math = Math;
  
  // Loading states
  isLoading = true;
  isLoadingSummary = true;
  
  // Filters
  filters: TransactionFilters = {};
  searchTerm = '';
  selectedStatus: TransactionStatus | '' = '';
  selectedType: TransactionType | '' = '';
  startDate = '';
  endDate = '';
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 0;
  
  
  // Theme
  isDarkMode = false;
  
  // Chart data
  chartData: any = null;
  chartOptions: any = null;

  constructor(
    private paymentTransactionService: PaymentTransactionService,
    private paymentService: PaymentService,
    private invoiceService: InvoiceService,
    private themeService: ThemeService,
    private userService: UserService,
    private sponsorService: SponsorService
  ) {}

  ngOnInit() {
    this.themeService.theme$.subscribe(theme => {
      this.isDarkMode = theme === 'dark';
    });
    
    this.loadCurrentUser();
  }

  ngOnDestroy() {
    // Cleanup if needed
  }

  loadCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
      this.loadSponsorProfile();
    }
  }

  loadSponsorProfile() {
    if (this.currentUser?.userId) {
      console.log('Payment Tracking: Loading sponsor profile for user:', this.currentUser.userId);
      this.sponsorService.getByUserId(this.currentUser.userId).subscribe({
        next: (sponsor) => {
          console.log('Payment Tracking: Sponsor profile loaded:', sponsor);
          this.currentSponsor = sponsor;
          this.loadData();
        },
        error: (error) => {
          console.error('Error loading sponsor profile:', error);
          console.error('Error details:', error.status, error.message);
          this.isLoading = false;
        }
      });
    } else {
      console.error('Payment Tracking: No user ID found in localStorage');
      this.isLoading = false;
    }
  }

  loadData() {
    if (this.currentSponsor?.sponsorId) {
      this.loadSummary(); // This will now load both summary and transactions
    }
  }

  loadTransactions() {
    this.isLoading = true;
    console.log('Payment Tracking: Loading transactions for sponsor:', this.currentSponsor!.sponsorId);
    this.paymentTransactionService.getTransactionsBySponsor(this.currentSponsor!.sponsorId).subscribe({
      next: (transactions) => {
        console.log('Payment Tracking: Transactions loaded:', transactions);
        console.log('Payment Tracking: Number of transactions:', transactions.length);
        if (transactions.length > 0) {
          console.log('Payment Tracking: First transaction amount:', transactions[0].amount, 'Type:', typeof transactions[0].amount);
        }
        this.transactions = transactions;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading transactions:', error);
        this.isLoading = false;
      }
    });
  }

  loadSummary() {
    this.isLoadingSummary = true;
    
    // First try to get payment stats from the existing payments API
    this.paymentService.getSponsorStats(this.currentSponsor!.sponsorId).subscribe({
      next: (stats: any) => {
        console.log('Payment Tracking: Payment stats loaded:', stats);
        
        // Convert payment stats to our summary format
        this.summary = {
          totalAmount: stats.totalAmountSpent || 0,
          totalCount: stats.totalSponsorships || 0,
          pendingAmount: 0, // We don't have pending data from this endpoint
          pendingCount: 0,
          recentTransactions: [] // We'll load this separately
        };
        
        this.isLoadingSummary = false;
        this.updateChartData();
        
        // Now load the actual invoice data for transactions
        this.loadInvoiceTransactions();
      },
      error: (error: any) => {
        console.error('Error loading payment stats:', error);
        // Fallback to payment transaction service
        this.paymentTransactionService.getTransactionSummary(this.currentSponsor!.sponsorId).subscribe({
          next: (summary) => {
            console.log('Payment Tracking: Transaction summary loaded:', summary);
            this.summary = summary;
            this.isLoadingSummary = false;
            this.updateChartData();
          },
          error: (fallbackError) => {
            console.error('Error loading transaction summary:', fallbackError);
            this.isLoadingSummary = false;
          }
        });
      }
    });
  }

  loadInvoiceTransactions() {
    this.isLoading = true;
    console.log('Payment Tracking: Loading invoice transactions for sponsor:', this.currentSponsor!.sponsorId);
    
    this.invoiceService.getAll().subscribe({
      next: (invoices) => {
        console.log('Payment Tracking: All invoices loaded:', invoices);
        
        // Filter invoices for this sponsor
        const sponsorInvoices = invoices.filter(invoice => 
          invoice.sponsor && invoice.sponsor.sponsorId === this.currentSponsor!.sponsorId
        );
        
        console.log('Payment Tracking: Sponsor invoices:', sponsorInvoices);
        
        // Convert invoices to payment transactions
        this.transactions = sponsorInvoices.map(invoice => this.convertInvoiceToTransaction(invoice));
        
        console.log('Payment Tracking: Converted transactions:', this.transactions);
        
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading invoices:', error);
        this.isLoading = false;
      }
    });
  }

  convertInvoiceToTransaction(invoice: Invoice): PaymentTransaction {
    // Helper function to convert date to string
    const dateToString = (date: string | Date | undefined): string => {
      if (!date) return new Date().toISOString();
      if (typeof date === 'string') {
        // If it's already a string, try to parse it to ensure it's valid
        try {
          return new Date(date).toISOString();
        } catch {
          return new Date().toISOString();
        }
      }
      if (date instanceof Date) {
        return date.toISOString();
      }
      // Fallback for any other type
      return new Date().toISOString();
    };

    return {
      transactionId: invoice.invoiceId || '',
      sponsorId: invoice.sponsor?.sponsorId || '',
      companyId: invoice.company?.companyId || '',
      amount: invoice.amount || 0,
      currency: 'EUR',
      paymentMethod: 'CREDIT_CARD', // Default since we don't have this in invoice
      transactionReference: invoice.invoiceId || '',
      description: `Sponsorship payment for ${invoice.company?.user?.fullName || 'Organization'}`,
      status: this.convertInvoiceStatusToTransactionStatus(invoice.status || 'PENDING'),
      type: TransactionType.SPONSORSHIP,
      transactionDate: dateToString(invoice.createdAt || invoice.invoiceDate),
      processedDate: invoice.status === 'PAID' ? dateToString(invoice.updatedAt) : undefined,
      uploadedFileName: invoice.generatedPdfUrl ? 'invoice.pdf' : undefined,
      uploadedFilePath: invoice.generatedPdfUrl ? `${environment.baseUrl}${invoice.generatedPdfUrl}` : undefined,
      notes: `Invoice ${invoice.invoiceId}`,
      createdAt: dateToString(invoice.createdAt),
      updatedAt: dateToString(invoice.updatedAt)
    };
  }

  convertInvoiceStatusToTransactionStatus(invoiceStatus: string): TransactionStatus {
    switch (invoiceStatus.toUpperCase()) {
      case 'PAID':
        return TransactionStatus.COMPLETED;
      case 'PENDING':
        return TransactionStatus.PENDING;
      case 'FAILED':
        return TransactionStatus.FAILED;
      case 'CANCELLED':
        return TransactionStatus.CANCELLED;
      default:
        return TransactionStatus.PENDING;
    }
  }

  applyFilters() {
    let filtered = [...this.transactions];

    if (this.selectedStatus) {
      filtered = filtered.filter(t => t.status === this.selectedStatus);
    }

    if (this.selectedType) {
      filtered = filtered.filter(t => t.type === this.selectedType);
    }

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(t => 
        t.description?.toLowerCase().includes(term) ||
        t.transactionReference?.toLowerCase().includes(term) ||
        t.bankReference?.toLowerCase().includes(term)
      );
    }

    if (this.startDate && this.endDate) {
      const start = new Date(this.startDate);
      const end = new Date(this.endDate);
      filtered = filtered.filter(t => {
        const transactionDate = new Date(t.transactionDate);
        return transactionDate >= start && transactionDate <= end;
      });
    }

    this.filteredTransactions = filtered;
    this.updatePagination();
  }

  updatePagination() {
    this.totalPages = Math.ceil(this.filteredTransactions.length / this.itemsPerPage);
    this.currentPage = 1;
  }

  getPaginatedTransactions(): PaymentTransaction[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return this.filteredTransactions.slice(startIndex, endIndex);
  }

  onSearchChange() {
    this.applyFilters();
  }

  onFilterChange() {
    this.applyFilters();
  }

  clearFilters() {
    this.searchTerm = '';
    this.selectedStatus = '';
    this.selectedType = '';
    this.startDate = '';
    this.endDate = '';
    this.applyFilters();
  }


  downloadFile(transaction: PaymentTransaction) {
    // Use the invoice service to download the PDF with proper authentication
    if (transaction.transactionId) {
      console.log('Downloading invoice PDF for transaction:', transaction.transactionId);
      
      // Try authenticated download first
      this.invoiceService.downloadInvoicePdfAlt(transaction.transactionId).subscribe({
        next: (blob) => {
          console.log('Download successful with authentication');
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = transaction.uploadedFileName || 'invoice.pdf';
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error: (authError) => {
          console.log('Authenticated download failed, trying public access:', authError);
          // Fallback: try public access
          this.invoiceService.downloadInvoicePdf(transaction.transactionId).subscribe({
            next: (blob) => {
              console.log('Download successful with public access');
              const url = window.URL.createObjectURL(blob);
              const link = document.createElement('a');
              link.href = url;
              link.download = transaction.uploadedFileName || 'invoice.pdf';
              link.click();
              window.URL.revokeObjectURL(url);
            },
            error: (publicError) => {
              console.error('Both authenticated and public download failed:', publicError);
              // Final fallback: try direct URL
              if (transaction.uploadedFilePath) {
                console.log('Trying direct URL as final fallback:', transaction.uploadedFilePath);
                const link = document.createElement('a');
                link.href = transaction.uploadedFilePath;
                link.download = transaction.uploadedFileName || 'invoice.pdf';
                link.target = '_blank';
                link.click();
              }
            }
          });
        }
      });
    } else {
      console.warn('No transaction ID available for download');
    }
  }

  updateChartData() {
    if (!this.summary) return;

    // Create chart data for monthly spending
    const monthlyData = this.calculateMonthlySpending();
    
    this.chartData = {
      labels: monthlyData.labels,
      datasets: [{
        label: 'Monthly Spending',
        data: monthlyData.amounts,
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
      }]
    };

    this.chartOptions = {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: function(value: any) {
              return '€' + value.toFixed(2);
            }
          }
        }
      }
    };
  }

  calculateMonthlySpending() {
    const monthlyData: { [key: string]: number } = {};
    
    this.transactions.forEach(transaction => {
      if (transaction.status === TransactionStatus.COMPLETED) {
        const amount = typeof transaction.amount === 'string' ? parseFloat(transaction.amount) : transaction.amount;
        if (!isNaN(amount)) {
          const date = new Date(transaction.transactionDate);
          const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
          monthlyData[monthKey] = (monthlyData[monthKey] || 0) + amount;
        }
      }
    });

    const labels = Object.keys(monthlyData).sort();
    const amounts = labels.map(label => monthlyData[label]);

    return { labels, amounts };
  }

  getStatusClass(status: TransactionStatus): string {
    const statusClasses: { [key in TransactionStatus]: string } = {
      [TransactionStatus.PENDING]: 'status-pending',
      [TransactionStatus.PROCESSING]: 'status-processing',
      [TransactionStatus.COMPLETED]: 'status-completed',
      [TransactionStatus.FAILED]: 'status-failed',
      [TransactionStatus.CANCELLED]: 'status-cancelled',
      [TransactionStatus.REFUNDED]: 'status-refunded'
    };
    return statusClasses[status] || 'status-unknown';
  }

  getStatusIcon(status: TransactionStatus): string {
    const statusIcons: { [key in TransactionStatus]: string } = {
      [TransactionStatus.PENDING]: '⏳',
      [TransactionStatus.PROCESSING]: '🔄',
      [TransactionStatus.COMPLETED]: '✅',
      [TransactionStatus.FAILED]: '❌',
      [TransactionStatus.CANCELLED]: '🚫',
      [TransactionStatus.REFUNDED]: '↩️'
    };
    return statusIcons[status] || '❓';
  }

  formatCurrency(amount: number | string): string {
    // Convert string to number if needed (BigDecimal from backend comes as string)
    const numericAmount = typeof amount === 'string' ? parseFloat(amount) : amount;
    
    // Check if the amount is valid
    if (isNaN(numericAmount) || numericAmount === null || numericAmount === undefined) {
      console.warn('Invalid amount value:', amount);
      return '€0.00';
    }
    
    return new Intl.NumberFormat('en-EU', {
      style: 'currency',
      currency: 'EUR'
    }).format(numericAmount);
  }

  exportToCSV() {
    const csvContent = this.generateCSV();
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `payment-transactions-${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  generateCSV(): string {
    const headers = ['Date', 'Amount', 'Description', 'Status', 'Type', 'Reference', 'File'];
    const rows = this.filteredTransactions.map(t => {
      const amount = typeof t.amount === 'string' ? parseFloat(t.amount) : t.amount;
      return [
        new Date(t.transactionDate).toLocaleDateString(),
        isNaN(amount) ? '0' : amount.toString(),
        t.description || '',
        t.status,
        t.type,
        t.transactionReference || '',
        t.uploadedFileName || ''
      ];
    });

    return [headers, ...rows].map(row => row.join(',')).join('\n');
  }
}
