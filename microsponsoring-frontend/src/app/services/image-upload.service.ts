import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

export interface ImageUploadResult {
  success: boolean;
  url?: string;
  error?: string;
}

export interface ImageValidationResult {
  valid: boolean;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ImageUploadService {
  private readonly MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private readonly ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
  private readonly UPLOAD_DIR = 'assets/uploads';
  
  private uploadStatusSubject = new BehaviorSubject<{ [key: string]: boolean }>({});
  public uploadStatus$ = this.uploadStatusSubject.asObservable();

  constructor() {
    // Ensure upload directory exists
    this.ensureUploadDirectory();
  }

  /**
   * Validates image file before upload
   */
  validateImage(file: File): ImageValidationResult {
    // Check file type
    if (!this.ALLOWED_TYPES.includes(file.type)) {
      return {
        valid: false,
        error: `Invalid file type. Allowed types: ${this.ALLOWED_TYPES.join(', ')}`
      };
    }

    // Check file size
    if (file.size > this.MAX_FILE_SIZE) {
      return {
        valid: false,
        error: `File too large. Maximum size: ${this.MAX_FILE_SIZE / (1024 * 1024)}MB`
      };
    }

    return { valid: true };
  }

  /**
   * Uploads image file to static assets folder
   */
  async uploadImage(file: File, userId: string, imageType: 'profile' | 'logo' | 'banner' | 'background'): Promise<ImageUploadResult> {
    const validation = this.validateImage(file);
    if (!validation.valid) {
      return {
        success: false,
        error: validation.error
      };
    }

    const uploadKey = `${userId}-${imageType}`;
    this.setUploadStatus(uploadKey, true);

    try {
      // Generate unique filename
      const timestamp = Date.now();
      const extension = this.getFileExtension(file.name);
      const filename = `${userId}-${imageType}-${timestamp}${extension}`;
      
      // Create FileReader to convert file to base64
      const base64Data = await this.fileToBase64(file);
      
      // In a real application, you would upload this to a server
      // For now, we'll simulate the upload and return a URL
      const imageUrl = `${this.UPLOAD_DIR}/${filename}`;
      
      // Store the image data in localStorage for demo purposes
      // In production, this would be uploaded to a file server
      this.storeImageLocally(filename, base64Data);
      
      this.setUploadStatus(uploadKey, false);
      
      return {
        success: true,
        url: imageUrl
      };
      
    } catch (error) {
      this.setUploadStatus(uploadKey, false);
      return {
        success: false,
        error: `Upload failed: ${error}`
      };
    }
  }

  /**
   * Uploads image to backend static folder (for Kubernetes deployment)
   */
  async uploadImageToBackend(file: File, userId: string, imageType: 'profile' | 'logo' | 'banner' | 'background'): Promise<ImageUploadResult> {
    const validation = this.validateImage(file);
    if (!validation.valid) {
      return {
        success: false,
        error: validation.error
      };
    }

    const uploadKey = `${userId}-${imageType}`;
    this.setUploadStatus(uploadKey, true);

    try {
      // Generate unique filename
      const timestamp = Date.now();
      const extension = this.getFileExtension(file.name);
      const filename = `${userId}-${imageType}-${timestamp}${extension}`;
      
      // Create FormData for upload
      const formData = new FormData();
      formData.append('file', file);
      formData.append('filename', filename);
      formData.append('userId', userId);
      formData.append('imageType', imageType);
      
      // Upload to backend static folder
      const response = await fetch('/api/upload/static', {
        method: 'POST',
        body: formData
      });
      
      if (!response.ok) {
        throw new Error(`Upload failed: ${response.statusText}`);
      }
      
      const result = await response.json();
      this.setUploadStatus(uploadKey, false);
      
      return {
        success: true,
        url: result.url
      };
      
    } catch (error) {
      this.setUploadStatus(uploadKey, false);
      return {
        success: false,
        error: `Upload failed: ${error}`
      };
    }
  }

  /**
   * Gets the public URL for an image
   */
  getImageUrl(filename: string): string {
    return `/${this.UPLOAD_DIR}/${filename}`;
  }

  /**
   * Checks if an upload is in progress
   */
  isUploading(key: string): boolean {
    return this.uploadStatusSubject.value[key] || false;
  }

  private setUploadStatus(key: string, status: boolean): void {
    const current = this.uploadStatusSubject.value;
    this.uploadStatusSubject.next({ ...current, [key]: status });
  }

  private getFileExtension(filename: string): string {
    return filename.substring(filename.lastIndexOf('.'));
  }

  private async fileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  private storeImageLocally(filename: string, base64Data: string): void {
    // Store in localStorage for demo purposes
    // In production, this would be uploaded to a file server
    localStorage.setItem(`image_${filename}`, base64Data);
  }

  private ensureUploadDirectory(): void {
    // In a real application, this would ensure the upload directory exists on the server
    // For now, we'll just log that we're checking
    console.log('Ensuring upload directory exists:', this.UPLOAD_DIR);
  }
}
