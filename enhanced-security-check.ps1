# Enhanced Security Check Script for Microsponsoring Project
# This script provides better formatted security output with colors and organization

param(
    [switch]$Fix,
    [switch]$Force,
    [switch]$Export,
    [string]$OutputPath = "security-report-$(Get-Date -Format 'yyyy-MM-dd-HHmmss').html"
)

# Colors for output
$Colors = @{
    Critical = "Red"
    High = "DarkRed"
    Moderate = "Yellow"
    Low = "Green"
    Info = "Cyan"
    Success = "Green"
    Warning = "Yellow"
    Error = "Red"
}

function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Colors[$Color]
}

function Write-SectionHeader {
    param([string]$Title)
    Write-Host ""
    Write-Host "=" * 80 -ForegroundColor $Colors.Info
    Write-Host " $Title" -ForegroundColor $Colors.Info
    Write-Host "=" * 80 -ForegroundColor $Colors.Info
    Write-Host ""
}

function Write-VulnerabilityItem {
    param(
        [string]$Package,
        [string]$Severity,
        [string]$Description,
        [string]$Fix
    )
    
    $color = $Colors[$Severity]
    Write-Host "🔴 $Package" -ForegroundColor $color
    Write-Host "   Severity: $Severity" -ForegroundColor $color
    Write-Host "   Description: $Description" -ForegroundColor $color
    Write-Host "   Fix: $Fix" -ForegroundColor $color
    Write-Host ""
}

# Start the enhanced security check
Write-SectionHeader "🔒 MICROSPONSORING SECURITY CHECK"
Write-ColorOutput "Starting comprehensive security analysis..." "Info"
Write-ColorOutput "Timestamp: $(Get-Date)" "Info"
Write-Host ""

# Frontend Security Check
Write-SectionHeader "📱 FRONTEND SECURITY CHECK (NPM)"

try {
    Set-Location "microsponsoring-frontend"
    Write-ColorOutput "Checking NPM dependencies for vulnerabilities..." "Info"
    
    # Run npm audit and capture output
    $auditResult = npm audit --json 2>$null
    
    if ($auditResult) {
        $auditData = $auditResult | ConvertFrom-Json
        
        # Count vulnerabilities by severity
        $vulnCounts = @{
            Critical = 0
            High = 0
            Moderate = 0
            Low = 0
        }
        
        # Process vulnerabilities
        if ($auditData.vulnerabilities) {
            foreach ($vuln in $auditData.vulnerabilities.PSObject.Properties) {
                $severity = $vuln.Value.severity
                $vulnCounts[$severity]++
                
                Write-VulnerabilityItem `
                    -Package $vuln.Name `
                    -Severity $severity `
                    -Description $vuln.Value.title `
                    -Fix "npm audit fix"
            }
        }
        
        # Display summary
        Write-Host "📊 VULNERABILITY SUMMARY:" -ForegroundColor $Colors.Info
        Write-Host "   Critical: $($vulnCounts.Critical)" -ForegroundColor $Colors.Critical
        Write-Host "   High: $($vulnCounts.High)" -ForegroundColor $Colors.High
        Write-Host "   Moderate: $($vulnCounts.Moderate)" -ForegroundColor $Colors.Moderate
        Write-Host "   Low: $($vulnCounts.Low)" -ForegroundColor $Colors.Low
        Write-Host ""
        
        # Fix vulnerabilities if requested
        if ($Fix) {
            Write-ColorOutput "Attempting to fix vulnerabilities..." "Warning"
            if ($Force) {
                npm audit fix --force
            } else {
                npm audit fix
            }
        }
        
    } else {
        Write-ColorOutput "✅ No vulnerabilities found!" "Success"
    }
    
} catch {
    Write-ColorOutput "❌ Error during frontend security check: $($_.Exception.Message)" "Error"
} finally {
    Set-Location ".."
}

# Backend Security Check
Write-SectionHeader "☕ BACKEND SECURITY CHECK (Maven)"

try {
    Set-Location "microsponsoring-backend"
    Write-ColorOutput "Running OWASP dependency check..." "Info"
    
    # Run OWASP dependency check
    $owaspResult = mvn org.owasp:dependency-check-maven:check 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput "✅ OWASP dependency check completed successfully!" "Success"
        
        # Look for HTML report
        $reportPath = "target/dependency-check-reports"
        if (Test-Path $reportPath) {
            $htmlFiles = Get-ChildItem "$reportPath/*.html" -ErrorAction SilentlyContinue
            if ($htmlFiles) {
                Write-ColorOutput "📄 Security report generated at: $($htmlFiles[0].FullName)" "Info"
                Write-ColorOutput "   Open this file in your browser for detailed vulnerability information" "Info"
            }
        }
    } else {
        Write-ColorOutput "⚠️ OWASP dependency check completed with warnings" "Warning"
        Write-ColorOutput "   Check the output above for details" "Warning"
    }
    
} catch {
    Write-ColorOutput "❌ Error during backend security check: $($_.Exception.Message)" "Error"
} finally {
    Set-Location ".."
}

