import assert from 'node:assert/strict';
import { existsSync, readdirSync, readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import test from 'node:test';

const testDir = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(testDir, '..', '..');
const terraformDir = path.join(rootDir, 'infra', 'aws', 'terraform');

function read(relativePath) {
  return readFileSync(path.join(rootDir, relativePath), 'utf8');
}

function terraformFiles() {
  return readdirSync(terraformDir)
    .filter((entry) => entry.endsWith('.tf') || entry.endsWith('.md') || entry.endsWith('.example'))
    .map((entry) => path.join(terraformDir, entry));
}

test('Terraform AWS esta em infra/aws/terraform e a raiz antiga nao existe @spec:AC-001', () => {
  assert.equal(existsSync(terraformDir), true);
  assert.equal(existsSync(path.join(terraformDir, 'versions.tf')), true);
  assert.equal(existsSync(path.join(rootDir, 'terraform-payment-architecture')), false);
});

test('ECS e ECR declaram somente os tres microsservicos reais da PoC @spec:AC-002', () => {
  const content = [
    read('infra/aws/terraform/locals.tf'),
    read('infra/aws/terraform/variables.tf'),
    read('infra/aws/terraform/ecs.tf'),
    read('infra/aws/terraform/ecr.tf'),
  ].join('\n');

  for (const service of ['payment-orchestrator-java', 'pix-boleto-kotlin', 'card-payment-csharp']) {
    assert.match(content, new RegExp(service));
  }

  for (const legacyName of ['billing-ws', 'billing-core', 'outbox-publisher', 'refund-worker', 'upgrade-plan-worker', 'worker-account', 'worker-fiscal', 'worker-invoice']) {
    assert.doesNotMatch(content, new RegExp(legacyName));
  }
});

test('Terraform provisiona PostgreSQL, DocumentDB e Amazon MQ sem filas genericas @spec:AC-003', () => {
  assert.match(read('infra/aws/terraform/rds.tf'), /resource\s+"aws_db_instance"/);
  assert.match(read('infra/aws/terraform/documentdb.tf'), /resource\s+"aws_docdb_cluster"/);
  assert.match(read('infra/aws/terraform/rabbitmq.tf'), /resource\s+"aws_mq_broker"/);

  const content = terraformFiles().map((file) => readFileSync(file, 'utf8')).join('\n').toLowerCase();
  for (const forbidden of ['aws_sqs_queue', 'aws_cloudwatch_event_bus', 'aws_scheduler', 'billing-', 'refund', 'upgrade-plan', 'worker-']) {
    assert.equal(content.includes(forbidden), false, `dependencia legada encontrada: ${forbidden}`);
  }
});

test('Task definitions exportam configuracao para Spring e .NET @spec:AC-004', () => {
  const content = read('infra/aws/terraform/ecs.tf');
  for (const variableName of [
    'SPRING_DATASOURCE_URL',
    'SPRING_DATASOURCE_USERNAME',
    'SPRING_DATASOURCE_PASSWORD',
    'SPRING_RABBITMQ_HOST',
    'SPRING_RABBITMQ_PORT',
    'SPRING_RABBITMQ_USERNAME',
    'SPRING_RABBITMQ_PASSWORD',
    'PAYMENTS_SECURITY_API_KEY',
    'ConnectionStrings__Postgres',
    'ConnectionStrings__Mongo',
    'RabbitMq__Host',
    'RabbitMq__Port',
    'RabbitMq__Username',
    'RabbitMq__Password',
    'Security__ApiKey',
  ]) {
    assert.match(content, new RegExp(variableName));
  }
});

test('ALB roteia as rotas de pagamentos para os microsservicos corretos @spec:AC-005', () => {
  const content = [
    read('infra/aws/terraform/alb_waf.tf'),
    read('infra/aws/terraform/locals.tf'),
  ].join('\n');
  for (const route of ['/payments*', '/reconciliation*', '/bank-rail*', '/cards*', '/webhooks*']) {
    assert.match(content, new RegExp(route.replace('*', '\\*')));
  }
  assert.match(content, /aws_lb_listener_rule/);
});

test('Documentacao AWS aponta para a estrutura nova e nao para workloads legados @spec:AC-006', () => {
  const content = [
    read('README.md'),
    read('docs/architecture.md'),
    read('infra/aws/terraform/README.md'),
    read('infra/aws/terraform/terraform.tfvars.example'),
  ].join('\n').toLowerCase();

  assert.match(content, /infra\/aws\/terraform/);
  for (const forbidden of ['billing-ws', 'refund-worker', 'upgrade-plan-worker', 'worker-account']) {
    assert.equal(content.includes(forbidden), false, `documentacao legada encontrada: ${forbidden}`);
  }
});
