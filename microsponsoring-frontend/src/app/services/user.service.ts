import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { OrganisationProfile } from '../models/OrganisationProfile';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = 'http://localhost:8080/api/users';
  private uploadUrl = 'http://localhost:8080/api/upload';

  constructor(private http: HttpClient) {}

  getAll(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  getById(userId: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${userId}`);
  }

  create(user: User): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }

  update(userId: string, user: User): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${userId}`, user);
  }

  delete(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}`);
  }

  block(userId: string): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${userId}/block`, {});
  }

  deblock(userId: string): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${userId}/deblock`, {});
  }

  getOrganisationProfile(userId: string): Observable<OrganisationProfile> {
    return this.http.get<OrganisationProfile>(`${this.apiUrl}/${userId}/organisation-profile`);
  }

  updateOrganisationProfile(userId: string, profile: OrganisationProfile): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${userId}/organisation-profile`, profile);
  }

  uploadProfilePicture(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<string>(`${this.uploadUrl}/profile-picture`, formData);
  }

  getAllByRole(role: string): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/role/${role}`);
  }

  getUserBySponsorId(sponsorId: string) {
    return this.http.get<User>(`http://localhost:8080/api/users/sponsor/${sponsorId}`);
  }

  getUsersBySponsorIds(sponsorIds: string[]) {
    return this.http.post<{ [sponsorId: string]: User }>(`http://localhost:8080/api/users/by-sponsor-ids`, sponsorIds);
  }
} 