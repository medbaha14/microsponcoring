import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Sponsor } from '../models/sponsor.model';

@Injectable({ providedIn: 'root' })
export class SponsorService {
  private apiUrl = 'http://localhost:8080/api/sponsors';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Sponsor[]> {
    return this.http.get<Sponsor[]>(this.apiUrl);
  }

  getById(id: number): Observable<Sponsor> {
    return this.http.get<Sponsor>(`${this.apiUrl}/${id}`);
  }

  getByUserId(userId: string): Observable<Sponsor> {
    return this.http.get<Sponsor>(`${this.apiUrl}/user/${userId}`);
  }

  create(data: Sponsor): Observable<Sponsor> {
    return this.http.post<Sponsor>(this.apiUrl, data);
  }

  update(id: number, data: Sponsor): Observable<Sponsor> {
    return this.http.put<Sponsor>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
} 