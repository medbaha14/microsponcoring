# WebSocket LoadBalancer testing script
Write-Host "🧪 Testing WebSocket with LoadBalancer..." -ForegroundColor Green

$NAMESPACE = "microsponsoring"

# Get LoadBalancer IP
$WEBSOCKET_IP = kubectl get service websocket-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
if (-not $WEBSOCKET_IP) {
    $WEBSOCKET_IP = kubectl get service websocket-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
}

if (-not $WEBSOCKET_IP) {
    Write-Host "❌ LoadBalancer IP not assigned yet. Waiting..." -ForegroundColor Yellow
    Write-Host "Current service status:" -ForegroundColor Cyan
    kubectl get services -n $NAMESPACE
    Write-Host "Please wait a few minutes for the LoadBalancer to get an external IP." -ForegroundColor Yellow
    exit 1
}

Write-Host "🌐 WebSocket LoadBalancer IP: $WEBSOCKET_IP" -ForegroundColor Cyan

# Test URLs
$API_URL = "http://$WEBSOCKET_IP:80"
$WS_URL = "ws://$WEBSOCKET_IP:8081/ws-notifications"
$HEALTH_URL = "http://$WEBSOCKET_IP:80/api/actuator/health"

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
Write-Host "🔍 Testing WebSocket connection..." -ForegroundColor Yellow

# Test WebSocket with curl (HTTP upgrade)
Write-Host "Testing WebSocket upgrade: $WS_URL" -ForegroundColor Cyan
try {
    $wsResponse = Invoke-WebRequest -Uri "http://$WEBSOCKET_IP:8081/ws-notifications" -Method GET -Headers @{
        "Upgrade" = "websocket"
        "Connection" = "Upgrade"
        "Sec-WebSocket-Key" = "dGhlIHNhbXBsZSBub25jZQ=="
        "Sec-WebSocket-Version" = "13"
    } -TimeoutSec 10
    Write-Host "✅ WebSocket upgrade: $($wsResponse.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ WebSocket upgrade failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "🔍 Checking pod status..." -ForegroundColor Yellow
kubectl get pods -n $NAMESPACE -l app=backend

Write-Host ""
Write-Host "🔍 Checking service endpoints..." -ForegroundColor Yellow
kubectl get endpoints -n $NAMESPACE

Write-Host ""
Write-Host "📊 WebSocket LoadBalancer Information:" -ForegroundColor Cyan
Write-Host "  API: http://$WEBSOCKET_IP:80" -ForegroundColor White
Write-Host "  WebSocket: ws://$WEBSOCKET_IP:8081/ws-notifications" -ForegroundColor White
Write-Host "  Health: http://$WEBSOCKET_IP:80/api/actuator/health" -ForegroundColor White

Write-Host ""
Write-Host "💡 WebSocket Testing Tips:" -ForegroundColor Yellow
Write-Host "1. Use browser developer tools to test WebSocket connection" -ForegroundColor White
Write-Host "2. Test with wscat: wscat -c ws://$WEBSOCKET_IP:8081/ws-notifications" -ForegroundColor White
Write-Host "3. Check for CORS issues in browser console" -ForegroundColor White
Write-Host "4. Verify session affinity is working (connections stick to same pod)" -ForegroundColor White

Write-Host ""
Write-Host "🔧 Frontend Configuration Update:" -ForegroundColor Yellow
Write-Host "Update your frontend environment to use:" -ForegroundColor White
Write-Host "  apiUrl: 'http://$WEBSOCKET_IP:80/api'" -ForegroundColor Gray
Write-Host "  wsUrl: 'ws://$WEBSOCKET_IP:8081/ws-notifications'" -ForegroundColor Gray
