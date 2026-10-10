import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ProfileService } from './profile.service';
import { ApiConfigService } from '../../../../core/services/api-config.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

describe('ProfileService', () => {
  let service: ProfileService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProfileService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub }
      ]
    });

    service = TestBed.inject(ProfileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads summary', () => {
    service.getSummaryForLast30Days().subscribe();

    const req = httpMock.expectOne('http://test/api/profile/summary');
    expect(req.request.method).toBe('GET');
    req.flush({ examinationsCompleted: 0, todosCompleted: 0, categoriesUsed: 0, streakDays: 0 });
  });

  it('loads progress with days parameter', () => {
    service.getProgress(10).subscribe();

    const req = httpMock.expectOne(request => request.url === 'http://test/api/profile/progress');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('days')).toBe('10');
    req.flush({ points: [] });
  });

  it('loads analytics with advanced score and anomaly metrics', () => {
    service.getAnalytics().subscribe(res => {
      expect(res.advancedScore?.compositeScore).toBe(84.5);
      expect(res.advancedScore?.confidenceLower).toBe(79.5);
      expect(res.advancedScore?.confidenceUpper).toBe(89.5);
      expect(res.anomalies?.length).toBe(1);
      expect(res.qualityMetrics?.sentimentDriftIndicator).toBe('STABLE');
    });

    const req = httpMock.expectOne('http://test/api/profile/analytics');
    expect(req.request.method).toBe('GET');
    req.flush({
      overallAverageScore: 82.0,
      overallMood: 4.1,
      categories: [],
      weeklyTrend: [],
      advancedScore: {
        compositeScore: 84.5,
        consistencyScore: 88.0,
        moodStabilityScore: 82.0,
        virtueStruggleRatio: 80.0,
        goalExecutionScore: 85.0,
        confidenceLower: 79.5,
        confidenceUpper: 89.5,
        confidenceLevel: 0.95,
        marginOfError: 5.0,
        sampleSize: 12,
        scoringRationale: 'Synthesized from 12 reflections'
      },
      anomalies: [
        {
          type: 'MOOD_DROP',
          severity: 'MEDIUM',
          title: 'Noticeable Drop in Feeling',
          description: 'Latest session was below baseline',
          detectedDate: '2026-03-22'
        }
      ],
      qualityMetrics: {
        feedbackSatisfactionRate: 0.9,
        totalFeedbackCount: 10,
        sentimentDriftIndicator: 'STABLE',
        evaluatedPeriodDays: 30
      }
    });
  });
});
