# Design: Prontidao para repositorio publico

O teste estatico usa os arquivos versionados por `git ls-files`, ignora apenas
artefatos de build e verifica padroes de alto risco: caminhos locais, e-mails
pessoais, chaves privadas, tokens AWS e credenciais de provedores. Placeholders
de exemplo permanecem permitidos quando marcados como ficticios.

O README passa a ser a porta de entrada operacional. `SECURITY.md` direciona
vulnerabilidades para o GitHub Security Advisories. O historico nao e
reescrito; o e-mail de autoria existente e registrado como risco residual.
