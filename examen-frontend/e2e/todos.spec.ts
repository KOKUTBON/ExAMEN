import { expect, test } from '@playwright/test';
import { apiPath, fulfillJson, seedAuth } from './support';

test('user can create, toggle, and delete todos', async ({ page }) => {
  await seedAuth(page);
  page.on('dialog', dialog => dialog.accept());

  let nextId = 1;
  let todos: Array<{ id: number; title: string; completed: boolean; dueAt: string }> = [];

  await page.route('**/api/**', async (route) => {
    const method = route.request().method();
    const path = apiPath(route);

    if (path === '/api/todos' && method === 'GET') {
      return fulfillJson(route, todos);
    }

    if (path === '/api/todos' && method === 'POST') {
      const body = route.request().postDataJSON() as { title: string; dueAt: string };
      const todo = { id: nextId++, title: body.title, completed: false, dueAt: body.dueAt };
      todos = [...todos, todo];
      return fulfillJson(route, todo);
    }

    const toggleMatch = path.match(/^\/api\/todos\/(\d+)\/toggle$/);
    if (toggleMatch && method === 'PATCH') {
      const id = Number(toggleMatch[1]);
      todos = todos.map(todo => todo.id === id ? { ...todo, completed: !todo.completed } : todo);
      const updated = todos.find(todo => todo.id === id);
      return fulfillJson(route, updated ?? {});
    }

    const deleteMatch = path.match(/^\/api\/todos\/(\d+)$/);
    if (deleteMatch && method === 'DELETE') {
      const id = Number(deleteMatch[1]);
      todos = todos.filter(todo => todo.id !== id);
      return fulfillJson(route, {}, 204);
    }

    return fulfillJson(route, {});
  });

  await page.goto('/todos');

  const title = 'Finish evening reflection';
  await page.getByPlaceholder('Add a todo').fill(title);
  await page.locator('input[type="datetime-local"]').fill('2030-01-02T10:00');
  await page.getByRole('button', { name: 'Add' }).click();

  const row = page.locator('.todo-item', { hasText: title });
  await expect(row).toBeVisible();

  await row.click();
  await expect(row.getByText('Done')).toBeVisible();

  await row.getByRole('button', { name: 'Delete' }).click();
  await expect(row).toHaveCount(0);
});
