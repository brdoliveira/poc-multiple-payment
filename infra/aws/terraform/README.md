# Terraform da infraestrutura AWS

Este diretorio provisiona a infraestrutura AWS da PoC de pagamentos em
`infra/aws/terraform`.

## Componentes

- VPC com sub-redes publicas e privadas, NAT opcional e security groups;
- ECS Fargate e ECR para `payment-orchestrator-java`, `pix-boleto-kotlin` e
  `card-payment-csharp`;
- Application Load Balancer com rotas `/payments`, `/reconciliation`,
  `/bank-rail`, `/cards` e `/webhooks`;
- RDS PostgreSQL para o estado transacional;
- Amazon DocumentDB para payloads operacionais e webhooks;
- Amazon MQ RabbitMQ com endpoint AMQPS privado;
- Secrets Manager para credenciais de banco, mensageria e API key interna;
- CloudWatch, SNS e WAF para observabilidade e protecao basica.

## Uso local

```bash
terraform init -backend=false
terraform validate
terraform plan -var-file=terraform.tfvars
terraform apply -var-file=terraform.tfvars
```

O CI/CD usa backend S3 parametrizado. Consulte
`docs/terraform-ci.md` para configurar OIDC, o bucket de state e o environment
de apply antes de executar `plan` ou `apply` na AWS.

Copie `terraform.tfvars.example` para `terraform.tfvars` e substitua as
imagens pelos repositorios ECR correspondentes. O `apply` nao faz build nem
publica imagens; essa etapa pertence ao pipeline de entrega.

## Custos e seguranca

RDS, DocumentDB, Amazon MQ, NAT Gateway, ALB e WAF geram custo AWS. Para a PoC,
o ambiente padrao usa classes pequenas e uma instancia DocumentDB. Em producao,
avalie alta disponibilidade, TLS no ALB, backup, rotacao de segredos, state
remoto e politicas IAM mais restritas antes do `apply`.
