# Simple WebSocket test script
Write-Host "🧪 Testing WebSocket connectivity..." -ForegroundColor Green

$NAMESPACE = "microsponsoring"

# Get ingress information
$INGRESS_IP = kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}'
$INGRESS_PORT = kubectl get service -n ingress-nginx ingress-nginx-controller -o jsonpath='{.spec.ports[?(@.name=="http")].nodePort}'

Write-Host "🌐 Ingress IP: $INGRESS_IP" -ForegroundColor Cyan
Write-Host "🌐 Ingress Port: $INGRESS_PORT" -ForegroundColor Cyan

# Test URLs
$API_URL = "http://microsponsoring.local:$INGRESS_PORT/api"
$WS_URL = "ws://microsponsoring.local:$INGRESS_PORT/ws-notifications"
$HEALTH_URL = "http://microsponsoring.local:$INGRESS_PORT/actuator/health"

Write-Host ""
Write-Host "🔍 Testing API endpoints..." -ForegroundColor Yellow

# Test health endpoint
Write-Host "Testing health endpoint: $HEALTH_URL" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri $HEALTH_URL -Method GET -TimeoutSec 10
    Write-Host "✅ Health check: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test API endpoint
Write-Host "Testing API endpoint: $API_URL" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri $API_URL -Method GET -TimeoutSec 10
    Write-Host "✅ API check: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ API check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "🔍 Checking pod status..." -ForegroundColor Yellow
kubectl get pods -n $NAMESPACE

Write-Host ""
Write-Host "🔍 Checking service endpoints..." -ForegroundColor Yellow
kubectl get endpoints -n $NAMESPACE

Write-Host ""
Write-Host "🔍 Checking ingress status..." -ForegroundColor Yellow
kubectl get ingress -n $NAMESPACE

Write-Host ""
Write-Host "📝 WebSocket URL for testing: $WS_URL" -ForegroundColor Cyan
Write-Host "💡 You can test WebSocket connection using browser developer tools or a WebSocket client" -ForegroundColor Yellow
