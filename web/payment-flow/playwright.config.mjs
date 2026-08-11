import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  testMatch: '**/*.integration.spec.mjs',
  timeout: 30_000,
  reporter: [
    ['list'],
    ['junit', { outputFile: 'test-results/payment-flow-integration.xml' }],
  ],
  use: {
    baseURL: 'http://127.0.0.1:4173',
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
  },
});
