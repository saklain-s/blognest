# BlogNest – Project Overview

This document provides a comprehensive overview of the BlogNest project: architecture, technologies, repository structure, services, data flows, and how to run it locally or in containers.

## Monorepo layout

```
blognest/
- pom.xml                        # Maven parent, dependency and plugin mgmt
- docker-compose-minimal.yml     # Infra stack for local dev
- build-all.bat | build-services.ps1 | build-docker-images.bat
- run-commands.md

- common/                        # Shared DTOs, security utilities (compiled classes present)
- service-discovery/             # Eureka server
- config-server/                 # Spring Cloud Config Server
- api-gateway/                   # Spring Cloud Gateway
- user-service/                  # Auth & users
- blog-service/                  # Blogs, tags, categories, search
- comment-service/               # Comments
- notification-service/          # Notifications
- frontend/                      # React app, CRA toolchain, NGINX config for static hosting
- monitoring/                    # Prometheus config
```

## Technology stack

- Backend:
  - Java 17, Spring Boot 3.2, Spring Cloud 2023.0
  - Spring Web, Spring Data JPA, Spring Security
  - Spring Cloud Gateway, Eureka (Service Discovery), Spring Cloud Config
  - JWT (io.jsonwebtoken: jjwt-*), Lombok
  - Datastores: PostgreSQL, Redis, Elasticsearch (for search)
  - API docs: springdoc-openapi
  - Testing: JUnit 5, Mockito, JaCoCo
- Frontend:
  - React 18 (Create React App), React Router, Redux Toolkit, Axios
  - Material UI, Emotion styling, React Markdown, Socket.IO client
  - ESLint (react-app config); TypeScript listed as devDependency
- DevOps:
  - Dockerfiles per service, Docker Compose for infra
  - NGINX for static frontend hosting and `/api` proxy
  - Prometheus config present

## Core configuration highlights

- Parent Maven (`pom.xml`):
  - Coordinates Spring Boot, Spring Cloud versions and dependency management
  - Manages JWT, PostgreSQL, Redis, Elasticsearch, Swagger, testing libs
- API Gateway routes (`api-gateway/src/main/resources/application.yml`):
  - `/api/v1/users/**` → `lb://user-service` (StripPrefix=2)
  - `/api/v1/blogs/**` → `lb://blog-service` (StripPrefix=2)
  - `/api/v1/comments/**` → `lb://comment-service` (StripPrefix=2)
  - `/api/v1/notifications/**` → `lb://notification-service` (StripPrefix=2)
  - CORS wide open for dev; Redis configured
- Service Discovery (Eureka) (`service-discovery/.../application.yml`):
  - Runs on port 8761, standalone server (no self-registration)
- Config Server (`config-server/.../application.yml`):
  - Git-backed config repo (placeholder `https://github.com/yourusername/blognest-config.git`)
  - Registers with Eureka when enabled
- Docker Compose minimal (`docker-compose-minimal.yml`):
  - Postgres @ host 5433, Redis @ 6379, Elasticsearch @ 9200
  - Eureka @ 8761, Config Server @ 8888 (expects images prebuilt)
- Frontend dev proxy (`frontend/package.json`):
  - Proxies API to `http://localhost:8080`
- Frontend container NGINX (`frontend/nginx.conf`):
  - Serves SPA on 3000 and proxies `/api` to `api-gateway:8080`

## Services overview

- API Gateway (Spring Cloud Gateway)
  - Port 8080
  - Single entry point; path-based routing to microservices
  - Global CORS; integrates with Redis

- Service Discovery (Eureka Server)
  - Port 8761
  - Registry for client service discovery

- Config Server (Spring Cloud Config)
  - Port 8888
  - Externalized configuration from a Git repo

- User Service
  - Port 8081
  - Responsibilities: authentication, registration, user profile & roles
  - Databases/caches: PostgreSQL `blognest_users`, Redis
  - JWT settings in `application.yml` (`app.jwt.secret`, `expiration`)
  - Local profile shows Eureka/Config disabled (runs standalone)

