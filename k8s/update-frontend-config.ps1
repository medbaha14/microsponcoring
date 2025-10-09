# PowerShell script to update frontend environment with actual LoadBalancer IPs
Write-Host "🔧 Updating frontend environment configuration..." -ForegroundColor Green

$NAMESPACE = "microsponsoring"
$FRONTEND_ENV_FILE = "../microsponsoring-frontend/src/environments/environment.pod.ts"

# Get LoadBalancer IPs
Write-Host "📊 Getting LoadBalancer IPs..." -ForegroundColor Yellow

$BACKEND_IP = kubectl get service backend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
$FRONTEND_IP = kubectl get service frontend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
$IMAGE_IP = kubectl get service image-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}'

if (-not $BACKEND_IP) {
    $BACKEND_IP = kubectl get service backend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
}
if (-not $FRONTEND_IP) {
    $FRONTEND_IP = kubectl get service frontend-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
}
if (-not $IMAGE_IP) {
    $IMAGE_IP = kubectl get service image-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
}

Write-Host "Backend LoadBalancer IP: $BACKEND_IP" -ForegroundColor Cyan
Write-Host "Frontend LoadBalancer IP: $FRONTEND_IP" -ForegroundColor Cyan
Write-Host "Image LoadBalancer IP: $IMAGE_IP" -ForegroundColor Cyan

if (-not $BACKEND_IP -or -not $FRONTEND_IP -or -not $IMAGE_IP) {
    Write-Host "❌ Error: Could not get all LoadBalancer IPs. Please check if services are running." -ForegroundColor Red
    Write-Host "Run: kubectl get services -n $NAMESPACE" -ForegroundColor Yellow
    exit 1
}

# Update the environment file
Write-Host "📝 Updating environment file..." -ForegroundColor Yellow

$ENV_CONTENT = @"
export const environment = {
  production: true,
  // LoadBalancer URLs - Updated with actual IPs
  apiUrl: 'http://$BACKEND_IP:8080/api',
  authUrl: 'http://$BACKEND_IP:8080/api/auth',
  usersUrl: 'http://$BACKEND_IP:8080/api/users',
  paymentsUrl: 'http://$BACKEND_IP:8080/api/payments',
  uploadUrl: 'http://$BACKEND_IP:8080/api/upload',
  companiesUrl: 'http://$BACKEND_IP:8080/api/companies-non-profits',
  sponsorsUrl: 'http://$BACKEND_IP:8080/api/sponsors',
  recognitionBenefitsUrl: 'http://$BACKEND_IP:8080/api/recognition-benefits',
  invoicesUrl: 'http://$BACKEND_IP:8080/api/invoices',
  pageCustomizationsUrl: 'http://$BACKEND_IP:8080/api/page-customizations',
  bankAccountsUrl: 'http://$BACKEND_IP:8080/api/bank-accounts',
  notificationsUrl: 'http://$BACKEND_IP:8080/api/notifications',
  wsUrl: 'ws://$BACKEND_IP:8081/ws-notifications',
  imageUrl: 'http://$IMAGE_IP',
  baseUrl: 'http://$FRONTEND_IP'
};
"@

# Write the updated content to the file
$ENV_CONTENT | Out-File -FilePath $FRONTEND_ENV_FILE -Encoding UTF8

Write-Host "✅ Frontend environment updated successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Updated URLs:" -ForegroundColor Cyan
Write-Host "  Frontend: http://$FRONTEND_IP" -ForegroundColor White
Write-Host "  API: http://$BACKEND_IP:8080" -ForegroundColor White
Write-Host "  WebSocket: ws://$BACKEND_IP:8081" -ForegroundColor White
Write-Host "  Images: http://$IMAGE_IP" -ForegroundColor White

Write-Host ""
Write-Host "🔄 Next steps:" -ForegroundColor Yellow
Write-Host "1. Rebuild frontend with new configuration" -ForegroundColor White
Write-Host "2. Update frontend Docker image" -ForegroundColor White
Write-Host "3. Redeploy frontend to Kubernetes" -ForegroundColor White

Write-Host ""
Write-Host "💡 To rebuild frontend:" -ForegroundColor Yellow
Write-Host "  cd ../microsponsoring-frontend" -ForegroundColor Gray
Write-Host "  npm run build" -ForegroundColor Gray
Write-Host "  docker build -t medbaha/pfefrontend:latest ." -ForegroundColor Gray
Write-Host "  docker push medbaha/pfefrontend:latest" -ForegroundColor Gray
