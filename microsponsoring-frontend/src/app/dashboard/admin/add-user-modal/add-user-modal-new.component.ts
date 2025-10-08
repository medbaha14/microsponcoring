import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ImageUploadService, ImageUploadResult } from '../../services/image-upload.service';
import { User } from '../../models/user';
import { environment } from '../../../environments/environment';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-add-user-modal-new',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-user-modal.component.html',
  styleUrls: ['./add-user-modal.component.css']
})
export class AddUserModalNewComponent implements OnChanges {
  @Input() user: User | null = null

  newUser: User = {
    userId: '',
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    userType: 'ORGANISATION_NONPROFIT',
    profilePicture: '',
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date()
  };

  uploadingImage = false;
  userTypes = ['ADMIN', 'SPONSOR', 'ORGANISATION_NONPROFIT'];

  constructor(private imageUploadService: ImageUploadService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['user'] && this.user) {
      this.newUser = { ...this.user };
    }
  }

  onUserTypeChange() {
    if (this.newUser.userType === 'ORGANISATION_NONPROFIT') {
      this.setDefaultCompanyFields();
    }
  }

  private setDefaultCompanyFields() {
    // Set default values for organization fields if needed
    console.log('Setting default company fields');
  }

  async onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (!file) {
      return;
    }

    if (!this.user || !this.user.userId) {
      Swal.fire('Error', 'User must be created or selected before uploading a profile picture.', 'error');
      return;
    }

    this.uploadingImage = true;

    try {
      // Upload image using the new service
      const result: ImageUploadResult = await this.imageUploadService.uploadImageToBackend(
        file, 
        this.user.userId, 
        'profile'
      );

      if (result.success && result.url) {
        // Update the user with the new image URL
        this.newUser.profilePicture = result.url;
        
        // Save the image URL to the database
        await this.saveImageUrlToDatabase(result.url);
        
        Swal.fire('Success', 'Profile picture uploaded successfully', 'success');
      } else {
        Swal.fire('Upload Failed', result.error || 'Unknown error occurred', 'error');
      }
    } catch (error) {
      console.error('Upload error:', error);
      Swal.fire('Upload Error', 'Failed to upload image', 'error');
    } finally {
      this.uploadingImage = false;
    }
  }

  private async saveImageUrlToDatabase(imageUrl: string): Promise<void> {
    if (!this.user?.userId) return;

    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('No authentication token available');
    }

    const requestBody = {
      userId: this.user.userId,
      imageUrl: imageUrl
    };

    try {
      const response = await fetch(`${environment.apiUrl}/api/image-url/profile-picture`, {
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

      console.log('Image URL saved to database:', imageUrl);
    } catch (error) {
      console.error('Error saving image URL:', error);
      throw error;
    }
  }

  submitAddUser() {
    if (this.user && this.user.userId) {
      // Update existing user
      console.log('Updating user:', this.newUser);
    } else {
      // Create new user
      console.log('Creating new user:', this.newUser);
    }
  }

  cancel() {
    // Reset form or close modal
    this.newUser = {
      userId: '',
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      userType: 'ORGANISATION_NONPROFIT',
      profilePicture: '',
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date()
    };
  }
}
