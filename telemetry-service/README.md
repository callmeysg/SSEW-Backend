# Telemetry Service

High-performance event polling microservice built with Go and Gin framework. This service handles real-time event distribution using Redis-backed polling mechanisms.

## Features

- **High Performance**: Utilizes Go routines and worker pools for concurrent event processing
- **Scalable Polling**: Supports both short and long polling strategies
- **Redis-backed**: All events stored in Redis for fast access and automatic expiration
- **JWT Authentication**: Secure endpoints with role-based access control
- **Event Types**: Support for multiple event types (orders, inventory, payments, etc.)
- **Admin & User Segregation**: Separate event streams for admins and users

## Architecture

### Key Improvements Over Spring Implementation

1. **Concurrent Processing**: Worker pool pattern for handling multiple events simultaneously
2. **Optimized Redis Operations**: Pipeline operations and connection pooling
3. **Non-blocking Event Publishing**: Async event saving with goroutines
4. **Efficient Long Polling**: Context-aware timeout handling
5. **Memory Efficient**: Minimal memory footprint with Go's lightweight threads

## Setup

### Prerequisites

- Go 1.21 or higher
- Redis server
- JWT secret key

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
```

Key configurations:

- `REDIS_URL`: Redis connection string
- `JWT_SECRET`: Secret key for JWT validation (must match your Spring app)
- `PORT`: Service port (default: 8081)
- `WORKER_POOL_SIZE`: Number of concurrent workers (default: 100)
- `LONG_POLL_TIMEOUT_MS`: Long polling timeout (default: 25000ms)

### Installation

```bash
# Clone the repository
git clone https://github.com/callmeysg/SSEW-Backend/
cd telemetry-service

# Download dependencies
go mod download

# Run the service
go run cmd/server/main.go
```

### Docker

```bash
# Build image
docker build -t telemetry-service .

# Run container
docker run -p 8081:8081 --env-file .env telemetry-service
```

## API Endpoints

### Polling Endpoints

#### Poll Events

```
GET /v1/polling/events?eventType={type}&lastEventId={id}&longPoll={bool}
Headers: Authorization: Bearer {token}
```

#### Poll User Events

```
GET /v1/polling/user/events?lastEventId={id}&longPoll={bool}
Headers: Authorization: Bearer {token}
```

#### Poll Admin Events

```
GET /v1/polling/admin/events?lastEventId={id}&longPoll={bool}
Headers: Authorization: Bearer {token}
Requires: ADMIN role
```

### Internal Event Publishing (for Spring service)

#### Publish Generic Event

```
POST /v1/internal/events/publish
Body: {
  "eventType": "CUSTOMER_ORDER_STATUS",
  "action": "REFRESH",
  "entityId": "order-123",
  "entityType": "ORDER",
  "userId": "user-456",
  "metadata": {}
}
```

#### Publish Order Status Change

```
POST /v1/internal/events/order-status-change
Body: {
  "orderId": "order-123",
  "userId": "user-456",
  "newStatus": "SHIPPED"
}
```

#### Publish New Order Event

```
POST /v1/internal/events/new-order
Body: {
  "orderId": "order-123",
  "customerName": "John Doe",
  "totalAmount": "999.99"
}
```

#### Publish Order Update Event

```
POST /v1/internal/events/order-update
Body: {
  "orderId": "order-123",
  "updateType": "STATUS_CHANGE",
  "details": {
    "newStatus": "DELIVERED",
    "previousStatus": "SHIPPED"
  }
}
```

## Integration with Spring Boot

Update your Spring Boot service to call the telemetry service instead of handling events directly:

```java
// In your OrderServiceImpl
@Service
public class OrderServiceImpl {
    @Value("${telemetry.service.url:http://localhost:8081}")
    private String telemetryServiceUrl;

    private final RestTemplate restTemplate;

    private void publishEventToTelemetry(String endpoint, Object payload) {
        try {
            restTemplate.postForEntity(
                telemetryServiceUrl + endpoint,
                payload,
                Void.class
            );
        } catch (Exception e) {
            log.error("Failed to publish event to telemetry service", e);
        }
    }

    // Replace Redis event publishing with:
    publishEventToTelemetry("/v1/internal/events/order-status-change",
        Map.of(
            "orderId", orderId,
            "userId", userId,
            "newStatus", newStatus
        )
    );
}
```

## Performance Characteristics

- **Concurrent Event Processing**: Up to 100 concurrent workers
- **Redis Connection Pool**: 50 connections with 10 minimum idle
- **Long Polling**: 25-second timeout with 1-second check intervals
- **Event Cleanup**: Automatic cleanup of expired events
- **Memory Usage**: ~50MB under normal load
- **Response Time**: <5ms for cache hits, <50ms for polling operations

## Monitoring

Health check endpoint:

```
GET /health
```

## Production Deployment

1. Set environment to production: `ENVIRONMENT=production`
2. Configure proper JWT secret
3. Set appropriate Redis credentials
4. Adjust worker pool size based on load
5. Configure CORS origins for your frontend
6. Use reverse proxy (nginx/traefik) for SSL termination
7. Enable rate limiting at proxy level
8. Set up monitoring (Prometheus/Grafana)

## Development

### Running Tests

```bash
go test ./...
```

### Linting

```bash
golangci-lint run
```

### Building

```bash
go build -o telemetry-service cmd/server/main.go
```

## License

Private repository - All rights reserved
