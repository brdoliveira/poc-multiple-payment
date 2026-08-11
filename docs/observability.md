# Observability and idempotency

## Idempotency contract

Every payment create/authorization command requires an idempotency key. The service stores and commits a SHA-256 fingerprint of normalized business fields before invoking a provider. A retry with the same key and fingerprint returns the stored operation; a retry with a different fingerprint returns HTTP `409 Conflict`.

The database unique constraint remains the authority across ECS replicas. The in-process lock reduces duplicate work inside a task, while the insert/reservation protects concurrent requests across tasks. Provider adapters receive the original idempotency key and must map it to their own external idempotency mechanism when real integrations are added.

## Correlation

HTTP services accept `X-Correlation-Id`, reject unsafe or oversized values by replacing them with a generated UUID, and return the accepted identifier. Outbox events carry `correlationId`; RabbitMQ consumers restore it to the logging context before processing.

## Structured logs

Expected fields include `service`, `operation`, `outcome`, `payment_id` or `charge_id`, `provider`/`rail`, `status`, `duration_ms` and `correlation_id`. Idempotency keys are represented only by a short hash. Card tokens, request bodies, passwords, API keys and provider credentials must never be logged.

## Metrics and alerts

The services expose Micrometer or .NET meters for completed operations, idempotency reuse/conflicts and operation duration. Terraform provisions encrypted CloudWatch log groups, Container Insights, alarms for ALB/service 5xx, target latency and ECS CPU, plus the `${local.name}-operations` dashboard connected to the existing SNS alert topic.

## Incident queries

Start with the correlation ID from the response header, then search the service log groups for `correlation_id=<value>`. Join the HTTP outcome with `payment_outcome` and `payment_event` records. For a duplicate request, compare the short `idempotency_key_hash` and fingerprint conflict outcome; do not request or log the original card token.
