# Development

[← Back to README](../README.md)

## Prerequisites

- **Java 21** or higher
- **Docker** and **Docker Compose**
- **Maven** (optional, use `./mvnw` wrapper)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/antorof1/flight-reservation-service.git
cd flight-reservation-service
```

### 2. Provide the required environment variables

```bash
export JWT_SECRET_KEY=$(openssl rand -base64 32)
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

The application will be available at `http://localhost:8080`, with the interactive Swagger UI at
`http://localhost:8080/swagger-ui.html` — see the [API Reference](api-reference.md) for the endpoint catalogue.

To run the full containerized stack instead, see the [Deployment guide](deployment.md).

## Testing

The project emphasizes high quality through a comprehensive testing strategy:

- **Unit Tests:** Isolated testing of business logic in services and controllers using JUnit 5 and Mockito.
- **Integration Tests:** End-to-end validation of API endpoints, database interactions and the event-driven notification
  pipeline using **Testcontainers** to spin up real PostgreSQL, Redis/Valkey and RabbitMQ instances, ensuring tests run
  in an environment identical to production.

Run the full test suite:

```bash
./mvnw test
```
