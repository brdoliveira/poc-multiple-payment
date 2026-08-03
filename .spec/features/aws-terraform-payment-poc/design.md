# Design: AWS Terraform para a PoC de pagamentos

## Estrutura alvo

- `infra/mongodb/`: permanece como suporte local do Docker Compose.
- `infra/aws/terraform/`: passa a ser o diretório canonico da infraestrutura AWS.
- `terraform-payment-architecture/`: sai da raiz depois da migracao.

## Workloads ECS

Somente tres servicos continuos:

- `payment-orchestrator-java`: Java/Spring Boot, porta 8080, exposto por ALB em
  `/payments` e `/reconciliation`.
- `pix-boleto-kotlin`: Kotlin/Spring Boot, porta 8081, exposto por ALB em
  `/bank-rail`.
- `card-payment-csharp`: .NET, porta 8082, exposto por ALB em `/cards`,
  `/webhooks` e `/health`.

## Dependencias AWS

- RDS PostgreSQL privado para dados transacionais.
- DocumentDB privado para dados operacionais Mongo-compatible.
- Amazon MQ for RabbitMQ privado para preservar a topologia RabbitMQ existente.
- ALB publico com WAF para entrada HTTP da PoC.
- ECR por servico.
- CloudWatch Logs, alarmes basicos e CloudTrail.

## Prova

Como `terraform` nao esta instalado no ambiente local, a prova automatica desta
feature sera estatica: testes Node leem os arquivos Terraform e documentacao,
checando nomes, caminhos, ausencia dos workloads antigos, recursos AWS
esperados e variaveis de ambiente. O `onp-spec verify` continuara executando as
suites Java/Kotlin/C# pelo runner existente.
