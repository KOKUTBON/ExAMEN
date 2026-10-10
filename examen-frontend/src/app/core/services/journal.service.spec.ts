import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { JournalService } from './journal.service';
import { ApiConfigService } from './api-config.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

describe('JournalService', () => {
  let service: JournalService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        JournalService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub }
      ]
    });

    service = TestBed.inject(JournalService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('creates journal entries', () => {
    service.create({ content: 'Daily reflection' }).subscribe();

    const req = httpMock.expectOne('http://test/api/journal');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.content).toBe('Daily reflection');
    req.flush({ id: 1, content: 'Daily reflection', createdAt: '2026-03-09T00:00:00Z' });
  });

  it('loads recent journal entries', () => {
    service.getRecent().subscribe();

    const req = httpMock.expectOne('http://test/api/journal');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
