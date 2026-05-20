# setup.ps1 - Complete local setup script for Windows
# Usage: powershell -ExecutionPolicy Bypass -File setup.ps1

Write-Host "================================" -ForegroundColor Green
Write-Host "Journal Backend Setup Script" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""

# Check Prerequisites
Write-Host "Checking prerequisites..." -ForegroundColor Yellow

try {
    $javaVersion = java -version 2>&1 | Select-String 'version'
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Java is installed" -ForegroundColor Green
        Write-Host $javaVersion -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Java 17+ not found. Install Java first." -ForegroundColor Red
    exit 1
}

try {
    $mvnVersion = mvn --version | Select-Object -First 1
    Write-Host "✓ Maven is installed" -ForegroundColor Green
    Write-Host $mvnVersion -ForegroundColor Green
} catch {
    Write-Host "❌ Maven not found. Install Maven first." -ForegroundColor Red
    exit 1
}

try {
    $dockerVersion = docker --version
    Write-Host "✓ Docker is installed: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker not found. Install Docker first." -ForegroundColor Red
    exit 1
}

# Start Docker services
Write-Host ""
Write-Host "Starting Docker services (MongoDB, Redis, Kafka, Zookeeper)..." -ForegroundColor Yellow
Write-Host ""

docker-compose up -d

Write-Host "✓ Services started" -ForegroundColor Green
Write-Host "  - MongoDB: localhost:27017" -ForegroundColor Gray
Write-Host "  - Redis: localhost:6379" -ForegroundColor Gray
Write-Host "  - Kafka: localhost:9092" -ForegroundColor Gray
Write-Host "  - Zookeeper: localhost:2181" -ForegroundColor Gray
Write-Host ""

# Wait for services
Write-Host "Waiting for services to be ready (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Test connections
Write-Host "Testing connections..." -ForegroundColor Yellow
Write-Host ""

$mongoHealthy = $false
$redisHealthy = $false

try {
    docker exec journal-mongo mongosh --eval "db.adminCommand('ping')" 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ MongoDB is ready" -ForegroundColor Green
        $mongoHealthy = $true
    }
} catch {
    Write-Host "⚠ MongoDB might not be ready. Check: docker logs journal-mongo" -ForegroundColor Yellow
}

try {
    $redisResult = docker exec redis redis-cli ping 2>$null
    if ($redisResult -like "*PONG*") {
        Write-Host "✓ Redis is ready" -ForegroundColor Green
        $redisHealthy = $true
    }
} catch {
    Write-Host "⚠ Redis might not be ready. Check: docker logs redis" -ForegroundColor Yellow
}

Write-Host ""

# Build the application
Write-Host "Building application with Maven..." -ForegroundColor Yellow
Write-Host ""

mvn clean package -DskipTests

if (Test-Path "target/Backend-0.0.1-SNAPSHOT.jar") {
    Write-Host "✓ Build successful" -ForegroundColor Green
} else {
    Write-Host "❌ Build failed" -ForegroundColor Red
    exit 1
}

# Display instructions
Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "Setup Complete! 🎉" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""

Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host ""

Write-Host "1️⃣  Start the application:" -ForegroundColor Cyan
Write-Host "    mvn -Dspring-boot.run.profiles=dev spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "    OR" -ForegroundColor Gray
Write-Host ""
Write-Host "    java -jar target/Backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev" -ForegroundColor Gray
Write-Host ""

Write-Host "2️⃣  Access the application:" -ForegroundColor Cyan
Write-Host "    - API: http://localhost:8081" -ForegroundColor Gray
Write-Host "    - Swagger UI: http://localhost:8081/swagger-ui.html" -ForegroundColor Gray
Write-Host "    - OpenAPI JSON: http://localhost:8081/v3/api-docs" -ForegroundColor Gray
Write-Host ""

Write-Host "3️⃣  Test Kafka:" -ForegroundColor Cyan
Write-Host "    - Register a user via POST /auth/register" -ForegroundColor Gray
Write-Host "    - Check Kafka consumer logs: docker logs kafka" -ForegroundColor Gray
Write-Host "    - Verify 'Welcome' message in logs" -ForegroundColor Gray
Write-Host ""

Write-Host "4️⃣  Useful Docker commands:" -ForegroundColor Cyan
Write-Host "    - View logs: docker logs -f journal-mongo" -ForegroundColor Gray
Write-Host "    - Stop services: docker-compose down" -ForegroundColor Gray
Write-Host "    - Reset everything: docker-compose down -v; docker-compose up -d" -ForegroundColor Gray
Write-Host ""

Write-Host "Note: Dev credentials are already configured in application-dev.yml" -ForegroundColor Yellow
Write-Host ""

Write-Host "Services Status:" -ForegroundColor Cyan
if ($mongoHealthy) {
    Write-Host "  ✓ MongoDB" -ForegroundColor Green
} else {
    Write-Host "  ⚠ MongoDB - might need time to start" -ForegroundColor Yellow
}

if ($redisHealthy) {
    Write-Host "  ✓ Redis" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Redis - might need time to start" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Happy Coding! 💻" -ForegroundColor Magenta

