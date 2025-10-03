import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Invoice } from '../models/invoice.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private apiUrl = environment.invoicesUrl;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  getAll(): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(this.apiUrl);
  }

  // Admin method to get all invoices in the system
  getAllAsAdmin(): Observable<Invoice[]> {
    // The backend has a simple /api/invoices GET endpoint
    // Try both with and without auth headers to handle different configurations
    return this.http.get<Invoice[]>(this.apiUrl, { headers: this.getAuthHeaders() });
  }

  getById(id: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/${id}`);
  }

  create(data: Invoice): Observable<Invoice> {
    return this.http.post<Invoice>(this.apiUrl, data);
  }

  update(id: number, data: Invoice): Observable<Invoice> {
    return this.http.put<Invoice>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Download PDF invoice
  downloadInvoicePdf(invoiceId: string): Observable<Blob> {
    // Backend has /api/invoices/{id}/pdf endpoint (public access)
    return this.http.get(`${this.apiUrl}/${invoiceId}/pdf`, {
      responseType: 'blob'
    });
  }

  // Alternative PDF download method (with auth just in case)
  downloadInvoicePdfAlt(invoiceId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${invoiceId}/pdf`, {
      headers: this.getAuthHeaders(),
      responseType: 'blob'
    });
  }

  // Get invoice statistics for admin
  getInvoiceStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats`, { headers: this.getAuthHeaders() });
  }
}