import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const rootDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..');
const java = readRoot('services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/application/PaymentOrchestratorService.java');
const javaStore = readRoot('services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/infrastructure/JdbcIdempotencyStore.java');
const javaFilter = readRoot('services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/infrastructure/CorrelationIdFilter.java');
const javaOutbox = readRoot('services/payment-orchestrator-java/src/main/java/com/acme/payments/orchestrator/infrastructure/JdbcOutboxEventPublisher.java');
const javaTest = readRoot('services/payment-orchestrator-java/src/test/java/com/acme/payments/orchestrator/application/PaymentOrchestratorServiceTest.java');
const kotlin = readRoot('services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/application/ChargeService.kt');
const kotlinRepo = readRoot('services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/infrastructure/JdbcChargeRepository.kt');
const kotlinFilter = readRoot('services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/infrastructure/CorrelationIdFilter.kt');
const kotlinConsumer = readRoot('services/pix-boleto-kotlin/src/main/kotlin/com/acme/payments/pixboleto/messaging/PaymentEventListener.kt');
const kotlinTest = readRoot('services/pix-boleto-kotlin/src/test/kotlin/com/acme/payments/pixboleto/application/ChargeServiceTest.kt');
const csharp = readRoot('services/card-payment-csharp/src/CardPaymentService/Application/ApplicationCardPaymentService.cs');
const csharpStore = readRoot('services/card-payment-csharp/src/CardPaymentService/Infrastructure/PostgresIdempotencyStore.cs');
const csharpMiddleware = readRoot('services/card-payment-csharp/src/CardPaymentService/Observability/CorrelationIdMiddleware.cs');
const csharpConsumer = readRoot('services/card-payment-csharp/src/CardPaymentService/Messaging/RabbitPaymentConsumer.cs');
const csharpTest = readRoot('services/card-payment-csharp/tests/CardPaymentService.Tests/CardPaymentServiceTests.cs');
const schema = readRoot('contracts/payment-events.schema.json');
const terraform = readRoot('infra/aws/terraform/observability.tf');
const ecsTerraform = readRoot('infra/aws/terraform/ecs.tf');
const docs = readRoot('docs/observability.md');

test('Java claims idempotency before creating events under concurrency @spec:AC-017', () => {
  assert.match(java, /synchronized\s*\(idempotencyLocks\.computeIfAbsent/);
  assert.match(javaStore, /INSERT INTO payment_idempotency_keys/);
  assert.match(javaTest, /concurrent duplicate requests @spec:AC-017/);
  assert.match(javaTest, /events\(\)\)\.hasSize\(2\)/);
});

test('Java rejects a different fingerprint for the same key @spec:AC-018', () => {
  assert.match(java, /IdempotencyConflictException/);
  assert.match(java, /requestFingerprint/);
  assert.match(javaTest, /different payload @spec:AC-018/);
});

test('Kotlin reserves the key before invoking the bank provider @spec:AC-019', () => {
  assert.match(kotlin, /chargeRepository\.reserve/);
  assert.ok(kotlin.indexOf('chargeRepository.reserve') < kotlin.indexOf('adapter.createCharge'));
  assert.match(kotlinTest, /provider once for concurrent duplicates @spec:AC-019/);
  assert.match(kotlinRepo, /ON CONFLICT \(idempotency_key\) DO NOTHING/);
});

test('C# reserves the key before invoking the card provider @spec:AC-020', () => {
  assert.match(csharp, /TryReserveAsync/);
  assert.ok(csharp.indexOf('TryReserveAsync') < csharp.indexOf('adapter.AuthorizeAsync'));
  assert.match(csharpTest, /ProviderOnceForConcurrentDuplicates_spec_AC_020/);
  assert.match(csharpStore, /ON CONFLICT \(idempotency_key\) DO NOTHING/);
});

test('all services carry a persisted fingerprint and explicit conflict path @spec:AC-021', () => {
  assert.match(javaStore, /request_fingerprint/);
  assert.match(kotlinRepo, /request_fingerprint/);
  assert.match(csharpStore, /request_fingerprint/);
  assert.match(java, /idempotency\.conflicts/);
  assert.match(kotlin, /IdempotencyConflictException/);
  assert.match(csharp, /IdempotencyConflictException/);
});

test('HTTP correlation IDs are bounded and request outcomes are logged @spec:AC-022', () => {
  for (const source of [javaFilter, kotlinFilter, csharpMiddleware]) {
    assert.match(source, /128/);
    assert.match(source, /http_outcome/);
    assert.match(source, /duration/);
  }
  assert.match(javaFilter, /X-Correlation-Id/);
  assert.match(csharpMiddleware, /X-Correlation-Id/);
});

test('correlation context crosses the outbox and RabbitMQ consumers @spec:AC-023', () => {
  assert.match(schema, /"correlationId"/);
  assert.match(javaOutbox, /correlationId/);
  assert.match(kotlinConsumer, /MDC\.put\("correlationId"/);
  assert.match(csharpConsumer, /BeginScope/);
});

test('payment telemetry is structured and excludes raw sensitive values @spec:AC-024', () => {
  for (const source of [java, kotlin, csharp]) {
    assert.match(source, /payment_outcome/);
    assert.match(source, /idempotency_key_hash/);
    assert.doesNotMatch(source, /logger\.(info|warn|LogInformation|LogWarning).*cardToken/);
  }
  assert.match(docs, /Card tokens, request bodies, passwords, API keys/);
});

test('Terraform defines encrypted logs, latency and CPU alarms, and a dashboard @spec:AC-025', () => {
  assert.match(ecsTerraform, /aws_cloudwatch_log_group/);
  assert.match(ecsTerraform, /kms_key_id/);
  assert.match(terraform, /aws_cloudwatch_metric_alarm/);
  assert.match(terraform, /TargetResponseTime/);
  assert.match(terraform, /CPUUtilization/);
  assert.match(terraform, /aws_cloudwatch_dashboard/);
  assert.match(terraform, /aws_sns_topic\.alerts\.arn/);
});

function readRoot(relativePath) {
  return readFileSync(path.join(rootDir, relativePath), 'utf8');
}
