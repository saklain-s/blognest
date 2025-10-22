# BlogNest Service Build Script
Write-Host "Building BlogNest Microservices..." -ForegroundColor Green

# Build each service individually
$services = @("user-service", "blog-service", "comment-service", "notification-service", "api-gateway")

foreach ($service in $services) {
    Write-Host "Building $service..." -ForegroundColor Yellow
    Set-Location $service
    mvn clean package -DskipTests
    if ($LASTEXITCODE -eq 0) {
        Write-Host "$service built successfully!" -ForegroundColor Green
    } else {
        Write-Host "Failed to build $service" -ForegroundColor Red
    }
    Set-Location ..
}

Write-Host "All services built!" -ForegroundColor Green



