# PowerShell deployment script with LoadBalancer
Write-Host "🚀 Starting deployment with LoadBalancer..." -ForegroundColor Green

$NAMESPACE = "microsponsoring"

# Check if we're in the right directory
if (-not (Test-Path "backend-deployment.yaml")) {
    Write-Host "❌ Error: backend-deployment.yaml not found. Please run this script from the k8s directory." -ForegroundColor Red
    exit 1
}

Write-Host "📋 Step 1: Clean up existing resources..." -ForegroundColor Yellow
kubectl delete ingress microsponsoring-ingress -n $NAMESPACE --ignore-not-found=true
kubectl delete service frontend-loadbalancer -n $NAMESPACE --ignore-not-found=true
kubectl delete service backend-loadbalancer -n $NAMESPACE --ignore-not-found=true
kubectl delete service image-loadbalancer -n $NAMESPACE --ignore-not-found=true
kubectl delete service backend-nodeport -n $NAMESPACE --ignore-not-found=true
kubectl delete service frontend-nodeport -n $NAMESPACE --ignore-not-found=true

Write-Host "⏳ Waiting for cleanup to complete..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "🔧 Step 2: Apply backend deployment..." -ForegroundColor Yellow
kubectl apply -f backend-deployment.yaml

Write-Host "⏳ Waiting for backend to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=available --timeout=300s deployment/backend-deployment -n $NAMESPACE

Write-Host "🌐 Step 3: Apply LoadBalancer services..." -ForegroundColor Yellow
kubectl apply -f loadbalancer-services.yaml

Write-Host "⏳ Waiting for LoadBalancer services to get external IPs..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

Write-Host "🔍 Step 4: Check service status..." -ForegroundColor Yellow
kubectl get services -n $NAMESPACE

Write-Host "🧪 Step 5: Test connectivity..." -ForegroundColor Yellow

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
Write-Host "🎉 LoadBalancer deployment completed!" -ForegroundColor Green
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
Write-Host "🔍 Current service status:" -ForegroundColor Yellow
kubectl get services -n $NAMESPACE

Write-Host ""
Write-Host "📝 To check external IPs:" -ForegroundColor Yellow
Write-Host "  kubectl get services -n $NAMESPACE" -ForegroundColor White

Write-Host ""
Write-Host "💡 Note: External IPs may take a few minutes to be assigned by your cloud provider." -ForegroundColor Cyan
