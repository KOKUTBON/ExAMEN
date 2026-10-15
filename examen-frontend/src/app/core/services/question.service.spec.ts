import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { QuestionService } from './question.service';
import { ApiConfigService } from './api-config.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

describe('QuestionService', () => {
  let service: QuestionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        QuestionService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub }
      ]
    });

    service = TestBed.inject(QuestionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads questions with pagination params', () => {
    service.getAll(100).subscribe();
    const req = httpMock.expectOne(
      request => request.url === 'http://test/api/questions'
        && request.params.get('page') === '0'
        && request.params.get('size') === '100'
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0 });
  });

  it('creates custom question', () => {
    service.createCustom({ text: 'New prompt', categoryId: 1 }).subscribe();
    const req = httpMock.expectOne('http://test/api/questions/custom');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, text: 'New prompt', category: { id: 1, name: 'Reflection' }, custom: true });
  });

  it('updates custom question', () => {
    service.updateCustom(3, { text: 'Updated', categoryId: 2 }).subscribe();
    const req = httpMock.expectOne('http://test/api/questions/custom/3');
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 3, text: 'Updated', category: { id: 2, name: 'Trust' }, custom: true });
  });

  it('deletes custom question', () => {
    service.deleteCustom(7).subscribe();
    const req = httpMock.expectOne('http://test/api/questions/custom/7');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
