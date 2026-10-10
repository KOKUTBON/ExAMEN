import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e-demo',
  workers: 1,
  timeout: 30_000,
  use: { baseURL: 'http://127.0.0.1:4300', trace: 'on-first-retry' },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run preview:demo',
    url: 'http://127.0.0.1:4300/ExAMEN/',
    reuseExistingServer: !process.env.CI,
    timeout: 30_000
  }
});
