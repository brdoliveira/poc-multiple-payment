# Tasks: Endurecimento do deploy ECS

> feature: ecs-deploy-hardening

## T-029 - Exigir imagens e segredo seguro [concluida]
- Refs: US-013, AC-030, US-014, AC-031
- Arquivos: infra/aws/terraform/variables.tf, infra/aws/terraform/locals.tf, infra/aws/terraform/ecs.tf, infra/aws/terraform/terraform.tfvars.example
- Modelo: gpt-5.6-luna
- Esforco: medio

## T-030 - Alinhar health check e documentacao [concluida]
- Refs: US-015, AC-032
- Arquivos: infra/aws/terraform/ecs.tf, infra/aws/terraform/README.md
- Modelo: gpt-5.6-luna
- Esforco: baixo

## T-031 - Provar endurecimento no CI [concluida]
- Refs: AC-030, AC-031, AC-032
- Arquivos: .spec/static-tests/ecs-deploy-hardening.test.mjs, onpspec.config.json, .spec/run-tests.mjs, .spec/features/ecs-deploy-hardening/spec.md, .spec/features/ecs-deploy-hardening/tasks.md, .spec/features/ecs-deploy-hardening/design.md
- Modelo: gpt-5.6-luna
- Esforco: baixo
