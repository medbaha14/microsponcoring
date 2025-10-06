# Microsponsoring Kubernetes Deployment Script (PowerShell)
# This script deploys all components in the correct order

Write-Host "🚀 Starting Microsponsoring Kubernetes Deployment..." -ForegroundColor Green

# Create namespace first
Write-Host "📁 Creating namespace..." -ForegroundColor Yellow
kubectl apply -f namespace.yaml

# Create storage class and persistent volume
Write-Host "💾 Creating storage class and persistent volume..." -ForegroundColor Yellow
kubectl apply -f storage-class.yaml

# Wait for storage class to be ready
Write-Host "⏳ Waiting for storage class to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Deploy MySQL first (database dependency)
Write-Host "🗄️ Deploying MySQL..." -ForegroundColor Yellow
kubectl apply -f mysql-deployment.yaml

# Wait for MySQL to be ready
Write-Host "⏳ Waiting for MySQL to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=mysql -n microsponsoring --timeout=300s

# Deploy backend (depends on MySQL)
Write-Host "🔧 Deploying backend..." -ForegroundColor Yellow
kubectl apply -f backend-deployment.yaml

# Deploy frontend
Write-Host "🎨 Deploying frontend..." -ForegroundColor Yellow
kubectl apply -f frontend-deployment.yaml

# Deploy monitoring
Write-Host "📊 Deploying monitoring..." -ForegroundColor Yellow
kubectl apply -f monitoring.yaml

# Deploy secrets
Write-Host "🔐 Deploying secrets..." -ForegroundColor Yellow
kubectl apply -f secrets.yaml

# Deploy ingress last (depends on all services)
Write-Host "🌐 Deploying ingress..." -ForegroundColor Yellow
kubectl apply -f ingress.yaml

# Wait for all deployments to be ready
Write-Host "⏳ Waiting for all deployments to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=available deployment --all -n microsponsoring --timeout=300s

# Check status
Write-Host "✅ Deployment completed! Checking status..." -ForegroundColor Green
Write-Host ""
Write-Host "📋 Pod Status:" -ForegroundColor Cyan
kubectl get pods -n microsponsoring
Write-Host ""
Write-Host "🔗 Services:" -ForegroundColor Cyan
kubectl get services -n microsponsoring
Write-Host ""
Write-Host "💾 PVCs:" -ForegroundColor Cyan
kubectl get pvc -n microsponsoring
Write-Host ""
Write-Host "🌐 Ingress:" -ForegroundColor Cyan
kubectl get ingress -n microsponsoring

Write-Host ""
Write-Host "🎉 Deployment completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 To access your application:" -ForegroundColor Yellow
Write-Host "1. Add 'microsponsoring.local' to your hosts file pointing to your master node IP" -ForegroundColor White
Write-Host "2. Access the application at: http://microsponsoring.local" -ForegroundColor White
Write-Host ""
Write-Host "🔍 To check logs:" -ForegroundColor Yellow
Write-Host "kubectl logs -f deployment/backend-deployment -n microsponsoring" -ForegroundColor White
Write-Host "kubectl logs -f deployment/frontend-deployment -n microsponsoring" -ForegroundColor White
Write-Host "kubectl logs -f deployment/mysql-deployment -n microsponsoring" -ForegroundColor White
