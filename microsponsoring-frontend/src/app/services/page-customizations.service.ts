import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageCustomizations } from '../models/page-customizations.model';

@Injectable({ providedIn: 'root' })
export class PageCustomizationsService {
  private apiUrl = 'http://localhost:8080/api/page-customizations';

  constructor(private http: HttpClient) {}

  getAll(): Observable<PageCustomizations[]> {
    return this.http.get<PageCustomizations[]>(this.apiUrl);
  }

  getById(id: number): Observable<PageCustomizations> {
    return this.http.get<PageCustomizations>(`${this.apiUrl}/${id}`);
  }

  create(data: PageCustomizations): Observable<PageCustomizations> {
    return this.http.post<PageCustomizations>(this.apiUrl, data);
  }

  update(id: number, data: PageCustomizations): Observable<PageCustomizations> {
    return this.http.put<PageCustomizations>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
} 