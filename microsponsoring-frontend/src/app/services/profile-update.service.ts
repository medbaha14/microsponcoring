import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { OrganisationProfile } from '../models/organisation-profile.model';

@Injectable({
  providedIn: 'root'
})
export class ProfileUpdateService {
  private profileUpdateSource = new Subject<OrganisationProfile>();
  
  profileUpdate$ = this.profileUpdateSource.asObservable();

  notifyProfileUpdate(profile: OrganisationProfile) {
    this.profileUpdateSource.next(profile);
  }
} 