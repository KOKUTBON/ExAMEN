import { expect, test } from '@playwright/test';
import { apiPath, fulfillJson, seedAuth } from './support';

test('profile page integrates growth, insights, and notifications interactions', async ({ page }) => {
  await seedAuth(page);

  let notifications = [
    {
      id: 1,
      title: 'Daily reminder',
      message: 'Time for your examination.',
      type: 'REMINDER',
      createdAt: '2026-03-09T08:00:00Z',
      readAt: null as string | null
    }
  ];

  await page.route('**/api/**', async (route) => {
    const method = route.request().method();
    const path = apiPath(route);

    if (path === '/api/profile/summary' && method === 'GET') {
      return fulfillJson(route, {
        examinationsCompleted: 3,
        todosCompleted: 4,
        categoriesUsed: 2,
        streakDays: 2,
        spiritualProgressScore: 68,
        sessionsThisWeek: 2,
        sessionsThisMonth: 3,
        averageMoodLast30Days: 4,
        todayMood: 4,
        todayCompleted: true,
        recentMoodTrend: []
      });
    }

    if (path === '/api/sessions/me' && method === 'GET') {
      return fulfillJson(route, {
        content: [{ id: 7, startedAt: '2026-03-09T07:00:00Z', completedAt: '2026-03-09T07:15:00Z', notes: 'Solid', score: 80 }]
      });
    }

    if (path === '/api/profile/progress' && method === 'GET') {
      return fulfillJson(route, { points: [] });
    }

    if (path === '/api/profile/summary/weekly' && method === 'GET') {
      return fulfillJson(route, { period: 'week', sessions: 2, completedDays: 2, averageMood: 4 });
    }

    if (path === '/api/profile/summary/monthly' && method === 'GET') {
      return fulfillJson(route, { period: 'month', sessions: 3, completedDays: 3, averageMood: 4 });
    }

    if (path === '/api/profile/analytics' && method === 'GET') {
      return fulfillJson(route, { overallAverageScore: 80, overallMood: 4, categories: [], weeklyTrend: [] });
    }

    if (path === '/api/growth/weekly-summary' && method === 'GET') {
      return fulfillJson(route, { sessionsCompleted: 2, todosCompleted: 1, habitsScored: 1, averageHabitScore: 4, gratitudeCount: 1 });
    }

    if (path === '/api/growth/gratitude' && method === 'GET') {
      return fulfillJson(route, []);
    }

    if (path === '/api/growth/habits' && method === 'GET') {
      return fulfillJson(route, []);
    }

    if (path === '/api/growth/meditation-suggestions' && method === 'GET') {
      return fulfillJson(route, ['Take 5 minutes to breathe']);
    }

    if (path === '/api/growth/export/pdf' && method === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/pdf',
        body: '%PDF-1.4\n% Mock PDF\n'
      });
      return;
    }

    if (path === '/api/insights/summary' && method === 'GET') {
      return fulfillJson(route, {
        summary: 'You are consistent this week.',
        highlights: ['Sessions completed: 2'],
        periodDays: 30,
        sessionsCompleted: 2,
        averageFeeling: 4,
        generatedAt: '2026-03-09T09:00:00Z'
      });
    }

    if (path === '/api/insights/questions' && method === 'POST') {
      const body = route.request().postDataJSON() as { focus?: string };
      const focus = body.focus?.trim() || 'gratitude';
      return fulfillJson(route, {
        focus,
        suggestions: [`Where did I practice ${focus} today?`],
        generatedAt: '2026-03-09T09:00:00Z'
      });
    }

    if (path === '/api/insights/session' && method === 'POST') {
      return fulfillJson(route, {
        sessionId: 7,
        summary: 'Great session quality.',
        insights: ['Clear reflection'],
        nextSteps: ['Repeat the same rhythm tomorrow'],
        averageFeeling: 4,
        generatedAt: '2026-03-09T09:00:00Z'
      });
    }

    if (path === '/api/insights/weekly-narrative' && method === 'GET') {
      return fulfillJson(route, {
        title: 'Weekly Reflection',
        narrative: 'A peaceful and reflective week.',
        periodStart: '2026-03-02',
        periodEnd: '2026-03-09',
        sessionsAnalyzed: 2,
        virtuesObserved: ['Patience'],
        strugglesIdentified: [],
        gratitudeThemes: ['Family'],
        stressorSignals: [],
        actionableAdvice: ['Continue daily reflection.'],
        generatedAt: '2026-03-09T09:00:00Z'
      });
    }

    if (path === '/api/insights/tags/summary' && method === 'GET') {
      return fulfillJson(route, {
        periodDays: 30,
        totalTags: 1,
        virtues: [{ tag: 'Patience', count: 1 }],
        struggles: [],
        gratitude: [],
        stressors: [],
        generatedAt: '2026-03-09T09:00:00Z'
      });
    }

    if (path === '/api/notifications' && method === 'GET') {
      return fulfillJson(route, notifications);
    }

    const markReadMatch = path.match(/^\/api\/notifications\/(\d+)\/read$/);
    if (markReadMatch && method === 'PATCH') {
      const id = Number(markReadMatch[1]);
      notifications = notifications.map(item => item.id === id
        ? { ...item, readAt: '2026-03-09T09:05:00Z' }
        : item);
      return fulfillJson(route, {}, 204);
    }

    return fulfillJson(route, {});
  });

  await page.goto('/profile');
  await expect(page.getByRole('heading', { name: 'Your last 30 days' })).toBeVisible();
  await expect(page.getByText('AI insights')).toBeVisible();

  await page.getByPlaceholder('Focus area (gratitude, prayer, relationships...)').fill('service');
  await page.getByRole('button', { name: 'Refresh suggestions' }).click();
  await expect(page.getByText('Where did I practice service today?')).toBeVisible();

  await page.getByRole('button', { name: 'Mark read' }).click();
  await expect(page.getByText('Read')).toBeVisible();

  await Promise.all([
    page.waitForRequest(request => request.url().includes('/api/growth/export/pdf')),
    page.getByRole('button', { name: 'Export PDF' }).click()
  ]);
});
