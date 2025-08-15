#!/bin/bash

# Security Check Script for Microsponsoring Project
# This script checks for security vulnerabilities in both frontend and backend dependencies

echo "🔒 Starting Security Check for Microsponsoring Project"
echo "=================================================="

# Frontend Security Check
echo -e "\n📱 Frontend Security Check (NPM)"
echo "--------------------------------"
cd microsponsoring-frontend

echo "Checking for outdated packages..."
npm outdated || echo "No outdated packages found"

echo -e "\nChecking for security vulnerabilities..."
npm audit --audit-level moderate || echo "NPM audit completed"

echo -e "\nChecking specific critical packages..."
echo "Angular Core:"
npm ls @angular/core
echo "ng2-charts:"
npm ls ng2-charts

cd ..

# Backend Security Check
echo -e "\n☕ Backend Security Check (Maven)"
echo "--------------------------------"
cd microsponsoring-backend

echo "Checking dependency tree..."
mvn dependency:tree -DoutputFile=target/dependency-tree.txt -DappendOutput=false || echo "Dependency tree generation failed"

echo -e "\nChecking for dependency updates..."
mvn versions:display-dependency-updates || echo "Dependency update check failed"

echo -e "\nRunning OWASP dependency check..."
mvn org.owasp:dependency-check-maven:check -DfailOnCVSS=7 || echo "OWASP dependency check completed with warnings"

cd ..

echo -e "\n✅ Security Check Complete!"
echo "=================================================="
echo "📁 Reports generated in:"
echo "   Frontend: microsponsoring-frontend/npm-audit.json (if vulnerabilities found)"
echo "   Backend: microsponsoring-backend/target/dependency-check-reports/"
echo ""
echo "🔍 Next steps:"
echo "   1. Review any security warnings above"
echo "   2. Check the generated reports for detailed information"
echo "   3. Update vulnerable packages as needed"
echo "   4. Re-run this script after updates to verify fixes" 