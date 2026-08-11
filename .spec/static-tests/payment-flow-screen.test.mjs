import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const specDir = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(specDir, '..', '..');
const screenDir = path.join(rootDir, 'web', 'payment-flow');

const html = readFile('index.html');
const css = readFile('styles.css');
const app = readFile('app.js');
const core = readFile('payment-flow-core.js');
const docs = `${readRootFile(path.join('docs', 'payment-flow.md'))}\n${readRootFile(path.join('web', 'payment-flow', 'README.md'))}`;
const workflow = readRootFile(path.join('.github', 'workflows', 'ci.yml'));
const runner = readRootFile(path.join('.spec', 'run-tests.mjs'));

test('screen exposes title, steps, form and summary @spec:AC-007', () => {
  assert.match(html, /<h1>Criar pagamento<\/h1>/);
  assert.match(html, />Dados<\/strong>/);
  assert.match(html, />Pagamento<\/strong>/);
  assert.match(html, /Revis\u00e3o/);
  assert.match(html, /id="payment-form"/);
  assert.match(html, /id="summary-title"/);
});

test('screen declares all payment methods and live method detail @spec:AC-008', () => {
  for (const method of ['pix', 'card', 'boleto']) {
    assert.match(html, new RegExp(`data-method="${method}"`));
  }
  assert.match(html, /role="radiogroup"/);
  assert.match(html, /id="method-detail"/);
  assert.match(app, /METHOD_DETAILS/);
});

test('screen connects empty fields to local validation feedback @spec:AC-009', () => {
  assert.match(html, /id="form-alert"[^>]*role="alert"/);
  assert.match(core, /function validateDetails/);
  assert.match(app, /validateDetailsState\(state\)/);
});

test('screen exposes back navigation and review values @spec:AC-010', () => {
  assert.match(html, /data-action="back"/);
  assert.match(html, /data-panel="3"/);
  assert.match(html, /data-review="description"/);
  assert.match(app, /previousStep/);
});

test('screen contains local success and reset states @spec:AC-011', () => {
  assert.match(html, /data-panel="success"/);
  assert.match(html, /id="operation-id"/);
  assert.match(app, /createOperationId/);
  assert.match(html, /Criar\s+novo pagamento/);
});

test('screen has a one-column narrow viewport layout @spec:AC-012', () => {
  assert.match(css, /@media\s*\(max-width:\s*600px\)/);
  assert.match(css, /\.two-columns, \.method-list\s*\{[^}]*grid-template-columns:\s*1fr/s);
  assert.match(css, /overflow-x:\s*hidden/);
});

test('screen documentation covers setup, responsibilities and limits @spec:AC-013', () => {
  for (const term of ['npm run test:unit', 'npm run test:integration', 'payment-flow-core.js', 'Limits of the prototype']) {
    assert.match(docs, new RegExp(term.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
  }
});

test('CI and spec runner execute unit, integration and static proofs @spec:AC-015', () => {
  assert.match(workflow, /npm ci/);
  assert.match(workflow, /playwright install --with-deps chromium/);
  assert.match(workflow, /npm test/);
  assert.match(runner, /payment-flow-unit/);
  assert.match(runner, /payment-flow-integration/);
  assert.match(runner, /payment-flow-screen\.test\.mjs/);
});

function readFile(relativePath) {
  const filePath = path.isAbsolute(relativePath) ? relativePath : path.join(screenDir, relativePath);
  return readFileSync(filePath, 'utf8');
}

function readRootFile(relativePath) {
  return readFileSync(path.join(rootDir, relativePath), 'utf8');
}
