# Estrategia de testes

Os testes desta PoC cobrem os riscos principais de pagamento:

- idempotencia no orquestrador Java;
- roteamento por capability no servico Kotlin de Pix/boleto;
- idempotencia na autorizacao de cartao em C#.

## Java

```bash
cd services/payment-orchestrator-java
mvn test
```

## Kotlin

```bash
cd services/pix-boleto-kotlin
gradle test
```

## C#

```bash
cd services/card-payment-csharp
dotnet test
```

## Proximos testes

- contrato dos eventos publicados;
- validacao de migrations em PostgreSQL real;
- retry/backoff por provedor;
- simulacao de webhook duplicado;
- conciliacao de pagamento pendente.

## Ferramentas locais

Nesta maquina, a validacao foi feita com JDK 17 instalado no Windows, Maven
3.9.11 e Gradle 8.8 instalados de forma portavel em `.local-tools`, e .NET SDK
8 instalado em `C:\Program Files\dotnet`.

Execute os exemplos abaixo a partir da raiz do repositorio.

Java:

```powershell
$root = (Resolve-Path ".").Path
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$root\.local-tools\apache-maven-3.9.11\bin;$env:PATH"
Push-Location "$root\services\payment-orchestrator-java"
& "$root\.local-tools\apache-maven-3.9.11\bin\mvn.cmd" "-Dmaven.repo.local=$root\.local-tools\m2" test
Pop-Location
```

Kotlin:

```powershell
$root = (Resolve-Path ".").Path
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$env:GRADLE_USER_HOME = "$root\.local-tools\gradle-home"
$env:PATH = "$env:JAVA_HOME\bin;$root\.local-tools\gradle-8.8\bin;$env:PATH"
Push-Location "$root\services\pix-boleto-kotlin"
& "$root\.local-tools\gradle-8.8\bin\gradle.bat" test --no-daemon
Pop-Location
```

C#:

```powershell
$root = (Resolve-Path ".").Path
$env:DOTNET_CLI_HOME = "$root\.local-tools\dotnet-home"
$env:NUGET_PACKAGES = "$root\.local-tools\nuget"
$env:APPDATA = "$root\.local-tools\appdata"
$env:LOCALAPPDATA = "$root\.local-tools\localappdata"
Push-Location "$root\services\card-payment-csharp"
& "C:\Program Files\dotnet\dotnet.exe" test --no-restore
Pop-Location
```

## Validacao manual

Com os containers no ar:

```bash
docker compose up -d --build
```

Criar pagamento:

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "X-Internal-Api-Key: local-dev-key" \
  -d '{"idempotencyKey":"checkout-1","method":"PIX","amount":99.90,"currency":"BRL","preferredProvider":"ASAAS"}'
```

Criar cobranca Pix:

```bash
curl -X POST http://localhost:8081/bank-rail/charges \
  -H "Content-Type: application/json" \
  -H "X-Internal-Api-Key: local-dev-key" \
  -d '{"idempotencyKey":"pix-1","rail":"PIX","amount":99.90,"currency":"BRL","preferredProvider":"ASAAS"}'
```

Autorizar cartao:

```bash
curl -X POST http://localhost:8082/cards/authorizations \
  -H "Content-Type: application/json" \
  -H "X-Internal-Api-Key: local-dev-key" \
  -d '{"idempotencyKey":"card-1","amount":199.90,"currency":"BRL","installments":3,"cardToken":"tok_test","preferredProvider":"Stripe"}'
```

## CI

O workflow `.github/workflows/ci.yml` executa:

- testes Java com Maven;
- testes Kotlin com Gradle;
- restore/testes C# com .NET 8;
- `docker compose build` apos as suites unitarias.
