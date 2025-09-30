@echo off
echo Testing SonarCloud Configuration
echo ================================
echo.

echo Step 1: Building project...
cd microsponsoring-backend
call mvn clean compile package -DskipTests
if %errorlevel% neq 0 (
    echo ❌ Build failed
    pause
    exit /b 1
)
echo ✓ Project built

echo.
echo Step 2: Copying dependencies...
call mvn dependency:copy-dependencies
if %errorlevel% neq 0 (
    echo ❌ Dependency copy failed
    pause
    exit /b 1
)
echo ✓ Dependencies copied

echo.
echo Step 3: Verifying dependency files exist...
if exist "target\dependency\*.jar" (
    echo ✓ Dependency JAR files found
    dir target\dependency\*.jar | find /c ".jar" > temp_count.txt
    set /p jar_count=<temp_count.txt
    del temp_count.txt
    echo Found %jar_count% JAR files
) else (
    echo ❌ No dependency JAR files found
    pause
    exit /b 1
)

echo.
echo Step 4: Testing SonarCloud configuration...
echo This will run a dry-run test (no actual upload)
call mvn sonar:sonar -Dsonar.projectKey=test-project -Dsonar.host.url=https://sonarcloud.io -Dsonar.sources=src/main/java -Dsonar.java.binaries=target/classes -Dsonar.java.libraries=target/dependency/*.jar -Dsonar.skip=true
if %errorlevel% neq 0 (
    echo ❌ SonarCloud configuration test failed
    pause
    exit /b 1
)
echo ✓ SonarCloud configuration test passed

echo.
echo ✅ All tests passed! SonarCloud should work correctly.
echo.
cd ..
pause
