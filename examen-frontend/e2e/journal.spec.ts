import { expect, test } from '@playwright/test';
import { apiPath, fulfillJson, seedAuth } from './support';

test('user can create and view journal entries', async ({ page }) => {
  await seedAuth(page);

  let nextId = 2;
  let entries = [
    {
      id: 1,
      content: 'Initial reflection',
      createdAt: '2026-03-08T20:00:00Z'
    }
  ];

  await page.route('**/api/**', async (route) => {
    const method = route.request().method();
    const path = apiPath(route);

    if (path === '/api/journal' && method === 'GET') {
      return fulfillJson(route, entries);
    }

    if (path === '/api/journal' && method === 'POST') {
      const body = route.request().postDataJSON() as { content: string };
      const created = {
        id: nextId++,
        content: body.content,
        createdAt: '2026-03-09T10:00:00Z'
      };
      entries = [created, ...entries];
      return fulfillJson(route, created);
    }

    return fulfillJson(route, {});
  });

  await page.goto('/journal');

  await expect(page.getByRole('heading', { name: 'Capture your reflections' })).toBeVisible();
  await expect(page.getByText('Initial reflection')).toBeVisible();

  await page.locator('#entry').fill('A new daily journal note');
  await page.getByRole('button', { name: 'Save entry' }).click();

  await expect(page.getByText('A new daily journal note')).toBeVisible();
});
