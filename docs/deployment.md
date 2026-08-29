# Deployment

[← Back to README](../README.md)

For production-like environments, a dedicated Docker configuration is provided.

## 1. Environment Configuration

Copy the template environment file and update the values for your production needs:

```bash
cp .env.example .env
```

The application relies on the following key environment variables:

| Variable                 | Description                                                    | Default / Example Value |
|:-------------------------|:---------------------------------------------------------------|:------------------------|
| `POSTGRES_USER`          | PostgreSQL administrative username                             | `db_user`               |
| `POSTGRES_PASSWORD`      | PostgreSQL administrative password                             | `db_password`           |
| `POSTGRES_DB`            | Name of the primary database                                   | `flight_reservation_db` |
| `VALKEY_PASSWORD`        | Password for the Valkey/Redis instance                         | `valkey_password`       |
| `JWT_SECRET_KEY`         | Base64-encoded secret used to sign/verify JWTs                 | `base64_32bytes_secret` |
| `SEED_ADMIN_EMAIL`       | Email for the seeded demo admin account                        | `admin@demo.local`      |
| `SEED_ADMIN_PASSWORD`    | Password for the seeded demo admin account                     | -                       |
| `SPRING_PROFILES_ACTIVE` | Active Spring boot profile(s)                                  | `prod`                  |
| `APP_IMAGE_TAG`          | Docker image tag to pull from GHCR                             | `latest`                | 
| `COMPOSE_PROFILES`       | Optional Compose profiles to enable                            | `proxy,observability`   |
| `DOMAIN`                 | Domain name configured in Caddy                                | `localhost`             |
| `TRUSTED_PROXIES`        | Space-separated list of trusted proxy IPs (CIDR)               | `127.0.0.1/32`          |
| `GRAFANA_ADMIN_USER`     | Grafana adminstrative username                                 | `admin`                 |
| `GRAFANA_ADMIN_PASSWORD` | Grafana adminstrative password                                 | `admin`                 |

*Note: Ensure you update sensitive credentials like `POSTGRES_PASSWORD` and `JWT_SECRET_KEY` in the `.env` file. If
`SEED_ADMIN_PASSWORD` is left blank, no demo admin account is created.*

*Note: On the automated VPS deployment these values are not read from a checked-in `.env` file — Ansible renders them
from GitHub Actions secrets and variables. See the [CI/CD guide](ci-cd.md) for the matching secret and variable names.*

## 2. Deploy with Docker Compose

Use the production-specific compose file, which pulls the pre-built image from GHCR using the tag specified by
`APP_IMAGE_TAG`:

```bash
docker compose -f docker-compose-prod.yaml --env-file .env up -d
```

The Caddy reverse proxy and the Prometheus/Grafana observability stack are optional and gated behind the `proxy` and
`observability` Compose profiles respectively. Enable them via the `COMPOSE_PROFILES` variable in `.env`, or omit it
to run only the core `app`, `db` and `valkey` services.

For local development with a locally built image instead of the GHCR one, layer the local override file on top:

```bash
docker compose -f docker-compose-prod.yaml -f docker-compose-local.yaml up -d
```

## 3. Modular Caddy Configuration

The reverse proxy is configured for modularity and security:

- **`./caddy/conf.d/`**: Add custom `.caddy` snippets here to extend functionality.
- **`./caddy/certs/`**: Store custom SSL certificates here.
- **Trusted Proxies**: Use the `TRUSTED_PROXIES` variable to define upstream proxy ranges (e.g., Cloudflare IPs) for
  accurate header processing.
