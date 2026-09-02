# CI/CD Pipeline

[← Back to README](../README.md)

The project includes an automated GitHub Actions workflow (`.github/workflows/maven.yml`) to ensure code quality and
build delivery:

- **Verification:** Runs the Maven test suite on every pull request and push to the `main` branch.
- **Delivery:** Upon a successful merge/push to `main`, a multi-architecture Docker image is built using Docker Buildx,
  tagged (using commit SHA, branch reference, and `latest`), and pushed to the **GitHub Container Registry**.
- **Deployment:** GitHub Actions uses **Ansible** to securely connect to the VPS, configure the host environment, pull
  the latest images from GHCR and restart the services using Docker Compose.

## GitHub Actions Configuration

The deployment workflow uses Ansible, which resolves configuration values from environment variables supplied by GitHub
Actions. These map onto the runtime variables documented in the [Deployment guide](deployment.md).

### 1. Repository Secrets (Sensitive Data)

Configure these as **Secrets** to protect sensitive credentials and keys.

| Secret Name              | Description                                                       |
|:-------------------------|:------------------------------------------------------------------|
| `VPS_SSH_KEY`            | Private SSH key used to authenticate with the VPS host.           |
| `VPS_HOST`               | The public IP address or domain of the target VPS.                |
| `POSTGRES_PASSWORD`      | Administrative password for the PostgreSQL database.              |
| `VALKEY_PASSWORD`        | Authentication password for the Valkey cache.                     |
| `RABBITMQ_PASSWORD`      | Password for the RabbitMQ broker and the app's AMQP connection.   |
| `JWT_SECRET_KEY`         | Base64-encoded secret used to sign/verify JWTs.                   |
| `RESEND_API_KEY`         | Resend API key. Required unless `NOTIFICATIONS_MODE` is `OFF`.    |
| `SEED_ADMIN_PASSWORD`    | Password for the seeded demo admin account.                       |
| `GRAFANA_ADMIN_PASSWORD` | Admin password for the Grafana dashboard.                         |
| `TLS_CERT`               | *Optional* Explicit TLS certificate block (PEM format) for Caddy. |
| `TLS_KEY`                | *Optional* Matching private key block for the TLS certificate.    |

### 2. Repository Variables (Non-Sensitive Configurations)

Configure these as **Variables** for general application settings.

| Variable Name             | Description                                                                                                                        |
|:--------------------------|:-----------------------------------------------------------------------------------------------------------------------------------|
| `VPS_USERNAME`            | SSH system user on the target VPS.                                                                                                 |
| `DOMAIN`                  | Production domain name configured in Caddy (defaults to `localhost`).                                                              |
| `SPRING_PROFILES_ACTIVE`  | Active Spring Boot profiles (defaults to `prod`).                                                                                  |
| `APP_IMAGE_TAG`           | Docker image tag to pull from GitHub Container Registry (defaults to `latest`).                                                    |
| `POSTGRES_USER`           | PostgreSQL administrative username (defaults to `db_user`).                                                                        |
| `POSTGRES_DB`             | Name of the primary database (defaults to `flight_reservation_db`).                                                                |
| `RABBITMQ_USER`           | RabbitMQ username shared by the broker and the app (defaults to `rabbitmq_user`).                                                  |
| `RESEND_FROM_EMAIL`       | Verified Resend sender address. Required unless `NOTIFICATIONS_MODE` is `OFF`.                                                     |
| `NOTIFICATIONS_MODE`      | Email delivery mode: `OFF`, `ALLOWLIST` or `ALL` (defaults to `OFF`).                                                              |
| `NOTIFICATIONS_ALLOWLIST` | *Optional* Comma-separated recipients honoured in `ALLOWLIST` mode.                                                                |
| `SEED_ADMIN_EMAIL`        | Email for the seeded demo admin account (defaults to `admin@demo.local`).                                                          |
| `GRAFANA_ADMIN_USER`      | Admin username for the Grafana dashboard (defaults to `admin`).                                                                    |
| `TRUSTED_PROXIES`         | *Optional* Space-separated CIDR ranges of upstream trusted proxies (e.g., Cloudflare IPs) for Caddy header mapping.                |
| `IS_DEMO`                 | *Optional* Boolean flag to enable demo-specific environment behaviors, such as daily database seeding/reset (defaults to `false`). |
