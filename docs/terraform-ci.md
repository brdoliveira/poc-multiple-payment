# CI/CD do Terraform no GitHub

O workflow fica em `.github/workflows/terraform.yml` e usa a configuracao
Terraform de `infra/aws/terraform`.

## O que roda automaticamente

- Pull request: `terraform fmt -check`, `terraform init -backend=false` e
  `terraform validate`, sem acesso a AWS.
- Push na `main`: os mesmos checks e, quando a configuracao AWS estiver pronta,
  um `terraform plan` usando o state remoto S3.
- Apply: somente pelo botao **Run workflow**, com `apply=true`, na `main` e com
  aprovacao do environment `terraform-dev`.

O workflow nunca executa `terraform apply` em pull request ou push comum.
Se `AWS_ROLE_TO_ASSUME` ou `TF_STATE_BUCKET` ainda nao estiver configurada,
o job de plan fica `skipped` e a validacao local continua funcionando.

## Configuracao no GitHub

Em **Settings > Secrets and variables > Actions**, crie estas variaveis do
repositorio:

| Nome | Tipo | Uso |
| --- | --- | --- |
| `AWS_ROLE_TO_ASSUME` | Variable | ARN da role IAM confiada pelo OIDC |
| `AWS_REGION` | Variable | Regiao AWS, por exemplo `sa-east-1` |
| `TF_STATE_BUCKET` | Variable | Bucket S3 ja criado para o state |
| `TF_ENVIRONMENT` | Variable | Ambiente, inicialmente `dev` |

Crie o secret `TF_INTERNAL_API_KEY` com a chave interna da aplicacao. Nao
commite `terraform.tfvars`, `terraform.tfstate` ou credenciais AWS.

Crie o environment `terraform-dev` e configure required reviewers antes de
permitir apply. O environment de producao nao e usado por este workflow.

## OIDC na AWS

Na conta AWS, cadastre o provedor OIDC
`https://token.actions.githubusercontent.com` com audiencia
`sts.amazonaws.com`. A trust policy da role deve aceitar somente:

```json
{
  "Effect": "Allow",
  "Principal": {
    "Federated": "arn:aws:iam::<ACCOUNT_ID>:oidc-provider/token.actions.githubusercontent.com"
  },
  "Action": "sts:AssumeRoleWithWebIdentity",
  "Condition": {
    "StringEquals": {
      "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
    },
    "StringLike": {
      "token.actions.githubusercontent.com:sub": "repo:brdoliveira/poc-multiple-payment:ref:refs/heads/main"
    }
  }
}
```

A role deve receber apenas as permissoes necessarias ao state S3, aos data
sources e aos recursos Terraform deste ambiente. Inclua `iam:PassRole` somente
para as roles ECS que o Terraform realmente criar.

## State remoto

O bucket deve existir antes do primeiro plan e ter versionamento, criptografia
e bloqueio de acesso publico habilitados. O Terraform declara `backend "s3" {}`
em `versions.tf`; o workflow fornece os valores por `-backend-config`.

Para validacao local, use:

```powershell
terraform -chdir=infra/aws/terraform init -backend=false
terraform -chdir=infra/aws/terraform validate
```

O workflow nao cria o bucket, a role OIDC ou o environment do GitHub.
