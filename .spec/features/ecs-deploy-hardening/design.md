# Design: Endurecimento do deploy ECS

O mapa de imagens passa a ser uma entrada obrigatoria e validada no Terraform.
As task definitions acessam diretamente as chaves conhecidas, sem fallback.
O ALB permanece responsavel pelo health check, enquanto o ECS nao declara um
comando que dependa de `wget` ou `curl` na imagem runtime.
