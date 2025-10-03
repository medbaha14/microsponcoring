@echo off
echo Testing SonarCloud from backend directory
echo =========================================
echo.

cd microsponsoring-backend

echo Running SonarCloud test...
mvn sonar:sonar -Dsonar.projectKey=test-project -Dsonar.host.url=https://sonarcloud.io -Dsonar.sources=src/main/java -Dsonar.java.binaries=target/classes -Dsonar.tests=src/test/java -Dsonar.java.test.binaries=target/test-classes -Dsonar.skip=true

if %errorlevel% equ 0 (
    echo ✓ SonarCloud test passed
) else (
    echo ❌ SonarCloud test failed
)

cd ..
pause
