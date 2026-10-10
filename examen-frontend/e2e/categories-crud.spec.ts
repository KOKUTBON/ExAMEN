import { expect, test } from '@playwright/test';
import { apiPath, fulfillJson, seedAuth } from './support';

test('user can create, edit, and delete categories from category pages', async ({ page }) => {
  await seedAuth(page);

  let nextId = 2;
  let categories = [
    {
      id: 1,
      name: 'Reflection',
      description: 'Core prompts'
    }
  ];

  await page.on('dialog', (dialog) => dialog.accept());

  await page.route('**/api/**', async (route) => {
    const method = route.request().method();
    const path = apiPath(route);

    if (path === '/api/categories' && method === 'GET') {
      return fulfillJson(route, {
        content: categories,
        totalElements: categories.length,
        totalPages: 1,
        size: 200,
        number: 0
      });
    }

    if (path === '/api/categories' && method === 'POST') {
      const body = route.request().postDataJSON() as { name: string; description?: string };
      const created = {
        id: nextId++,
        name: body.name,
        description: body.description ?? ''
      };
      categories = [...categories, created];
      return fulfillJson(route, created);
    }

    const byIdMatch = path.match(/^\/api\/categories\/(\d+)$/);
    if (byIdMatch && method === 'GET') {
      const id = Number(byIdMatch[1]);
      const category = categories.find((item) => item.id === id);
      return fulfillJson(route, category ?? {}, category ? 200 : 404);
    }

    if (byIdMatch && method === 'PUT') {
      const id = Number(byIdMatch[1]);
      const body = route.request().postDataJSON() as { name: string; description?: string };
      categories = categories.map((item) =>
        item.id === id
          ? {
              ...item,
              name: body.name,
              description: body.description ?? ''
            }
          : item
      );
      const updated = categories.find((item) => item.id === id);
      return fulfillJson(route, updated ?? {}, updated ? 200 : 404);
    }

    if (byIdMatch && method === 'DELETE') {
      const id = Number(byIdMatch[1]);
      categories = categories.filter((item) => item.id !== id);
      return fulfillJson(route, {}, 204);
    }

    return fulfillJson(route, {});
  });

  await page.goto('/categories');

  await expect(page.getByRole('heading', { name: 'Organize your reflections' })).toBeVisible();

  await page.getByRole('button', { name: 'New category' }).click();
  await expect(page).toHaveURL(/\/categories\/create$/);

  await page.locator('#name').fill('Prayer');
  await page.locator('#description').fill('Prayer-focused prompts');
  await page.getByRole('button', { name: 'Create category' }).click();

  await expect(page).toHaveURL(/\/categories$/);
  const createdRow = page.locator('tr', { hasText: 'Prayer' });
  await expect(createdRow).toBeVisible();

  await createdRow.getByRole('button', { name: 'Edit' }).click();
  await expect(page).toHaveURL(/\/categories\/edit\/\d+$/);

  await page.locator('#name').fill('Prayer and Silence');
  await page.locator('#description').fill('Updated category description');
  await page.getByRole('button', { name: 'Save changes' }).click();

  await expect(page).toHaveURL(/\/categories$/);
  const updatedRow = page.locator('tr', { hasText: 'Prayer and Silence' });
  await expect(updatedRow).toBeVisible();

  await updatedRow.getByRole('button', { name: 'Delete' }).click();
  await expect(page.locator('tr', { hasText: 'Prayer and Silence' })).toHaveCount(0);
});
