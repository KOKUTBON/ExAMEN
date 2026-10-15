import { Page, Route } from '@playwright/test';

export const AUTH = {
  userId: 1,
  fullName: 'E2E User',
  email: 'e2e@example.com',
  token: 'e2e-token'
};

export async function seedAuth(page: Page): Promise<void> {
  await page.addInitScript((auth) => {
    localStorage.setItem('auth', JSON.stringify(auth));
    localStorage.setItem('token', auth.token);
  }, AUTH);
}

export async function fulfillJson(route: Route, body: unknown, status = 200): Promise<void> {
  await route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify(body)
  });
}

export function apiPath(route: Route): string {
  return new URL(route.request().url()).pathname;
}
