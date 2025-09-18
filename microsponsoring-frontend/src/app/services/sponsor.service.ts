import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Sponsor } from '../models/sponsor.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SponsorService {
  private apiUrl = environment.sponsorsUrl;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  getAll(): Observable<Sponsor[]> {
    return this.http.get<Sponsor[]>(this.apiUrl);
  }

  getById(id: string): Observable<Sponsor> {
    return this.http.get<Sponsor>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  getByUserId(userId: string): Observable<Sponsor> {
    return this.http.get<Sponsor>(`${this.apiUrl}/user/${userId}`);
  }

  create(data: Sponsor): Observable<Sponsor> {
    return this.http.post<Sponsor>(this.apiUrl, data, { headers: this.getAuthHeaders() });
  }

  update(id: string, data: Sponsor): Observable<Sponsor> {
    return this.http.put<Sponsor>(`${this.apiUrl}/${id}`, data, { headers: this.getAuthHeaders() });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }
}