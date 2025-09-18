export interface PaymentTransaction {
  transactionId: string;
  sponsorId: string;
  companyId?: string;
  amount: number | string; // Backend returns BigDecimal as string
  currency: string;
  paymentMethod: string;
  transactionReference?: string;
  bankReference?: string;
  description?: string;
  status: TransactionStatus;
  type: TransactionType;
  transactionDate: string;
  processedDate?: string;
  uploadedFileName?: string;
  uploadedFilePath?: string;
  uploadedFileType?: string;
  uploadedFileSize?: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

export enum TransactionType {
  SPONSORSHIP = 'SPONSORSHIP',
  REFUND = 'REFUND',
  ADJUSTMENT = 'ADJUSTMENT',
  FEE = 'FEE',
  OTHER = 'OTHER'
}

export interface TransactionSummary {
  totalAmount: number | string; // Backend returns BigDecimal as string
  totalCount: number;
  pendingAmount: number | string; // Backend returns BigDecimal as string
  pendingCount: number;
  recentTransactions: PaymentTransaction[];
}

export interface TransactionFilters {
  status?: TransactionStatus;
  type?: TransactionType;
  startDate?: string;
  endDate?: string;
  searchTerm?: string;
}
