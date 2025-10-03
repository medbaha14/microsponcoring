import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../../services/invoice.service';
import { Invoice } from '../../../models/invoice.model';
import { ThemeService } from '../../../services/theme.service';

interface InvoiceStats {
  totalInvoices: number;
  totalAmount: number;
  paidAmount: number;
  pendingAmount: number;
  paidCount: number;
  pendingCount: number;
}

@Component({
  selector: 'app-invoices',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './invoices.component.html',
  styleUrl: './invoices.component.css'
})
export class InvoicesComponent implements OnInit {
  invoices: Invoice[] = [];
  filteredInvoices: Invoice[] = [];
  isLoading = true;
  error: string | null = null;
  isDarkMode = false;
  
  // Filtering and sorting
  searchTerm = '';
  statusFilter = 'all';
  sortField = 'createdAt';
  sortDirection: 'asc' | 'desc' = 'desc';
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 0;
  
  // Statistics
  stats: InvoiceStats = {
    totalInvoices: 0,
    totalAmount: 0,
    paidAmount: 0,
    pendingAmount: 0,
    paidCount: 0,
    pendingCount: 0
  };

  constructor(
    private invoiceService: InvoiceService,
    private themeService: ThemeService
  ) {}

  ngOnInit() {
    // Subscribe to theme changes
    this.themeService.theme$.subscribe(theme => {
      this.isDarkMode = theme === 'dark';
    });
    
    this.loadInvoices();
  }

  loadInvoices() {
    this.isLoading = true;
    this.error = null;
    
    this.invoiceService.getAllAsAdmin().subscribe({
      next: (invoices) => {
        this.invoices = invoices;
        this.applyFilters();
        this.calculateStats();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading invoices (with auth):', error);
        console.error('API URL attempted:', `${this.invoiceService['apiUrl']}`);
        console.error('Error status:', error.status);
        console.error('Error message:', error.message);
        
        // If auth fails (403), try without auth headers
        if (error.status === 403) {
          console.log('Trying without auth headers...');
          this.invoiceService.getAll().subscribe({
            next: (invoices) => {
              this.invoices = invoices;
              this.applyFilters();
              this.calculateStats();
              this.isLoading = false;
              console.log('Successfully loaded invoices without auth');
            },
            error: (noAuthError) => {
              console.error('Error loading invoices (without auth):', noAuthError);
              this.handleLoadError(noAuthError);
            }
          });
        } else {
          this.handleLoadError(error);
        }
      }
    });
  }

  private handleLoadError(error: any) {
    if (error.status === 403) {
      this.error = 'Access denied. Admin permissions required.';
    } else if (error.status === 404) {
      this.error = 'Invoice API endpoint not found. Please check backend configuration.';
    } else if (error.status === 0) {
      this.error = 'Cannot connect to server. Please check if backend is running.';
    } else {
      this.error = `Failed to load invoices (Error ${error.status}). Please try again.`;
    }
    
    this.isLoading = false;
    // Load sample data for demonstration
    this.loadSampleData();
  }

  loadSampleData() {
    // Sample data for demonstration when backend is not available
    this.invoices = [
      {
        invoiceId: 'INV-001',
        amount: 500.00,
        status: 'PAID',
        paymentStatus: 'PAID',
        createdAt: new Date('2024-01-15'),
        invoiceDate: new Date('2024-01-15'),
        company: { name: 'Tech Nonprofit', email: 'contact@technonprofit.org' },
        sponsor: { user: { username: 'johndoe', email: 'john@example.com' } },
        generatedPdfUrl: 'invoice-001.pdf'
      },
      {
        invoiceId: 'INV-002',
        amount: 750.00,
        status: 'PENDING',
        paymentStatus: 'PENDING',
        createdAt: new Date('2024-01-20'),
        invoiceDate: new Date('2024-01-20'),
        company: { name: 'Green Initiative', email: 'info@greeninit.org' },
        sponsor: { user: { username: 'janedoe', email: 'jane@example.com' } },
        generatedPdfUrl: 'invoice-002.pdf'
      },
      {
        invoiceId: 'INV-003',
        amount: 1000.00,
        status: 'PAID',
        paymentStatus: 'PAID',
        createdAt: new Date('2024-02-01'),
        invoiceDate: new Date('2024-02-01'),
        company: { name: 'Education Foundation', email: 'hello@edufoundation.org' },
        sponsor: { user: { username: 'bobsmith', email: 'bob@example.com' } },
        generatedPdfUrl: 'invoice-003.pdf'
      }
    ];
    this.applyFilters();
    this.calculateStats();
    this.isLoading = false;
  }

