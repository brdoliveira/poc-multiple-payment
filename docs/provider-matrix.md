# Matriz de provedores

Esta matriz resume como a PoC trata os provedores por meio de pagamento.

| Provedor | Pix | Boleto | Cartao | Observacao |
| --- | --- | --- | --- | --- |
| Asaas | Sim | Sim | Sim | Bom para cobrancas recorrentes e automacao |
| Mercado Pago | Sim | Sim | Sim | Bom para integracao rapida e alto alcance no Brasil |
| PagBank | Sim | Sim | Sim | Bom para carteira digital e parcelamento local |
| iugu | Sim | Sim | Sim | Bom para SaaS, marketplace e split |
| Stripe | Nao neste servico | Nao neste servico | Sim | Foco em cartao e expansao internacional |

## Regras da PoC

- Pix e boleto ficam no `pix-boleto-kotlin`.
- Cartao fica no `card-payment-csharp`.
- O orquestrador Java decide o provider default por meio de pagamento.
- Stripe nao e usado para Pix/boleto nesta PoC para evitar promessa de suporte
  que depende de produto, pais e conta.
