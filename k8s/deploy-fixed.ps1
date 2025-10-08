# Fixed deployment script for microsponsoring application
# This script addresses all the identified issues

Write-Host "🚀 Starting fixed deployment of microsponsoring application..." -ForegroundColor Green

# Clean up existing deployments that might be causing conflicts
Write-Host "🧹 Cleaning up existing deployments..." -ForegroundColor Yellow
kubectl delete deployment backend-deployment frontend-deployment mysql-deployment image-service -n microsponsoring --ignore-not-found=true
kubectl delete service backend-service frontend-service mysql-service image-service -n microsponsoring --ignore-not-found=true
kubectl delete ingress microsponsoring-ingress -n microsponsoring --ignore-not-found=true
kubectl delete configmap nginx-image-config mysql-config -n microsponsoring --ignore-not-found=true
Start-Sleep -Seconds 10

# Create namespace
Write-Host "📁 Creating namespace..." -ForegroundColor Yellow
kubectl apply -f namespace.yaml

# Create secrets
Write-Host "🔐 Creating secrets..." -ForegroundColor Yellow
kubectl apply -f secrets.yaml

# Create WebSocket headers ConfigMap
Write-Host "🔌 Creating WebSocket headers ConfigMap..." -ForegroundColor Yellow
kubectl apply -f websocket-headers.yaml

# Deploy storage first
Write-Host "💾 Deploying storage..." -ForegroundColor Yellow
kubectl apply -f storage-class.yaml

# Deploy MySQL first
Write-Host "🗄️ Deploying MySQL..." -ForegroundColor Yellow
kubectl apply -f mysql-deployment.yaml

# Wait for MySQL to be ready
Write-Host "⏳ Waiting for MySQL to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=mysql -n microsponsoring --timeout=300s

# Deploy backend
Write-Host "🔧 Deploying Backend..." -ForegroundColor Yellow
kubectl apply -f backend-deployment.yaml

# Wait for backend to initialize with database
Write-Host "⏳ Waiting for backend to connect to database..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# Check backend logs to see if it started successfully
Write-Host "📋 Checking backend logs..." -ForegroundColor Yellow
kubectl logs deployment/backend-deployment -n microsponsoring --tail=20

# Deploy image service
Write-Host "🖼️ Deploying Image Service..." -ForegroundColor Yellow
kubectl apply -f image-service.yaml

# Wait for image service to be ready
Write-Host "⏳ Waiting for image service to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=image-service -n microsponsoring --timeout=300s

# Deploy WebSocket service
Write-Host "🔌 Deploying WebSocket Service..." -ForegroundColor Yellow
kubectl apply -f websocket-service.yaml

# Wait for WebSocket service to be ready
Write-Host "⏳ Waiting for WebSocket service to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=websocket-proxy -n microsponsoring --timeout=300s

# Sync images from backend to image service
Write-Host "🔄 Syncing images from backend to image service..." -ForegroundColor Yellow
# Get pod names
$backendPod = kubectl get pods -n microsponsoring -l app=backend -o jsonpath='{.items[0].metadata.name}'
$imagePod = kubectl get pods -n microsponsoring -l app=image-service -o jsonpath='{.items[0].metadata.name}'

Write-Host "Backend pod: $backendPod"
Write-Host "Image service pod: $imagePod"

# Create images directory in image service if it doesn't exist
kubectl exec -it $imagePod -n microsponsoring -- mkdir -p /usr/share/nginx/html/images

# Get list of images from backend
Write-Host "📋 Getting list of images from backend..." -ForegroundColor Yellow
kubectl exec -it $backendPod -n microsponsoring -- ls /app/images/ > /tmp/backend_images.txt

# Copy each image to the image service
Write-Host "📤 Copying images to image service..." -ForegroundColor Yellow
$images = Get-Content /tmp/backend_images.txt
foreach ($image in $images) {
    if ($image -and $image -ne "total" -and $image -notlike "drwx*") {
        Write-Host "Copying $image..." -ForegroundColor Cyan
        kubectl exec -it $backendPod -n microsponsoring -- sh -c "base64 /app/images/$image" > /tmp/${image}.b64
        kubectl cp /tmp/${image}.b64 microsponsoring/$imagePod:/tmp/${image}.b64
        kubectl exec -it $imagePod -n microsponsoring -- sh -c "base64 -d /tmp/${image}.b64 > /usr/share/nginx/html/images/$image"
        Remove-Item /tmp/${image}.b64 -ErrorAction SilentlyContinue
        kubectl exec -it $imagePod -n microsponsoring -- rm -f /tmp/${image}.b64
    }
}

# Clean up
Remove-Item /tmp/backend_images.txt -ErrorAction SilentlyContinue

# Deploy frontend
Write-Host "🎨 Deploying Frontend..." -ForegroundColor Yellow
kubectl apply -f frontend-deployment.yaml

# Wait for frontend to be ready
Write-Host "⏳ Waiting for frontend to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=frontend -n microsponsoring --timeout=300s

# Deploy ingress
Write-Host "🌐 Deploying Ingress..." -ForegroundColor Yellow
kubectl apply -f ingress.yaml

# Deploy monitoring (optional)
Write-Host "📊 Deploying Monitoring..." -ForegroundColor Yellow
kubectl apply -f monitoring.yaml

# Final status check
Write-Host "✅ Deployment completed! Checking status..." -ForegroundColor Green
kubectl get pods -n microsponsoring
kubectl get services -n microsponsoring
kubectl get ingress -n microsponsoring

Write-Host ""
Write-Host "🎉 Deployment completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Access URLs:" -ForegroundColor Cyan
Write-Host "   Frontend: http://microsponsoring.local:32403" -ForegroundColor White
Write-Host "   API Health: http://microsponsoring.local:32403/api/actuator/health" -ForegroundColor White
Write-Host "   Images: http://microsponsoring.local:32403/images/" -ForegroundColor White
Write-Host ""
Write-Host "🔧 To test the deployment:" -ForegroundColor Cyan
Write-Host "   curl -I http://microsponsoring.local:32403/api/actuator/health" -ForegroundColor White
Write-Host "   curl -I http://microsponsoring.local:32403/images/" -ForegroundColor White
Write-Host "   curl -I http://microsponsoring.local:32403/" -ForegroundColor White
