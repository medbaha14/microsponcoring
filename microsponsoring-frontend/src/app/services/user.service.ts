import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
<<<<<<< Updated upstream
import { OrganisationProfile } from '../models/OrganisationProfile';
import { environment } from '../../environments/environment';
=======
import { OrganisationProfile } from '../models/organisation-profile.model';
import { environment } from '../../environments/environment';
import { TokenHandler } from './token-handler';
>>>>>>> Stashed changes

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = environment.usersUrl;
  private uploadUrl = environment.uploadUrl;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = TokenHandler.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

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
    return this.http.get<OrganisationProfile>(`${this.apiUrl}/${userId}/organisation-profile`, 
      { headers: this.getAuthHeaders() });
  }

  updateOrganisationProfile(userId: string, profile: OrganisationProfile): Observable<void> {
    console.log('UserService: Updating organization profile for user:', userId);
    console.log('UserService: Profile data:', profile);
    console.log('UserService: API URL:', `${this.apiUrl}/${userId}/organisation-profile`);
    
    return this.http.put<void>(`${this.apiUrl}/${userId}/organisation-profile`, profile, 
      { headers: this.getAuthHeaders() });
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
    return this.http.get<User>(`${environment.usersUrl}/sponsor/${sponsorId}`);
  }

  getUsersBySponsorIds(sponsorIds: string[]) {
    return this.http.post<{ [sponsorId: string]: User }>(`${environment.usersUrl}/by-sponsor-ids`, sponsorIds);
<<<<<<< Updated upstream
=======
  }
  
  initializeUserProfiles(userId: string): Observable<any> {
    console.log('UserService: Initializing profiles for user:', userId);
    return this.http.post<any>(`${this.apiUrl}/${userId}/initialize-profiles`, {}, 
      { headers: this.getAuthHeaders() });
>>>>>>> Stashed changes
  }
} 