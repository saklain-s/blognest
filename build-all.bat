@echo off
echo Building BlogNest Microservices...

echo.
echo Step 1: Building Java services...
mvn clean package -DskipTests

echo.
echo Step 2: Building Docker images...
call build-docker-images.bat

echo.
echo Step 3: Starting services with Docker Compose...
docker-compose up -d

echo.
echo BlogNest is now running!
echo.
echo Access points:
echo - Frontend: http://localhost:3000
echo - API Gateway: http://localhost:8080
echo - Eureka Dashboard: http://localhost:8761
echo - Grafana: http://localhost:3001
echo.
echo To view logs: docker-compose logs -f
echo To stop: docker-compose down



