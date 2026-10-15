import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { InsightsService } from './insights.service';
import { ApiConfigService } from '../../../../core/services/api-config.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

describe('InsightsService', () => {
  let service: InsightsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        InsightsService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub }
      ]
    });

    service = TestBed.inject(InsightsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads summary with confidence score and evidence snippets', () => {
    service.getSummary(21).subscribe(res => {
      expect(res.confidenceScore).toBe(0.85);
      expect(res.evidenceSnippets?.length).toBe(1);
      expect(res.evidenceSnippets?.[0].source).toBe('SESSION_REFLECTION');
      expect(res.evidenceSnippets?.[0].snippet).toBe('Felt peace during morning silence.');
    });

    const req = httpMock.expectOne(request => request.url === 'http://test/api/insights/summary');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('days')).toBe('21');
    req.flush({
      summary: 'ok',
      highlights: [],
      periodDays: 21,
      sessionsCompleted: 4,
      averageFeeling: 4.2,
      confidenceScore: 0.85,
      evidenceSnippets: [
        {
          source: 'SESSION_REFLECTION',
          date: '2026-03-08T10:00:00Z',
          snippet: 'Felt peace during morning silence.',
          rationale: 'Demonstrates recurring contemplative peace'
        }
      ],
      generatedAt: '2026-03-09T00:00:00Z'
    });
  });

  it('records profile summary feedback', () => {
    service.recordFeedback({
      targetType: 'PROFILE_SUMMARY',
      targetId: 'summary-30d',
      helpful: true,
      comments: 'Accurate summary'
    }).subscribe(res => {
      expect(res.helpful).toBe(true);
      expect(res.targetType).toBe('PROFILE_SUMMARY');
    });

    const req = httpMock.expectOne('http://test/api/insights/feedback');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.targetType).toBe('PROFILE_SUMMARY');
    expect(req.request.body.targetId).toBe('summary-30d');
    expect(req.request.body.helpful).toBe(true);
    req.flush({
      id: 1,
      targetType: 'PROFILE_SUMMARY',
      targetId: 'summary-30d',
      helpful: true,
      comments: 'Accurate summary',
      createdAt: '2026-03-09T00:00:00Z'
    });
  });

  it('suggests questions', () => {
    service.suggestQuestions('gratitude', 3).subscribe();

    const req = httpMock.expectOne('http://test/api/insights/questions');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.focus).toBe('gratitude');
    expect(req.request.body.count).toBe(3);
    req.flush({ focus: 'gratitude', suggestions: ['q1'], generatedAt: '2026-03-09T00:00:00Z' });
  });

  it('analyzes a session', () => {
    service.analyzeSession(9).subscribe();

    const req = httpMock.expectOne('http://test/api/insights/session');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.sessionId).toBe(9);
    req.flush({ sessionId: 9, summary: 'ok', insights: [], nextSteps: [], averageFeeling: 4, generatedAt: '2026-03-09T00:00:00Z' });
  });

  it('loads follow-up prompts', () => {
    service.getFollowUpPrompts(4).subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].question).toContain('prayer');
      expect(res[0].sourceContext).toBe('ACTIVE_GOAL');
    });

    const req = httpMock.expectOne('http://test/api/insights/follow-ups?count=4');
    expect(req.request.method).toBe('GET');
    req.flush([
      {
        question: 'How can you protect time for prayer today?',
        rationale: 'Follows up on your pending evening prayer goal',
        sourceContext: 'ACTIVE_GOAL',
        targetFocus: 'Prayer'
      }
    ]);
  });
});
