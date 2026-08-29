# Architecture

[← Back to README](../README.md)

The project follows a modular monolithic architecture, organized by domains:

- `flight`: Flight scheduling and data.
- `seat`: Inventory management and seat state.
- `user`: User profiles and authentication context.
- `reservation`: Orchestration of the booking process and concurrency control.

## Concurrency Control

Seat locking is implemented using Redis (Valkey) to ensure that two users cannot hold the same seat simultaneously. The
`ReservationSweeper` runs a scheduled task every minute to release seats that were held but never confirmed.

See the [API Reference](api-reference.md) for the endpoints that drive the reservation lifecycle (hold, confirm, cancel).
