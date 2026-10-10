import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TodoService } from './todo.service';
import { ApiConfigService } from '../../../../core/services/api-config.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

describe('TodoService', () => {
  let service: TodoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TodoService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub }
      ]
    });

    service = TestBed.inject(TodoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads all todos', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne('http://test/api/todos');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('creates a todo', () => {
    service.create({ title: 'Pray', dueAt: '2025-01-01T10:00:00Z' }).subscribe();
    const req = httpMock.expectOne('http://test/api/todos');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.title).toBe('Pray');
    req.flush({ id: 1, title: 'Pray', completed: false, dueAt: '2025-01-01T10:00:00Z' });
  });

  it('toggles a todo', () => {
    service.toggle(5).subscribe();
    const req = httpMock.expectOne('http://test/api/todos/5/toggle');
    expect(req.request.method).toBe('PATCH');
    req.flush({ id: 5, title: 'Test', completed: true, dueAt: '2025-01-01T10:00:00Z' });
  });

  it('deletes a todo', () => {
    service.delete(9).subscribe();
    const req = httpMock.expectOne('http://test/api/todos/9');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
