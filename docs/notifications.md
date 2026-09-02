# Notifications & Messaging

[← Back to README](../README.md)

Details of the email pipeline sketched in the [Architecture guide](architecture.md).

## Pipeline

```
ReservationService.confirm/cancel  ──(after commit)──▶  reservation.events  ──[ reservation.* ]──▶
    email-notification.reservation-event  ──▶  ReservationEventListener  ──▶  ReservationEmailComposer
        ──▶  NotificationService (mode gate)  ──▶  RetryingEmailSender  ──▶  ResendEmailSender
```

`RabbitConfig` declares the exchange, queue and binding above, all durable. Confirmations publish under
`reservation.confirmed`, cancellations under `reservation.cancelled`; the payload is a JSON `ReservationEvent` carrying
the passenger, flight number, route, departure time and seat.

Publishing is registered as a `TransactionSynchronization` and runs on `afterCommit`, so a rolled-back reservation
never mails a booking that does not exist, at the cost of sending outside the transaction, where an unreachable broker
loses the event while the reservation stands. On the consuming side `default-requeue-rejected=false` drops a message
that makes the listener throw instead of requeueing it forever.

## Delivery gating

`app.notifications.mode` decides who can be mailed, so non-production environments cannot reach real passengers.
Addresses and allow-list entries are trimmed and lower-cased before comparison.

| Mode        | Behaviour                                                            |
|:------------|:---------------------------------------------------------------------|
| `OFF`       | Nothing is sent; every message is logged as suppressed. **Default.** |
| `ALLOWLIST` | Only recipients in `app.notifications.allowlist` are sent to         |
| `ALL`       | Every recipient is sent to                                           |

`OFF` also needs no `RESEND_API_KEY` or `RESEND_FROM_EMAIL`, which is what makes the app runnable without a Resend
account; the other modes require both and `ResendConfig` rejects a missing value at startup.

## Retries and error classification

`RetryingEmailSender` retries only `TransientEmailException`, `app.notifications.retry.max-retries` attempts
(default 2), spaced by `app.notifications.retry.delay` (default 2s). `ResendEmailSender` classifies failures first:

| Classified as | Signal                                                                                                                                                          |
|:--------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Transient     | Resend `rate_limit_exceeded`, `concurrent_idempotent_requests`, `application_error`, `service_unavailable`; HTTP 429 or 5xx; missing status code; `IOException` |
| Permanent     | Resend `daily_quota_exceeded`, `monthly_quota_exceeded`; any other 4xx (bad address, rejected sender, invalid key)                                              |

Whatever survives is logged by `NotificationService` and never rethrown, so a failed email fails neither the listener
nor the reservation. Both emails are plain text, subject `Your flight <number> is confirmed` or `Your reservation for
flight <number> was cancelled`.

## Configuration

`SPRING_RABBITMQ_HOST`, `_PORT`, `_USERNAME` and `_PASSWORD` point the app at the broker; in the Compose stacks the
last two come from `RABBITMQ_USER` and `RABBITMQ_PASSWORD`. `NOTIFICATIONS_RETRY_MAX_RETRIES` (`2`) and
`NOTIFICATIONS_RETRY_DELAY` (`2s`) tune the retries. The rest: `RESEND_API_KEY`, `RESEND_FROM_EMAIL`,
`NOTIFICATIONS_MODE`, `NOTIFICATIONS_ALLOWLIST` are in the [Deployment guide](deployment.md).