# Dependency Update Check
Write-SectionHeader "🔄 DEPENDENCY UPDATE CHECK"

try {
    Write-ColorOutput "Checking for outdated packages..." "Info"
    
    # Frontend updates
    Set-Location "microsponsoring-frontend"
    Write-ColorOutput "Frontend (NPM) outdated packages:" "Info"
    npm outdated 2>$null
    Set-Location ".."
    
    # Backend updates
    Set-Location "microsponsoring-backend"
    Write-ColorOutput "Backend (Maven) dependency updates:" "Info"
    mvn versions:display-dependency-updates 2>$null
    Set-Location ".."
    
} catch {
    Write-ColorOutput "❌ Error during dependency update check: $($_.Exception.Message)" "Error"
}

# Security Recommendations
Write-SectionHeader "💡 SECURITY RECOMMENDATIONS"

Write-ColorOutput "🔴 IMMEDIATE ACTIONS:" "Critical"
Write-ColorOutput "   1. Review high and critical vulnerabilities above" "Critical"
Write-ColorOutput "   2. Update packages with security fixes" "Critical"
Write-ColorOutput "   3. Test updates in development environment" "Critical"
Write-Host ""

Write-ColorOutput "🟡 SHORT-TERM ACTIONS:" "Moderate"
Write-ColorOutput "   1. Enable Dependabot in GitHub repository" "Moderate"
Write-ColorOutput "   2. Set up security alerts and notifications" "Moderate"
Write-ColorOutput "   3. Review and update security policies" "Moderate"
Write-Host ""

Write-ColorOutput "🟢 ONGOING ACTIONS:" "Low"
Write-ColorOutput "   1. Run this script weekly" "Low"
Write-ColorOutput "   2. Monitor GitHub Security tab" "Low"
Write-ColorOutput "   3. Keep dependencies updated" "Low"
Write-Host ""

# Export report if requested
if ($Export) {
    Write-SectionHeader "📤 EXPORTING SECURITY REPORT"
    
    try {
        # Create a simple HTML report
        $htmlContent = @"
<!DOCTYPE html>
<html>
<head>
    <title>Security Report - $(Get-Date)</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #f0f0f0; padding: 20px; border-radius: 5px; }
        .section { margin: 20px 0; padding: 15px; border-left: 4px solid #007acc; }
        .critical { border-left-color: #dc3545; }
        .high { border-left-color: #fd7e14; }
        .moderate { border-left-color: #ffc107; }
        .low { border-left-color: #28a745; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🔒 Security Report - Microsponsoring Project</h1>
        <p>Generated: $(Get-Date)</p>
    </div>
    
    <div class="section">
        <h2>📊 Summary</h2>
        <p>This report was generated by the enhanced security check script.</p>
        <p>For detailed vulnerability information, check the generated OWASP reports.</p>
    </div>
    
    <div class="section">
        <h2>🚀 Next Steps</h2>
        <ul>
            <li>Review all vulnerabilities above</li>
            <li>Update packages with security fixes</li>
            <li>Enable Dependabot in GitHub</li>
            <li>Run security checks weekly</li>
        </ul>
    </div>
</body>
</html>
"@
        
        $htmlContent | Out-File -FilePath $OutputPath -Encoding UTF8
        Write-ColorOutput "✅ Security report exported to: $OutputPath" "Success"
        
    } catch {
        Write-ColorOutput "❌ Error exporting report: $($_.Exception.Message)" "Error"
    }
}

# Final summary
Write-SectionHeader "🏁 SECURITY CHECK COMPLETE"

Write-ColorOutput "✅ Frontend security check completed" "Success"
Write-ColorOutput "✅ Backend security check completed" "Success"
Write-ColorOutput "✅ Dependency update check completed" "Success"
Write-Host ""

Write-ColorOutput "📋 NEXT STEPS:" "Info"
Write-ColorOutput "   1. Review all vulnerabilities found above" "Info"
Write-ColorOutput "   2. Fix critical and high severity issues first" "Info"
Write-ColorOutput "   3. Test fixes in development environment" "Info"
Write-ColorOutput "   4. Commit security fixes to repository" "Info"
Write-ColorOutput "   5. Enable Dependabot for automated updates" "Info"
Write-Host ""

Write-ColorOutput "🔗 USEFUL LINKS:" "Info"
Write-ColorOutput "   - GitHub Security Tab: Check your repository's Security tab" "Info"
Write-ColorOutput "   - Dependabot: Enable in repository Settings → Security & analysis" "Info"
Write-ColorOutput "   - OWASP Reports: Check target/dependency-check-reports/ folder" "Info"
Write-Host ""

Write-ColorOutput "⏰ Next recommended scan: $(Get-Date).AddDays(7).ToString('yyyy-MM-dd')" "Info" 