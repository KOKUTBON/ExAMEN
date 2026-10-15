import { expect, test } from '@playwright/test';
import { apiPath, fulfillJson, AUTH } from './support';

test('user can sign in and reach dashboard', async ({ page }) => {
  await page.route('**/api/**', async (route) => {
    const method = route.request().method();
    const path = apiPath(route);

    if (method === 'POST' && path === '/api/auth/login') {
      return fulfillJson(route, AUTH);
    }

    if (method === 'GET' && path === '/api/profile/summary') {
      return fulfillJson(route, {
        examinationsCompleted: 2,
        todosCompleted: 1,
        categoriesUsed: 2,
        streakDays: 1,
        spiritualProgressScore: 62,
        sessionsThisWeek: 1,
        sessionsThisMonth: 2,
        averageMoodLast30Days: 4,
        todayMood: null,
        todayCompleted: false,
        recentMoodTrend: []
      });
    }

    if (method === 'GET' && path === '/api/sessions/active') {
      return fulfillJson(route, null);
    }

    if (method === 'GET' && path === '/api/todos') {
      return fulfillJson(route, []);
    }

    if (method === 'GET' && path === '/api/questions') {
      return fulfillJson(route, { content: [] });
    }

    if (method === 'GET' && path === '/api/questions/my') {
      return fulfillJson(route, []);
    }

    if (method === 'GET' && path === '/api/categories') {
      return fulfillJson(route, { content: [] });
    }

    return fulfillJson(route, {});
  });

  await page.goto('/auth/login');
  await page.locator('#email').fill('e2e@example.com');
  await page.locator('#password').fill('Password123!');
  await page.getByRole('button', { name: 'Sign in' }).click();

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole('heading', { name: 'Examination of Conscience' })).toBeVisible();
});
