# BlogNest Docker Setup Commands

## Step 1: Build Java Services
```bash
# Build all services (skip tests to avoid compilation issues)
mvn clean package -DskipTests

# If that fails, build services individually:
mvn clean package -pl service-discovery -DskipTests
mvn clean package -pl config-server -DskipTests
mvn clean package -pl user-service -DskipTests
mvn clean package -pl blog-service -DskipTests
mvn clean package -pl comment-service -DskipTests
mvn clean package -pl notification-service -DskipTests
mvn clean package -pl api-gateway -DskipTests
```

## Step 2: Build Docker Images
```bash
# Build all Docker images
docker build -t blognest/service-discovery:latest ./service-discovery
docker build -t blognest/config-server:latest ./config-server
docker build -t blognest/user-service:latest ./user-service
docker build -t blognest/blog-service:latest ./blog-service
docker build -t blognest/comment-service:latest ./comment-service
docker build -t blognest/notification-service:latest ./notification-service
docker build -t blognest/api-gateway:latest ./api-gateway
docker build -t blognest/frontend:latest ./frontend
```

## Step 3: Start Infrastructure Services
```bash
# Start only infrastructure services first
docker-compose -f docker-compose-minimal.yml up -d

# Or start all services
docker-compose up -d
```

## Step 4: Verify Services
```bash
# Check running containers
docker ps

# View logs
docker-compose logs -f

# Check specific service logs
docker-compose logs -f user-service
docker-compose logs -f blog-service
```

## Access Points
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Grafana**: http://localhost:3001
- **PostgreSQL**: localhost:5433
- **Redis**: localhost:6379
- **Elasticsearch**: http://localhost:9200

## Troubleshooting
```bash
# Stop all services
docker-compose down

# Remove all containers and volumes
docker-compose down -v

# Rebuild and restart
docker-compose up --build -d

# Check service health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## PowerShell Commands (Windows)
```powershell
# Build services
mvn clean package -DskipTests

# Build Docker images
docker build -t blognest/service-discovery:latest ./service-discovery
docker build -t blognest/config-server:latest ./config-server
docker build -t blognest/user-service:latest ./user-service
docker build -t blognest/blog-service:latest ./blog-service
docker build -t blognest/comment-service:latest ./comment-service
docker build -t blognest/notification-service:latest ./notification-service
docker build -t blognest/api-gateway:latest ./api-gateway
docker build -t blognest/frontend:latest ./frontend

# Start services
docker-compose up -d

# View logs
docker-compose logs -f
```



