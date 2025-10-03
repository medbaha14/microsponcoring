import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CompanyNonProfits } from '../models/companies-non-profits.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class companyNonProfitsService {
  private apiUrl = environment.companiesUrl;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  getAll(): Observable<CompanyNonProfits[]> {
    return this.http.get<CompanyNonProfits[]>(this.apiUrl);
  }

  getById(id: string): Observable<CompanyNonProfits> {
    return this.http.get<CompanyNonProfits>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  getCompanyByUserId(userId: string): Observable<CompanyNonProfits> {
    return this.http.get<CompanyNonProfits>(`${this.apiUrl}/user/${userId}`);
  }

  create(data: CompanyNonProfits): Observable<CompanyNonProfits> {
    console.log('CompanyService: Creating company with data:', data);
    return this.http.post<CompanyNonProfits>(this.apiUrl, data, { headers: this.getAuthHeaders() });
  }

  update(id: string, data: CompanyNonProfits): Observable<CompanyNonProfits> {
    return this.http.put<CompanyNonProfits>(`${this.apiUrl}/${id}`, data, { headers: this.getAuthHeaders() });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }
}