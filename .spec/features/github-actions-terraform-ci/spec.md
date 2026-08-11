# Spec: CI/CD Terraform com GitHub Actions

> feature: github-actions-terraform-ci
> status: pronta

## Contexto

O Terraform da PoC precisa entrar no CI/CD do repositorio sem armazenar chaves
AWS de longa duracao e sem aplicar infraestrutura automaticamente enquanto o
ambiente ainda esta em validacao.

## Historias

### US-009 - Validacao automatica da infraestrutura

Como mantenedor, quero validar a formatacao e a configuracao Terraform em cada
alteracao relevante, para bloquear configuracoes invalidas antes do merge.

#### AC-026 - Pull requests executam os checks locais do Terraform

- **Dado** um pull request ou push que altere Terraform
- **Quando** o workflow de infraestrutura for executado
- **Entao** ele instala a versao fixada do Terraform, roda `fmt -check`,
  `init -backend=false` e `validate`
- **E** esses checks nao dependem de credenciais AWS.

### US-010 - Plan autenticado sem chaves estaticas

Como mantenedor, quero gerar plan no GitHub Actions com OIDC, para que o
pipeline use uma role AWS de curta duracao e um state remoto compartilhado.

#### AC-027 - Plan usa OIDC e backend S3 parametrizado

- **Dado** que as variaveis `AWS_ROLE_TO_ASSUME` e `TF_STATE_BUCKET` estejam
  configuradas no repositorio
- **Quando** houver push na `main` ou execucao manual sem apply
- **Entao** o workflow autentica na AWS por `id-token: write`, inicializa o
  backend S3 com bucket, chave, regiao e lockfile, e executa `terraform plan`
- **E** o workflow nao usa `AWS_ACCESS_KEY_ID` nem `AWS_SECRET_ACCESS_KEY`.

### US-011 - Apply controlado

Como responsavel pela infraestrutura, quero que o apply seja manual e protegido
por environment, para impedir provisionamento acidental.

#### AC-028 - Apply so ocorre manualmente na main

- **Dado** o workflow disparado por `workflow_dispatch`
- **Quando** a entrada `apply` for confirmada na branch `main`
- **Entao** o job exige o environment `terraform-dev` e executa
  `terraform apply -auto-approve` depois de gerar um plan no mesmo job
- **E** pull requests e pushes comuns nunca executam apply.

### US-012 - Operacao documentada

Como mantenedor, quero saber quais recursos do GitHub e AWS preparar, para que
o pipeline seja reproduzivel sem credenciais ou caminhos locais no repositorio.

#### AC-029 - Configuracao do pipeline esta documentada

- **Dado** o repositorio sem secrets configurados
- **Quando** alguem consultar a documentacao do CI/CD Terraform
- **Entao** ela lista OIDC, trust policy, variaveis, secret da API interna,
  bucket de state e environment de deploy
- **E** ela deixa claro que o workflow nao executa `apply` por padrao.

## Fora de escopo

- Criar a conta AWS, o bucket de state ou a role OIDC automaticamente.
- Executar `terraform apply` nesta maquina ou em uma conta real.
- Liberar deploy de producao.

## Suposicoes

- O repositorio GitHub correto e `brdoliveira/poc-multiple-payment`.
- O ambiente inicial do pipeline e `terraform-dev`.

## Perguntas em aberto

Nenhuma.
