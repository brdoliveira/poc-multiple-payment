# Design: Idempotency and observability hardening

## Decision

Each service uses its own PostgreSQL-owned idempotency record as a small state machine:

```text
ABSENT -> IN_PROGRESS -> COMPLETED
                    \-> FAILED/EXPIRED
```

The claim operation stores a normalized request fingerprint and is protected by a unique idempotency key. A duplicate with the same fingerprint waits/reads the existing result; a different fingerprint returns a conflict. The provider is called only after the claim is committed; completion updates the same record.

## Correlation

The HTTP filter/middleware accepts a bounded `X-Correlation-Id`, generates one when absent, returns it, and places it in the logging context. Java outbox payloads include `correlationId`; Kotlin and C# consumers restore that value into MDC/log scope while processing.

## Telemetry

- HTTP outcome logs: `service`, `operation`, `method`, `route`, `status`, `duration_ms`, `correlation_id`.
- Payment outcome logs: `payment_id`, `idempotency_key_hash`, `provider` or `rail`, `status`, `duration_ms`, `correlation_id`.
- Micrometer/.NET meters: payment attempts, successes, conflicts, failures and provider latency.
- No raw card token, password, API key or body is logged.
- Terraform retains encrypted log groups and adds latency/error alarms plus a dashboard connected to the existing SNS alerts.

## Verification layers

1. Service unit tests prove atomic reservation behavior with in-memory implementations and concurrent tasks.
2. Static repository tests prove contracts, safe logging patterns, event correlation and Terraform resources.
3. Existing Java/Kotlin/.NET test commands remain the integration boundary for compile and framework wiring.
