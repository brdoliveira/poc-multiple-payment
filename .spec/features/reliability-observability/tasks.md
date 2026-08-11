# Tasks: Idempotency and observability hardening

> feature: reliability-observability

## T-010 - Harden Java idempotency and telemetry [concluida]

- Refs: US-007, US-008, AC-017, AC-018, AC-021, AC-022, AC-023, AC-024
- Arquivos: services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/Application.java, services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/application/IdempotencyRecord.java, services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/application/RequestFingerprint.java, services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/application/IdempotencyStore.java, services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/application/PaymentOrchestratorService.java, services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/api/IdempotencyConflictException.java, services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/infrastructure/CorrelationIdFilter.java, services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/infrastructure/InMemoryIdempotencyStore.java, services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/infrastructure/JdbcIdempotencyStore.java, services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/infrastructure/JdbcOutboxEventPublisher.java, services/payment-orchestrator-java/src/main/resources/application.yml, services/payment-orchestrator-java/src/main/resources/db/migration/V20260811_001__add_idempotency_fingerprint.sql, services/payment-orchestrator-java/src/test/java/com/acme/payments/orchestrator/application/PaymentOrchestratorServiceTest.java, contracts/payment-events.schema.json
- Modelo: gpt-5.6-luna
- Esforço: alto

## T-011 - Harden Kotlin bank rail idempotency and telemetry [concluida]

- Refs: US-007, US-008, AC-019, AC-021, AC-022, AC-023, AC-024
- Arquivos: services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/application/RequestFingerprint.kt, services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/application/ChargeRepository.kt, services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/application/ChargeService.kt, services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/api/IdempotencyConflictException.kt, services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/infrastructure/CorrelationIdFilter.kt, services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/infrastructure/JdbcChargeRepository.kt, services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/messaging/PaymentEventListener.kt, services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/messaging/PaymentProcessingEvent.kt, services/pix-boleto-kotlin/src/main/resources/application.yml, services/pix-boleto-kotlin/src/main/resources/db/migration/V20260811_001__add_idempotency_fingerprint.sql, services/pix-boleto-kotlin/src/test/kotlin/com/acme/payments/pixboleto/application/ChargeServiceTest.kt
- Modelo: gpt-5.6-luna
- Esforço: alto

## T-012 - Harden C# card idempotency and telemetry [concluida]

- Refs: US-007, US-008, AC-020, AC-021, AC-022, AC-023, AC-024
- Arquivos: services/card-payment-csharp/src/CardPaymentService/Application/ApplicationCardPaymentService.cs, services/card-payment-csharp/src/CardPaymentService/Application/RequestFingerprint.cs, services/card-payment-csharp/src/CardPaymentService/Application/IdempotencyConflictException.cs, services/card-payment-csharp/src/CardPaymentService/Infrastructure/IIdempotencyStore.cs, services/card-payment-csharp/src/CardPaymentService/Infrastructure/InMemoryIdempotencyStore.cs, services/card-payment-csharp/src/CardPaymentService/Infrastructure/PostgresIdempotencyStore.cs, services/card-payment-csharp/src/CardPaymentService/Messaging/PaymentProcessingEvent.cs, services/card-payment-csharp/src/CardPaymentService/Messaging/RabbitPaymentConsumer.cs, services/card-payment-csharp/src/CardPaymentService/Migrations/20260811_001_add_idempotency_fingerprint.sql, services/card-payment-csharp/src/CardPaymentService/Observability/CorrelationIdMiddleware.cs, services/card-payment-csharp/src/CardPaymentService/Program.cs, services/card-payment-csharp/tests/CardPaymentService.Tests/CardPaymentServiceTests.cs
- Modelo: gpt-5.6-luna
- Esforço: alto

## T-013 - Expand AWS observability and repository proofs [concluida]

- Refs: US-008, AC-022, AC-023, AC-024, AC-025
- Arquivos: infra/aws/terraform/observability.tf, infra/aws/terraform/ecs.tf, .spec/static-tests/reliability-observability.test.mjs, .spec/run-tests.mjs, onpspec.config.json, docs/observability.md, contracts/payment-events.schema.json
- Modelo: gpt-5.6-luna
- Esforço: medio

## T-014 - Add concurrency and cross-service integration tests [concluida]

- Refs: US-007, US-008, AC-017, AC-018, AC-019, AC-020, AC-021, AC-023, AC-024
- Arquivos: services/payment-orchestrator-java/src/test/java/com/acme/payments/orchestrator/application/PaymentOrchestratorServiceTest.java, services/pix-boleto-kotlin/src/test/kotlin/com/acme/payments/pixboleto/application/ChargeServiceTest.kt, services/card-payment-csharp/tests/CardPaymentService.Tests/CardPaymentServiceTests.cs
- Modelo: gpt-5.6-luna
- Esforço: medio
