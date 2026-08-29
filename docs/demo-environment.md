# Demo Environment

[← Back to README](../README.md)

The [live demo](https://flight.antonbazykin.com) runs on the infrastructure described below.

## Infrastructure Stack

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

The Caddy and Prometheus/Grafana services are gated behind the `proxy` and `observability` Compose profiles — see the
[Deployment guide](deployment.md) for the `COMPOSE_PROFILES` variable that enables them.

## Automated Maintenance

To ensure the demo remains performant and accessible to everyone, the environment includes:

- **Daily State Reset & Seeding:** When the demo mode is enabled, a scheduled cron job runs once every 24 hours to clear
  the PostgreSQL and Valkey databases, running a seeding script to populate fresh sample data for visitors.
- **Automated Health Checks:** The service is automatically restarted during maintenance to verify deployment integrity.

Demo mode is toggled by the `IS_DEMO` repository variable documented in the [CI/CD guide](ci-cd.md).
