db = db.getSiblingDB("payment_operational");

db.createCollection("idempotency_keys");
db.idempotency_keys.createIndex({ key: 1 }, { unique: true });
db.idempotency_keys.createIndex({ paymentId: 1 });
db.idempotency_keys.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 });

db.createCollection("webhook_payloads");
db.webhook_payloads.createIndex({ provider: 1, externalEventId: 1 }, { unique: true });
db.webhook_payloads.createIndex({ receivedAt: -1 });

db.createCollection("provider_snapshots");
db.provider_snapshots.createIndex({ paymentId: 1, provider: 1 });
db.provider_snapshots.createIndex({ capturedAt: -1 });

db.createCollection("retry_control");
db.retry_control.createIndex({ operationId: 1 }, { unique: true });
db.retry_control.createIndex({ nextRunAt: 1 });
