import { expect, test } from '@playwright/test';

test('demo works under the Pages path, persists edits, and makes no API calls', async ({ page }) => {
  const apiRequests: string[] = [];
  const errors: string[] = [];
  page.on('request', request => {
    if (/\/(?:api|demo-api)\//.test(request.url())) apiRequests.push(request.url());
  });
  page.on('pageerror', error => errors.push(error.message));
  await page.goto('/ExAMEN/');
  await expect(page.getByText('Interactive demo', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: 'Explore demo' }).click();
  await expect(page).toHaveURL(/\/ExAMEN\/#\/dashboard$/);
  for (const route of ['todos', 'categories', 'questions', 'profile', 'settings', 'journal']) {
    await page.goto(`/ExAMEN/#/${route}`);
    await expect(page.locator('main h1')).toBeVisible();
  }
  await page.locator('#entry').fill('A browser-only demo note');
  await page.getByRole('button', { name: 'Save entry' }).click();
  await expect(page.getByText('A browser-only demo note', { exact: true })).toBeVisible();
  await page.reload();
  await expect(page.getByText('A browser-only demo note', { exact: true })).toBeVisible();
  await page.goto('/ExAMEN/#/settings');
  await expect(page.locator('#emailReminder')).toBeDisabled();
  const download = page.waitForEvent('download');
  await page.getByRole('button', { name: 'Download my data (JSON)' }).click();
  expect((await download).suggestedFilename()).toMatch(/\.json$/);
  await page.evaluate(() => localStorage.setItem('unrelated-key', 'keep'));
  await page.getByRole('button', { name: 'Reset demo', exact: true }).click();
  await expect(page).toHaveURL(/#\/dashboard$/);
  await page.goto('/ExAMEN/#/journal');
  await expect(page.getByText('A browser-only demo note', { exact: true })).toHaveCount(0);
  expect(await page.evaluate(() => localStorage.getItem('unrelated-key'))).toBe('keep');
  expect(apiRequests).toEqual([]);
  expect(errors).toEqual([]);
});

test('a demo examination can be completed and survives a deep-link refresh', async ({ page }) => {
  await page.goto('/ExAMEN/#/examination');
  await page.reload();
  await page.getByRole('button', { name: /Start today's examen/ }).click();
  await expect(page.getByText('Session in progress')).toBeVisible();
  for (let i = 0; i < 5; i++) {
    const card = page.locator('.question-card').nth(i);
    await card.click();
    await card.locator('textarea').fill(`Sample reflection ${i + 1}`);
    await card.locator('.feeling-block .mood-picker button', { hasText: '4' }).click();
    await card.getByRole('button', { name: /Submit response|Update response/ }).click();
  }
  await expect(page).toHaveURL(/#\/profile$/);
  await expect(page.getByRole('heading', { name: 'Your last 30 days' })).toBeVisible();
  expect(await page.evaluate(() => JSON.parse(localStorage.getItem('examen-demo:data')!).sessions[0].answers.length)).toBe(5);
});

test('demo library and todos support local edits, including on a narrow screen', async ({ page }) => {
  page.on('dialog', dialog => dialog.accept());
  await page.goto('/ExAMEN/#/questions');
  await page.locator('#categoryName').fill('Demo category');
  await page.locator('#categoryDescription').fill('Sample prompts');
  await page.getByRole('button', { name: 'Add category' }).click();
  await page.locator('#text').fill('What did I learn in this demo?');
  await page.selectOption('#categoryId', { label: 'Demo category' });
  await page.getByRole('button', { name: 'Add question' }).click();
  await expect(page.getByText('What did I learn in this demo?', { exact: true })).toBeVisible();
  await page.reload();
  await expect(page.getByText('What did I learn in this demo?', { exact: true })).toBeVisible();
  await page.goto('/ExAMEN/#/categories');
  const row = page.locator('tr', { hasText: 'Demo category' });
  await row.getByRole('button', { name: 'Edit', exact: true }).click();
  await page.locator('#name').fill('Renamed demo category');
  await page.getByRole('button', { name: 'Save changes' }).click();
  await expect(page.locator('tr', { hasText: 'Renamed demo category' })).toBeVisible();
  await page.goto('/ExAMEN/#/todos');
  await page.getByPlaceholder('Add a todo').fill('Explore browser-local data');
  await page.locator('input[type="datetime-local"]').fill('2030-01-02T10:00');
  await page.getByRole('button', { name: 'Add', exact: true }).click();
  const todo = page.locator('.todo-item', { hasText: 'Explore browser-local data' });
  await todo.click();
  await expect(todo.getByText('Done', { exact: true })).toBeVisible();
  await page.reload();
  await expect(todo.getByText('Done', { exact: true })).toBeVisible();
  await todo.getByRole('button', { name: 'Delete', exact: true }).click();
  await expect(todo).toHaveCount(0);
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto('/ExAMEN/#/dashboard');
  await expect(page.getByRole('heading', { name: 'Examination of Conscience', exact: true })).toBeVisible();
  await expect.poll(() => page.evaluate(() => window.scrollY)).toBe(0);
  await page.screenshot({ path: '/tmp/examen-demo-desktop.png' });
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.getByRole('button', { name: 'Explore demo' })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
  await page.screenshot({ path: '/tmp/examen-demo-mobile.png' });
});
