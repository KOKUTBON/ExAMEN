import { expect, test } from '@playwright/test';
import { apiPath, fulfillJson, seedAuth } from './support';

test('user can update reminder settings', async ({ page }) => {
  await seedAuth(page);

  let settings = {
    timeZone: 'Europe/Rome',
    reminderTime: '08:30',
    theme: 'system',
    emailReminder: false,
    inAppReminder: false
  };

  await page.route('**/api/**', async (route) => {
    const method = route.request().method();
    const path = apiPath(route);

    if (path === '/api/settings' && method === 'GET') {
      return fulfillJson(route, settings);
    }

    if (path === '/api/settings' && method === 'PUT') {
      settings = route.request().postDataJSON() as typeof settings;
      return fulfillJson(route, settings);
    }

    return fulfillJson(route, {});
  });

  await page.goto('/settings');

  await page.locator('#reminder').fill('09:45');
  await page.selectOption('#theme', 'dark');

  const emailReminder = page.getByRole('checkbox', { name: 'Email reminders' });
  const appReminder = page.getByRole('checkbox', { name: 'In-app reminders' });
  await emailReminder.check();
  await appReminder.check();

  await page.getByRole('button', { name: 'Save settings' }).click();

  await expect.poll(() => settings.timeZone).toBe('Europe/Rome');
  await expect.poll(() => settings.reminderTime).toBe('09:45');
  await expect.poll(() => settings.theme).toBe('dark');
  await expect.poll(() => settings.emailReminder).toBe(true);
  await expect.poll(() => settings.inAppReminder).toBe(true);
});
