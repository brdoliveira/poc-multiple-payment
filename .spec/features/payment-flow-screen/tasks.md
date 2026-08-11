# Tasks: Payment flow screen

> feature: payment-flow-screen

## T-005 - Criar a interface visual do fluxo [concluida]

- Refs: US-005, AC-007, AC-008, AC-012
- Arquivos: web/payment-flow/index.html, web/payment-flow/styles.css
- Modelo: gpt-5.6-luna
- Esforco: baixo
- Notas: tela operacional responsiva com seletor de método, etapas, resumo e estados visuais.

## T-006 - Implementar interações e validação local [concluida]

- Refs: US-005, AC-009, AC-010, AC-011
- Arquivos: web/payment-flow/app.js, web/payment-flow/payment-flow-core.js
- Modelo: gpt-5.6-luna
- Esforco: baixo
- Notas: separar a lógica pura da manipulação do DOM; manter o fluxo em memória, sem chamadas externas.

## T-007 - Criar testes unitários da lógica do fluxo [concluida]

- Refs: US-006, AC-014
- Arquivos: web/payment-flow/tests/payment-flow.unit.test.mjs, web/payment-flow/payment-flow-core.js
- Modelo: gpt-5.6-luna
- Esforco: baixo
- Notas: usar `node:test`, sem navegador e sem dependências de infraestrutura.

## T-008 - Criar testes de integração do navegador [concluida]

- Refs: US-006, AC-016
- Arquivos: web/payment-flow/tests/payment-flow.integration.spec.mjs, web/payment-flow/playwright.config.mjs, web/payment-flow/test-server.mjs, web/payment-flow/package.json, web/payment-flow/package-lock.json
- Modelo: gpt-5.6-luna
- Esforco: medio
- Notas: Playwright abre a página servida localmente e percorre o fluxo até o estado de sucesso.

## T-009 - Documentar e conectar as provas ao CI/CD [concluida]

- Refs: US-006, AC-007, AC-008, AC-009, AC-010, AC-011, AC-012, AC-013, AC-014, AC-015, AC-016
- Arquivos: docs/payment-flow.md, web/payment-flow/README.md, .spec/static-tests/payment-flow-screen.test.mjs, .spec/run-tests.mjs, onpspec.config.json, .github/workflows/ci.yml, .gitignore
- Modelo: gpt-5.6-luna
- Esforco: medio
- Notas: documentação operacional, seis provas estáticas anotadas, execução local e job de CI/CD com as três camadas.
