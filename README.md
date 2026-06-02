# Flight Reservation Service

A robust, modern backend service for managing flight reservations, built with Spring Boot 4 and Java 21. This service
provides a comprehensive API for handling flights, seats, users and the reservation lifecycle, including temporary seat
locking and automated cleanup of expired reservations.

## Features

- **Flight Management:** Create and retrieve flight details including schedules and airports.
- **Seat Management:** Manage seat availability, pricing and classes (Economy, Business, First Class).
- **User Management:** Register users and track their reservation history.
- **Reservation Lifecycle:**
    - **Temporary Hold:** Temporary seat reservation with Redis-backed locking to prevent overbooking.
    - **Confirmation:** Finalize reservations after payment/processing.
    - **Cancellation:** Release seats back to availability.
    - **Automated Cleanup:** Scheduled background task (Sweeper) to release expired holds.
- **Error Handling:** Structured global exception handling with consistent error responses.
- **Continuous Integration & Delivery:** Automated testing and containerization pipeline using GitHub Actions,
  publishing production-ready images to GitHub Container Registry.
- **Production Ready:** Optimized Docker configuration for production environments, pre-built images, and environment
  variable management.
- **Robust Testing:** Extensive coverage with rich unit tests and integration tests using Testcontainers.
- **API Documentation:** Interactive Swagger/OpenAPI UI.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 4.0.6
- **Database:** PostgreSQL (Primary)
- **Caching/Locking:** Valkey (Redis-compatible)
- **Migrations:** Flyway
- **Documentation:** SpringDoc OpenAPI
- **Build Tool:** Maven
- **Infrastructure:** Docker & Docker Compose (Dev & Prod)
- **Testing:** JUnit 5, Mockito, Testcontainers, WebTestClient
- **CI/CD:** GitHub Actions & GitHub Container Registry

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

### 2. Run the Application

```bash
./mvnw spring-boot:run
```

The application will be available at `http://localhost:8080`.

## Production Deployment

For production-like environments, a dedicated Docker configuration is provided.

### 1. Environment Configuration

Copy the template environment file and update the values for your production needs:

```bash
cp .env.example .env
```

The application relies on the following key environment variables:

| Variable                 | Description                               | Default / Example Value |
|:-------------------------|:------------------------------------------|:------------------------|
| `POSTGRES_USER`          | PostgreSQL administrative username        | `db_user`               |
| `POSTGRES_PASSWORD`      | PostgreSQL administrative password        | `db_password`           |
| `POSTGRES_DB`            | Name of the primary database              | `flight_reservation_db` |
| `VALKEY_PASSWORD`        | Password for the Valkey/Redis instance    | `valkey_password`       |
| `APP_PORT`               | Port exposed by the application container | `8080`                  |
| `SPRING_PROFILES_ACTIVE` | Active Spring boot profile(s)             | `prod`                  |
| `APP_IMAGE_TAG`          | Docker image tag to pull from GHCR        | `latest`                | 

*Note: Ensure you update sensitive credentials like `POSTGRES_PASSWORD` in the `.env` file.*

### 2. Deploy with Docker Compose

Use the production-specific compose file, which pulls the pre-built image from GHCR using the tag specified by
`APP_IMAGE_TAG`:

```bash
docker compose -f docker-compose-prod.yaml --env-file .env up -d
```

## API Documentation

Once the application is running, you can access the interactive API documentation at:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

### Key Endpoints

| Resource         | Method  | Endpoint                            | Description                                      |
|:-----------------|:--------|:------------------------------------|:-------------------------------------------------|
| **Flights**      | `GET`   | `/api/v1/flights`                   | List all flights                                 |
|                  | `GET`   | `/api/v1/flights/{id}`              | Get flight details by ID                         |
|                  | `GET`   | `/api/v1/flights/{id}/seats`        | List seats for a flight (optional status filter) |
|                  | `POST`  | `/api/v1/flights`                   | Create a new flight                              |
| **Seats**        | `PATCH` | `/api/v1/seats/{id}/status`         | Update seat status                               |
| **Reservations** | `GET`   | `/api/v1/reservations/{id}`         | Get reservation details by ID                    |
|                  | `POST`  | `/api/v1/reservations`              | Create a temporary hold                          |
|                  | `PUT`   | `/api/v1/reservations/{id}/confirm` | Confirm a reservation                            |
|                  | `PUT`   | `/api/v1/reservations/{id}/cancel`  | Cancel a reservation                             |
| **Users**        | `GET`   | `/api/v1/users`                     | Find user by email                               |
|                  | `GET`   | `/api/v1/users/{id}`                | Get user profile by ID                           |
|                  | `GET`   | `/api/v1/users/{id}/reservations`   | Get user reservation history                     |
|                  | `POST`  | `/api/v1/users`                     | Register a new user                              |

### Global Error Handling

The application maps controller exceptions to a consistent error response format.

#### Error Response Payload Schema

```json
{
  "timestamp": "2065-02-15T10:15:30.123Z",
  "status": 409,
  "error": "Conflict",
  "message": "Seat is currently held by another user",
  "path": "/api/v1/reservations",
  "details": []
}
```

## Testing

The project emphasizes high quality through a comprehensive testing strategy:

- **Unit Tests:** Isolated testing of business logic in services and controllers using JUnit 5 and Mockito.
- **Integration Tests:** End-to-end validation of API endpoints and database interactions using **Testcontainers** to
  spin up real PostgreSQL and Redis/Valkey instances, ensuring tests run in an environment identical to production.

Run the full test suite:

```bash
./mvnw test
```

## CI/CD Pipeline

The project includes an automated GitHub Actions workflow (`.github/workflows/maven.yml`) to ensure code quality and
build delivery:

- **Verification:** Runs the Maven test suite on every pull request and push to the `main` branch.
- **Delivery:** Upon a successful merge/push to `main`, a multi-architecture Docker image is built using Docker Buildx,
  tagged (using commit SHA, branch reference, and `latest`), and pushed to the **GitHub Container Registry**.

## Architecture

The project follows a modular monolithic architecture, organized by domains:

- `flight`: Flight scheduling and data.
- `seat`: Inventory management and seat state.
- `user`: User profiles and authentication context.
- `reservation`: Orchestration of the booking process and concurrency control.

### Concurrency Control

Seat locking is implemented using Redis (Valkey) to ensure that two users cannot hold the same seat simultaneously. The
`ReservationSweeper` runs a scheduled task every minute to release seats that were held but never confirmed.

## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.
