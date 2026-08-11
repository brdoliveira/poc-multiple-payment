# Plano de execução — reliability-observability

> gerado por `onp-spec plano` em 2026-08-11 13:03 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano reliability-observability --sequencial`

## Resumo — o que vai acontecer

- **modo SEQUENCIAL (escolha do usuário)**: 5 tarefa(s) pendente(s), UMA APÓS A OUTRA, na árvore principal
- sem worktrees e sem paralelismo — cada tarefa roda numa janela de contexto limpa, na ordem do tasks.md
- tudo acontece na branch de trabalho `spec/reliability-observability`; levar para a main é decisão sua

## Ordem de execução (uma tarefa após a outra)

| tarefa | título | modelo | esforço |
|---|---|---|---|
| T-010 | Harden Java idempotency and telemetry | `gpt-5.6-luna` | high |
| T-011 | Harden Kotlin bank rail idempotency and telemetry | `gpt-5.6-luna` | high |
| T-012 | Harden C# card idempotency and telemetry | `gpt-5.6-luna` | high |
| T-013 | Expand AWS observability and repository proofs | `gpt-5.6-luna` | medium |
| T-014 | Add concurrency and cross-service integration tests | `gpt-5.6-luna` | medium |

## Gestão de branches e commits

1. branch de trabalho `spec/reliability-observability` criada do ponto atual (se ainda não existir)
2. as tarefas rodam nela mesma, na ordem — **1 tarefa = 1 commit** (`T-xxx feature: título`), marcada `[concluida]` só com trabalho feito
3. gate final na branch de trabalho: `onp-spec verify reliability-observability` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/reliability-observability/executar-tarefas.sh
```

Cada tarefa roda `codex exec` com **janela de contexto limpa**, na árvore principal,
uma após a outra, com `--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`.
Os prompts exatos estão embutidos no script.
Logs: `../onp-worktrees/poc-payment-reliability-observability-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano reliability-observability --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa reliability-observability T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo reliability-observability --tabela   # a tabela de andamento
onp-spec resumo reliability-observability            # o resumo em texto
```

