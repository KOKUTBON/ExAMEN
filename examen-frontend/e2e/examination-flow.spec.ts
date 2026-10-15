import { expect, test } from '@playwright/test';
import { apiPath, fulfillJson, seedAuth } from './support';

test('user can complete an examination session and submit answers', async ({ page }) => {
  await seedAuth(page);

  const category = { id: 1, name: 'Reflection', description: 'Core' };
  const questions = [1, 2, 3, 4, 5].map(index => ({
    id: index,
    text: `Prompt ${index}`,
    orderNumber: index,
    responseType: 'SCALE_1_5',
    custom: false,
    category
  }));

  let activeSession: null | { id: number; startedAt: string } = null;
  const history: Array<Record<string, unknown>> = [];
  let submitted: { answers: Array<Record<string, unknown>>; notes?: string; moodScore?: number } | null = null;

  await page.route('**/api/**', async (route) => {
    const method = route.request().method();
    const path = apiPath(route);

    if (path === '/api/sessions/active' && method === 'GET') {
      return fulfillJson(route, activeSession);
    }

    if (path === '/api/categories' && method === 'GET') {
      return fulfillJson(route, { content: [category] });
    }

    if (path === '/api/questions/category/1' && method === 'GET') {
      return fulfillJson(route, questions);
    }

    if (path === '/api/sessions/start' && method === 'POST') {
      activeSession = { id: 42, startedAt: '2026-03-09T08:00:00Z' };
      return fulfillJson(route, activeSession);
    }

    if (path === '/api/sessions/42/submit' && method === 'POST') {
      submitted = route.request().postDataJSON() as typeof submitted;
      activeSession = null;
      history.unshift({
        id: 42,
        startedAt: '2026-03-09T08:00:00Z',
        completedAt: '2026-03-09T08:10:00Z',
        notes: submitted?.notes ?? '',
        moodScore: submitted?.moodScore ?? 4,
        score: 80,
        answers: submitted?.answers ?? []
      });
      return fulfillJson(route, history[0]);
    }

    if (path === '/api/sessions/me' && method === 'GET') {
      return fulfillJson(route, { content: history });
    }

    if (path === '/api/profile/summary' && method === 'GET') {
      return fulfillJson(route, {
        examinationsCompleted: history.length,
        todosCompleted: 0,
        categoriesUsed: 1,
        streakDays: 1,
        spiritualProgressScore: 70,
        sessionsThisWeek: history.length,
        sessionsThisMonth: history.length,
        averageMoodLast30Days: 4,
        todayMood: 4,
        todayCompleted: history.length > 0,
        recentMoodTrend: []
      });
    }

    if (path === '/api/profile/progress' && method === 'GET') {
      return fulfillJson(route, { points: [] });
    }

    if (path === '/api/profile/summary/weekly' && method === 'GET') {
      return fulfillJson(route, { period: 'week', sessions: history.length, completedDays: history.length, averageMood: 4 });
    }

    if (path === '/api/profile/summary/monthly' && method === 'GET') {
      return fulfillJson(route, { period: 'month', sessions: history.length, completedDays: history.length, averageMood: 4 });
    }

    if (path === '/api/profile/analytics' && method === 'GET') {
      return fulfillJson(route, { overallAverageScore: 80, overallMood: 4, categories: [], weeklyTrend: [] });
    }

    if (path === '/api/growth/weekly-summary' && method === 'GET') {
      return fulfillJson(route, { sessionsCompleted: history.length, todosCompleted: 0, habitsScored: 0, averageHabitScore: 0, gratitudeCount: 0 });
    }

    if (path === '/api/growth/gratitude' && method === 'GET') {
      return fulfillJson(route, []);
    }

    if (path === '/api/growth/habits' && method === 'GET') {
      return fulfillJson(route, []);
    }

    if (path === '/api/growth/meditation-suggestions' && method === 'GET') {
      return fulfillJson(route, ['Breathe slowly']);
    }

    if (path === '/api/insights/summary' && method === 'GET') {
      return fulfillJson(route, {
        summary: 'Good momentum this week.',
        highlights: ['Sessions completed: 1'],
        periodDays: 30,
        sessionsCompleted: 1,
        averageFeeling: 4,
        generatedAt: '2026-03-09T09:00:00Z'
      });
    }

    if (path === '/api/insights/questions' && method === 'POST') {
      return fulfillJson(route, {
        focus: 'gratitude',
        suggestions: ['Where did I notice gratitude today?'],
        generatedAt: '2026-03-09T09:00:00Z'
      });
    }

    if (path === '/api/insights/session' && method === 'POST') {
      return fulfillJson(route, {
        sessionId: 42,
        summary: 'Strong session',
        insights: ['Consistent focus'],
        nextSteps: ['Continue tomorrow'],
        averageFeeling: 4,
        generatedAt: '2026-03-09T09:00:00Z'
      });
    }

    if (path === '/api/notifications' && method === 'GET') {
      return fulfillJson(route, []);
    }

    return fulfillJson(route, {});
  });

  await page.goto('/examination');

  await page.getByRole('button', { name: /Start today's examen/ }).click();
  await expect(page.getByText('Session in progress')).toBeVisible();

  for (let i = 0; i < 5; i += 1) {
    const card = page.locator('.question-card').nth(i);
    await card.click();
    await card.locator('textarea').fill(`Reflection ${i + 1}`);
    await card.locator('.feeling-block .mood-picker button', { hasText: '4' }).click();
    await card.getByRole('button', { name: /Submit response|Update response/ }).click();
  }

  await expect.poll(() => submitted?.answers?.length ?? 0).toBe(5);
  await expect(page).toHaveURL(/\/profile$/);
  await expect(page.getByRole('heading', { name: 'Your last 30 days' })).toBeVisible();
});
