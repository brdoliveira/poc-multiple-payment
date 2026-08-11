import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import test from 'node:test';

const testDir = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(testDir, '..', '..');

function read(relativePath) {
  return readFileSync(path.join(rootDir, relativePath), 'utf8');
}

test('Workflow instala Terraform e valida sem AWS em pull requests @spec:AC-026', () => {
  const workflow = read('.github/workflows/terraform.yml');

  assert.match(workflow, /pull_request:/);
  assert.match(workflow, /hashicorp\/setup-terraform@v4/);
  assert.match(workflow, /TERRAFORM_VERSION: "1\.15\.8"/);
  assert.match(workflow, /terraform fmt -check -recursive/);
  assert.match(workflow, /terraform init -backend=false -input=false/);
  assert.match(workflow, /terraform validate -no-color/);
});

test('Plan usa OIDC e backend S3 sem chaves AWS estaticas @spec:AC-027', () => {
  const workflow = read('.github/workflows/terraform.yml');

  assert.match(workflow, /id-token: write/);
  assert.match(workflow, /aws-actions\/configure-aws-credentials@v4/);
  assert.match(workflow, /role-to-assume: \$\{\{ vars\.AWS_ROLE_TO_ASSUME \}\}/);
  assert.match(workflow, /backend-config="bucket=\$\{\{ vars\.TF_STATE_BUCKET \}\}"/);
  assert.match(workflow, /backend-config="use_lockfile=true"/);
  assert.doesNotMatch(workflow, /AWS_ACCESS_KEY_ID|AWS_SECRET_ACCESS_KEY/);
});

test('Apply e manual, restrito a main e ao environment de desenvolvimento @spec:AC-028', () => {
  const workflow = read('.github/workflows/terraform.yml');

  assert.match(workflow, /workflow_dispatch:/);
  assert.match(workflow, /inputs:\s+apply:/);
  assert.match(workflow, /github\.event_name == 'workflow_dispatch'/);
  assert.match(workflow, /github\.ref == 'refs\/heads\/main'/);
  assert.match(workflow, /environment: terraform-dev/);
  assert.match(workflow, /terraform apply -input=false -auto-approve tfplan/);
});

test('Documentacao lista OIDC, state, variaveis e ausencia de apply automatico @spec:AC-029', () => {
  const docs = read('docs/terraform-ci.md');

  for (const required of [
    'AWS_ROLE_TO_ASSUME',
    'TF_STATE_BUCKET',
    'TF_INTERNAL_API_KEY',
    'terraform-dev',
    'token.actions.githubusercontent.com',
    'O workflow nunca executa `terraform apply` em pull request ou push comum.',
  ]) {
    assert.match(docs, new RegExp(required.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')));
  }
});
