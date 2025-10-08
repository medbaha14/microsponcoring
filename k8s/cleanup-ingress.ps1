# PowerShell script to clean up Ingress and simplify to LoadBalancer-only setup
Write-Host "🧹 Cleaning up Ingress resources..." -ForegroundColor Green

$NAMESPACE = "microsponsoring"

Write-Host "📋 Step 1: Check current Ingress resources..." -ForegroundColor Yellow
kubectl get ingress -n $NAMESPACE
kubectl get services -n $NAMESPACE

Write-Host ""
Write-Host "🗑️ Step 2: Remove Ingress resources..." -ForegroundColor Yellow

# Remove all Ingress resources
kubectl delete ingress microsponsoring-ingress -n $NAMESPACE --ignore-not-found=true
kubectl delete ingress microsponsoring-ingress-simple -n $NAMESPACE --ignore-not-found=true
kubectl delete ingress microsponsoring-ingress-fixed -n $NAMESPACE --ignore-not-found=true

# Remove Ingress-related ConfigMaps
kubectl delete configmap websocket-headers -n $NAMESPACE --ignore-not-found=true

# Remove NodePort services (if using LoadBalancer)
kubectl delete service backend-nodeport -n $NAMESPACE --ignore-not-found=true
kubectl delete service frontend-nodeport -n $NAMESPACE --ignore-not-found=true
kubectl delete service websocket-nodeport -n $NAMESPACE --ignore-not-found=true

Write-Host ""
Write-Host "🔧 Step 3: Apply LoadBalancer-only configuration..." -ForegroundColor Yellow

# Apply LoadBalancer services
kubectl apply -f loadbalancer-services.yaml

Write-Host ""
Write-Host "⏳ Step 4: Wait for LoadBalancer IPs to be assigned..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host ""
Write-Host "🔍 Step 5: Check LoadBalancer status..." -ForegroundColor Yellow
kubectl get services -n $NAMESPACE

Write-Host ""
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
Write-Host "🎉 Ingress cleanup completed!" -ForegroundColor Green
Write-Host "📊 Direct LoadBalancer Access:" -ForegroundColor Cyan

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
Write-Host "🔍 Current service status:" -ForegroundColor Yellow
kubectl get services -n $NAMESPACE

Write-Host ""
Write-Host "📝 Next steps:" -ForegroundColor Yellow
Write-Host "1. Update frontend configuration with LoadBalancer IPs" -ForegroundColor White
Write-Host "2. Test direct access to services" -ForegroundColor White
Write-Host "3. Remove Ingress controller if not needed elsewhere" -ForegroundColor White

Write-Host ""
Write-Host "💡 Benefits of LoadBalancer-only setup:" -ForegroundColor Cyan
Write-Host "  ✅ Simpler configuration" -ForegroundColor Green
Write-Host "  ✅ Better performance" -ForegroundColor Green
Write-Host "  ✅ Direct service access" -ForegroundColor Green
Write-Host "  ✅ No Ingress complexity" -ForegroundColor Green
Write-Host "  ✅ Better WebSocket support" -ForegroundColor Green
