import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ApiConfigService } from './api-config.service';
import { LocalStorageService } from './local-storage.service';

export interface AuthRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  userId: number;
  fullName: string;
  email: string;
  token: string;
}

export interface PasswordResetRequestPayload {
  email: string;
}

export interface PasswordResetConfirmPayload {
  token: string;
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl: string;

  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private storage: LocalStorageService,
    private api: ApiConfigService
  ) {
    this.baseUrl = this.api.endpoint('auth');
    const saved = this.storage.get('auth');
    if (saved) {
      try {
        const parsed = JSON.parse(saved) as AuthResponse;
        this.currentUserSubject.next(parsed);
      } catch {
        this.storage.remove('auth');
      }
    }
  }

  login(data: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, data)
      .pipe(tap(res => this.handleAuth(res)));
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    // For security, do not auto-login after registration; force a fresh login.
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, data);
  }

  logout(): void {
    this.storage.remove('auth');
    this.storage.remove('token');
    this.currentUserSubject.next(null);
  }

  get token(): string | null {
    const current = this.currentUserSubject.value;
    if (current?.token) {
      return current.token;
    }
    return this.storage.get('token');
  }

  get isLoggedIn(): boolean {
    return !!this.token;
  }

  private handleAuth(res: AuthResponse): void {
    this.storage.set('auth', JSON.stringify(res));
    this.storage.set('token', res.token);
    this.currentUserSubject.next(res);
  }

  requestPasswordReset(payload: PasswordResetRequestPayload): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/reset/request`, payload);
  }

  confirmPasswordReset(payload: PasswordResetConfirmPayload): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/reset/confirm`, payload);
  }
}

// keep default export because the interceptor imports it as default
export default AuthService;
