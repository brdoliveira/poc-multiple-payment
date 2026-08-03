# Spec: AWS Terraform para a PoC de pagamentos

> feature: aws-terraform-payment-poc
> status: pronta

## Contexto

A estrutura Terraform criada em `terraform-payment-architecture/` descreve uma
arquitetura de billing generica. A PoC atual, porem, possui tres servicos reais:
`payment-orchestrator-java`, `pix-boleto-kotlin` e `card-payment-csharp`, com
PostgreSQL, MongoDB operacional e RabbitMQ. Esta feature alinha a infraestrutura
AWS ao desenho atual do repositorio.

## Historias

### US-001 - Estrutura Terraform no lugar certo

Como mantenedor da PoC, quero que a infraestrutura AWS fique dentro da pasta
`infra/`, para que a estrutura do repositorio separe claramente infra local e
infra cloud.

#### AC-001 - Terraform AWS fica em `infra/aws/terraform`

- **Dado** o repositorio da PoC com infraestrutura local em `infra/`
- **Quando** a estrutura AWS for reorganizada
- **Entao** os arquivos Terraform ficam em `infra/aws/terraform`
- **E** nao existe mais a pasta raiz `terraform-payment-architecture`

### US-002 - Workloads AWS representam os servicos reais

Como arquiteto da PoC, quero que o Terraform modele os microsservicos reais de
pagamento, para que o deploy AWS nao carregue nomes e responsabilidades de
billing que nao existem neste projeto.

#### AC-002 - ECS e ECR usam somente os tres servicos da PoC

- **Dado** a lista de microsservicos do README
- **Quando** o Terraform declarar workloads, imagens e contagens desejadas
- **Entao** os nomes declarados incluem `payment-orchestrator-java`,
  `pix-boleto-kotlin` e `card-payment-csharp`
- **E** nao incluem `billing-ws`, `billing-core`, `refund-worker`,
  `upgrade-plan-worker` nem `worker-*`

### US-003 - Dependencias AWS batem com o codigo atual

Como desenvolvedor dos servicos, quero que a infraestrutura AWS forneca os
mesmos tipos de dependencias usados localmente, para que a PoC possa evoluir
para cloud sem trocar RabbitMQ/MongoDB por outro modelo sem decisao explicita.

#### AC-003 - Terraform provisiona RDS, DocumentDB e RabbitMQ gerenciado

- **Dado** que os servicos usam PostgreSQL, MongoDB e RabbitMQ
- **Quando** a infraestrutura AWS for lida
- **Entao** ela provisiona RDS PostgreSQL, DocumentDB ou equivalente
  Mongo-compatible e Amazon MQ RabbitMQ
- **E** remove a mensageria generica SQS/EventBridge de billing/refund/worker

#### AC-004 - Variaveis de ambiente atendem Java, Kotlin e C#

- **Dado** os arquivos `application.yml` e `appsettings.json` dos servicos
- **Quando** as task definitions ECS forem geradas
- **Entao** Java/Kotlin recebem variaveis Spring para datasource, RabbitMQ e
  API key interna
- **E** C# recebe `ConnectionStrings__Postgres`, `ConnectionStrings__Mongo`,
  `RabbitMq__*` e `Security__ApiKey`

### US-004 - Entrada publica e documentacao ficam coerentes

Como pessoa revisando a arquitetura, quero que os endpoints e a documentacao
mostrem a PoC real, para que seja claro o que sera exposto no ALB e como usar o
Terraform.

#### AC-005 - ALB roteia para endpoints reais

- **Dado** os endpoints principais do README
- **Quando** o Terraform configurar o ALB
- **Entao** existem rotas para `/payments`, `/reconciliation`, `/bank-rail`,
  `/cards` e `/webhooks`

#### AC-006 - Documentacao AWS nao cita a arquitetura antiga de billing

- **Dado** a documentacao de infraestrutura AWS
- **Quando** alguem ler como usar o Terraform
- **Entao** ela aponta para `infra/aws/terraform`
- **E** nao cita workloads antigos de billing, refund ou upgrade de plano

## Fora de escopo

- Executar `terraform apply` em uma conta AWS real.
- Criar backend remoto de state.
- Criar pipeline completo de deploy de imagens.
- Reescrever os servicos para usar SQS/EventBridge.

## Suposições

Nenhuma.

## Perguntas em aberto

Nenhuma.
