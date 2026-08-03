# Tasks: AWS Terraform para a PoC de pagamentos

> feature: aws-terraform-payment-poc

## T-001 - Reorganizar diretorios e escopo do onp-spec [concluida]
- Refs: US-001, AC-001
- Arquivos: onpspec.config.json, .gitignore
- Modelo: gpt-5.6-luna
- Esforco: baixo
- Notas: mover o Terraform para dentro de `infra/aws/terraform`, remover a pasta raiz antiga e limitar `srcGlobs` ao escopo de infra/spec desta entrega.

## T-002 - Alinhar ECS, ECR e ALB aos tres servicos reais [concluida]
- Refs: US-002, AC-002, US-004, AC-005
- Arquivos: infra/aws/terraform/versions.tf, infra/aws/terraform/data.tf, infra/aws/terraform/network.tf, infra/aws/terraform/locals.tf, infra/aws/terraform/variables.tf, infra/aws/terraform/ecs.tf, infra/aws/terraform/ecr.tf, infra/aws/terraform/alb_waf.tf, infra/aws/terraform/security.tf, infra/aws/terraform/outputs.tf
- Modelo: gpt-5.6-terra
- Esforco: medio
- Notas: remover workloads de billing/worker/refund/upgrade e criar target groups/rotas para `payments`, `bank-rail`, `cards` e `webhooks`.

## T-003 - Trocar dependencias genericas por RDS, DocumentDB e Amazon MQ [concluida]
- Refs: US-003, AC-003, AC-004
- Arquivos: infra/aws/terraform/rds.tf, infra/aws/terraform/documentdb.tf, infra/aws/terraform/rabbitmq.tf, infra/aws/terraform/security.tf, infra/aws/terraform/iam.tf, infra/aws/terraform/outputs.tf, infra/aws/terraform/variables.tf, infra/aws/terraform/locals.tf
- Modelo: gpt-5.6-terra
- Esforco: medio
- Notas: manter RDS, adicionar DocumentDB e Amazon MQ RabbitMQ, remover SQS/EventBridge billing e expor variaveis de ambiente compativeis com Spring/.NET.

## T-004 - Atualizar documentacao e provas estaticas da arquitetura [concluida]
- Refs: US-001, AC-001, US-002, AC-002, US-003, AC-003, AC-004, US-004, AC-005, AC-006
- Arquivos: infra/aws/terraform/README.md, infra/aws/terraform/terraform.tfvars.example, infra/aws/terraform/observability.tf, docs/architecture.md, README.md, .github/workflows/ci.yml, .spec/run-tests.mjs, .spec/static-tests/aws-terraform-payment-poc.test.mjs
- Modelo: gpt-5.6-luna
- Esforco: baixo
- Notas: documentar a nova estrutura AWS e adicionar testes Node com titulos `@spec:AC-xxx`.
