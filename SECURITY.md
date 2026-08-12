# Politica de seguranca

Este repositorio e uma PoC publica. Os exemplos locais usam credenciais
ficticias e nao devem ser reutilizados fora do ambiente local.

## Reporte privado

Nao abra uma issue publica para vulnerabilidades, credenciais expostas ou
detalhes exploraveis. Use **Private vulnerability reporting** ou **Security
Advisories** nas configuracoes do repositorio GitHub.

Inclua impacto, passos para reproduzir, versao/commit afetado e uma sugestao
de correcao quando possivel. Nao inclua dados pessoais, tokens ou dumps reais.

## Segredos

- Nunca commite `.env`, `terraform.tfvars`, state Terraform, chaves privadas ou
  credenciais de provedores.
- Use Secrets Manager, GitHub Actions Secrets e OIDC para ambientes reais.
- Os valores `payments`, `guest` e `local-dev-key` existem apenas para testes
  locais da PoC.
