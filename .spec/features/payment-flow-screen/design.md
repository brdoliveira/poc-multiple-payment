# Design: Payment flow screen

## Direção visual

A tela usa uma composição de ferramenta operacional: fundo azul-marinho quase
preto, superfície clara para o formulário, verde-menta para estados positivos e
laranja para atenção. O conteúdo é dividido em uma área principal de trabalho
e um resumo lateral que permanece visível em telas largas.

## Componentes

- `index.html`: estrutura semântica, indicadores de etapa, campos e resumo.
- `styles.css`: tokens de cor, tipografia, grid responsivo e estados de foco.
- `app.js`: ligação dos controles de tela com o estado do fluxo.
- `payment-flow-core.js`: regras puras de valor, método, validação e transição.
- `tests/payment-flow.unit.test.mjs`: testes rápidos da lógica sem navegador.
- `tests/payment-flow.integration.spec.mjs`: jornada completa no Chromium.
- `test-server.mjs`: servidor local mínimo para a integração.
- `README.md` e `docs/payment-flow.md`: operação, arquitetura e testes.

## Camadas de qualidade

1. Testes unitários executam a lógica pura em Node.
2. Provas estáticas verificam markup, acessibilidade básica, rotas locais,
   responsividade declarada e rastreabilidade dos critérios.
3. Teste de integração abre a tela em Chromium, preenche o fluxo e confirma a
   operação.
4. O CI/CD roda as três camadas em pull requests e pushes.

## Decisões

- O fluxo tem três etapas: dados, pagamento e revisão.
- O método padrão é Pix, por ser o caminho mais curto para a PoC.
- O botão de confirmação não chama backend; gera um identificador local para
  demonstrar o estado final sem simular uma resposta de rede.
- O servidor de integração serve somente arquivos estáticos e encerra junto
  com o processo do Playwright.
