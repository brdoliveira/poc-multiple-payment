# Spec: Idempotency and observability hardening

> feature: reliability-observability
> status: pronta

## Contexto

The three payment microsservices already have basic persistence, correlation headers and CloudWatch log forwarding, but concurrent retries, request mismatches, asynchronous correlation and operational signals need stronger guarantees before the PoC can be treated as production-ready.

## Historias

### US-007 - Payment operations are idempotent under retries

As a payment client, I want a repeated idempotency key to return the original operation and a changed payload to be rejected, so network retries cannot create a second charge or authorization.

#### AC-017 - Java orchestrator claims idempotency atomically

- **Dado** two concurrent create requests with the same key and equivalent payload
- **Quando** the Java orchestrator processes both requests
- **Entao** only one payment and one pair of outbox events are created
- **E** both callers receive the same payment identifier

#### AC-018 - Java rejects a reused key with a different payload

- **Dado** an idempotency key already associated with a payment fingerprint
- **Quando** the same key is submitted with a different amount, method or currency
- **Entao** the request is rejected as an idempotency conflict
- **E** no new payment or event is created

#### AC-019 - Bank rail protects provider calls from concurrent duplicates

- **Dado** two concurrent Pix or boleto requests with the same key
- **Quando** the Kotlin service starts charge creation
- **Entao** the provider adapter is invoked at most once
- **E** both callers receive the stored result

#### AC-020 - Card authorization protects provider calls from concurrent duplicates

- **Dado** two concurrent card authorization requests with the same key
- **Quando** the C# service starts authorization
- **Entao** the provider adapter is invoked at most once
- **E** both callers receive the same authorization result

#### AC-021 - All services reject idempotency fingerprint conflicts

- **Dado** a key already claimed for a different request fingerprint
- **Quando** a conflicting request reaches Java, Kotlin or C#
- **Entao** the service returns a conflict instead of returning or creating the old result

### US-008 - Operations are traceable without leaking secrets

As an operator, I want every HTTP and asynchronous operation to carry a correlation identifier and structured outcome data, so incidents can be followed across services without logging card tokens or credentials.

#### AC-022 - HTTP correlation is validated, returned and logged

- **Dado** a request with or without `X-Correlation-Id`
- **Quando** it reaches any HTTP service
- **Entao** the service returns a bounded correlation identifier and includes it in request outcome logs
- **E** request logs include method, route, status and duration

#### AC-023 - Asynchronous events propagate correlation context

- **Dado** a payment event created from a correlated request
- **Quando** the outbox dispatcher publishes it and a bank/card consumer handles it
- **Entao** the event carries correlation context and consumer logs use the same identifier

#### AC-024 - Payment outcomes expose safe structured telemetry

- **Dado** a successful, rejected or failed payment operation
- **Quando** the service records the outcome
- **Entao** logs/metrics include service, operation, provider or rail, status and duration
- **E** card tokens, passwords, API keys and full request bodies are absent

#### AC-025 - AWS telemetry alerts on operational degradation

- **Dado** the Terraform observability module
- **Quando** it is inspected
- **Entao** it declares encrypted service log groups, latency/error alarms and a dashboard for ALB/ECS signals
- **E** alerts are connected to the existing SNS topic

## Fora de escopo

- Real provider calls, distributed tracing backend deployment or a production load test.
- Changing payment business states or replacing PostgreSQL, DocumentDB or RabbitMQ.
- Storing raw request bodies, card data or credentials in logs.
- Applying Terraform to a real AWS account.

## Suposições

- **ASM-008 confirmada**: PostgreSQL remains the source of truth for idempotency claims in Java, Kotlin and C# deployments.
- **ASM-009 confirmada**: a fingerprint is a SHA-256 digest of normalized business fields and never contains raw secrets in logs.
- **ASM-010 confirmada**: `X-Correlation-Id` is propagated in the event payload because the current outbox uses JSON payloads across RabbitMQ.

## Perguntas em aberto

Nenhuma.
