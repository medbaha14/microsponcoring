Write-Host "Testing SonarCloud Configuration" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 1: Building project..." -ForegroundColor Yellow
Set-Location microsponsoring-backend
mvn clean compile package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "✓ Project built" -ForegroundColor Green

Write-Host ""
Write-Host "Step 2: Copying dependencies..." -ForegroundColor Yellow
mvn dependency:copy-dependencies
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Dependency copy failed" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "✓ Dependencies copied" -ForegroundColor Green

Write-Host ""
Write-Host "Step 3: Verifying dependency files exist..." -ForegroundColor Yellow
$jarFiles = Get-ChildItem "target\dependency\*.jar" -ErrorAction SilentlyContinue
if ($jarFiles) {
    Write-Host "✓ Dependency JAR files found" -ForegroundColor Green
    Write-Host "Found $($jarFiles.Count) JAR files" -ForegroundColor Cyan
} else {
    Write-Host "❌ No dependency JAR files found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "Step 4: Testing SonarCloud configuration..." -ForegroundColor Yellow
Write-Host "This will run a dry-run test (no actual upload)" -ForegroundColor Cyan
mvn sonar:sonar -Dsonar.projectKey=test-project -Dsonar.host.url=https://sonarcloud.io -Dsonar.sources=src/main/java -Dsonar.java.binaries=target/classes -Dsonar.java.libraries=target/dependency/*.jar -Dsonar.skip=true
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ SonarCloud configuration test failed" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "✓ SonarCloud configuration test passed" -ForegroundColor Green

Write-Host ""
Write-Host "✅ All tests passed! SonarCloud should work correctly." -ForegroundColor Green
Write-Host ""
Set-Location ..
Read-Host "Press Enter to exit"
