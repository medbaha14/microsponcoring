import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrganisationProfileComponent } from '../organisation-profile/organisation-profile.component';
import { EditRecognitionBenefitsComponent } from '../edit-recognition-benefits/edit-recognition-benefits.component';
import { UserService } from '../../services/user.service';
import { ImageUploadService, ImageUploadResult } from '../../services/image-upload.service';
import { OrganisationProfile } from '../../models/organisation-profile';
import { User } from '../../models/user';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-customize-profile-new',
  standalone: true,
  imports: [CommonModule, FormsModule, OrganisationProfileComponent, EditRecognitionBenefitsComponent],
  templateUrl: './customize-profile.component.html',
  styleUrl: './customize-profile.component.css'
})
export class CustomizeProfileNewComponent implements OnInit {
  @ViewChild(OrganisationProfileComponent)
  organisationProfileComponent!: OrganisationProfileComponent;

  profile: OrganisationProfile | null = null;
  user: User | null = null;
  uploadingImages: { [key: string]: boolean } = {};
  
  fontOptions = ['Arial', 'Helvetica', 'Times New Roman', 'Georgia', 'Verdana', 'Courier New'];

  constructor(
    private userService: UserService,
    private imageUploadService: ImageUploadService
  ) {}

  ngOnInit() {
    this.loadUserProfile();
  }

  private loadUserProfile() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.user = JSON.parse(userStr);
      if (this.user?.userId) {
        this.loadProfile();
      }
    }
  }

  private loadProfile() {
    if (!this.user?.userId) return;

    this.userService.getOrganisationProfile(this.user.userId).subscribe({
      next: (profile) => {
        this.profile = profile;
        console.log('Profile loaded:', profile);
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        // Initialize with default values if no profile exists
        this.profile = {
          userId: this.user!.userId,
          organisationName: '',
          description: '',
          website: '',
          contactEmail: '',
          phoneNumber: '',
          address: '',
          city: '',
          country: '',
          postalCode: '',
          profilePicture: '',
          logoUrl: '',
          bannerImageUrl: '',
          backgroundImageUrl: '',
          backgroundColor: '#ffffff',
          primaryColor: '#007bff',
          secondaryColor: '#6c757d',
          fontStyle: 'Arial'
        };
      }
    });
  }

  onProfileChange() {
    if (!this.profile || !this.user?.userId) return;

    this.userService.updateOrganisationProfile(this.user.userId, this.profile).subscribe({
      next: () => {
        console.log('Profile updated successfully');
      },
      error: (error) => {
        console.error('Error updating profile:', error);
        Swal.fire('Error', 'Failed to update profile', 'error');
      }
    });
  }

  onImageChange(event: any, field: 'profilePicture' | 'logoUrl' | 'bannerImageUrl' | 'backgroundImageUrl') {
    const file: File = event.target.files[0];
    if (!file) return;

    if (!this.profile?.userId) {
      console.error('No profile or userId available for upload');
      return;
    }

    this.uploadImageNew(file, field);
  }

  private async uploadImageNew(file: File, field: 'profilePicture' | 'logoUrl' | 'bannerImageUrl' | 'backgroundImageUrl') {
    if (!this.profile?.userId) {
      console.error('No profile or userId available for upload');
      return;
    }

    const user = JSON.parse(localStorage.getItem('user') || 'null');
    
    // Validate user type
    if (user?.userType !== 'ORGANISATION_NONPROFIT') {
      Swal.fire('Permission Error', `Only ORGANISATION_NONPROFIT users can upload ${field}. Your account type is: ${user?.userType}`, 'error');
      return;
    }

    // Map field names to image types
    const imageTypeMap: { [key: string]: 'profile' | 'logo' | 'banner' | 'background' } = {
      'profilePicture': 'profile',
      'logoUrl': 'logo',
      'bannerImageUrl': 'banner',
      'backgroundImageUrl': 'background'
    };

    const imageType = imageTypeMap[field];
    if (!imageType) {
      console.error('Invalid field type:', field);
      return;
    }

    try {
      // Upload image using the new service
      const result: ImageUploadResult = await this.imageUploadService.uploadImageToBackend(
        file, 
        this.profile.userId, 
        imageType
      );

      if (result.success && result.url) {
        // Update the profile with the new image URL
        (this.profile as any)[field] = result.url;
        
        // Save the image URL to the database via the new endpoint
        await this.saveImageUrlToDatabase(field, result.url);
        
        // Update the profile
        this.onProfileChange();
        
        Swal.fire('Success', `${field} uploaded successfully`, 'success');
      } else {
        Swal.fire('Upload Failed', result.error || 'Unknown error occurred', 'error');
      }
    } catch (error) {
      console.error('Upload error:', error);
      Swal.fire('Upload Error', 'Failed to upload image', 'error');
    }
  }

  private async saveImageUrlToDatabase(field: string, imageUrl: string): Promise<void> {
    if (!this.profile?.userId) return;

    const user = JSON.parse(localStorage.getItem('user') || 'null');
    const token = localStorage.getItem('token');

    if (!token) {
      throw new Error('No authentication token available');
    }

    // Map field names to API endpoints
    const endpointMap: { [key: string]: string } = {
      'profilePicture': '/api/image-url/profile-picture',
      'logoUrl': '/api/image-url/organisation-logo',
      'bannerImageUrl': '/api/image-url/organisation-banner',
      'backgroundImageUrl': '/api/image-url/organisation-background'
    };

    const endpoint = endpointMap[field];
    if (!endpoint) {
      throw new Error(`Invalid field: ${field}`);
    }

    const requestBody = {
      userId: this.profile.userId,
      imageUrl: imageUrl
    };

    try {
      const response = await fetch(`${environment.apiUrl}${endpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(requestBody)
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Failed to save image URL: ${errorText}`);
      }

      console.log(`Image URL saved to database for ${field}:`, imageUrl);
    } catch (error) {
      console.error(`Error saving image URL for ${field}:`, error);
      throw error;
    }
  }

  isUploading(field: string): boolean {
    return this.uploadingImages[field] || false;
  }

  onBenefitsChange() {
    // Handle benefits change if needed
    console.log('Benefits changed');
  }
}
