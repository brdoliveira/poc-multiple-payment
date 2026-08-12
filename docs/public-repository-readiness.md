# Plano de prontidao publica

## Estado atual

- O conteudo versionado nao contem CPF, CNPJ, telefone, caminho local ou chave
  privada identificados pela auditoria.
- As credenciais em `docker-compose.yml` e `application.yml` sao valores
  locais ficticios para a PoC e nao devem ser reutilizadas.
- O historico Git contem o e-mail de autoria usado nos commits. Remover isso
  exigiria reescrever o historico remoto e nao faz parte deste plano.

## Plano executado

1. Auditar arquivos versionados contra dados pessoais, caminhos locais e
   padroes de segredos.
2. Melhorar o README com inicio rapido, mapa da arquitetura, testes, limites e
   configuracao AWS.
3. Adicionar `SECURITY.md` com canal privado para vulnerabilidades.
4. Integrar a auditoria ao CI para impedir regressao.

## Proximos passos operacionais

1. Ativar secret scanning e push protection nas configuracoes do repositorio.
2. Configurar branch protection exigindo os jobs do CI.
3. Revisar manualmente o historico Git se a privacidade do e-mail do autor for
   um requisito, usando `git filter-repo` com janela de manutencao e force-push
   coordenado.
4. Configurar AWS OIDC, state remoto e ECR conforme `docs/terraform-ci.md`.
