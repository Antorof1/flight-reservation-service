# Flight Reservation Service

[![Build Status](https://github.com/Antorof1/flight-reservation-service/actions/workflows/maven.yml/badge.svg)](https://github.com/Antorof1/flight-reservation-service/actions)
[![Java Version](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-brightgreen?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A robust, modern backend service for managing flight reservations, built with Spring Boot 4 and Java 21. This service
provides a comprehensive API for handling flights, seats, users and the reservation lifecycle, including temporary seat
locking and automated cleanup of expired reservations.

## Live Demo

The application is deployed as a headless API service. You can interact with the live endpoints directly via the Swagger
UI.

* **API Base URL:** [https://flight.antonbazykin.com](https://flight.antonbazykin.com)
* **Monitoring Dashboard:** [https://grafana-flight.antonbazykin.com](https://grafana-flight.antonbazykin.com)
* **Interactive Swagger UI:** [https://flight.antonbazykin.com/swagger-ui/index.html](https://flight.antonbazykin.com/swagger-ui/index.html)
* **OpenAPI Specification:** [https://flight.antonbazykin.com/v3/api-docs](https://flight.antonbazykin.com/v3/api-docs)

**Demo admin account:** `admin@example.com` / `DemoAdmin2026!` Use this to try admin-only endpoints (creating
flights, updating seat status) via Swagger. The database resets daily (see below), so don't rely on anything you
create surviving past 00:00 UTC.

#### Infrastructure Stack

- **Cloud Provider:** Hosted on an **Oracle Cloud VPS** (ARM64).
- **Deployment Automation:** **Ansible** manages the remote configuration, container lifecycle and system environment
  setup on the VPS.
- **Monitoring & Observability:**
    - **Prometheus:** Collects application metrics via Spring Boot Actuator.
    - **Grafana:** Visualizes JVM metrics, request rates and system health using pre-configured dashboards.
- **Edge Network:**
    - **Cloudflare Proxy:** The application sits behind the Cloudflare proxy, masking the origin IP and providing a
      layer of protection against DDoS attacks.
    - **TLS:** End-to-end encryption managed via Cloudflare and Caddy.
- **Reverse Proxy:** **Caddy** handles internal routing and serves as the entry point for the Docker network.
- **Containerization:** Fully containerized using **Docker Compose**, pulling images directly from the GitHub Container
  Registry (GHCR).

#### Automated Maintenance

To ensure the demo remains performant and accessible to everyone, the environment includes:

- **Daily State Reset & Seeding:** When the demo mode is enabled, a scheduled cron job runs once every 24 hours to clear
  the PostgreSQL and Valkey databases, running a seeding script to populate fresh sample data for visitors.
- **Automated Health Checks:** The service is automatically restarted during maintenance to verify deployment integrity.

## Features

- **Authentication & Authorization:** Stateless JWT-based authentication with role-based access control (`USER` /
  `ADMIN`) protecting reservation ownership and admin-only operations.
- **Flight Management:** Create and retrieve flight details including schedules and airports.
- **Seat Management:** Manage seat availability, pricing and classes (Economy, Business, First Class).
- **User Management:** Register users and track their reservation history.
- **Reservation Lifecycle:**
    - **Temporary Hold:** Temporary seat reservation with Redis-backed locking to prevent overbooking.
    - **Confirmation:** Finalize reservations after payment/processing.
    - **Cancellation:** Release seats back to availability.
    - **Automated Cleanup:** Scheduled background task (Sweeper) to release expired holds.
- **Error Handling:** Structured global exception handling with consistent error responses.
- **Continuous Integration & Delivery:** Automated testing, containerization and deployment to VPS via GitHub Actions,
  publishing production-ready images to GitHub Container Registry.
- **Production Ready:** Optimized Docker configuration featuring a Caddy reverse proxy, pre-built images, and
  environment
  variable management.
- **Robust Testing:** Extensive coverage with rich unit tests and integration tests using Testcontainers.
- **API Documentation:** Interactive Swagger/OpenAPI UI.
- **Observability & Monitoring:**
    - **Real-time Metrics:** Integrated Spring Boot Actuator with Micrometer.
    - **Prometheus Integration:** Automated metrics scraping.
    - **Grafana Dashboards:** Provisioned JVM (Micrometer) dashboards for monitoring heap usage, CPU and thread counts.

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
- **Security:** Spring Security, JJWT (JSON Web Tokens), BCrypt password hashing
- **CI/CD:** GitHub Actions & GitHub Container Registry
- **Reverse Proxy:** Caddy
- **Monitoring:** Prometheus, Grafana
- **Metrics Tooling:** Micrometer, Spring Boot Actuator
- **Configuration Management / Deployment:** Ansible

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

| Variable                 | Description                                      | Default / Example Value |
|:-------------------------|:-------------------------------------------------|:------------------------|
| `POSTGRES_USER`          | PostgreSQL administrative username               | `db_user`               |
| `POSTGRES_PASSWORD`      | PostgreSQL administrative password               | `db_password`           |
| `POSTGRES_DB`            | Name of the primary database                     | `flight_reservation_db` |
| `VALKEY_PASSWORD`        | Password for the Valkey/Redis instance           | `valkey_password`       |
| `JWT_SECRET_KEY`         | Base64-encoded secret used to sign/verify JWTs   | `base64_32bytes_secret` |
| `SEED_ADMIN_EMAIL`       | Email for the seeded demo admin account          | `admin@demo.local`      |
| `SEED_ADMIN_PASSWORD`    | Password for the seeded demo admin account       | -                       |
| `SPRING_PROFILES_ACTIVE` | Active Spring boot profile(s)                    | `prod`                  |
| `APP_IMAGE_TAG`          | Docker image tag to pull from GHCR               | `latest`                | 
| `DOMAIN`                 | Domain name configured in Caddy                  | `localhost`             |
| `TRUSTED_PROXIES`        | Space-separated list of trusted proxy IPs (CIDR) | `127.0.0.1/32`          |
| `GRAFANA_ADMIN_USER`     | Grafana adminstrative username                   | `admin`                 |
| `GRAFANA_ADMIN_PASSWORD` | Grafana adminstrative password                   | `admin`                 |

*Note: Ensure you update sensitive credentials like `POSTGRES_PASSWORD` and `JWT_SECRET_KEY` in the `.env` file. If
`SEED_ADMIN_PASSWORD` is left blank, no demo admin account is created.*

### 2. Deploy with Docker Compose

Use the production-specific compose file, which pulls the pre-built image from GHCR using the tag specified by
`APP_IMAGE_TAG`:

```bash
docker compose -f docker-compose-prod.yaml --env-file .env up -d
```

### 3. Modular Caddy Configuration

The reverse proxy is configured for modularity and security:

- **`./caddy/conf.d/`**: Add custom `.caddy` snippets here to extend functionality.
- **`./caddy/certs/`**: Store custom SSL certificates here.
- **Trusted Proxies**: Use the `TRUSTED_PROXIES` variable to define upstream proxy ranges (e.g., Cloudflare IPs) for
  accurate header processing.

## API Documentation

Once the application is running, you can access the interactive API documentation at:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

### Key Endpoints

| Resource         | Method  | Endpoint                            | Access           | Description                                        |
|:-----------------|:--------|:------------------------------------|:-----------------|:---------------------------------------------------|
| **Auth**         | `POST`  | `/api/v1/auth/register`             | Public           | Register a new user and receive a JWT              |
|                  | `POST`  | `/api/v1/auth/login`                | Public           | Authenticate with email/password and receive a JWT |
| **Flights**      | `GET`   | `/api/v1/flights`                   | Public           | List all flights with pagination                   |
|                  | `GET`   | `/api/v1/flights/{id}`              | Public           | Get flight details by ID                           |
|                  | `GET`   | `/api/v1/flights/{id}/seats`        | Public           | List seats for a flight (optional status filter)   |
|                  | `POST`  | `/api/v1/flights`                   | `ADMIN`          | Create a new flight                                |
| **Seats**        | `PATCH` | `/api/v1/seats/{id}/status`         | `ADMIN`          | Update seat status                                 |
| **Reservations** | `GET`   | `/api/v1/reservations/{id}`         | Owner or `ADMIN` | Get reservation details by ID                      |
|                  | `POST`  | `/api/v1/reservations`              | Authenticated    | Create a temporary hold                            |
|                  | `PUT`   | `/api/v1/reservations/{id}/confirm` | Owner or `ADMIN` | Confirm a reservation                              |
|                  | `PUT`   | `/api/v1/reservations/{id}/cancel`  | Owner or `ADMIN` | Cancel a reservation                               |
| **Users**        | `GET`   | `/api/v1/users/reservations`        | Authenticated    | Get current user's reservation history             |

### Authentication

Authentication is handled via stateless **JSON Web Tokens (JWT)**. Register or log in to obtain a token, then pass it on
subsequent requests using the `Authorization: Bearer <token>` header.

```bash
# Register a new user (or use POST /api/v1/auth/login for existing users)
curl -X POST https://flight.antonbazykin.com/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "jane.doe@example.com", "name": "Jane Doe", "password": "supersecret"}'

# Response: { "token": "<jwt>", "email": "jane.doe@example.com", "name": "Jane Doe" }

# Use the token to call a protected endpoint
curl https://flight.antonbazykin.com/api/v1/reservations/1 \
  -H "Authorization: Bearer <jwt>"
```

Each token embeds the user's ID (as the subject) and role (`USER` or `ADMIN`), which drives method-level authorization:

- **Public endpoints:** Auth endpoints, `GET /api/v1/flights/**` and the Swagger/OpenAPI docs are accessible without a
  token.
- **Authenticated endpoints:** All other endpoints require a valid JWT.
- **Admin-only endpoints:** Creating flights and updating seat status require the `ADMIN` role.
- **Ownership checks:** Reservation endpoints are restricted to the reservation's owner or an `ADMIN`, enforced via a
  `ReservationSecurity` `@PreAuthorize` expression.
- Unauthenticated or invalid-token requests receive a consistent JSON `401 Unauthorized` response.

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
- **Deployment:** GitHub Actions uses **Ansible** to securely connect to the VPS, configure the host environment, pull
  the latest images from GHCR and restart the services using Docker Compose.

### GitHub Actions Configuration

The deployment workflow uses Ansible, which resolves configuration values from environment variables supplied by GitHub
Actions.

#### 1. Repository Secrets (Sensitive Data)

Configure these as **Secrets** to protect sensitive credentials and keys.

| Secret Name              | Description                                                       |
|:-------------------------|:------------------------------------------------------------------|
| `VPS_SSH_KEY`            | Private SSH key used to authenticate with the VPS host.           |
| `VPS_HOST`               | The public IP address or domain of the target VPS.                |
| `POSTGRES_PASSWORD`      | Administrative password for the PostgreSQL database.              |
| `VALKEY_PASSWORD`        | Authentication password for the Valkey cache.                     |
| `JWT_SECRET_KEY`         | Base64-encoded secret used to sign/verify JWTs.                   |
| `SEED_ADMIN_PASSWORD`    | Password for the seeded demo admin account.                       |
| `GRAFANA_ADMIN_PASSWORD` | Admin password for the Grafana dashboard.                         |
| `TLS_CERT`               | *Optional* Explicit TLS certificate block (PEM format) for Caddy. |
| `TLS_KEY`                | *Optional* Matching private key block for the TLS certificate.    |

#### 2. Repository Variables (Non-Sensitive Configurations)

Configure these as **Variables** for general application settings.

| Variable Name            | Description                                                                                                                        |
|:-------------------------|:-----------------------------------------------------------------------------------------------------------------------------------|
| `VPS_USERNAME`           | SSH system user on the target VPS.                                                                                                 |
| `DOMAIN`                 | Production domain name configured in Caddy (defaults to `localhost`).                                                              |
| `SPRING_PROFILES_ACTIVE` | Active Spring Boot profiles (defaults to `prod`).                                                                                  |
| `APP_IMAGE_TAG`          | Docker image tag to pull from GitHub Container Registry (defaults to `latest`).                                                    |
| `POSTGRES_USER`          | PostgreSQL administrative username (defaults to `db_user`).                                                                        |
| `POSTGRES_DB`            | Name of the primary database (defaults to `flight_reservation_db`).                                                                |
| `SEED_ADMIN_EMAIL`       | Email for the seeded demo admin account (defaults to `admin@demo.local`).                                                          |
| `GRAFANA_ADMIN_USER`     | Admin username for the Grafana dashboard (defaults to `admin`).                                                                    |
| `TRUSTED_PROXIES`        | *Optional* Space-separated CIDR ranges of upstream trusted proxies (e.g., Cloudflare IPs) for Caddy header mapping.                |
| `IS_DEMO`                | *Optional* Boolean flag to enable demo-specific environment behaviors, such as daily database seeding/reset (defaults to `false`). |

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
