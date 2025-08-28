import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CompanyNonProfits } from '../models/companies-non-profits.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class companyNonProfitsService {
  private apiUrl = environment.companiesUrl;

  constructor(private http: HttpClient) {}

  getAll(): Observable<CompanyNonProfits[]> {
    return this.http.get<CompanyNonProfits[]>(this.apiUrl);
  }

  getById(id: number): Observable<CompanyNonProfits> {
    return this.http.get<CompanyNonProfits>(`${this.apiUrl}/${id}`);
  }

  getCompanyByUserId(userId: string): Observable<CompanyNonProfits> {
    return this.http.get<CompanyNonProfits>(`${this.apiUrl}/user/${userId}`);
  }

  create(data: CompanyNonProfits): Observable<CompanyNonProfits> {
    return this.http.post<CompanyNonProfits>(this.apiUrl, data);
  }

  update(id: number, data: CompanyNonProfits): Observable<CompanyNonProfits> {
    return this.http.put<CompanyNonProfits>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
} 