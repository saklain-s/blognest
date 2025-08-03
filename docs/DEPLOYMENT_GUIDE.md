# BlogNest Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying BlogNest in various environments, from local development to production.

## Prerequisites

### System Requirements

- **Java**: OpenJDK 17 or higher
- **Node.js**: 18.x or higher
- **Docker**: 20.10 or higher
- **Docker Compose**: 2.0 or higher
- **PostgreSQL**: 15 or higher
- **Redis**: 7.x or higher
- **Elasticsearch**: 8.11.x or higher

### Minimum Hardware Requirements

- **CPU**: 4 cores
- **RAM**: 8GB
- **Storage**: 50GB SSD
- **Network**: 100Mbps

## Local Development Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/blognest.git
cd blognest
```

### 2. Set Up Environment Variables

Create `.env` file in the root directory:

```bash
# Database Configuration
POSTGRES_DB=blognest
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Elasticsearch Configuration
ELASTICSEARCH_URL=http://localhost:9200

# JWT Configuration
JWT_SECRET=your-secret-key-here-make-it-long-and-secure
JWT_EXPIRATION=86400000

# Service Ports
USER_SERVICE_PORT=8081
BLOG_SERVICE_PORT=8082
COMMENT_SERVICE_PORT=8083
NOTIFICATION_SERVICE_PORT=8084
API_GATEWAY_PORT=8080
FRONTEND_PORT=3000

# Monitoring
PROMETHEUS_PORT=9090
GRAFANA_PORT=3001
```

### 3. Start Infrastructure Services

```bash
# Start PostgreSQL, Redis, and Elasticsearch
docker-compose up -d postgres redis elasticsearch

# Wait for services to be ready
docker-compose logs -f postgres redis elasticsearch
```

### 4. Build and Run Backend Services

```bash
# Build all services
mvn clean install -DskipTests

# Start services in order
mvn spring-boot:run -pl service-discovery
mvn spring-boot:run -pl config-server
mvn spring-boot:run -pl user-service
mvn spring-boot:run -pl blog-service
mvn spring-boot:run -pl comment-service
mvn spring-boot:run -pl notification-service
mvn spring-boot:run -pl api-gateway
```

### 5. Start Frontend

```bash
cd frontend
npm install
npm start
```

### 6. Verify Deployment

- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Frontend**: http://localhost:3000
- **Eureka Dashboard**: http://localhost:8761
- **Grafana**: http://localhost:3001 (admin/admin)

## Docker Deployment

### 1. Build Docker Images

```bash
# Build all services
docker build -t blognest/user-service:latest ./user-service
docker build -t blognest/blog-service:latest ./blog-service
docker build -t blognest/comment-service:latest ./comment-service
docker build -t blognest/notification-service:latest ./notification-service
docker build -t blognest/api-gateway:latest ./api-gateway
docker build -t blognest/frontend:latest ./frontend
```

### 2. Deploy with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### 3. Production Docker Compose

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  # Database with persistent storage
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: blognest
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - blognest-network
    restart: unless-stopped

  # Redis with persistence
  redis:
    image: redis:7-alpine
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    networks:
      - blognest-network
    restart: unless-stopped

  # Elasticsearch with security
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.3
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=true
      - ELASTIC_PASSWORD=${ELASTIC_PASSWORD}
      - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    networks:
      - blognest-network
    restart: unless-stopped

  # Application services
  user-service:
    image: blognest/user-service:latest
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/blognest_users
      - SPRING_REDIS_HOST=redis
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka:8761/eureka/
    networks:
      - blognest-network
    restart: unless-stopped
    depends_on:
      - postgres
      - redis

  # ... other services

  # Nginx reverse proxy
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    networks:
      - blognest-network
    restart: unless-stopped
    depends_on:
      - api-gateway
      - frontend

volumes:
  postgres_data:
  redis_data:
  elasticsearch_data:

networks:
  blognest-network:
    driver: bridge
```

## Kubernetes Deployment

### 1. Create Namespace

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: blognest
```

### 2. Create ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: blognest-config
  namespace: blognest
data:
  POSTGRES_DB: blognest
  REDIS_HOST: redis-service
  ELASTICSEARCH_URL: http://elasticsearch-service:9200
```

### 3. Create Secrets

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: blognest-secrets
  namespace: blognest
type: Opaque
data:
  POSTGRES_PASSWORD: <base64-encoded-password>
  JWT_SECRET: <base64-encoded-jwt-secret>
  ELASTIC_PASSWORD: <base64-encoded-elastic-password>
```

### 4. Deploy Services

```bash
# Apply all Kubernetes manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/elasticsearch.yaml
kubectl apply -f k8s/services.yaml
kubectl apply -f k8s/deployments.yaml
kubectl apply -f k8s/ingress.yaml
```

### 5. Monitor Deployment

```bash
# Check pod status
kubectl get pods -n blognest

# View logs
kubectl logs -f deployment/user-service -n blognest

# Check services
kubectl get svc -n blognest
```

## Cloud Deployment

### AWS Deployment

#### 1. EKS Setup

```bash
# Create EKS cluster
eksctl create cluster --name blognest-cluster --region us-west-2 --nodes 3

