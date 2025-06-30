import { Component, ChangeDetectorRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrganisationProfileComponent } from '../profile/profile.component';
import { OrganisationProfile } from '../../../models/organisation-profile.model';
import { DomSanitizer } from '@angular/platform-browser';
import { ThemeService } from '../../../services/theme.service';
import { TokenHandler } from '../../../services/token-handler';
import { UserService } from '../../../services/user.service';
import Swal from 'sweetalert2';
import { ProfileUpdateService } from '../../../services/profile-update.service';
import { CompanyNonProfits } from '../../../models/companies-non-profits.model';
import { companyNonProfitsService } from '../../../services/companies-non-profits.service';
import { EditRecognitionBenefitsComponent } from '../edit-recognition-benefits/edit-recognition-benefits.component';

@Component({
  selector: 'app-customize-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, OrganisationProfileComponent, EditRecognitionBenefitsComponent],
  templateUrl: './customize-profile.component.html',
  styleUrl: './customize-profile.component.css'
})
export class CustomizeProfileComponent implements OnInit {
  @ViewChild(OrganisationProfileComponent) profilePreview!: OrganisationProfileComponent;

  profile: OrganisationProfile | null = null;
  company: CompanyNonProfits | null = null;
  isEditBenefitsOverlayVisible = false;
  uploadingImages: { [key: string]: boolean } = {};

  fontOptions = [
    'Arial, sans-serif',
    'Georgia, serif',
    'Courier New, monospace',
    'Tahoma, Geneva, sans-serif',
    'Times New Roman, Times, serif'
  ];

  backgroundStyle: any;
  isDarkMode = false;

  constructor(
    public cdr: ChangeDetectorRef,
    private sanitizer: DomSanitizer,
    private themeService: ThemeService,
    private userService: UserService,
    private profileUpdateService: ProfileUpdateService,
    private companyService: companyNonProfitsService
  ) {
    this.backgroundStyle = this.sanitizer.bypassSecurityTrustStyle('background-color: #1976d2');
    this.themeService.darkMode$.subscribe((isDark: boolean) => {
      this.isDarkMode = isDark;
      this.updateBackgroundStyle();
    }); 
  }

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    const user = TokenHandler.getUser();
    if (user && user.userId) {
      this.userService.getOrganisationProfile(user.userId).subscribe(profile => {
        this.profile = profile;
        this.updateBackgroundStyle();
      });

      this.companyService.getCompanyByUserId(user.userId).subscribe(company => {
        this.company = company;
      });
    }
  }

  async onImageChange(event: any, field: 'profilePicture' | 'logoUrl' | 'bannerImageUrl' | 'backgroundImageUrl') {
    const file = event.target.files[0];
    if (file && this.profile) {
      // Show preview immediately
      console.log(this.profile,field,file);
      
      const reader = new FileReader();
      reader.onload = (e: any) => {
        if (this.profile) {
          this.profile[field] = e.target.result;
          this.cdr.detectChanges();
        }
      };
      reader.readAsDataURL(file);

      // Upload the file
      await this.uploadImage(file, field);
    }
  }

  private async uploadImage(file: File, field: 'profilePicture' | 'logoUrl' | 'bannerImageUrl' | 'backgroundImageUrl') {
    if (!this.profile?.userId) return;

    this.uploadingImages[field] = true;
    const user = TokenHandler.getUser();
    
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('userId', user.userId);

      let uploadUrl: string;
      switch (field) {
        case 'profilePicture':
          uploadUrl = 'http://localhost:8080/api/upload/profile-picture';
          break;
        case 'logoUrl':
          uploadUrl = 'http://localhost:8080/api/upload/organisation-logo';
          break;
        case 'bannerImageUrl':
          uploadUrl = 'http://localhost:8080/api/upload/organisation-banner';
          break;
        case 'backgroundImageUrl':
          uploadUrl = 'http://localhost:8080/api/upload/organisation-background';
          break;
        default:
          throw new Error('Invalid field type');
      }

      const response = await fetch(uploadUrl, {
        method: 'POST',
        body: formData,
        headers: {
          'Authorization': `Bearer ${TokenHandler.getToken()}`
        }
      });

      if (response.ok) {
        const fileUrl = await response.text();
        if (this.profile) {
          this.profile[field] = fileUrl;
          this.profileUpdateService.notifyProfileUpdate(this.profile);
          this.cdr.detectChanges();
        }
        console.log(`${field} uploaded successfully:`, fileUrl);
      } else {
        throw new Error(`Upload failed: ${response.statusText}`);
      }
    } catch (error) {
      console.error(`Error uploading ${field}:`, error);
      Swal.fire('Error', `Failed to upload ${field}. Please try again.`, 'error');
    } finally {
      this.uploadingImages[field] = false;
    }
  }

  private updateBackgroundStyle() {
    if (!this.profile) {
      this.backgroundStyle = this.sanitizer.bypassSecurityTrustStyle('background-color: #1976d2');
      return;
    }

    let style = '';

    if (this.profile.backgroundImageUrl) {
      let overlayColor: string;
      if (this.isDarkMode) {
        // Use a dark overlay in dark mode
        overlayColor = 'rgba(0,0,0,0.6)';
      } else {
        // Use the selected color in light mode
        overlayColor = this.hexToRgba(this.profile.backgroundColor || '#1976d2', 0.6);
      }

      style += `
        background: 
          linear-gradient(${overlayColor}, ${overlayColor}),
          url('${this.profile.backgroundImageUrl}') center/cover no-repeat;
      `;
    } else {
      style += `background-color: ${this.profile.backgroundColor || '#1976d2'};`;
    }

    this.backgroundStyle = this.sanitizer.bypassSecurityTrustStyle(style);
  }

  // Helper to convert hex to rgba
  private hexToRgba(hex: string, alpha: number): string {
    hex = hex.replace('#', '');
    const r = parseInt(hex.substring(0, 2), 16);
    const g = parseInt(hex.substring(2, 4), 16);
    const b = parseInt(hex.substring(4, 6), 16);
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  }

  saveChanges() {
    if (this.profile && this.profile.userId) {
      console.log(this.profile);
      
      // Create a fresh copy to ensure all latest bindings are captured
      const profileToSave: OrganisationProfile = { ...this.profile };

      this.userService.updateOrganisationProfile(this.profile.userId, profileToSave).subscribe({
        next: () => {
          Swal.fire('Success', 'Profile updated successfully!', 'success');
        },
        error: (err) => {
          console.error('Error updating profile:', err);
          Swal.fire('Error', 'Failed to update profile.', 'error');
        }
      });
    }
  }

  onProfileChange() {
    if (this.profile) {
      this.profileUpdateService.notifyProfileUpdate(this.profile);
    }
  }

  isUploading(field: string): boolean {
    return this.uploadingImages[field] || false;
  }

  openEditBenefitsOverlay() {
    this.isEditBenefitsOverlayVisible = true;
  }

  closeEditBenefitsOverlay() {
    this.isEditBenefitsOverlayVisible = false;
  }
}