- Blog Service
  - Port 8082
  - Responsibilities: blog posts, categories, tags
  - Databases/search: PostgreSQL `blognest_blogs`, Elasticsearch `:9200`
  - Redis caching enabled

- Comment Service
  - Port 8083
  - Responsibilities: comments for posts
  - Database/cache: PostgreSQL `blognest_comments`, Redis
  - Registers with Eureka

- Notification Service
  - Port 8084
  - Responsibilities: user notifications
  - Database/cache: PostgreSQL `blognest_notifications`, Redis
  - Registers with Eureka

- Common Library
  - Shared code across services (e.g., `JwtTokenProvider`, `JwtAuthenticationFilter`, `ApiResponse` DTO)

## Data stores

- PostgreSQL (Docker Compose): single Postgres server exposed on host 5433
  - Separate databases per microservice (users/blogs/comments/notifications)
- Redis (6379): caching/session support for services and gateway
- Elasticsearch (9200): full-text search for Blog Service

## Request flow

1. Browser → Frontend SPA (CRA dev server at 3000 or NGINX when containerized)
2. SPA → API Gateway (port 8080): routes by path
3. Gateway → Target microservice via service discovery (or direct in local standalone modes)
4. Services → Datastores (Postgres/Redis/Elasticsearch)

Authentication uses JWT issued by User Service; subsequent requests include `Authorization: Bearer <token>` and are validated by common security utilities.

## How to run (local development)

- Start infra with Docker Compose (recommended):
  - `docker-compose -f docker-compose-minimal.yml up -d`
  - Exposes: Postgres 5433, Redis 6379, Elasticsearch 9200, Eureka 8761, Config Server 8888
- Run backend services (one terminal each):
  - Build once: `mvn -DskipTests package`
  - User Service: `mvn -pl user-service spring-boot:run`
  - Blog Service: `mvn -pl blog-service spring-boot:run`
  - Comment Service: `mvn -pl comment-service spring-boot:run`
  - Notification Service: `mvn -pl notification-service spring-boot:run`
  - API Gateway: `mvn -pl api-gateway spring-boot:run`
  - Eureka Server: `mvn -pl service-discovery spring-boot:run`
  - Config Server: `mvn -pl config-server spring-boot:run`
- Run frontend (dev):
  - `cd frontend && npm install && npm start`
  - Opens `http://localhost:3000`, proxying API to `http://localhost:8080`

Notes:
- Some services’ `application.yml` disable Eureka/Config for local convenience; enable as needed.
- Ensure Postgres/Redis/Elasticsearch are up before starting services.

## Containerized workflow

- Build Java artifacts/images via provided scripts:
  - `build-all.bat` or `build-services.ps1` then `build-docker-images.bat`
- Use `docker-compose-minimal.yml` for infra; extend with service containers if desired
- Frontend static hosting: build CRA and serve via NGINX using `frontend/nginx.conf`

## Ports summary (host)

- 3000: Frontend (dev server or NGINX configured)
- 8080: API Gateway
- 8081/8082/8083/8084: User/Blog/Comment/Notification services
- 8761: Eureka
- 8888: Config Server
- 5433: PostgreSQL
- 6379: Redis
- 9200: Elasticsearch

## Security

- JWT-based authentication via User Service
- Shared security utilities in `common`
- For production, rotate and secure `app.jwt.secret`, database credentials, and CORS settings

## Testing & quality

- JUnit 5, Mockito for unit tests
- JaCoCo profile available for coverage
- ESLint for frontend (`npm run lint`, `npm run lint:fix`)

## Monitoring

- `monitoring/prometheus.yml` present (extend to scrape services)

## Future improvements (suggested)

- Centralize configs via Config Server for all services; remove local overrides
- Add API docs UIs per service (springdoc) behind Gateway
- Add CI/CD pipeline for build/test/image push
- Add Grafana dashboards; log aggregation (ELK or OpenSearch)
- Harden security (CORS, secrets mgmt via Vault), add rate limiting at Gateway

---

This overview consolidates the main aspects of the BlogNest project from the current repository configuration and build outputs. For deeper API-level details, inspect controllers within each service module and the shared code in `common`.
