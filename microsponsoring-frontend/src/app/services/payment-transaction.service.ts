import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentTransaction, TransactionSummary, TransactionFilters, TransactionStatus } from '../models/payment-transaction.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PaymentTransactionService {
  private apiUrl = environment.apiUrl + '/payment-transactions';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  getTransactionsBySponsor(sponsorId: string): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.apiUrl}/sponsor/${sponsorId}`, {
      headers: this.getAuthHeaders()
    });
  }

  getTransactionSummary(sponsorId: string): Observable<TransactionSummary> {
    return this.http.get<TransactionSummary>(`${this.apiUrl}/sponsor/${sponsorId}/summary`, {
      headers: this.getAuthHeaders()
    });
  }

  getTransactionsByStatus(sponsorId: string, status: TransactionStatus): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.apiUrl}/sponsor/${sponsorId}/status/${status}`, {
      headers: this.getAuthHeaders()
    });
  }

  getTransactionsByDateRange(sponsorId: string, startDate: string, endDate: string): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.apiUrl}/sponsor/${sponsorId}/date-range`, {
      headers: this.getAuthHeaders(),
      params: { startDate, endDate }
    });
  }

  createTransaction(transaction: PaymentTransaction): Observable<PaymentTransaction> {
    return this.http.post<PaymentTransaction>(this.apiUrl, transaction, {
      headers: this.getAuthHeaders()
    });
  }

  updateTransaction(transactionId: string, transaction: PaymentTransaction): Observable<PaymentTransaction> {
    return this.http.put<PaymentTransaction>(`${this.apiUrl}/${transactionId}`, transaction, {
      headers: this.getAuthHeaders()
    });
  }

  deleteTransaction(transactionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${transactionId}`, {
      headers: this.getAuthHeaders()
    });
  }

  uploadTransactionFile(transactionId: string, file: File): Observable<PaymentTransaction> {
    const formData = new FormData();
    formData.append('file', file);
    
    const token = localStorage.getItem('token');
    return this.http.post<PaymentTransaction>(`${this.apiUrl}/${transactionId}/upload`, formData, {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${token}`
        // Don't set Content-Type for FormData, let browser set it
      })
    });
  }

  downloadTransactionFile(transactionId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${transactionId}/download`, {
      headers: this.getAuthHeaders(),
      responseType: 'blob'
    });
  }

  searchTransactions(sponsorId: string, searchTerm: string): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.apiUrl}/sponsor/${sponsorId}/search`, {
      headers: this.getAuthHeaders(),
      params: { searchTerm }
    });
  }

  getUploadedTransactions(sponsorId: string): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.apiUrl}/sponsor/${sponsorId}/uploaded`, {
      headers: this.getAuthHeaders()
    });
  }

  getFilteredTransactions(sponsorId: string, filters: TransactionFilters): Observable<PaymentTransaction[]> {
    let url = `${this.apiUrl}/sponsor/${sponsorId}`;
    const params: any = {};

    if (filters.status) {
      url = `${this.apiUrl}/sponsor/${sponsorId}/status/${filters.status}`;
    } else if (filters.startDate && filters.endDate) {
      url = `${this.apiUrl}/sponsor/${sponsorId}/date-range`;
      params.startDate = filters.startDate;
      params.endDate = filters.endDate;
    } else if (filters.searchTerm) {
      url = `${this.apiUrl}/sponsor/${sponsorId}/search`;
      params.searchTerm = filters.searchTerm;
    }

    return this.http.get<PaymentTransaction[]>(url, {
      headers: this.getAuthHeaders(),
      params
    });
  }
}
