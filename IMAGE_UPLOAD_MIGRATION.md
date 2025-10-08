# Image Upload Migration: Backend to Frontend

## Overview
This document describes the migration of image upload functionality from backend processing to frontend direct upload, while maintaining database compatibility.

## Changes Made

### Backend Changes

#### 1. New Controllers
- **`StaticUploadController.java`**: Handles direct file uploads to static folder
  - Endpoint: `POST /api/upload/static`
  - Saves files to `/app/static/uploads/`
  - Returns public URL: `/uploads/{filename}`

- **`ImageUrlController.java`**: Handles image URL updates in database
  - `POST /api/image-url/profile-picture`
  - `POST /api/image-url/organisation-logo`
  - `POST /api/image-url/organisation-banner`
  - `POST /api/image-url/organisation-background`

#### 2. Updated Configuration
- **`WebMvcConfig.java`**: Added static file serving
  - `/uploads/**` → `/app/static/uploads/`
  - `/api/images/**` → `/app/images/` (existing)

- **`application.properties`**: Added static directory configuration
  - `file.static-dir=/app/static/uploads`
  - Added `/uploads/**` to public endpoints

#### 3. Kubernetes Configuration
- **`backend-deployment.yaml`**: Added static storage volume
  - New volume: `static-storage` mounted at `/app/static/uploads`

### Frontend Changes

#### 1. New Service
- **`ImageUploadService`**: Handles image uploads with validation
  - File type validation (JPEG, PNG, GIF, WebP)
  - File size validation (10MB max)
  - Upload status tracking
  - Two upload methods:
    - `uploadImage()`: Local storage (demo)
    - `uploadImageToBackend()`: Backend static folder

#### 2. Updated Components
- **`CustomizeProfileNewComponent`**: Uses new upload service
- **`AddUserModalNewComponent`**: Uses new upload service

#### 3. Environment Configuration
- **`environment.pod.ts`**: Added missing URL configurations

## New Upload Flow

### 1. Frontend Upload Process
```typescript
// 1. Validate file
const validation = this.imageUploadService.validateImage(file);
if (!validation.valid) {
  // Show error
  return;
}

// 2. Upload to backend static folder
const result = await this.imageUploadService.uploadImageToBackend(
  file, userId, imageType
);

// 3. Save URL to database
await this.saveImageUrlToDatabase(field, result.url);
```

### 2. Backend Processing
```java
// 1. Receive file upload
@PostMapping("/static")
public ResponseEntity<?> uploadToStatic(
    @RequestParam("file") MultipartFile file,
    @RequestParam("filename") String filename,
    @RequestParam("userId") String userId,
    @RequestParam("imageType") String imageType
) {
    // Save to /app/static/uploads/
    // Return public URL: /uploads/{filename}
}

// 2. Receive URL update
@PostMapping("/profile-picture")
public ResponseEntity<?> updateProfilePicture(
    @RequestBody Map<String, String> request
) {
    // Update database with image URL
}
```

## File Structure

### Backend
```
/app/
├── images/           # Original backend uploads
└── static/
    └── uploads/      # New frontend uploads
```

### Frontend
```
src/app/services/
└── image-upload.service.ts    # New upload service
```

## URL Mapping

| Purpose | URL Pattern | Backend Path | Public Access |
|---------|-------------|--------------|---------------|
| Original uploads | `/api/images/**` | `/app/images/` | Yes |
| New uploads | `/uploads/**` | `/app/static/uploads/` | Yes |
| Static upload API | `/api/upload/static` | N/A | No (auth required) |
| URL update API | `/api/image-url/**` | N/A | No (auth required) |

## Database Compatibility

The database structure remains unchanged:
- `users.profile_picture` stores image URLs
- `page_customizations.logo_url` stores image URLs
- `page_customizations.banner_image_url` stores image URLs
- `page_customizations.background_image_url` stores image URLs

## Migration Steps

### 1. Deploy Backend Changes
```bash
# Build and deploy backend with new controllers
mvn clean package
docker build -t medbaha/pfebackend:latest .
docker push medbaha/pfebackend:latest
```

### 2. Deploy Frontend Changes
```bash
# Build and deploy frontend with new service
ng build --configuration=production
docker build -t medbaha/pfefrontend:latest .
docker push medbaha/pfefrontend:latest
```

### 3. Update Kubernetes
```bash
# Apply updated configurations
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml
```

## Testing

### 1. Test Image Upload
1. Navigate to organization profile customization
2. Select an image file
3. Verify upload progress indicator
4. Check that image appears after upload
5. Verify database contains correct URL

### 2. Test Image Serving
1. Access image directly: `http://microsponsoring.local:32403/uploads/{filename}`
2. Verify image loads correctly
3. Test different image types (JPEG, PNG, GIF)

### 3. Test Validation
1. Try uploading non-image files (should fail)
2. Try uploading files > 10MB (should fail)
3. Try uploading without authentication (should fail)

## Benefits

1. **Reduced Backend Load**: File processing moved to frontend
2. **Better User Experience**: Real-time validation and progress
3. **Scalability**: Static files can be served by CDN
4. **Maintainability**: Clear separation of concerns
5. **Database Compatibility**: No schema changes required

## Rollback Plan

If issues arise, you can rollback by:
1. Reverting to original components
2. Using original upload endpoints
3. Removing new static upload endpoints
4. The database structure remains compatible

## Future Enhancements

1. **CDN Integration**: Serve static files from CDN
2. **Image Processing**: Add thumbnail generation
3. **Cloud Storage**: Upload to AWS S3, Azure Blob, etc.
4. **Image Optimization**: Compress images before upload
5. **Batch Upload**: Support multiple file uploads
