# PowerShell script to clean up everything and deploy the final clean version
Write-Host "🧹 CLEANING UP ALL RESOURCES AND DEPLOYING FINAL VERSION..." -ForegroundColor Green

$NAMESPACE = "microsponsoring"

Write-Host "📋 Step 1: Clean up ALL existing resources..." -ForegroundColor Yellow

# Delete all deployments
kubectl delete deployment --all -n $NAMESPACE --ignore-not-found=true

# Delete all services
kubectl delete service --all -n $NAMESPACE --ignore-not-found=true

# Delete all ingress
kubectl delete ingress --all -n $NAMESPACE --ignore-not-found=true

# Delete all configmaps
kubectl delete configmap --all -n $NAMESPACE --ignore-not-found=true

# Delete all secrets
kubectl delete secret --all -n $NAMESPACE --ignore-not-found=true

# Delete all persistent volume claims
kubectl delete pvc --all -n $NAMESPACE --ignore-not-found=true

Write-Host "⏳ Waiting for cleanup to complete..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "🔧 Step 2: Deploy FINAL CLEAN VERSION..." -ForegroundColor Yellow
kubectl apply -f FINAL_CLEAN_DEPLOYMENT.yaml

Write-Host "⏳ Step 3: Waiting for all deployments to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=available --timeout=600s deployment/mysql-deployment -n $NAMESPACE
kubectl wait --for=condition=available --timeout=600s deployment/backend-deployment -n $NAMESPACE
kubectl wait --for=condition=available --timeout=600s deployment/frontend-deployment -n $NAMESPACE
kubectl wait --for=condition=available --timeout=600s deployment/image-service -n $NAMESPACE

Write-Host "🔍 Step 4: Check deployment status..." -ForegroundColor Yellow
kubectl get pods -n $NAMESPACE
kubectl get services -n $NAMESPACE

Write-Host "⏳ Step 5: Wait for LoadBalancer IPs..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

Write-Host "📊 Step 6: Get access information..." -ForegroundColor Yellow

# Get LoadBalancer IPs
$FRONTEND_IP = kubectl get service frontend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
$BACKEND_IP = kubectl get service backend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
$IMAGE_IP = kubectl get service image-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}'

if (-not $FRONTEND_IP) {
    $FRONTEND_IP = kubectl get service frontend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
}
if (-not $BACKEND_IP) {
    $BACKEND_IP = kubectl get service backend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
}
if (-not $IMAGE_IP) {
    $IMAGE_IP = kubectl get service image-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
}

Write-Host ""
Write-Host "🎉 FINAL DEPLOYMENT COMPLETED!" -ForegroundColor Green
Write-Host "📊 Access Information:" -ForegroundColor Cyan

if ($FRONTEND_IP) {
    Write-Host "  Frontend: http://$FRONTEND_IP" -ForegroundColor White
} else {
    Write-Host "  Frontend: External IP pending..." -ForegroundColor Yellow
}

if ($BACKEND_IP) {
    Write-Host "  API: http://$BACKEND_IP:8080" -ForegroundColor White
    Write-Host "  WebSocket: ws://$BACKEND_IP:8081" -ForegroundColor White
    Write-Host "  Health: http://$BACKEND_IP:8080/api/actuator/health" -ForegroundColor White
} else {
    Write-Host "  Backend: External IP pending..." -ForegroundColor Yellow
}

if ($IMAGE_IP) {
    Write-Host "  Images: http://$IMAGE_IP" -ForegroundColor White
} else {
    Write-Host "  Images: External IP pending..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🔍 Current status:" -ForegroundColor Yellow
kubectl get all -n $NAMESPACE

Write-Host ""
Write-Host "📝 What was cleaned up:" -ForegroundColor Cyan
Write-Host "  ❌ All old deployments" -ForegroundColor Red
Write-Host "  ❌ All old services" -ForegroundColor Red
Write-Host "  ❌ All ingress resources" -ForegroundColor Red
Write-Host "  ❌ All configmaps" -ForegroundColor Red
Write-Host "  ❌ All secrets" -ForegroundColor Red
Write-Host "  ❌ All PVCs" -ForegroundColor Red

Write-Host ""
Write-Host "✅ What was deployed:" -ForegroundColor Cyan
Write-Host "  ✅ MySQL (1 replica)" -ForegroundColor Green
Write-Host "  ✅ Backend (2 replicas) with LoadBalancer" -ForegroundColor Green
Write-Host "  ✅ Frontend (1 replica) with LoadBalancer" -ForegroundColor Green
Write-Host "  ✅ Image Service (1 replica) with LoadBalancer" -ForegroundColor Green
Write-Host "  ✅ Internal services for communication" -ForegroundColor Green

Write-Host ""
Write-Host "💡 Next steps:" -ForegroundColor Yellow
Write-Host "1. Update frontend configuration with LoadBalancer IPs" -ForegroundColor White
Write-Host "2. Test all services" -ForegroundColor White
Write-Host "3. Remove old YAML files you don't need" -ForegroundColor White

Write-Host ""
Write-Host "🧪 Test commands:" -ForegroundColor Yellow
Write-Host "  kubectl get pods -n $NAMESPACE" -ForegroundColor Gray
Write-Host "  kubectl get services -n $NAMESPACE" -ForegroundColor Gray
Write-Host "  kubectl logs -l app=backend -n $NAMESPACE --tail=50" -ForegroundColor Gray
