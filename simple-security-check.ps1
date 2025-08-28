# Simple Security Check Script for Microsponsoring Project
# This script provides better formatted security output with colors

param(
    [switch]$Fix,
    [switch]$Force,
    [switch]$Export
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
    if ($Colors.ContainsKey($Color)) {
        Write-Host $Message -ForegroundColor $Colors[$Color]
    } else {
        Write-Host $Message
    }
}

function Write-SectionHeader {
    param([string]$Title)
    Write-Host ""
    Write-Host "=" * 80 -ForegroundColor $Colors.Info
    Write-Host " $Title" -ForegroundColor $Colors.Info
    Write-Host "=" * 80 -ForegroundColor $Colors.Info
    Write-Host ""
}

# Start the security check
Write-SectionHeader "🔒 MICROSPONSORING SECURITY CHECK"
Write-ColorOutput "Starting comprehensive security analysis..." "Info"
Write-ColorOutput "Timestamp: $(Get-Date)" "Info"
Write-Host ""

# Frontend Security Check
Write-SectionHeader "📱 FRONTEND SECURITY CHECK (NPM)"

try {
    Set-Location "microsponsoring-frontend"
    Write-ColorOutput "Checking NPM dependencies for vulnerabilities..." "Info"
    
    # Run npm audit
    npm audit --audit-level moderate
    
    if ($Fix) {
        Write-ColorOutput "Attempting to fix vulnerabilities..." "Warning"
        if ($Force) {
            npm audit fix --force
        } else {
            npm audit fix
        }
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
    mvn org.owasp:dependency-check-maven:check
    
    # Look for HTML report
    $reportPath = "target/dependency-check-reports"
    if (Test-Path $reportPath) {
        $htmlFiles = Get-ChildItem "$reportPath/*.html" -ErrorAction SilentlyContinue
        if ($htmlFiles) {
            Write-ColorOutput "📄 Security report generated at: $($htmlFiles[0].FullName)" "Info"
            Write-ColorOutput "   Open this file in your browser for detailed vulnerability information" "Info"
        }
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
    npm outdated
    Set-Location ".."
    
    # Backend updates
    Set-Location "microsponsoring-backend"
    Write-ColorOutput "Backend (Maven) dependency updates:" "Info"
    mvn versions:display-dependency-updates
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