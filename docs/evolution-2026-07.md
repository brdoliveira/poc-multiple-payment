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

## 13/07/2026 a 17/07/2026

Criacao do `card-payment-csharp`, incluindo adapters para gateways, store de
idempotencia e migrations SQL.

## 18/07/2026 a 20/07/2026

Testes unitarios para idempotencia, roteamento de provedores e autorizacao de
cartao.

## 21/07/2026 a 22/07/2026

Revisao da documentacao, padronizacao dos contratos e validacao final do
historico.
