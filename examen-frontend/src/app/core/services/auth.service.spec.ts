import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { AuthService } from './auth.service';
import { ApiConfigService } from './api-config.service';
import { LocalStorageService } from './local-storage.service';

class ApiConfigStub {
  endpoint(path: string): string {
    return `http://test/api/${path}`;
  }
}

class MemoryStorage {
  private store = new Map<string, string>();

  get(key: string): string | null {
    return this.store.get(key) ?? null;
  }

  set(key: string, value: string): void {
    this.store.set(key, value);
  }

  remove(key: string): void {
    this.store.delete(key);
  }
}

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiConfigService, useClass: ApiConfigStub },
        { provide: LocalStorageService, useClass: MemoryStorage }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('posts login and stores token', () => {
    service.login({ email: 'test@example.com', password: 'secret' }).subscribe();

    const req = httpMock.expectOne('http://test/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({
      userId: 1,
      fullName: 'Test User',
      email: 'test@example.com',
      token: 'token-123'
    });

    expect(service.token).toBe('token-123');
    expect(service.isLoggedIn).toBe(true);
  });

  it('posts register without auto-login', () => {
    service.register({ fullName: 'Test User', email: 'test@example.com', password: 'secret' }).subscribe();

    const req = httpMock.expectOne('http://test/api/auth/register');
    expect(req.request.method).toBe('POST');
    req.flush({
      userId: 1,
      fullName: 'Test User',
      email: 'test@example.com',
      token: 'token-123'
    });

    expect(service.token).toBeNull();
  });
});
