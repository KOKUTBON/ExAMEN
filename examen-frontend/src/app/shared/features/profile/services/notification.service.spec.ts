import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { NotificationService } from './notification.service';
import { ApiConfigService } from '../../../../core/services/api-config.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        NotificationService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub }
      ]
    });

    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists notifications with unread filter', () => {
    service.list(true).subscribe();

    const req = httpMock.expectOne(request => request.url === 'http://test/api/notifications');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('unreadOnly')).toBe('true');
    req.flush([]);
  });

  it('marks a notification as read', () => {
    service.markRead(4).subscribe();

    const req = httpMock.expectOne('http://test/api/notifications/4/read');
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });
});
