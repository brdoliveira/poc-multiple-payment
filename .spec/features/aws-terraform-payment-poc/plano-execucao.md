# Plano de execução — aws-terraform-payment-poc

> gerado por `onp-spec plano` em 2026-08-09 12:01 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano aws-terraform-payment-poc`

## Resumo — o que vai acontecer

- **4 tarefa(s) pendente(s)**: 4 em 3 faixa(s) paralela(s) + 0 sequencial(is)
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano aws-terraform-payment-poc --paralelizar T-xxx,T-yyy` ou `--sequencial`
- tudo acontece na branch de trabalho `spec/aws-terraform-payment-poc`; levar para a main é decisão sua

## Faixas e ondas

### Onda 1 — faixa-1 ∥ faixa-2 ∥ faixa-3

#### faixa-1 — branch `spec/aws-terraform-payment-poc-faixa-1` — worktree `../onp-worktrees/poc-payment-aws-terraform-payment-poc-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-001 | Reorganizar diretorios e escopo do onp-spec | `gpt-5.6-luna` | low | `onpspec.config.json`, `.gitignore` |

#### faixa-2 — branch `spec/aws-terraform-payment-poc-faixa-2` — worktree `../onp-worktrees/poc-payment-aws-terraform-payment-poc-faixa-2`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-002 | Alinhar ECS, ECR e ALB aos tres servicos reais | `gpt-5.6-terra` | medium | `infra/aws/terraform/versions.tf`, `infra/aws/terraform/data.tf`, `infra/aws/terraform/network.tf`, `infra/aws/terraform/locals.tf`, `infra/aws/terraform/variables.tf`, `infra/aws/terraform/ecs.tf`, `infra/aws/terraform/ecr.tf`, `infra/aws/terraform/alb_waf.tf`, `infra/aws/terraform/security.tf`, `infra/aws/terraform/outputs.tf` |
| T-003 | Trocar dependencias genericas por RDS, DocumentDB e Amazon MQ | `gpt-5.6-terra` | medium | `infra/aws/terraform/rds.tf`, `infra/aws/terraform/documentdb.tf`, `infra/aws/terraform/rabbitmq.tf`, `infra/aws/terraform/security.tf`, `infra/aws/terraform/iam.tf`, `infra/aws/terraform/outputs.tf`, `infra/aws/terraform/variables.tf`, `infra/aws/terraform/locals.tf` |

#### faixa-3 — branch `spec/aws-terraform-payment-poc-faixa-3` — worktree `../onp-worktrees/poc-payment-aws-terraform-payment-poc-faixa-3`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-004 | Atualizar documentacao e provas estaticas da arquitetura | `gpt-5.6-luna` | low | `infra/aws/terraform/README.md`, `infra/aws/terraform/terraform.tfvars.example`, `infra/aws/terraform/observability.tf`, `docs/architecture.md`, `README.md`, `.github/workflows/ci.yml`, `.spec/run-tests.mjs`, `.spec/static-tests/aws-terraform-payment-poc.test.mjs` |

## Gestão de branches e commits

1. branch de trabalho `spec/aws-terraform-payment-poc` criada do ponto atual (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify aws-terraform-payment-poc` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/aws-terraform-payment-poc/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
Logs: `../onp-worktrees/poc-payment-aws-terraform-payment-poc-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano aws-terraform-payment-poc --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa aws-terraform-payment-poc T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo aws-terraform-payment-poc --tabela   # a tabela de andamento
onp-spec resumo aws-terraform-payment-poc            # o resumo em texto
```

