import { test, expect } from '@playwright/test';
import { startPaymentFlowServer } from '../test-server.mjs';

let server;

test.beforeAll(async () => {
  server = await startPaymentFlowServer(4173);
});

test.afterAll(async () => {
  await new Promise((resolve) => server.close(resolve));
});

test('operator completes payment flow and starts a new payment @spec:AC-016', async ({ page }) => {
  await page.goto('/index.html');

  await expect(page.getByRole('heading', { name: 'Criar pagamento' })).toBeVisible();
  await page.locator('#description').fill('Pedido #4821');
  await page.locator('#amount').fill('149,90');
  await page.locator('#customer').fill('Cliente de teste');
  await page.getByRole('button', { name: /Continuar/ }).click();

  await expect(page.getByRole('heading', { name: 'Meio de pagamento', exact: true })).toBeVisible();
  await page.getByRole('radio', { name: /Cartão/ }).click();
  await expect(page.locator('#method-detail strong')).toHaveText('Cartão selecionado');
  await page.getByRole('button', { name: /Continuar/ }).click();

  await expect(page.getByRole('heading', { name: 'Revisão do pagamento', exact: true })).toBeVisible();
  await expect(page.locator('[data-review="description"]')).toHaveText('Pedido #4821');
  await expect(page.locator('[data-review="amount"]')).toHaveText('R$ 149,90');
  await expect(page.locator('[data-review="method"]')).toHaveText('Cartão');
  await page.getByRole('button', { name: /Continuar/ }).click();

  await expect(page.getByRole('heading', { name: 'Operação iniciada' })).toBeVisible();
  await expect(page.locator('#operation-id')).toHaveText(/^PAY-\d{6}$/);
  await page.getByRole('button', { name: /Criar novo pagamento/ }).click();
  await expect(page.getByRole('heading', { name: 'Dados do pagamento' })).toBeVisible();
  await expect(page.locator('#description')).toHaveValue('');
});

test('screen reflows without horizontal overflow on narrow viewport @spec:AC-012', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/index.html');

  const layout = await page.evaluate(() => ({
    documentWidth: document.documentElement.scrollWidth,
    viewportWidth: window.innerWidth,
    columns: getComputedStyle(document.querySelector('.workspace-grid')).gridTemplateColumns,
  }));

  expect(layout.documentWidth).toBeLessThanOrEqual(layout.viewportWidth);
  expect(layout.columns.trim().split(/\s+/)).toHaveLength(1);
});
