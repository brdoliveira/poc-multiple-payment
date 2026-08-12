# poc-multiple-payment

PoC educacional de uma plataforma de pagamentos com microsservicos em Java,
Kotlin e C#. O projeto demonstra orquestracao, idempotencia, eventos,
observabilidade, conciliacao e uma base Terraform para AWS.

> **Status:** PoC para estudo e evolucao arquitetural. Nao e um produto pronto
> para producao e nao deve receber dados reais de clientes, cartoes ou contas.

## O que esta demonstrado

- Orquestracao de pagamentos e status principal;
- Fluxos separados para Pix, boleto e cartao;
- Idempotencia com fingerprint da requisicao;
- PostgreSQL para estado transacional e migrations;
- MongoDB/DocumentDB para dados operacionais e webhooks;
- Outbox, RabbitMQ, retry e DLQ;
- Correlation ID, logs estruturados e metricas;
- Tela local para revisar o fluxo de pagamento;
- Terraform para ECS Fargate, ECR, RDS, DocumentDB, Amazon MQ, ALB, WAF,
  Secrets Manager e CloudWatch.

## Arquitetura

```text
Cliente
  |
  v
ALB / tela local
  |
  +--> payment-orchestrator-java  --> PostgreSQL / Outbox
  +--> pix-boleto-kotlin          --> PostgreSQL / RabbitMQ
  +--> card-payment-csharp       --> PostgreSQL / MongoDB / RabbitMQ
                                      |
                                      +--> adapters de provedores (PoC)
```

Diagramas completos: [arquitetura-fluxo-pagamento.svg](arquitetura-fluxo-pagamento.svg)
e [docs/architecture.md](docs/architecture.md).

## Microsservicos

| Servico | Stack | Responsabilidade |
| --- | --- | --- |
| `payment-orchestrator-java` | Java 17 / Spring Boot | Criacao do pagamento, idempotencia e outbox |
| `pix-boleto-kotlin` | Kotlin / Spring Boot | Cobrancas Pix/boleto e processamento por rail |
| `card-payment-csharp` | C# / .NET 8 | Autorizacao de cartao e webhooks |

Os adapters de provedores sao pontos de extensao da PoC. Nao configure tokens
reais de Asaas, Mercado Pago, PagBank, iugu ou Stripe neste repositorio.

## Pre-requisitos

- Docker Desktop com Docker Compose;
- Java 17;
- .NET SDK 8;
- Node.js 22;
- Terraform 1.15.8 ou compativel com `infra/aws/terraform/versions.tf`.

## Inicio rapido local

Suba as dependencias e os tres servicos:

```bash
docker compose up -d --build
```

Servicos locais:

| Componente | Endereco |
| --- | --- |
| Orquestrador Java | `http://localhost:8080` |
| Pix/boleto Kotlin | `http://localhost:8081` |
| Cartao/Webhook C# | `http://localhost:8082` |
| RabbitMQ console | `http://localhost:15672` |

Os valores locais sao ficticios: `payments`, `guest` e `local-dev-key`. Nunca
os reutilize em AWS ou em qualquer ambiente compartilhado.

Tela do fluxo de pagamento:

```bash
cd web/payment-flow
npm ci
npm run test
```

## Testes

Por servico:

```bash
cd services/payment-orchestrator-java && mvn test
cd services/pix-boleto-kotlin && gradle test
cd services/card-payment-csharp && dotnet test
```

Suite completa e auditoria spec:

```bash
node .spec/run-tests.mjs
node <caminho-da-skill>/scripts/onp-spec.mjs audit --ci
```

No CI do GitHub, Java, Kotlin, C#, Docker, tela, Terraform e a auditoria de
conteudo publico rodam automaticamente.

## Terraform e AWS

O Terraform fica em [infra/aws/terraform](infra/aws/terraform). Ele nao faz
build nem publica imagens. Antes de qualquer apply, publique as imagens reais
no ECR e forneca `container_images` e `internal_api_key` por secrets/variaveis.
Nao existe fallback para `nginx`.

Validacao local sem acessar state remoto:

```bash
terraform -chdir=infra/aws/terraform init -backend=false
terraform -chdir=infra/aws/terraform validate
```

Para CI/CD, consulte [docs/terraform-ci.md](docs/terraform-ci.md). O fluxo usa
OIDC, state remoto S3 e `apply` manual protegido pelo environment
`terraform-dev`.

## Documentacao

- [Fluxo de pagamento](docs/payment-flow.md)
- [Arquitetura](docs/architecture.md)
- [Matriz de provedores](docs/provider-matrix.md)
- [Observabilidade, logs e idempotencia](docs/observability.md)
- [Testes](docs/testing.md)
- [CI/CD Terraform](docs/terraform-ci.md)
- [Prontidao para repositorio publico](docs/public-repository-readiness.md)
- [Politica de seguranca](SECURITY.md)

## Limites conhecidos

- Nao ha credenciais ou chamadas reais de provedores financeiros;
- `terraform apply` em AWS ainda exige configuracao externa de OIDC, state,
  ECR, secrets e permissao IAM;
- A infraestrutura AWS gera custos e requer revisao de capacidade, backup,
  TLS, rotacao de segredos e alta disponibilidade;
- Exemplos locais nao representam uma politica de seguranca de producao.

## Contribuicao

Mantenha segredos fora do repositorio, adicione testes para novas regras e
atualize a documentacao correspondente. Para vulnerabilidades, consulte
[SECURITY.md](SECURITY.md) e nao abra uma issue publica com detalhes sensiveis.
