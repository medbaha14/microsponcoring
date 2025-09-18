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
import { environment } from '../../../../environments/environment';

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
    
    // Check for company profile after a brief delay to ensure everything loads
    setTimeout(() => {
      this.checkCompanyProfileStatus();
    }, 2000);
  }

  loadProfile() {
    const user = TokenHandler.getUser();
    console.log('CustomizeProfile: Loading profile for user:', user);
    
    if (user && user.userId) {
      console.log('CustomizeProfile: Calling userService.getOrganisationProfile with userId:', user.userId);
      
      this.userService.getOrganisationProfile(user.userId).subscribe({
        next: (profile) => {
          console.log('CustomizeProfile: Profile loaded successfully:', profile);
          this.profile = profile;
          this.updateBackgroundStyle();
        },
        error: (err) => {
          console.error('CustomizeProfile: Error loading profile:', err);
          console.error('CustomizeProfile: Error status:', err.status);
          console.error('CustomizeProfile: Error message:', err.message);
          
          // Show user-friendly error message
          if (err.status === 404) {
            // Profile not found - offer to create company profile
            Swal.fire({
              title: 'Company Profile Required',
              text: 'You need to create your company profile before you can customize your organization page. Would you like to create it now?',
              icon: 'info',
              showCancelButton: true,
              confirmButtonText: 'Create Company Profile',
              cancelButtonText: 'Cancel'
            }).then((result) => {
              if (result.isConfirmed) {
                this.createCompanyProfile();
              }
            });
          } else if (err.status === 401) {
            Swal.fire('Authentication Error', 'Please log in again to access your profile.', 'error');
          } else {
            Swal.fire('Error', 'Failed to load your profile. Please try refreshing the page.', 'error');
          }
        }
      });

      this.companyService.getCompanyByUserId(user.userId).subscribe({
        next: (company) => {
          console.log('CustomizeProfile: Company loaded successfully:', company);
          this.company = company;
        },
        error: (err) => {
          console.error('CustomizeProfile: Error loading company:', err);
          console.error('CustomizeProfile: Company error status:', err.status);
          
          // If company not found, offer to create it
          if (err.status === 404) {
            console.log('CustomizeProfile: Company profile not found, offering to create it');
            
            Swal.fire({
              title: 'Profile Setup Required',
              text: 'Your organization profile needs to be initialized. Would you like to set it up now?',
              icon: 'info',
              showCancelButton: true,
              confirmButtonText: 'Initialize Profile',
              cancelButtonText: 'Later',
              allowOutsideClick: false
            }).then((result) => {
              if (result.isConfirmed) {
                this.initializeUserProfiles();
              }
            });
          }
        }
      });
    } else {
      console.error('CustomizeProfile: No user or userId found');
      Swal.fire('Error', 'User information not found. Please log in again.', 'error');
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
    if (!this.profile?.userId) {
      console.error('CustomizeProfile: No profile or userId available for upload');
      return;
    }

    this.uploadingImages[field] = true;
    const user = TokenHandler.getUser();
    const token = TokenHandler.getToken();
    
    console.log('CustomizeProfile: Starting image upload');
    console.log('CustomizeProfile: Field:', field);
    console.log('CustomizeProfile: File:', file.name, file.size, file.type);
    console.log('CustomizeProfile: User:', user);
    console.log('CustomizeProfile: User type:', user?.userType);
    console.log('CustomizeProfile: Token available:', !!token);
    
    // Validate user type before upload
    if (user?.userType !== 'ORGANISATION_NONPROFIT') {
      this.uploadingImages[field] = false;
      Swal.fire('Permission Error', `Only ORGANISATION_NONPROFIT users can upload ${field}. Your account type is: ${user?.userType}`, 'error');
      return;
    }
    
    // Validate file type
    if (!file.type.startsWith('image/')) {
      this.uploadingImages[field] = false;
      Swal.fire('Invalid File', 'Please select a valid image file (JPEG, PNG, GIF, etc.)', 'error');
      return;
    }
    
    // Validate file size (10MB limit)
    const maxSize = 10 * 1024 * 1024; // 10MB
    if (file.size > maxSize) {
      this.uploadingImages[field] = false;
      Swal.fire('File Too Large', 'Image size must be less than 10MB. Please choose a smaller image.', 'error');
      return;
    }
    
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('userId', user.userId);

      let uploadUrl: string;
      switch (field) {
        case 'profilePicture':
          uploadUrl = `${environment.uploadUrl}/profile-picture`;
          break;
        case 'logoUrl':
          uploadUrl = `${environment.uploadUrl}/organisation-logo`;
          break;
        case 'bannerImageUrl':
          uploadUrl = `${environment.uploadUrl}/organisation-banner`;
          break;
        case 'backgroundImageUrl':
          uploadUrl = `${environment.uploadUrl}/organisation-background`;
          break;
        default:
          throw new Error('Invalid field type');
      }
      
      console.log('CustomizeProfile: Upload URL:', uploadUrl);
      console.log('CustomizeProfile: FormData contents:');
      console.log('CustomizeProfile: - file:', file.name);
      console.log('CustomizeProfile: - userId:', user.userId);

      const response = await fetch(uploadUrl, {
        method: 'POST',
        body: formData,
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      console.log('CustomizeProfile: Upload response status:', response.status);
      console.log('CustomizeProfile: Upload response headers:', response.headers);

      if (response.ok) {
        const fileUrl = await response.text();
        console.log('CustomizeProfile: Upload successful, file URL:', fileUrl);
        
        if (this.profile) {
          this.profile[field] = fileUrl;
          this.profileUpdateService.notifyProfileUpdate(this.profile);
          this.cdr.detectChanges();
        }
        
        Swal.fire('Success', `${field} uploaded successfully!`, 'success');
      } else {
        // Get detailed error message from backend
        const errorText = await response.text();
        console.error('CustomizeProfile: Upload failed with status:', response.status);
        console.error('CustomizeProfile: Error response:', errorText);
        
        let userMessage = `Failed to upload ${field}.`;
        
        if (response.status === 400) {
          if (errorText.includes('User not found')) {
            userMessage = 'User not found. Please log in again.';
          } else if (errorText.includes('not an organisation')) {
            userMessage = 'Only organization accounts can upload these images.';
          } else if (errorText.includes('Company profile not found')) {
            userMessage = 'Company profile not found. Please complete your organization profile first.';
          } else if (errorText.includes('File is required')) {
            userMessage = 'No file was selected. Please choose an image file.';
          } else {
            userMessage = errorText || userMessage;
          }
        } else if (response.status === 401) {
          userMessage = 'Authentication failed. Please log in again.';
        } else if (response.status === 403) {
          userMessage = 'You do not have permission to upload images. Please ensure you are logged in as an organization user.';
        }
        
        throw new Error(userMessage);
      }
    } catch (error) {
      console.error(`CustomizeProfile: Error uploading ${field}:`, error);
      
      let errorMessage = `Failed to upload ${field}. Please try again.`;
      if (error instanceof Error) {
        errorMessage = error.message;
      }
      
      Swal.fire('Upload Error', errorMessage, 'error');
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
    // Debug user authentication and role
    const token = TokenHandler.getToken();
    const user = TokenHandler.getUser();
    console.log('CustomizeProfile: Current token:', token ? 'Present' : 'Missing');
    console.log('CustomizeProfile: Current user:', user);
    console.log('CustomizeProfile: User type/role:', user?.userType);
    
    if (!token) {
      Swal.fire('Authentication Error', 'You are not logged in. Please log in and try again.', 'error');
      return;
    }
    
    if (user?.userType !== 'ORGANISATION_NONPROFIT') {
      Swal.fire('Permission Error', `Your account type (${user?.userType}) does not have permission to edit organization profiles. You need to be logged in as an ORGANISATION_NONPROFIT user.`, 'error');
      return;
    }

    if (this.profile && this.profile.userId) {
      console.log('CustomizeProfile: Starting save operation');
      console.log('CustomizeProfile: Profile data:', this.profile);
      console.log('CustomizeProfile: User ID:', this.profile.userId);
      
      // Create a fresh copy to ensure all latest bindings are captured
      const profileToSave: OrganisationProfile = { ...this.profile };

      // Show loading indicator
      Swal.fire({
        title: 'Saving...',
        text: 'Please wait while we update your profile',
        allowOutsideClick: false,
        showConfirmButton: false,
        didOpen: () => {
          Swal.showLoading();
        }
      });

      this.userService.updateOrganisationProfile(this.profile.userId, profileToSave).subscribe({
        next: (response) => {
          console.log('CustomizeProfile: Save successful:', response);
          Swal.fire('Success', 'Profile updated successfully!', 'success');
          
          // Notify other components about the profile update
          this.profileUpdateService.notifyProfileUpdate(this.profile!);
        },
        error: (err) => {
          console.error('CustomizeProfile: Error updating profile:', err);
          console.error('CustomizeProfile: Error status:', err.status);
          console.error('CustomizeProfile: Error message:', err.message);
          console.error('CustomizeProfile: Error details:', err.error);
          
          let errorMessage = 'Failed to update profile.';
          
          if (err.status === 401) {
            errorMessage = 'You are not authorized to perform this action. Please log in again.';
          } else if (err.status === 403) {
            errorMessage = 'You do not have permission to update this profile.';
          } else if (err.status === 404) {
            errorMessage = 'Profile not found. Please try refreshing the page.';
          } else if (err.status === 500) {
            errorMessage = 'Server error. Please try again later.';
          } else if (err.status === 0) {
            errorMessage = 'Cannot connect to server. Please check your internet connection.';
          }
          
          Swal.fire('Error', errorMessage, 'error');
        }
      });
    } else {
      console.error('CustomizeProfile: Cannot save - no profile or user ID');
      Swal.fire('Error', 'Profile data is missing. Please refresh the page and try again.', 'error');
    }
  }

  checkCompanyProfileStatus() {
    console.log('CustomizeProfile: Checking company profile status');
    console.log('CustomizeProfile: Profile:', !!this.profile);
    console.log('CustomizeProfile: Company:', !!this.company);
    
    // If we have a profile but no company, we need to create the company
    if (this.profile && !this.company) {
      console.log('CustomizeProfile: Profile exists but company is missing');
      
      Swal.fire({
        title: 'Complete Your Setup',
        text: 'Your organization profile is partially set up. Would you like to complete the initialization now?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Complete Setup',
        cancelButtonText: 'Later',
        allowOutsideClick: false
      }).then((result) => {
        if (result.isConfirmed) {
          this.initializeUserProfiles();
        }
      });
    } else if (!this.profile && !this.company) {
      console.log('CustomizeProfile: Both profile and company are missing');
    } else {
      console.log('CustomizeProfile: All profiles are properly loaded');
    }
  }

  initializeUserProfiles() {
    const user = TokenHandler.getUser();
    if (!user || !user.userId) {
      Swal.fire('Error', 'User information not found. Please log in again.', 'error');
      return;
    }

    console.log('CustomizeProfile: Initializing missing profiles for user:', user.userId);

    // Show loading
    Swal.fire({
      title: 'Setting Up Your Profile...',
      text: 'Please wait while we initialize your organization profile',
      allowOutsideClick: false,
      showConfirmButton: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    this.userService.initializeUserProfiles(user.userId).subscribe({
      next: (response) => {
        console.log('CustomizeProfile: Profile initialization successful:', response);
        
        Swal.fire({
          title: 'Setup Complete!',
          text: response.message || 'Your organization profile has been initialized successfully.',
          icon: 'success',
          confirmButtonText: 'Continue'
        }).then(() => {
          // Reload the profile data
          this.loadProfile();
        });
      },
      error: (err) => {
        console.error('CustomizeProfile: Error initializing profiles:', err);
        
        let errorMessage = 'Failed to initialize your profile. Please try again.';
        if (err.status === 401) {
          errorMessage = 'Authentication failed. Please log in again.';
        } else if (err.status === 403) {
          errorMessage = 'You do not have permission to initialize profiles.';
        } else if (err.error && err.error.error) {
          errorMessage = err.error.error;
        }
        
        Swal.fire('Setup Error', errorMessage, 'error');
      }
    });
  }

  // Legacy method - now calls the new initialization
  createCompanyProfile() {
    this.initializeUserProfiles();
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
