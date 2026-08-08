# poc-multiple-payment

PoC de uma plataforma de pagamentos com microsservicos escritos em Java, Kotlin
e C#, suportando Pix, cartao e boleto por meio de adapters para Asaas, Mercado
Pago, PagBank, iugu e Stripe.

## Objetivo

Demonstrar uma arquitetura senior para pagamentos:

- orquestracao do ciclo de vida do pagamento;
- separacao por meio de pagamento;
- adapters por provedor;
- idempotencia;
- SQL migrations;
- uso complementar de NoSQL;
- outbox e eventos para processamento assincrono;
- webhooks com payload bruto em NoSQL;
- conciliacao de pagamentos pendentes;
- consumidores RabbitMQ por meio de pagamento;
- filas de retry e DLQ por consumidor;
- API key interna e correlation-id;
- testes unitarios por servico;
- pipeline CI para Java, Kotlin, C# e Docker build;
- documentacao de evolucao do projeto.

## Microsservicos

| Servico | Linguagem | Responsabilidade |
| --- | --- | --- |
| `payment-orchestrator-java` | Java / Spring Boot | Criacao do pagamento, idempotencia, status principal e publicacao de eventos |
| `pix-boleto-kotlin` | Kotlin / Spring Boot | Geracao de cobrancas Pix e boleto, roteamento por provedor e regras por rail |
| `card-payment-csharp` | C# / .NET | Autorizacao de cartao, captura logica, escolha de gateway e idempotencia |

## Provedores

| Provedor | Pix | Cartao | Boleto | Melhor uso |
| --- | --- | --- | --- | --- |
| Asaas | Sim | Sim | Sim | cobrancas, recorrencia e automacao financeira |
| Mercado Pago | Sim | Sim | Sim | checkout rapido e ecossistema Mercado Livre |
| PagBank | Sim | Sim | Sim | carteira digital e adquirencia popular no Brasil |
| iugu | Sim | Sim | Sim | SaaS, marketplace e split complexo |
| Stripe | Limitado no Brasil | Sim | Limitado | cartao, internacionalizacao e APIs globais |

## Infraestrutura local

```bash
docker compose up -d
```

Servicos locais:

- Orquestrador Java em `localhost:8080`;
- Pix/boleto Kotlin em `localhost:8081`;
- Cartao/Webhook C# em `localhost:8082`;
- PostgreSQL em `localhost:5432`;
- MongoDB em `localhost:27017`;
- RabbitMQ em `localhost:5672` e console em `localhost:15672`.

## Infraestrutura AWS

O Terraform da PoC fica em `infra/aws/terraform`. Ele cria ECS Fargate/ECR para
os tres microsservicos, RDS PostgreSQL, Amazon DocumentDB, Amazon MQ RabbitMQ,
ALB, WAF, Secrets Manager e observabilidade basica.

Consulte [infra/aws/terraform/README.md](infra/aws/terraform/README.md) para
validar e aplicar a infraestrutura.

## Banco SQL e NoSQL

O PostgreSQL guarda o estado transacional confiavel:

- pagamentos;
- tentativas;
- transacoes externas;
- eventos outbox;
- conciliacoes;
- ledger financeiro.

O MongoDB guarda dados operacionais flexiveis:

- chaves de idempotencia;
- payloads brutos de webhook;
- snapshots de respostas dos provedores;
- controle operacional de retry;
- rastros de diagnostico.

## Executando os servicos

### Java

```bash
cd services/payment-orchestrator-java
mvn test
mvn spring-boot:run
```

### Kotlin

```bash
cd services/pix-boleto-kotlin
gradle test
gradle bootRun
```

### C#

```bash
cd services/card-payment-csharp
dotnet test
dotnet run --project src/CardPaymentService
```

## Fluxo principal

1. Cliente chama `POST /payments` no orquestrador.
2. O orquestrador valida `idempotencyKey` e grava o pagamento.
3. Eventos `PaymentCreated` e `PaymentProcessing` sao gravados na outbox.
4. O servico especializado processa Pix, boleto ou cartao.
5. O adapter do provedor chama Asaas, Mercado Pago, PagBank, iugu ou Stripe.
6. O dispatcher publica eventos pendentes no RabbitMQ.
7. Consumidores Kotlin/C# recebem `PaymentProcessing` em filas proprias.
8. Falhas transitorias retornam para fila de retry e, apos o limite, seguem para DLQ.
9. Webhooks armazenam payload bruto no MongoDB para auditoria operacional.
10. A conciliacao reprocessa pagamentos pendentes de confirmacao.

## Endpoints principais

```text
POST /payments
GET  /payments/{paymentId}
POST /reconciliation/payments/{paymentId}
POST /reconciliation/stale-payments?olderThanMinutes=15
POST /bank-rail/charges
POST /cards/authorizations
POST /webhooks/{provider}
GET  /actuator/health
GET  /health
```

Endpoints de negocio exigem `X-Internal-Api-Key`. O valor local padrao da PoC e
`local-dev-key`. Webhooks usam validacao de assinatura propria.

## Evolucao historica

Os commits deste repositorio foram organizados com datas entre 01/07/2026 e
22/07/2026 para simular a evolucao incremental do projeto.