  applyFilters() {
    let filtered = [...this.invoices];
    
    // Search filter
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(invoice => 
        invoice.invoiceId?.toLowerCase().includes(term) ||
        invoice.company?.name?.toLowerCase().includes(term) ||
        invoice.sponsor?.user?.username?.toLowerCase().includes(term) ||
        invoice.sponsor?.user?.email?.toLowerCase().includes(term)
      );
    }
    
    // Status filter
    if (this.statusFilter !== 'all') {
      filtered = filtered.filter(invoice => invoice.status === this.statusFilter);
    }
    
    // Sort
    filtered.sort((a, b) => {
      let aValue: any, bValue: any;
      
      switch (this.sortField) {
        case 'amount':
          aValue = a.amount;
          bValue = b.amount;
          break;
        case 'createdAt':
          aValue = new Date(a.createdAt || 0);
          bValue = new Date(b.createdAt || 0);
          break;
        case 'company':
          aValue = a.company?.name || '';
          bValue = b.company?.name || '';
          break;
        case 'sponsor':
          aValue = a.sponsor?.user?.username || '';
          bValue = b.sponsor?.user?.username || '';
          break;
        default:
          aValue = a.invoiceId || '';
          bValue = b.invoiceId || '';
      }
      
      if (aValue < bValue) return this.sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
    
    this.filteredInvoices = filtered;
    this.totalPages = Math.ceil(filtered.length / this.itemsPerPage);
    this.currentPage = 1;
  }

  calculateStats() {
    this.stats = {
      totalInvoices: this.invoices.length,
      totalAmount: this.invoices.reduce((sum, inv) => sum + inv.amount, 0),
      paidAmount: this.invoices.filter(inv => inv.paymentStatus === 'PAID').reduce((sum, inv) => sum + inv.amount, 0),
      pendingAmount: this.invoices.filter(inv => inv.paymentStatus === 'PENDING').reduce((sum, inv) => sum + inv.amount, 0),
      paidCount: this.invoices.filter(inv => inv.paymentStatus === 'PAID').length,
      pendingCount: this.invoices.filter(inv => inv.paymentStatus === 'PENDING').length
    };
  }

  downloadPdf(invoice: Invoice) {
    if (!invoice.invoiceId) {
      alert('Invoice ID not available');
      return;
    }
    
    // First try the main download method
    this.invoiceService.downloadInvoicePdf(invoice.invoiceId).subscribe({
      next: (blob) => {
        this.handlePdfDownload(blob, invoice.invoiceId!);
      },
      error: (error) => {
        console.error('Error downloading PDF (method 1):', error);
        
        // Try alternative download method
        this.invoiceService.downloadInvoicePdfAlt(invoice.invoiceId!).subscribe({
          next: (blob) => {
            this.handlePdfDownload(blob, invoice.invoiceId!);
          },
          error: (altError) => {
            console.error('Error downloading PDF (method 2):', altError);
            
            // If both methods fail, show appropriate error message
            if (error.status === 404 || altError.status === 404) {
              alert('PDF download endpoint not configured on server. Please contact administrator.');
            } else if (error.status === 403 || altError.status === 403) {
              alert('Access denied. You may not have permission to download this invoice.');
            } else {
              alert('Failed to download PDF. Please try again later.');
            }
          }
        });
      }
    });
  }

  private handlePdfDownload(blob: Blob, invoiceId: string) {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `invoice-${invoiceId}.pdf`;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  sort(field: string) {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.applyFilters();
  }

  onSearchChange() {
    this.applyFilters();
  }

  onStatusFilterChange() {
    this.applyFilters();
  }

  getPaginatedInvoices(): Invoice[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return this.filteredInvoices.slice(startIndex, endIndex);
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PAID': return 'status-paid';
      case 'PENDING': return 'status-pending';
      case 'CANCELLED': return 'status-cancelled';
      default: return 'status-unknown';
    }
  }

  formatDate(date: Date | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString();
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  refreshData() {
    this.loadInvoices();
  }
}
