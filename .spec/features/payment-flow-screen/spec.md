# Spec: Payment flow screen

> feature: payment-flow-screen
> status: pronta

## Contexto

Criar uma tela de operação para iniciar, revisar e confirmar um pagamento da
PoC, permitindo que uma pessoa escolha o meio de pagamento sem depender de uma
integração real com os microsserviços nesta primeira entrega.

## Histórias

### US-005 - Operador cria um pagamento pela tela

Como operador de pagamentos, quero preencher e revisar um pagamento em uma
tela clara, para que eu consiga confirmar a operação com segurança.

#### AC-007 - A tela apresenta o fluxo e o resumo da operação

- **Dado** que a tela de pagamento foi aberta
- **Quando** o usuário visualiza a página
- **Então** ele vê o título da operação, um indicador das etapas Dados/Pagamento/Revisão, um formulário principal e um resumo do pedido

#### AC-008 - O meio de pagamento altera a etapa exibida

- **Dado** que o formulário está na etapa de pagamento
- **Quando** o usuário seleciona Pix, cartão ou boleto
- **Então** a opção fica marcada e a tela exibe o conteúdo específico do meio escolhido

#### AC-009 - Campos obrigatórios são validados antes do avanço

- **Dado** que o valor ou a descrição do pagamento estão vazios
- **Quando** o usuário tenta avançar
- **Então** a tela mostra uma mensagem de validação e permanece na etapa atual

#### AC-010 - O usuário consegue revisar e voltar sem perder os dados

- **Dado** que os dados obrigatórios estão preenchidos
- **Quando** o usuário avança para revisão e depois retorna
- **Então** a tela mostra o resumo do pagamento e mantém os dados preenchidos

#### AC-011 - A confirmação mostra o resultado da operação

- **Dado** que o usuário está na etapa de revisão
- **Quando** ele confirma o pagamento
- **Então** a tela mostra um estado de sucesso com identificador da operação e opção para iniciar outro pagamento

#### AC-012 - A tela funciona em viewport estreito

- **Dado** que a tela é aberta em um viewport de até 720px
- **Quando** o usuário percorre o formulário
- **Então** o conteúdo se reorganiza em uma coluna sem exigir rolagem horizontal

### US-006 - Equipe mantém e valida a tela de pagamento

Como equipe de engenharia, quero documentação e uma esteira de testes em
camadas, para que a tela possa evoluir com feedback rápido e uma prova de fluxo
completo.

#### AC-013 - A documentação explica como usar e testar a tela

- **Dado** que uma pessoa nova precisa executar a tela
- **Quando** ela consulta a documentação do fluxo
- **Então** encontra o objetivo, como abrir localmente, as responsabilidades dos arquivos, os limites do protótipo e os comandos de testes unitários e de integração

#### AC-014 - A lógica do fluxo possui testes unitários

- **Dado** que a lógica de pagamento está separada da manipulação visual do DOM
- **Quando** a suíte unitária é executada
- **Então** ela verifica formatação de valor, seleção de método, validação dos dados e transições principais sem abrir um navegador

#### AC-015 - O CI/CD executa as camadas de teste da tela

- **Dado** que existe um push ou pull request no repositório
- **Quando** o workflow de CI/CD é executado
- **Então** ele instala as dependências da tela e executa testes unitários, provas estáticas e testes de integração do navegador

#### AC-016 - O teste de integração cobre o fluxo completo

- **Dado** que a tela está servida localmente em um navegador automatizado
- **Quando** o teste preenche os dados, escolhe um meio de pagamento, revisa e confirma
- **Então** ele encontra o estado de sucesso com um identificador de operação e consegue iniciar um novo pagamento

## Fora de escopo

- Chamada real ao `payment-orchestrator-java` ou aos demais microsserviços.
- Persistência, autenticação e captura de dados de cartão.
- Checkout público ou integração com provedores externos.
- Testes contra AWS, banco, RabbitMQ ou os microsserviços reais.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-005 | A primeira entrega é um protótipo funcional local, sem backend real e sem dados sensíveis persistidos. | confirmada | Escopo limitado à tela e às interações locais solicitadas. |
| ASM-006 | O resumo usa dados de exemplo da PoC para demonstrar o fluxo. | confirmada | Dados fictícios ficam visíveis apenas na interface. |
| ASM-007 | O teste de integração usa Playwright e um servidor HTTP local mínimo. | confirmada | A cobertura é de navegador e não exige deploy nem backend real. |

## Perguntas em aberto

Nenhuma.
