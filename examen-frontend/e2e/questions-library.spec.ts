import { expect, test } from '@playwright/test';
import { apiPath, fulfillJson, seedAuth } from './support';

test('user can manage categories and custom questions', async ({ page }) => {
  await seedAuth(page);

  let nextCategoryId = 2;
  let nextQuestionId = 10;

  let categories = [
    { id: 1, name: 'Reflection', description: 'Core prompts' }
  ];

  let customQuestions: Array<{ id: number; text: string; custom: boolean; category: { id: number; name: string; description: string } }> = [];
  const aiQuestions = [
    { id: 100, text: 'Where did I notice gratitude today?', custom: false, category: categories[0] }
  ];

  await page.on('dialog', dialog => dialog.accept());

  await page.route('**/api/**', async (route) => {
    const method = route.request().method();
    const path = apiPath(route);

    if (path === '/api/categories' && method === 'GET') {
      return fulfillJson(route, { content: categories });
    }

    if (path === '/api/categories' && method === 'POST') {
      const body = route.request().postDataJSON() as { name: string; description: string };
      const created = { id: nextCategoryId++, name: body.name, description: body.description ?? '' };
      categories = [...categories, created];
      return fulfillJson(route, created);
    }

    if (path === '/api/questions' && method === 'GET') {
      return fulfillJson(route, { content: [...aiQuestions, ...customQuestions] });
    }

    if (path === '/api/questions/my' && method === 'GET') {
      return fulfillJson(route, customQuestions);
    }

    if (path === '/api/questions/custom' && method === 'POST') {
      const body = route.request().postDataJSON() as { text: string; categoryId: number };
      const category = categories.find(item => item.id === Number(body.categoryId)) ?? categories[0];
      const created = {
        id: nextQuestionId++,
        text: body.text,
        custom: true,
        category
      };
      customQuestions = [created, ...customQuestions];
      return fulfillJson(route, created);
    }

    return fulfillJson(route, {});
  });

  await page.goto('/questions');

  await page.locator('#categoryName').fill('Gratitude');
  await page.locator('#categoryDescription').fill('Thankfulness prompts');
  await page.getByRole('button', { name: 'Add category' }).click();

  await expect(page.locator('.category-name', { hasText: /^Gratitude$/ })).toBeVisible();

  await page.locator('#text').fill('What did I learn today?');
  await page.selectOption('#categoryId', { label: 'Gratitude' });
  await page.getByRole('button', { name: 'Add question' }).click();

  await expect(page.getByText('What did I learn today?')).toBeVisible();
});
