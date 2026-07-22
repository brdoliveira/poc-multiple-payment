# Estrategia de testes

Os testes desta PoC cobrem os riscos principais de pagamento:

- idempotencia no orquestrador Java;
- roteamento por capability no servico Kotlin de Pix/boleto;
- idempotencia na autorizacao de cartao em C#.

## Java

```bash
cd services/payment-orchestrator-java
mvn test
```

## Kotlin

```bash
cd services/pix-boleto-kotlin
gradle test
```

## C#

```bash
cd services/card-payment-csharp
dotnet test
```

## Proximos testes

- contrato dos eventos publicados;
- validacao de migrations em PostgreSQL real;
- retry/backoff por provedor;
- simulacao de webhook duplicado;
- conciliacao de pagamento pendente.

## Validacao manual

Com os containers no ar:

```bash
docker compose up -d --build
```

Criar pagamento:

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{"idempotencyKey":"checkout-1","method":"PIX","amount":99.90,"currency":"BRL"}'
```

Criar cobranca Pix:

```bash
curl -X POST http://localhost:8081/bank-rail/charges \
  -H "Content-Type: application/json" \
  -d '{"idempotencyKey":"pix-1","rail":"PIX","amount":99.90,"currency":"BRL","preferredProvider":"ASAAS"}'
```

Autorizar cartao:

```bash
curl -X POST http://localhost:8082/cards/authorizations \
  -H "Content-Type: application/json" \
  -d '{"idempotencyKey":"card-1","amount":199.90,"currency":"BRL","installments":3,"cardToken":"tok_test","preferredProvider":"Stripe"}'
```
