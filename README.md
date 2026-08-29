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
flights, updating seat status) via Swagger. The database resets daily (see the
[Demo Environment guide](docs/demo-environment.md)), so don't rely on anything you create surviving past 00:00 UTC.

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

## Quick Start

Requires **Java 21** or higher, plus **Docker** and **Docker Compose** for the containerized stack. Maven is optional —
use the bundled `./mvnw` wrapper.

```bash
git clone https://github.com/antorof1/flight-reservation-service.git
cd flight-reservation-service
./mvnw spring-boot:run
```

The application will be available at `http://localhost:8080`.

For the full local workflow and test suite see the [Development guide](docs/development.md); for the containerized
production setup see the [Deployment guide](docs/deployment.md).

## Documentation

| Guide                                        | Contents                                                   |
|:---------------------------------------------|:-----------------------------------------------------------|
| [Architecture](docs/architecture.md)         | Domain modules, concurrency control and seat locking       |
| [API Reference](docs/api-reference.md)       | Endpoint catalogue, JWT auth, error payload schema         |
| [Development](docs/development.md)           | Prerequisites, running locally, test suite                 |
| [Deployment](docs/deployment.md)             | Docker Compose, environment variables, Caddy configuration |
| [CI/CD](docs/ci-cd.md)                       | GitHub Actions workflow, repository secrets and variables  |
| [Demo Environment](docs/demo-environment.md) | Hosting stack, observability, daily reset                  |

## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.
