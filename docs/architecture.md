# Architecture

[← Back to README](../README.md)

The project follows a modular monolithic architecture, organized by domains:

- `flight`: Flight scheduling and data.
- `seat`: Inventory management and seat state.
- `user`: User profiles and authentication context.
- `reservation`: Orchestration of the booking process and concurrency control.
- `notification`: Asynchronous email delivery driven by reservation events.

## Concurrency Control

Seat locking is implemented using Redis (Valkey) to ensure that two users cannot hold the same seat simultaneously. The
`ReservationSweeper` runs a scheduled task every minute to release seats that were held but never confirmed.

See the [API Reference](api-reference.md) for the endpoints that drive the reservation lifecycle (hold, confirm, cancel).

## Asynchronous Messaging

When a reservation is confirmed or canceled, the service sends a `ReservationEvent` message to RabbitMQ. The message is
only sent after the database transaction has been committed, so no event goes out for a change that was rolled back.
The user's HTTP request returns as soon as the reservation is saved; it does not wait for the message to be handled.

The `notification` module reads those messages from its own queue and sends the passenger an email. It checks whether
sending is turned on, retries when the email provider fails temporarily and then calls the Resend API. If the email
still cannot be sent, the error is logged and nothing else happens, a failed email never breaks the reservation. See
the [Notifications & Messaging guide](notifications.md) for queue setup, delivery modes and retry details.
