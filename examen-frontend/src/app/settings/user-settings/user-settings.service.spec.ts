import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { UserSettingsService } from './user-settings.service';
import { ApiConfigService } from '../../core/services/api-config.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

describe('UserSettingsService', () => {
  let service: UserSettingsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserSettingsService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub }
      ]
    });

    service = TestBed.inject(UserSettingsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('gets settings', () => {
    service.get().subscribe();

    const req = httpMock.expectOne('http://test/api/settings');
    expect(req.request.method).toBe('GET');
    req.flush({ timeZone: 'UTC', theme: 'system', emailReminder: false, inAppReminder: false });
  });

  it('updates settings', () => {
    service.update({
      timeZone: 'UTC',
      reminderTime: '09:30',
      theme: 'dark',
      emailReminder: true,
      inAppReminder: true
    }).subscribe();

    const req = httpMock.expectOne('http://test/api/settings');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.timeZone).toBe('UTC');
    req.flush({ timeZone: 'UTC', reminderTime: '09:30', theme: 'dark', emailReminder: true, inAppReminder: true });
  });
});
