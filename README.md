# SSEW Backend - E-Commerce Platform for Hardware Tools

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Go](https://img.shields.io/badge/Go-1.21+-00ADD8.svg)](https://golang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-316192.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.x-DC382D.svg)](https://redis.io/)
[![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20S3-FF9900.svg)](https://aws.amazon.com/)

A high-performance, scalable backend system built for **Sri Shasta Engineering Works (SSEW)** to modernize their hardware tools business operations through a comprehensive e-commerce and inventory management platform.

## 🌐 Live Applications

- **Storefront**: [https://srishastabangalore.in/](https://srishastabangalore.in/)
- **Admin Dashboard**: [https://dash.srishastabangalore.in/](https://dash.srishastabangalore.in/)

## 📋 Table of Contents

- [Overview](#overview)
- [Problem Statement](#problem-statement)
- [Architecture](#architecture)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Performance Metrics](#performance-metrics)
- [System Design](#system-design)
- [API Services](#api-services)
- [Deployment](#deployment)
- [Security](#security)
- [Getting Started](#getting-started)
- [Contributing](#contributing)
- [Team](#team)

## 🎯 Overview

SSEW Backend is a polyglot microservices architecture designed to handle complex e-commerce operations for a hardware tools business. The system manages inventory, orders, customer queries, and provides real-time updates through an event-driven architecture.

## 🔍 Problem Statement

Sri Shasta Engineering Works faced significant operational challenges:

- **Manual Order Processing**: Time-consuming and error-prone manual order management
- **Inventory Chaos**: Difficult to track stock levels and product variants across multiple categories
- **Communication Gaps**: No centralized system for customer queries and order status updates
- **Scalability Issues**: Unable to handle growing business demands efficiently

**Solution**: A robust, automated web-based platform that streamlines operations, provides real-time insights, and scales with business growth.

## 🏗️ Architecture

This project implements a **polyglot microservices architecture** with two specialized services:

```
┌─────────────────────────────────────────────────────────────┐
│                         API Gateway                          │
│                    (Nginx + Rate Limiting)                   │
└─────────────────────┬───────────────────────────────────────┘
                      │
          ┌───────────┴───────────┐
          │                       │
┌─────────▼──────────┐   ┌───────▼────────────┐
│  Commerce Service  │   │ Telemetry Service  │
│   (Spring Boot)    │◄─►│      (Go/Gin)      │
│                    │   │                    │
│  • Auth & RBAC     │   │  • Event Polling   │
│  • Catalog Mgmt    │   │  • Redis Streams   │
│  • Order Mgmt      │   │  • Real-time Push  │
│  • Email Service   │   │  • 10K concurrent  │
└─────────┬──────────┘   └──────────┬─────────┘
          │                         │
    ┌─────┴─────┐            ┌─────┴─────┐
    │           │            │           │
┌───▼───┐   ┌──▼──┐     ┌───▼────┐  ┌──▼──┐
│ PostgreSQL│ │Redis│     │ Redis  │  │Redis│
│  (Primary)│ │Cache│     │Streams │  │Cache│
└───────────┘ └─────┘     └────────┘  └─────┘
          │
      ┌───▼────┐
      │ AWS S3 │
      │ (Media)│
      └────────┘
```

## ✨ Key Features

### Authentication & Authorization

- **JWT-based Authentication**: Short-lived access tokens (15 min) and long-lived refresh tokens (30 days)
- **Role-Based Access Control (RBAC)**: Fine-grained permissions using Spring Security
- **Token Rotation**: Automatic token refresh mechanism
- **Multi-Session Management**: Support for concurrent sessions across devices

### Catalog Management

- Hierarchical category structure with manufacturer brand associations
- Product and product variant management with rich metadata
- **Smart Image Processing**: Automated image optimization and S3 storage
- **Presigned URLs**: Short-lived, secure URLs for media access (enhanced security)
- Bulk operations support for efficient catalog updates

### Order Management System

- **Real-time Order Updates**: Event-driven architecture using Redis Streams
- **Long Polling**: Near real-time communication with clients (<300ms latency)
- Comprehensive order lifecycle management (placed → processing → shipped → delivered)
- Order status tracking for both admin and customers
- Automated order validation and inventory reservation

### Customer Query Management

- Centralized query handling system
- Priority-based query routing
- Response tracking and analytics
- Integration with admin dashboard

### Email Notification System

- **Asynchronous Email Processing**: Non-blocking, background email delivery
- **Event-Driven Architecture**: Redis-based event publishing/consumption
- **Batch Processing**: Efficient email sending with JavaMailSender
- **Exponential Backoff**: Automatic retry mechanism for failed deliveries
- Order confirmation, status updates, and admin notifications

### Caching Strategy

- **Redis Caching Layer**: Reduces database load for frequently accessed data
- Cache warming for product search endpoints
- Smart cache invalidation based on data updates
- TTL-based cache expiration policies

## 🛠️ Technology Stack

### Commerce Service (Java/Spring Boot)

- **Framework**: Spring Boot 3.x with Spring Cloud
- **Architecture**: Clean Architecture + SOLID Principles
- **Database**: PostgreSQL 15+ (Primary)
- **Caching**: Redis 7.x
- **Storage**: AWS S3 (SDK v2)
- **Email**: JavaMailSender with async processing
- **Security**: Spring Security 6.x with JWT
- **Communication**: gRPC for inter-service communication
- **Monitoring**: Sentry for logging and crash analytics
- **Profiling**: Spring Boot Profiles (dev, staging, prod)

### Telemetry Service (Go/Gin)

- **Framework**: Gin Web Framework
- **Concurrency**: Go Routines for handling 10K+ concurrent connections
- **Event Streaming**: Redis Streams
- **Protocol**: gRPC server for receiving events
- **Monitoring**: Sentry integration
- **Response Time**: <300ms average latency

### Infrastructure & DevOps

- **Cloud Provider**: AWS (EC2, S3)
- **Containerization**: Docker
- **CI/CD**: GitHub Actions (automated build, test, deploy)
- **Reverse Proxy**: Nginx with rate limiting
- **Database**: PostgreSQL with connection pooling
- **Message Queue**: Redis Streams (lightweight alternative to Kafka)

## 📊 Performance Metrics

### Commerce Service

- **Concurrent Users**: 1,500 - 2,000 (single EC2 instance)
- **Average Response Time**:
    - Non-cached endpoints: 500-900ms
    - Cached endpoints: <400ms
- **Scalability**: Horizontal scaling ready (ASG, K8s compatible)

### Telemetry Service

- **Concurrent Connections**: 10,000+ active polling connections
- **Average Response Time**: <300ms
- **Throughput**: Handles high-frequency events with Go routines

### System Reliability

- **Fail-Safe Design**: Graceful degradation for gRPC and Redis failures
- **Exponential Backoff**: Automatic retry for transient failures
- **Circuit Breaker**: Prevents cascade failures
- **Health Checks**: Regular service health monitoring

## 🎨 System Design

### Clean Architecture Implementation

The Commerce Service follows Clean Architecture principles:

```
┌──────────────────────────────────────────────┐
│           Presentation Layer (API)            │
│        (Controllers, DTOs, Validators)        │
└─────────────────┬────────────────────────────┘
                  │
┌─────────────────▼────────────────────────────┐
│          Application Layer (Use Cases)        │
│     (Business Logic, Service Orchestration)   │
└─────────────────┬────────────────────────────┘
                  │
┌─────────────────▼────────────────────────────┐
│            Domain Layer (Entities)            │
│      (Business Models, Domain Rules)          │
└─────────────────┬────────────────────────────┘
                  │
┌─────────────────▼────────────────────────────┐
│       Infrastructure Layer (Data Access)      │
│  (Repositories, External APIs, DB Adapters)   │
└──────────────────────────────────────────────┘
```

### Event-Driven Communication

**Order Update Flow**:

1. Client places order via REST API → Commerce Service
2. Commerce Service:
    - Validates order and reserves inventory
    - Persists order to PostgreSQL
    - Publishes event to Redis Stream (fail-safe)
    - Makes gRPC call to Telemetry Service
    - Publishes email notification event
3. Telemetry Service:
    - Receives gRPC event
    - Publishes to Redis Stream
    - Notifies active polling clients
4. Email Service (background):
    - Consumes email events from Redis
    - Sends batch emails with exponential backoff

### Database Schema Design

**Key Entities**:

- Users & Roles (RBAC)
- Categories & Manufacturers
- Products & Product Variants
- Orders & Order Items
- Customer Queries
- Media Assets

**Indexing Strategy**:

- B-tree indexes on frequently queried fields
- Composite indexes for complex queries
- Full-text search on product descriptions

## 🔌 API Services

### Commerce Service Endpoints

**Authentication**

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
```

**Catalog Management**

```
GET    /api/v1/categories
POST   /api/v1/categories
GET    /api/v1/products
POST   /api/v1/products
PUT    /api/v1/products/{id}
GET    /api/v1/products/search?q={query}
```

**Order Management**

```
GET    /api/v1/orders
POST   /api/v1/orders
GET    /api/v1/orders/{id}
PUT    /api/v1/orders/{id}/status
```

**Media Management**

```
POST   /api/v1/media/upload
GET    /api/v1/media/{id}/presigned-url
```

### Telemetry Service Endpoints

**Polling**

```
GET    /api/v1/poll/orders?lastEventId={id}
GET    /api/v1/poll/notifications?lastEventId={id}
```

**gRPC Service**

```
rpc PublishOrderEvent(OrderEventRequest) returns (OrderEventResponse)
rpc PublishNotificationEvent(NotificationRequest) returns (NotificationResponse)
```

## 🚀 Deployment

### Prerequisites

- Docker & Docker Compose
- AWS Account (EC2, S3)
- PostgreSQL 15+
- Redis 7+
- Domain with SSL certificate

### Environment Variables

Create `.env` files for each service:

**Commerce Service**

```env
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://localhost:5432/ssew
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
REDIS_HOST=localhost
REDIS_PORT=6379
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_S3_BUCKET_NAME=ssew-media
JWT_SECRET=your_jwt_secret
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email
SMTP_PASSWORD=your_app_password
TELEMETRY_SERVICE_URL=localhost:50051
SENTRY_DSN=your_sentry_dsn
```

**Telemetry Service**

```env
REDIS_ADDR=localhost:6379
REDIS_PASSWORD=
GRPC_PORT=50051
HTTP_PORT=8081
SENTRY_DSN=your_sentry_dsn
```

### Docker Deployment

```bash
# Build images
docker-compose build

# Run services
docker-compose up -d

# View logs
docker-compose logs -f
```

### CI/CD Pipeline

GitHub Actions workflow automatically:

1. Runs tests on every push
2. Builds Docker images
3. Pushes to container registry
4. Deploys to AWS EC2
5. Runs health checks

**Workflow triggers**: Push to `main` or `develop` branches

### Manual Deployment

```bash
# Commerce Service
cd commerce-service
./mvnw clean package -DskipTests
java -jar target/commerce-service.jar

# Telemetry Service
cd telemetry-service
go build -o telemetry-service
./telemetry-service
```

### Nginx Configuration

```nginx
upstream commerce_service {
    server localhost:8080;
}

upstream telemetry_service {
    server localhost:8081;
}

server {
    listen 80;
    server_name api.srishastabangalore.in;

    location /api/v1/poll {
        proxy_pass http://telemetry_service;
        proxy_http_version 1.1;
        proxy_read_timeout 30s;
    }

    location /api {
        proxy_pass http://commerce_service;
        limit_req zone=api_limit burst=20;
    }
}
```

## 🔒 Security

### Authentication & Authorization

- JWT tokens with RS256 signing algorithm
- Refresh token rotation on every use
- Token blacklisting for logout functionality
- CORS configuration for allowed origins

### Data Protection

- Password hashing with BCrypt (strength: 12)
- SQL injection prevention via prepared statements
- XSS protection with input sanitization
- CSRF tokens for state-changing operations

### API Security

- Rate limiting via Nginx (100 req/min per IP)
- Request size limits (10MB for file uploads)
- API key authentication for service-to-service calls
- Presigned URLs with 5-minute expiration

### Infrastructure Security

- Private subnets for database and cache
- Security groups with minimal port exposure
- SSL/TLS for all external communications
- Regular security updates and patches

## 🏁 Getting Started

### Local Development Setup

1. **Clone the repository**

```bash
git clone https://github.com/callmeysg/SSEW-Backend.git
cd SSEW-Backend
```

2. **Start dependencies**

```bash
docker-compose -f docker-compose.dev.yml up -d
```

3. **Run Commerce Service**

```bash
cd commerce-service
./mvnw spring-boot:run
```

4. **Run Telemetry Service**

```bash
cd telemetry-service
go run main.go
```

5. **Access APIs**

- Commerce Service: http://localhost:8080
- Telemetry Service: http://localhost:8081
- API Documentation: http://localhost:8080/swagger-ui.html

### Running Tests

```bash
# Commerce Service
cd commerce-service
./mvnw test

# Telemetry Service
cd telemetry-service
go test ./...
```

## 🤝 Contributing

This is a production system for a real client. Contributions are not being accepted at this time.

## 👥 Team

**Backend Developer**: [Aryan Singh](https://singhtwenty2.pages.dev/)

- Microservices architecture and implementation
- AWS infrastructure and deployment
- CI/CD pipeline setup
- System design and optimization

**Frontend Developer**: [Ranjan Verma](https://ranjanverma.pages.dev/)

- Storefront development (React.js)
- Admin dashboard (Next.js)
- API integration

## 📄 License

This is proprietary software developed for Sri Shasta Engineering Works. All rights reserved.

## 📞 Contact

For inquiries about this project, please visit the [project documentation page](coming-soon) or reach out through [my portfolio](https://singhtwenty2.pages.dev/).

---

**Note**: This README showcases the backend architecture and implementation. For information about the frontend applications, please refer to their respective repositories or visit the live applications linked above.
