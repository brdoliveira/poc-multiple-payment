# Spec: Endurecimento do deploy ECS

> feature: ecs-deploy-hardening
> status: pronta

## Contexto

As task definitions ECS devem executar as imagens reais dos microsservicos e
usar os health checks do ALB, sem fallbacks de demonstracao ou comandos que nao
existem nas imagens runtime.

## Historias

### US-013 - Executar imagens reais no ECS

Como mantenedor, quero que o Terraform exija as imagens dos tres servicos,
para que um apply nao publique acidentalmente um container nginx.

#### AC-030 - Imagens reais sao obrigatorias

- **Dado** o mapa `container_images`
- **Quando** o Terraform validar as variaveis
- **Entao** ele exige exatamente os tres microsservicos da PoC
- **E** rejeita mapa vazio, valores vazios ou imagens nginx.

### US-014 - Proteger segredo interno

Como mantenedor, quero que a API key interna seja fornecida pelo ambiente,
para que o Terraform nao tenha uma credencial padrao insegura.

#### AC-031 - API key nao possui fallback inseguro

- **Dado** a variavel `internal_api_key`
- **Quando** o Terraform validar a configuracao
- **Entao** a variavel nao possui valor default
- **E** exige pelo menos 16 caracteres.

### US-015 - Health check compatível com runtime

Como operador, quero que o ECS nao dependa de ferramentas ausentes na imagem,
para que a saude seja determinada pelos endpoints reais do ALB.

#### AC-032 - Health check usa ALB sem wget ou curl no container

- **Dado** os endpoints `/actuator/health` e `/health` configurados no ALB
- **Quando** as task definitions forem geradas
- **Entao** elas nao declaram health check baseado em `wget` ou `curl`
- **E** o target group continua com os endpoints de saude dos servicos.

## Fora de escopo

- Criar ou publicar imagens no ECR nesta feature.
- Executar `terraform apply` em uma conta AWS.

## Suposicoes

Nenhuma.

## Perguntas em aberto

Nenhuma.
