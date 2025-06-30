import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Invoice } from '../models/invoice.model';
import { TokenHandler } from './token-handler';

export interface PaymentRequest {
  sponsorId: string;
  companyId: string;
  benefitIds: string[];
  amount: number;
  paymentMethod: string;
  bankAccountId?: string;
}

export interface SponsorStats {
  totalSponsorships: number;
  totalAmountSpent: number;
  totalInvoices: number;
  totalSpentFromInvoices: number;
}

export interface CompanyStats {
  totalSponsorships: number;
  totalAmountReceived: number;
  totalInvoices: number;
  totalReceivedFromInvoices: number;
}

export interface CheckoutPaymentSessionRequest {
  amount: number;
  currency: string;
  reference: string;
  processing_channel_id?: string;
  shipping: {
    address: {
      address_line1: string;
      address_line2?: string;
      city: string;
      state: string;
      zip: string;
      country: string;
    };
  };
  billing: {
    address: {
      address_line1: string;
      address_line2?: string;
      city: string;
      state: string;
      zip: string;
      country: string;
    };
  };
  threeDs: {
    enabled: boolean;
    attempt_n3d: boolean;
    challenge_indicator: string;
    exemption: string;
    allow_upgrade: boolean;
  };
  enabled_payment_methods: string[];
  success_url: string;
  failure_url: string;
  metadata: {
    sponsor_id: string;
    company_id: string;
    benefit_ids: string;
  };
}

export interface CheckoutPaymentSessionResponse {
  id: string;
  session_secret: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = 'http://localhost:8080/api/payments';

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders {
    const token = TokenHandler.getToken();
    console.log('Token from storage:', token);
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  /**
   * Process a payment and generate invoice
   */
  processPayment(paymentRequest: PaymentRequest): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/process`, paymentRequest, { headers: this.getAuthHeaders() });
  }

  /**
   * Get invoices for a sponsor
   */
  getInvoicesBySponsor(sponsorId: string): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${this.apiUrl}/sponsor/${sponsorId}/invoices`, { headers: this.getAuthHeaders() });
  }

  /**
   * Get invoices for a company
   */
  getInvoicesByCompany(companyId: string): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${this.apiUrl}/company/${companyId}/invoices`, { headers: this.getAuthHeaders() });
  }

  /**
   * Get sponsor statistics
   */
  getSponsorStats(sponsorId: string): Observable<SponsorStats> {
    return this.http.get<SponsorStats>(`${this.apiUrl}/sponsor/${sponsorId}/stats`, { headers: this.getAuthHeaders() });
  }

  /**
   * Get company statistics
   */
  getCompanyStats(companyId: string): Observable<CompanyStats> {
    return this.http.get<CompanyStats>(`${this.apiUrl}/company/${companyId}/stats`, { headers: this.getAuthHeaders() });
  }

  /**
   * Create Checkout.com payment session
   */
  createCheckoutSession(request: CheckoutPaymentSessionRequest): Observable<CheckoutPaymentSessionResponse> {
    return this.http.post<CheckoutPaymentSessionResponse>(`${this.apiUrl}/checkout-session`, request, { headers: this.getAuthHeaders() });
  }
} 