import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { GrowthService } from './growth.service';
import { ApiConfigService } from '../../../../core/services/api-config.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

describe('GrowthService', () => {
  let service: GrowthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        GrowthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub }
      ]
    });

    service = TestBed.inject(GrowthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads weekly summary', () => {
    service.getWeeklySummary().subscribe();

    const req = httpMock.expectOne('http://test/api/growth/weekly-summary');
    expect(req.request.method).toBe('GET');
    req.flush({ sessionsCompleted: 1, todosCompleted: 2, habitsScored: 1, averageHabitScore: 4.0, gratitudeCount: 1 });
  });

  it('loads habits with days filter', () => {
    service.getHabits(14).subscribe();

    const req = httpMock.expectOne(request => request.url === 'http://test/api/growth/habits');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('days')).toBe('14');
    req.flush([]);
  });

  it('exports pdf report as blob', () => {
    service.exportPdf().subscribe();

    const req = httpMock.expectOne('http://test/api/growth/export/pdf');
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob([]));
  });
});
