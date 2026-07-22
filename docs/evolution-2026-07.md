# Evolucao de julho de 2026

Este documento descreve a linha evolutiva usada nos commits semanticos da PoC.

## 01/07/2026

Criacao da visao da plataforma, com separacao entre orquestracao, meios de
pagamento e provedores.

## 02/07/2026

Definicao de arquitetura, infraestrutura local e divisao entre SQL e NoSQL.

## 03/07/2026

Contratos de eventos e inicio do `payment-orchestrator-java`.

## 04/07/2026 a 07/07/2026

Dominio Java, endpoints, idempotencia, outbox e migrations do orquestrador.

## 08/07/2026 a 12/07/2026

Criacao do `pix-boleto-kotlin`, incluindo router de provedores, capabilities e
migrations para Pix e boleto.

## 21/07/2026

Criacao do `card-payment-csharp`, incluindo solucao .NET, endpoint de
autorizacao, dominio, adapters para gateways e idempotencia.

## 22/07/2026

Migrations SQL do servico C#, testes unitarios de autorizacao de cartao,
revisao da documentacao, padronizacao dos contratos e validacao final do
historico.

Tambem foram adicionados persistencia real em PostgreSQL, outbox com RabbitMQ,
webhook com MongoDB, conciliacao de pagamentos pendentes, circuit breaker/retry
nos adapters e Dockerfiles para execucao local.
