# Spec: Prontidao para repositorio publico

> feature: public-repository-readiness
> status: pronta

## Contexto

O repositorio sera publico e precisa deixar claros seus limites de PoC,
proteger contra o commit acidental de dados pessoais e segredos, e oferecer uma
entrada simples para novos contribuidores.

## Historias

### US-016 - Auditar conteudo publico

Como mantenedor, quero detectar dados pessoais, caminhos locais e segredos no
conteudo versionado, para reduzir o risco de publicar informacoes privadas.

#### AC-033 - Conteudo versionado nao expõe dados pessoais ou segredos

- **Dado** o conjunto de arquivos versionados do repositorio
- **Quando** a checagem de publicacao for executada
- **Entao** ela rejeita caminhos locais de usuarios, e-mails pessoais, chaves
  privadas, tokens AWS e credenciais reais
- **E** aceita somente placeholders explicitamente documentados.

### US-017 - Orientar contribuidores

Como pessoa que encontra o repositorio, quero saber o que ele demonstra e como
executa-lo, para reproduzir a PoC sem confundir prototipo com producao.

#### AC-034 - README explica escopo, inicio rapido e limites

- **Dado** um leitor novo
- **Quando** ele abrir o README
- **Entao** encontrara status da PoC, arquitetura, pre-requisitos, comandos de
  execucao, testes, documentacao e limites de producao
- **E** vera explicitamente que as credenciais locais sao ficticias.

### US-018 - Receber relatos de seguranca

Como mantenedor, quero instrucoes de reporte de vulnerabilidades, para evitar
que detalhes sensiveis sejam publicados em issues.

#### AC-035 - Politica de seguranca orienta reporte privado

- **Dado** uma vulnerabilidade encontrada no repositorio
- **Quando** o pesquisador consultar `SECURITY.md`
- **Entao** vera a orientacao para usar o recurso privado de advisories do
  GitHub e nao abrir uma issue publica com detalhes exploraveis.

### US-019 - Manter a auditoria no CI

Como mantenedor, quero executar a auditoria de repositorio publico no CI, para
que novas alteracoes sejam verificadas antes do merge.

#### AC-036 - CI executa a checagem de repositorio publico

- **Dado** um pull request ou push
- **Quando** o CI rodar os testes estaticos
- **Entao** ele executara a checagem de conteudo publico e falhara ao encontrar
  uma ocorrencia proibida.

## Fora de escopo

- Reescrever o historico Git existente.
- Remover o e-mail do autor dos commits ja publicados.
- Tornar a PoC pronta para producao.

## Suposicoes

- Credenciais locais documentadas sao dados ficticios e nao concedem acesso.
- O repositorio continuara em `brdoliveira/poc-multiple-payment`.

## Perguntas em aberto

Nenhuma.