# Configure kubectl
aws eks update-kubeconfig --name blognest-cluster --region us-west-2
```

#### 2. RDS Setup

```bash
# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier blognest-postgres \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username postgres \
  --master-user-password <password> \
  --allocated-storage 20
```

#### 3. ElastiCache Setup

```bash
# Create Redis cluster
aws elasticache create-replication-group \
  --replication-group-id blognest-redis \
  --replication-group-description "BlogNest Redis" \
  --node-type cache.t3.micro \
  --num-cache-clusters 1
```

### Google Cloud Deployment

#### 1. GKE Setup

```bash
# Create GKE cluster
gcloud container clusters create blognest-cluster \
  --zone us-central1-a \
  --num-nodes 3 \
  --machine-type e2-standard-2
```

#### 2. Cloud SQL Setup

```bash
# Create Cloud SQL instance
gcloud sql instances create blognest-postgres \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=us-central1
```

## Production Configuration

### 1. Security Configuration

```yaml
# application-prod.yml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
  
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

logging:
  level:
    root: WARN
    com.blognest: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 2. Monitoring Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'blognest-services'
    static_configs:
      - targets: ['user-service:8081', 'blog-service:8082', 'api-gateway:8080']
    metrics_path: '/actuator/prometheus'
```

### 3. Load Balancer Configuration

```nginx
# nginx.conf
upstream api_backend {
    server api-gateway:8080;
}

upstream frontend_backend {
    server frontend:3000;
}

server {
    listen 80;
    server_name blognest.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name blognest.com;
    
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    
    location /api {
        proxy_pass http://api_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location / {
        proxy_pass http://frontend_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## CI/CD Pipeline

### GitHub Actions Workflow

The project includes a comprehensive CI/CD pipeline in `.github/workflows/ci-cd.yml` that:

1. **Tests**: Runs unit tests with 90% coverage requirement
2. **Security**: Performs security scans
3. **Builds**: Creates Docker images
4. **Deploys**: Deploys to staging and production environments

### Deployment Commands

```bash
# Deploy to staging
./scripts/deploy-staging.sh

# Deploy to production
./scripts/deploy-production.sh

# Rollback deployment
./scripts/rollback.sh
```

## Monitoring and Logging

### 1. Application Monitoring

- **Prometheus**: Metrics collection
- **Grafana**: Visualization and dashboards
- **Jaeger**: Distributed tracing
- **ELK Stack**: Log aggregation

### 2. Health Checks

```bash
# Check service health
curl http://localhost:8080/actuator/health

# Check specific service
curl http://localhost:8081/actuator/health
```

### 3. Performance Monitoring

```bash
# Monitor JVM metrics
curl http://localhost:8081/actuator/metrics/jvm.memory.used

# Monitor HTTP requests
curl http://localhost:8081/actuator/metrics/http.server.requests
```

## Backup and Recovery

### 1. Database Backup

```bash
# PostgreSQL backup
pg_dump -h localhost -U postgres blognest > backup.sql

# Automated backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -h localhost -U postgres blognest > backup_$DATE.sql
gzip backup_$DATE.sql
aws s3 cp backup_$DATE.sql.gz s3://blognest-backups/
```

### 2. Recovery Procedures

```bash
# Restore database
psql -h localhost -U postgres blognest < backup.sql

# Restore from S3
aws s3 cp s3://blognest-backups/backup_20231201_120000.sql.gz .
gunzip backup_20231201_120000.sql.gz
psql -h localhost -U postgres blognest < backup_20231201_120000.sql
```

## Troubleshooting

### Common Issues

1. **Service Discovery Issues**
   ```bash
   # Check Eureka status
   curl http://localhost:8761/eureka/apps
   
   # Restart Eureka
   docker-compose restart eureka
   ```

2. **Database Connection Issues**
   ```bash
   # Check PostgreSQL status
   docker-compose logs postgres
   
   # Test connection
   psql -h localhost -U postgres -d blognest
   ```

3. **Memory Issues**
   ```bash
   # Check JVM memory usage
   curl http://localhost:8081/actuator/metrics/jvm.memory.used
   
   # Increase heap size
   export JAVA_OPTS="-Xmx2g -Xms1g"
   ```

### Performance Optimization

1. **Database Optimization**
   ```sql
   -- Create indexes
   CREATE INDEX idx_users_username ON users(username);
   CREATE INDEX idx_blogs_author_id ON blog_posts(author_id);
   CREATE INDEX idx_blogs_status ON blog_posts(status);
   ```

2. **Caching Configuration**
   ```yaml
   spring:
     cache:
       type: redis
       redis:
         time-to-live: 600000
   ```

3. **Connection Pooling**
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 5
         connection-timeout: 30000
   ```

## Support and Maintenance

### 1. Regular Maintenance

- **Weekly**: Security updates and dependency checks
- **Monthly**: Performance reviews and capacity planning
- **Quarterly**: Architecture reviews and optimization

### 2. Monitoring Alerts

- **High CPU/Memory usage**
- **Database connection issues**
- **Service unavailability**
- **Error rate spikes**

### 3. Contact Information

- **Technical Support**: tech-support@blognest.com
- **Emergency**: +1-555-BLOG-HELP
- **Documentation**: https://docs.blognest.com
- **GitHub Issues**: https://github.com/yourusername/blognest/issues 