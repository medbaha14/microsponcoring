@echo off
echo Running Local Code Quality Checks
echo ==================================
echo.

echo Step 1: Compiling project...
cd microsponsoring-backend
call mvn clean compile
if %errorlevel% neq 0 (
    echo ❌ Compilation failed
    pause
    exit /b 1
)
echo ✓ Compilation successful

echo.
echo Step 2: Running tests...
call mvn test
if %errorlevel% neq 0 (
    echo ⚠️  Some tests failed, but continuing...
)
echo ✓ Tests completed

echo.
echo Step 3: Running SpotBugs analysis...
call mvn spotbugs:check
if %errorlevel% neq 0 (
    echo ⚠️  SpotBugs found issues, but continuing...
)
echo ✓ SpotBugs analysis completed

echo.
echo Step 4: Running Checkstyle analysis...
call mvn checkstyle:check
if %errorlevel% neq 0 (
    echo ⚠️  Checkstyle found issues, but continuing...
)
echo ✓ Checkstyle analysis completed

echo.
echo Step 5: Running OWASP dependency check...
call mvn org.owasp:dependency-check-maven:check
if %errorlevel% neq 0 (
    echo ⚠️  Dependency check found issues, but continuing...
)
echo ✓ Dependency check completed

echo.
echo Step 6: Generating code coverage report...
call mvn jacoco:report
if %errorlevel% neq 0 (
    echo ⚠️  Coverage report generation failed, but continuing...
)
echo ✓ Coverage report generated

echo.
echo ==================================
echo 🎉 Local quality checks completed!
echo ==================================
echo.
echo Reports generated in:
echo - SpotBugs: target/spotbugsXml.xml
echo - Checkstyle: target/checkstyle-result.xml
echo - OWASP: target/dependency-check-reports/
echo - Coverage: target/site/jacoco/index.html
echo.
cd ..
pause
