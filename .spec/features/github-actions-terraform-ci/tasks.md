# Tasks: CI/CD Terraform com GitHub Actions

> feature: github-actions-terraform-ci

## T-026 - Adicionar workflow de validacao, plan e apply manual [concluida]
- Refs: US-009, AC-026, US-010, AC-027, US-011, AC-028
- Arquivos: .github/workflows/terraform.yml, .github/workflows/ci.yml, .spec/static-tests/github-actions-terraform-ci.test.mjs
- Modelo: gpt-5.6-luna
- Esforco: medio

## T-027 - Parametrizar backend remoto e documentar a operacao [concluida]
- Refs: US-010, AC-027, US-012, AC-029
- Arquivos: infra/aws/terraform/versions.tf, infra/aws/terraform/README.md, docs/terraform-ci.md
- Modelo: gpt-5.6-luna
- Esforco: medio

## T-028 - Registrar provas e fechar a auditoria [concluida]
- Refs: AC-026, AC-027, AC-028, AC-029
- Arquivos: onpspec.config.json, .spec/run-tests.mjs, .spec/features/github-actions-terraform-ci/spec.md, .spec/features/github-actions-terraform-ci/tasks.md
- Modelo: gpt-5.6-luna
- Esforco: baixo
