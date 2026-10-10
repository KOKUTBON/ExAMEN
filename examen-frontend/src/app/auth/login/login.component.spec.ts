import { TestBed } from '@angular/core/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { Router, provideRouter } from '@angular/router';
import { LoginComponent } from './login.component';
import { AuthService } from '../../core/services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let authService: { login: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(() => {
    authService = {
      login: vi.fn()
    };
    TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        provideRouter([])
      ]
    });

    const fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
  });

  it('blocks submit when form is invalid', () => {
    component.loginForm.setValue({ email: '', password: '' });
    component.login();

    expect(component.errorMessage).toBe('Enter a valid email and password.');
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('logs in and navigates to dashboard', () => {
    authService.login.mockReturnValue(of({
      userId: 1,
      fullName: 'Test User',
      email: 'test@example.com',
      token: 'token'
    }));

    component.loginForm.setValue({ email: 'test@example.com', password: 'secret' });
    component.login();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'test@example.com',
      password: 'secret'
    });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('shows error message on failed login', () => {
    authService.login.mockReturnValue(throwError(() => ({ error: { message: 'Invalid credentials' } })));

    component.loginForm.setValue({ email: 'test@example.com', password: 'secret' });
    component.login();

    expect(component.errorMessage).toBe('Invalid credentials');
  });
});
