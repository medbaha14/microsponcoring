import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { Sponsor } from '../models/sponsor.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SponsorService {
  private apiUrl = environment.sponsorsUrl;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') ?? '';
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  getAll(): Observable<Sponsor[]> {
    return this.http.get<Sponsor[]>(this.apiUrl, { headers: this.getAuthHeaders() });
  }

  getById(id: string): Observable<Sponsor> {
    return this.http.get<Sponsor>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  getByUserId(userId: string): Observable<Sponsor> {
    return this.http.get<Sponsor>(`${this.apiUrl}/user/${userId}`, { headers: this.getAuthHeaders() });
  }

  create(data: Sponsor): Observable<Sponsor> {
    return this.http.post<Sponsor>(this.apiUrl, data, { headers: this.getAuthHeaders() });
  }

  update(id: string, data: Partial<Sponsor>): Observable<Sponsor> {
    return this.http.put<Sponsor>(`${this.apiUrl}/${id}`, data, { headers: this.getAuthHeaders() });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  // Optionnel : éditer à partir de userId directement
  updateByUserId(userId: string, data: Partial<Sponsor>): Observable<Sponsor> {
    return this.getByUserId(userId).pipe(
      switchMap(sp => this.update(String(sp.sponsorId), data))
    );
  }
}
