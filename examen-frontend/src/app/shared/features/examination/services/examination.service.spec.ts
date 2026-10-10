import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ExaminationService } from './examination.service';
import { ApiConfigService } from '../../../../core/services/api-config.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

describe('ExaminationService', () => {
  let service: ExaminationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ExaminationService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub }
      ]
    });

    service = TestBed.inject(ExaminationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('starts a session', () => {
    service.startSession().subscribe();

    const req = httpMock.expectOne('http://test/api/sessions/start');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, startedAt: '2026-03-09T00:00:00Z' });
  });

  it('submits a session payload', () => {
    service.submitSession(5, [
      {
        questionId: 1,
        answerText: '4',
        correct: false,
        examinationSessionId: 5,
        reflectionText: 'Reflection',
        feelingScore: 4
      }
    ], 'Note', 4).subscribe();

    const req = httpMock.expectOne('http://test/api/sessions/5/submit');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.answers.length).toBe(1);
    expect(req.request.body.notes).toBe('Note');
    req.flush({ id: 5 });
  });

  it('loads categories and questions by category', () => {
    service.getCategories(100).subscribe();
    const catReq = httpMock.expectOne(request => request.url === 'http://test/api/categories');
    expect(catReq.request.method).toBe('GET');
    expect(catReq.request.params.get('size')).toBe('100');
    catReq.flush({ content: [] });

    service.getQuestionsByCategory(7).subscribe();
    const qReq = httpMock.expectOne('http://test/api/questions/category/7');
    expect(qReq.request.method).toBe('GET');
    qReq.flush([]);
  });
});
