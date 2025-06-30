import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RecognitionBenefits } from '../models/recognition-benefits.model';

@Injectable({ providedIn: 'root' })
export class RecognitionBenefitsService {
  private apiUrl = 'http://localhost:8080/api/recognition-benefits';

  constructor(private http: HttpClient) {}

  getAll(): Observable<RecognitionBenefits[]> {
    return this.http.get<RecognitionBenefits[]>(this.apiUrl);
  }

  getById(id: number): Observable<RecognitionBenefits> {
    return this.http.get<RecognitionBenefits>(`${this.apiUrl}/${id}`);
  }

  getAllByCompanyId(companyId: string): Observable<RecognitionBenefits[]> {
    return this.http.get<RecognitionBenefits[]>(`${this.apiUrl}/company/${companyId}`);
  }

  create(data: RecognitionBenefits): Observable<RecognitionBenefits> {
    return this.http.post<RecognitionBenefits>(this.apiUrl, data);
  }

  update(id: number, data: RecognitionBenefits): Observable<RecognitionBenefits> {
    return this.http.put<RecognitionBenefits>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
} 