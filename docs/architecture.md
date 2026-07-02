# Arquitetura de pagamentos

Esta PoC usa microsservicos por responsabilidade de negocio e adapters para
isolar cada provedor de pagamento. A separacao evita que regras de um gateway
contaminem o dominio principal.

## Principios

- O pagamento nao termina na resposta sincrona do provedor.
- Toda chamada de criacao precisa ser idempotente.
- Falhas transitorias usam retry com backoff e circuit breaker.
- Falhas funcionais nao sao reprocessadas automaticamente.
- SQL e a fonte confiavel para estado financeiro.
- NoSQL guarda payloads flexiveis, idempotencia operacional e diagnostico.
- Eventos conectam os servicos sem acoplamento direto.

## Servicos

### payment-orchestrator-java

Responsavel por receber criacoes de pagamento, validar a idempotencia, manter o
estado principal e publicar eventos. Ele nao conhece detalhes de cada provedor.

### pix-boleto-kotlin

Responsavel por rails bancarios de cobranca: Pix e boleto. O servico conhece
capabilities por provedor, como QR Code Pix, vencimento de boleto e registro.

### card-payment-csharp

Responsavel por autorizacao de cartao e escolha do gateway. O servico protege
contra dupla autorizacao por idempotencia e pode trocar de provedor apenas
quando a regra de negocio permitir.

## Estado de pagamento

```text
CREATED -> PROCESSING -> AUTHORIZED -> PAID
                      -> FAILED
                      -> CANCELED
                      -> REFUNDED
```

Pix e boleto podem ficar em `PROCESSING` ate confirmacao posterior por webhook
ou conciliacao. Cartao pode retornar `AUTHORIZED` antes da captura financeira.

## Consistencia

Cada servico possui banco proprio. Em producao, a integracao entre bancos e
eventos deve usar Outbox para evitar gravar a transacao sem publicar o evento.

## Resiliencia

Cada provedor deve ter seu proprio circuit breaker. Isso evita que falhas no
Asaas, Mercado Pago, PagBank, iugu ou Stripe gerem cascata sobre os demais.

## Observabilidade

Metricas essenciais:

- taxa de sucesso por provedor;
- circuit breakers abertos;
- tempo de resposta por operacao;
- crescimento de filas;
- mensagens em DLQ;
- pagamentos pendentes de conciliacao.
