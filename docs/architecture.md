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
estado principal e gravar eventos na outbox. Ele nao conhece detalhes de cada
provedor.

### pix-boleto-kotlin

Responsavel por rails bancarios de cobranca: Pix e boleto. O servico conhece
capabilities por provedor, como QR Code Pix, vencimento de boleto e registro.
Alem da API HTTP, consome eventos `PaymentProcessing` pela fila
`payment-events.bank-rail`.

### card-payment-csharp

Responsavel por autorizacao de cartao e escolha do gateway. O servico protege
contra dupla autorizacao por idempotencia e pode trocar de provedor apenas
quando a regra de negocio permitir.

Tambem recebe webhooks de provedores nesta PoC, validando uma assinatura HMAC
generica e salvando o payload bruto no MongoDB. Em producao, esse endpoint pode
ser extraido para um microsservico dedicado.
Alem da API HTTP, consome eventos `PaymentProcessing` pela fila
`payment-events.card`.

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
Nesta PoC, o orquestrador Java ja grava eventos em `payment_outbox_events` e um
dispatcher publica mensagens no RabbitMQ.

Cada consumidor possui fila principal, fila de retry com TTL e DLQ final:

- `payment-events.bank-rail`;
- `payment-events.bank-rail.retry`;
- `payment-events.bank-rail.dlq`;
- `payment-events.card`;
- `payment-events.card.retry`;
- `payment-events.card.dlq`.

Quando uma mensagem falha, ela e rejeitada sem requeue, passa pela fila de retry
e volta para a fila principal. Apos o limite de tentativas, o consumidor publica
a mensagem na DLQ e confirma o processamento para impedir loop infinito.

## Resiliencia

Cada provedor deve ter seu proprio circuit breaker. Isso evita que falhas no
Asaas, Mercado Pago, PagBank, iugu ou Stripe gerem cascata sobre os demais.

No Kotlin, as chamadas de Pix/boleto usam Resilience4j. No C#, as autorizacoes
de cartao usam Polly com retry exponencial e circuit breaker.

## Observabilidade

Metricas essenciais:

- taxa de sucesso por provedor;
- circuit breakers abertos;
- tempo de resposta por operacao;
- crescimento de filas;
- mensagens em DLQ;
- pagamentos pendentes de conciliacao.

Os servicos HTTP propagam `X-Correlation-Id`. Java e Kotlin expoem Actuator com
`health`, `metrics` e `prometheus`; o servico C# expoe `/health`.

## Seguranca

Endpoints internos exigem `X-Internal-Api-Key` configuravel por ambiente. Em
producao, esse mecanismo deve evoluir para mTLS, JWT assinado ou autorizacao
centralizada por API Gateway. Webhooks externos nao usam a API key interna: eles
devem ser protegidos por assinatura HMAC especifica do provedor.
