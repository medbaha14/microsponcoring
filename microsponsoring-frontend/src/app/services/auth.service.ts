import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequest } from '../models/login-request.model';
import { RegisterRequest } from '../models/register-request.model';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  login(data: LoginRequest): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/login`, data);
  }

  register(data: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/register`, data);
  }

  forgotPassword(data: { email: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/forgot-password`, data);
  }

  resetPassword(data: { token: string; newPassword: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/reset-password`, data);
  }

  validateResetToken(token: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/validate-reset-token?token=${token}`);
  }
} 