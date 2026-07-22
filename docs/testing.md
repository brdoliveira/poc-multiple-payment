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
