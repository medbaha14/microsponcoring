# PowerShell script to fix common Checkstyle issues

Write-Host "Fixing Checkstyle issues..." -ForegroundColor Green

# Get all Java files in the backend
$javaFiles = Get-ChildItem -Path "microsponsoring-backend\src\main\java" -Filter "*.java" -Recurse

foreach ($file in $javaFiles) {
    Write-Host "Processing: $($file.FullName)" -ForegroundColor Yellow
    
    # Read file content
    $content = Get-Content $file.FullName -Raw
    
    # Fix trailing spaces
    $content = $content -replace '\s+$', ''
    
    # Ensure file ends with newline
    if ($content -notmatch '\n$') {
        $content += "`n"
    }
    
    # Write back to file
    Set-Content -Path $file.FullName -Value $content -NoNewline
}

Write-Host "Fixed trailing spaces and newlines" -ForegroundColor Green

# Fix star imports in specific files
$filesToFix = @(
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\model\Sponsor.java",
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\model\PaymentTransaction.java",
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\model\SecurityRule.java",
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\model\PerformanceMetric.java",
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\model\SecurityAuditLog.java",
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\model\Vulnerability.java"
)

foreach ($filePath in $filesToFix) {
    if (Test-Path $filePath) {
        Write-Host "Fixing star imports in: $filePath" -ForegroundColor Yellow
        
        $content = Get-Content $filePath -Raw
        
        # Replace star imports with specific imports
        $content = $content -replace 'import jakarta\.persistence\.\*;', @'
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
'@
        
        Set-Content -Path $filePath -Value $content -NoNewline
    }
}

Write-Host "Fixed star imports" -ForegroundColor Green

# Create package-info.java files
$packages = @(
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\model",
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\controller",
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\service",
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\repository",
    "microsponsoring-backend\src\main\java\com\example\microsponsoringbackend\config"
)

foreach ($package in $packages) {
    $packageInfoPath = Join-Path $package "package-info.java"
    if (-not (Test-Path $packageInfoPath)) {
        $packageName = $package -replace '.*\\src\\main\\java\\', '' -replace '\\', '.'
        $packageInfoContent = @"
/**
 * Package containing $($packageName.Split('.')[-1]) classes.
 */
package $packageName;
"@
        Set-Content -Path $packageInfoPath -Value $packageInfoContent
        Write-Host "Created: $packageInfoPath" -ForegroundColor Yellow
    }
}

Write-Host "Created package-info.java files" -ForegroundColor Green
Write-Host "Checkstyle issues fixed!" -ForegroundColor Green
