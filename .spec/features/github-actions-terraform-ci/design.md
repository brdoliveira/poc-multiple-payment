# Design: CI/CD Terraform com GitHub Actions

## Fluxo

1. Pull requests executam apenas formatacao, init sem backend e validate.
2. Push na `main` gera plan usando OIDC e state remoto S3 quando a role e o
   bucket estiverem configurados.
3. `workflow_dispatch` permite gerar plan ou, com `apply=true`, aplicar somente
   na `main` dentro do environment `terraform-dev`.

## Seguranca

- Nenhuma chave AWS de longa duracao entra em secrets ou no workflow.
- OIDC fornece credenciais temporarias por `aws-actions/configure-aws-credentials`.
- O trust policy deve restringir o repositorio e a branch `main`.
- O bucket de state e o lockfile sao configurados fora do codigo por variaveis
  do repositorio.

## Estado

O backend S3 e declarado sem valores fixos. O workflow injeta bucket, chave,
regiao, criptografia e lockfile em `terraform init`; validacoes locais usam
`-backend=false`.
