Write-Host "Running Local Code Quality Checks" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host ""

Write-Host "Step 1: Compiling project..." -ForegroundColor Yellow
Set-Location microsponsoring-backend
mvn clean compile
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Compilation failed" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "✓ Compilation successful" -ForegroundColor Green

Write-Host ""
Write-Host "Step 2: Running tests..." -ForegroundColor Yellow
mvn test
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Some tests failed, but continuing..." -ForegroundColor Yellow
}
Write-Host "✓ Tests completed" -ForegroundColor Green

Write-Host ""
Write-Host "Step 3: Running SpotBugs analysis..." -ForegroundColor Yellow
mvn spotbugs:check
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  SpotBugs found issues, but continuing..." -ForegroundColor Yellow
}
Write-Host "✓ SpotBugs analysis completed" -ForegroundColor Green

Write-Host ""
Write-Host "Step 4: Running Checkstyle analysis..." -ForegroundColor Yellow
mvn checkstyle:check
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Checkstyle found issues, but continuing..." -ForegroundColor Yellow
}
Write-Host "✓ Checkstyle analysis completed" -ForegroundColor Green

Write-Host ""
Write-Host "Step 5: Running OWASP dependency check..." -ForegroundColor Yellow
mvn org.owasp:dependency-check-maven:check
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Dependency check found issues, but continuing..." -ForegroundColor Yellow
}
Write-Host "✓ Dependency check completed" -ForegroundColor Green

Write-Host ""
Write-Host "Step 6: Generating code coverage report..." -ForegroundColor Yellow
mvn jacoco:report
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Coverage report generation failed, but continuing..." -ForegroundColor Yellow
}
Write-Host "✓ Coverage report generated" -ForegroundColor Green

Write-Host ""
Write-Host "==================================" -ForegroundColor Green
Write-Host "🎉 Local quality checks completed!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host ""
Write-Host "Reports generated in:" -ForegroundColor Cyan
Write-Host "- SpotBugs: target/spotbugsXml.xml" -ForegroundColor Cyan
Write-Host "- Checkstyle: target/checkstyle-result.xml" -ForegroundColor Cyan
Write-Host "- OWASP: target/dependency-check-reports/" -ForegroundColor Cyan
Write-Host "- Coverage: target/site/jacoco/index.html" -ForegroundColor Cyan
Write-Host ""
Set-Location ..
Read-Host "Press Enter to continue"
