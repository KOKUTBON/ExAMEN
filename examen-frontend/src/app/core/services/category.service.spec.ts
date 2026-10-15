import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { CategoryService } from './category.service';
import { ApiConfigService } from './api-config.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

describe('CategoryService', () => {
  let service: CategoryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CategoryService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub }
      ]
    });

    service = TestBed.inject(CategoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('gets paged categories', () => {
    service.getAll().subscribe();

    const req = httpMock.expectOne(request => request.url === 'http://test/api/categories');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('200');
    req.flush({ content: [] });
  });

  it('creates a category', () => {
    service.create({ name: 'Reflection', description: 'Core' }).subscribe();

    const req = httpMock.expectOne('http://test/api/categories');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.name).toBe('Reflection');
    req.flush({ id: 1, name: 'Reflection', description: 'Core' });
  });

  it('updates a category', () => {
    service.update(5, { name: 'Updated', description: 'New desc' }).subscribe();

    const req = httpMock.expectOne('http://test/api/categories/5');
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 5, name: 'Updated', description: 'New desc' });
  });

  it('deletes a category', () => {
    service.delete(9).subscribe();

    const req = httpMock.expectOne('http://test/api/categories/9');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
