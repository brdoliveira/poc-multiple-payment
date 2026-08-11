# Plano de execução — payment-flow-screen

> gerado por `onp-spec plano` em 2026-08-11 11:50 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano payment-flow-screen`

## Resumo — o que vai acontecer

- **5 tarefa(s) pendente(s)**: 5 em 4 faixa(s) paralela(s) + 0 sequencial(is)
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano payment-flow-screen --paralelizar T-xxx,T-yyy` ou `--sequencial`
- tudo acontece na branch de trabalho `spec/payment-flow-screen`; levar para a main é decisão sua

## Faixas e ondas

### Onda 1 — faixa-1 ∥ faixa-2 ∥ faixa-3

#### faixa-1 — branch `spec/payment-flow-screen-faixa-1` — worktree `../onp-worktrees/poc-payment-payment-flow-screen-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-005 | Criar a interface visual do fluxo | `gpt-5.6-luna` | low | `web/payment-flow/index.html`, `web/payment-flow/styles.css` |

#### faixa-2 — branch `spec/payment-flow-screen-faixa-2` — worktree `../onp-worktrees/poc-payment-payment-flow-screen-faixa-2`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-006 | Implementar interações e validação local | `gpt-5.6-luna` | low | `web/payment-flow/app.js`, `web/payment-flow/payment-flow-core.js` |
| T-007 | Criar testes unitários da lógica do fluxo | `gpt-5.6-luna` | low | `web/payment-flow/tests/payment-flow.unit.test.mjs`, `web/payment-flow/payment-flow-core.js` |

#### faixa-3 — branch `spec/payment-flow-screen-faixa-3` — worktree `../onp-worktrees/poc-payment-payment-flow-screen-faixa-3`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-008 | Criar testes de integração do navegador | `gpt-5.6-luna` | medium | `web/payment-flow/tests/payment-flow.integration.spec.mjs`, `web/payment-flow/playwright.config.mjs`, `web/payment-flow/test-server.mjs`, `web/payment-flow/package.json`, `web/payment-flow/package-lock.json` |

### Onda 2 — faixa-4

#### faixa-4 — branch `spec/payment-flow-screen-faixa-4` — worktree `../onp-worktrees/poc-payment-payment-flow-screen-faixa-4`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-009 | Documentar e conectar as provas ao CI/CD | `gpt-5.6-luna` | medium | `docs/payment-flow.md`, `web/payment-flow/README.md`, `.spec/static-tests/payment-flow-screen.test.mjs`, `.spec/run-tests.mjs`, `onpspec.config.json`, `.github/workflows/ci.yml`, `.gitignore` |

## Gestão de branches e commits

1. branch de trabalho `spec/payment-flow-screen` criada do ponto atual (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify payment-flow-screen` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/payment-flow-screen/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
Logs: `../onp-worktrees/poc-payment-payment-flow-screen-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano payment-flow-screen --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa payment-flow-screen T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo payment-flow-screen --tabela   # a tabela de andamento
onp-spec resumo payment-flow-screen            # o resumo em texto
```

