import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import test from 'node:test';

const testDir = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(testDir, '..', '..');

function trackedFiles() {
  return execFileSync('git', ['-c', 'safe.directory=*', 'ls-files'], { cwd: rootDir, encoding: 'utf8' })
    .split(/\r?\n/)
    .filter(Boolean);
}

function trackedText() {
  return trackedFiles()
    .map((relativePath) => {
      const content = readFileSync(path.join(rootDir, relativePath));
      if (content.includes(0)) return '';
      return content.toString('utf8');
    })
    .join('\n');
}

test('arquivos versionados nao expoem dados pessoais ou segredos @spec:AC-033', () => {
  const content = trackedText();
  const knownPersonalEmail = new RegExp(
    ['brunoribeirooliveira64', 'gmail.com'].join('@').replace('@gmail.com', '@gmail\\.com'),
    'i',
  );

  for (const [label, pattern] of [
    ['caminho local de usuario', /[A-Za-z]:[\\/]Users[\\/][^\s"'`<>]+/i],
    ['e-mail pessoal conhecido', knownPersonalEmail],
    ['provedor de e-mail pessoal', /[\w.+-]+@(gmail|outlook|hotmail|yahoo)\.[\w.-]+/i],
    ['chave privada', /-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----/i],
    ['access key AWS', /\bAKIA[0-9A-Z]{16}\b/],
  ]) {
    assert.doesNotMatch(content, pattern, `ocorrencia proibida: ${label}`);
  }
});

test('README orienta o leitor sobre escopo, execucao e limites @spec:AC-034', () => {
  const readme = readFileSync(path.join(rootDir, 'README.md'), 'utf8');

  for (const required of [
    'Status:',
    'Inicio rapido local',
    'Pre-requisitos',
    'Testes',
    'Terraform e AWS',
    'Limites conhecidos',
    'ficticios',
    'SECURITY.md',
  ]) {
    assert.match(readme, new RegExp(required.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
  }
});

test('SECURITY.md orienta reporte privado e nao reutilizacao de credenciais @spec:AC-035', () => {
  const security = readFileSync(path.join(rootDir, 'SECURITY.md'), 'utf8');

  assert.match(security, /Private vulnerability reporting/);
  assert.match(security, /Security\s+Advisories/);
  assert.match(security, /Nao abra uma issue publica/);
  assert.match(security, /Nunca commite/);
});

test('CI executa a auditoria de conteudo publico @spec:AC-036', () => {
  const workflow = readFileSync(path.join(rootDir, '.github', 'workflows', 'ci.yml'), 'utf8');
  const runner = readFileSync(path.join(rootDir, '.spec', 'run-tests.mjs'), 'utf8');

  assert.match(workflow, /public-repository\.test\.mjs/);
  assert.match(runner, /public-repository\.test\.mjs/);
});
