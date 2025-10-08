# PowerShell deployment script for complete fix
Write-Host "🚀 Starting complete deployment fix..." -ForegroundColor Green

# Set namespace
$NAMESPACE = "microsponsoring"

# Check if we're in the right directory
if (-not (Test-Path "backend-deployment.yaml")) {
    Write-Host "❌ Error: backend-deployment.yaml not found. Please run this script from the k8s directory." -ForegroundColor Red
    exit 1
}

Write-Host "📋 Step 1: Clean up existing resources..." -ForegroundColor Yellow
kubectl delete ingress microsponsoring-ingress -n $NAMESPACE --ignore-not-found=true
kubectl delete deployment backend-deployment -n $NAMESPACE --ignore-not-found=true
kubectl delete service backend-service -n $NAMESPACE --ignore-not-found=true
kubectl delete service websocket-service -n $NAMESPACE --ignore-not-found=true
kubectl delete service websocket-nodeport -n $NAMESPACE --ignore-not-found=true

Write-Host "⏳ Waiting for cleanup to complete..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "🔧 Step 2: Apply backend deployment with fixed configuration..." -ForegroundColor Yellow
kubectl apply -f backend-deployment.yaml

Write-Host "⏳ Waiting for backend to be ready..." -ForegroundColor Yellow
kubectl wait --for=condition=available --timeout=300s deployment/backend-deployment -n $NAMESPACE

Write-Host "🔍 Step 3: Check backend status..." -ForegroundColor Yellow
kubectl get pods -n $NAMESPACE -l app=backend
kubectl get services -n $NAMESPACE

Write-Host "🌐 Step 4: Apply simple ingress configuration..." -ForegroundColor Yellow
kubectl apply -f ingress-simple.yaml

Write-Host "⏳ Waiting for ingress to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "🔍 Step 5: Check ingress status..." -ForegroundColor Yellow
kubectl get ingress -n $NAMESPACE

Write-Host "🧪 Step 6: Test backend connectivity..." -ForegroundColor Yellow
$BACKEND_POD = kubectl get pods -l app=backend -n $NAMESPACE -o jsonpath='{.items[0].metadata.name}'
if ($BACKEND_POD) {
    Write-Host "Testing backend health endpoint..." -ForegroundColor Cyan
    kubectl exec -it $BACKEND_POD -n $NAMESPACE -- wget -qO- http://localhost:8080/api/actuator/health
    
    Write-Host "Testing WebSocket endpoint..." -ForegroundColor Cyan
    kubectl exec -it $BACKEND_POD -n $NAMESPACE -- wget -qO- http://localhost:8080/api/ws-notifications
} else {
    Write-Host "❌ No backend pod found" -ForegroundColor Red
}

Write-Host "🌍 Step 7: Get access information..." -ForegroundColor Yellow
$INGRESS_IP = kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}'
$INGRESS_PORT = kubectl get service -n ingress-nginx ingress-nginx-controller -o jsonpath='{.spec.ports[?(@.name=="http")].nodePort}'

Write-Host ""
Write-Host "🎉 Deployment completed!" -ForegroundColor Green
Write-Host "📊 Access Information:" -ForegroundColor Cyan
Write-Host "  Frontend: http://microsponsoring.local:$INGRESS_PORT" -ForegroundColor White
Write-Host "  API: http://microsponsoring.local:$INGRESS_PORT/api" -ForegroundColor White
Write-Host "  WebSocket: ws://microsponsoring.local:$INGRESS_PORT/ws-notifications" -ForegroundColor White
Write-Host "  Health: http://microsponsoring.local:$INGRESS_PORT/actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "🔧 If you need to test locally, add this to your /etc/hosts:" -ForegroundColor Yellow
Write-Host "  $INGRESS_IP microsponsoring.local" -ForegroundColor White
Write-Host ""
Write-Host "📝 To check logs:" -ForegroundColor Yellow
Write-Host "  kubectl logs -l app=backend -n $NAMESPACE --tail=50" -ForegroundColor White
Write-Host "  kubectl logs -l app=frontend -n $NAMESPACE --tail=50" -ForegroundColor White
