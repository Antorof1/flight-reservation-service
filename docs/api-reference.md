# API Reference

[← Back to README](../README.md)

Once the application is running, you can access the interactive API documentation at:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

## Key Endpoints

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

Confirming or cancelling a reservation also emails the passenger. The email is dispatched asynchronously after the
transaction commits, so it does not affect the response status or latency, see the
[Notifications & Messaging guide](notifications.md).

## Authentication

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

## Global Error Handling

The application maps controller exceptions to a consistent error response format.

### Error Response Payload Schema

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
