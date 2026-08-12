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

test('Terraform exige imagens reais e rejeita nginx @spec:AC-030', () => {
  const variables = read('infra/aws/terraform/variables.tf');
  const ecs = read('infra/aws/terraform/ecs.tf');
  const locals = read('infra/aws/terraform/locals.tf');

  assert.match(variables, /variable "container_images"/);
  assert.match(variables, /setsubtract\(/);
  assert.match(variables, /strcontains\(lower\(image\), "nginx"\)/);
  assert.match(ecs, /image\s*=\s*var\.container_images\[each\.key\]/);
  assert.doesNotMatch(locals, /default_container_image|nginx/);
});

test('Terraform nao define fallback inseguro para API key @spec:AC-031', () => {
  const variables = read('infra/aws/terraform/variables.tf');
  const example = read('infra/aws/terraform/terraform.tfvars.example');

  const apiKeyBlock = variables.match(/variable "internal_api_key"[\s\S]*?\n}\n/);
  assert.ok(apiKeyBlock);
  assert.doesNotMatch(apiKeyBlock[0], /default\s*=/);
  assert.match(apiKeyBlock[0], /length\(trimspace\(var\.internal_api_key\)\)\s*>=\s*16/);
  assert.match(example, /internal_api_key\s*=\s*"[^"\n]{16,}"/);
  assert.doesNotMatch(example, /change-me-in-secrets-manager/);
});

test('ECS delega health check ao ALB e nao usa wget ou curl @spec:AC-032', () => {
  const ecs = read('infra/aws/terraform/ecs.tf');
  const alb = read('infra/aws/terraform/alb_waf.tf');

  assert.doesNotMatch(ecs, /healthCheck|wget|curl/);
  assert.match(alb, /path\s*=\s*each\.value\.health_path/);
  assert.match(read('infra/aws/terraform/locals.tf'), /health_path\s*=\s*"\/actuator\/health"/);
  assert.match(read('infra/aws/terraform/locals.tf'), /health_path\s*=\s*"\/health"/);
});
