@echo off
echo Building BlogNest Docker Images...

echo.
echo Building Service Discovery...
docker build -t blognest/service-discovery:latest ./service-discovery

echo.
echo Building Config Server...
docker build -t blognest/config-server:latest ./config-server

echo.
echo Building User Service...
docker build -t blognest/user-service:latest ./user-service

echo.
echo Building Blog Service...
docker build -t blognest/blog-service:latest ./blog-service

echo.
echo Building Comment Service...
docker build -t blognest/comment-service:latest ./comment-service

echo.
echo Building Notification Service...
docker build -t blognest/notification-service:latest ./notification-service

echo.
echo Building API Gateway...
docker build -t blognest/api-gateway:latest ./api-gateway

echo.
echo Building Frontend...
docker build -t blognest/frontend:latest ./frontend

echo.
echo All Docker images built successfully!
echo.
echo Available images:
docker images | findstr blognest



