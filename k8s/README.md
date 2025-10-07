# 🚀 Microsponsoring Kubernetes Configuration

## 📁 Cleaned Up File Structure

### Core Application Files
- **`backend-deployment.yaml`** - Backend service deployment (fixed database connection)
- **`frontend-deployment.yaml`** - Frontend service deployment
- **`mysql-deployment.yaml`** - MySQL database deployment (fixed service name)
- **`image-service.yaml`** - Nginx service for serving images without authentication

### Infrastructure Files
- **`namespace.yaml`** - Kubernetes namespace definition
- **`secrets.yaml`** - Application secrets and credentials
- **`storage-class.yaml`** - Storage class for persistent volumes
- **`ingress.yaml`** - Ingress controller configuration (routes images to image service)
- **`monitoring.yaml`** - Prometheus and Grafana monitoring setup

### Deployment Scripts
- **`deploy-fixed.sh`** - Fixed deployment script for Linux/Mac
- **`deploy-fixed.ps1`** - Fixed deployment script for Windows
- **`sync-images.sh`** - Script to sync images from backend to image service

### Documentation
- **`FIXES_APPLIED.md`** - Detailed documentation of all fixes applied
- **`README.md`** - This file

## 🚀 Quick Start

### Deploy the Application

**Linux/Mac:**
```bash
cd k8s
chmod +x deploy-fixed.sh sync-images.sh
./deploy-fixed.sh
```

**Windows:**
```powershell
cd k8s
.\deploy-fixed.ps1
```

### Test the Deployment

```bash
# Test API health
curl -I http://microsponsoring.local:32403/api/actuator/health

# Test image serving
curl -I http://microsponsoring.local:32403/images/

# Test frontend
curl -I http://microsponsoring.local:32403/
```

## 🔧 Key Fixes Applied

1. **Database Connection** - Fixed MySQL service name and password
2. **Image Loading** - Created separate image service to bypass authentication
3. **Deployment Order** - Proper sequence with waiting periods
4. **Image Sync** - Automatic synchronization between backend and image service

## 📋 Access URLs

- **Frontend**: http://microsponsoring.local:32403
- **API Health**: http://microsponsoring.local:32403/api/actuator/health
- **Images**: http://microsponsoring.local:32403/images/
- **WebSocket**: ws://microsponsoring.local:32403/ws-notifications

## 🗑️ Files Removed

The following redundant files were cleaned up:
- `backend-deployment-simple.yaml` (test file)
- `cookies.txt` (temporary file)
- `deploy-all.ps1` (old deployment script)
- `deploy-all.sh` (old deployment script)
- `local-storage-class.yaml` (redundant storage class)
