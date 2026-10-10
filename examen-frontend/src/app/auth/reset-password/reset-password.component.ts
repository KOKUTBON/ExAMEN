import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { IconComponent } from '../../shared/ui/icon/icon.component';
import { LogoComponent } from '../../shared/ui/logo/logo.component';
import { EXAMEN_MOVEMENTS } from '../login/login.component';

// Mirrors the backend rule in AuthenticationServiceImpl.
const STRONG_PASSWORD = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, IconComponent, LogoComponent],
  templateUrl: './reset-password.component.html',
  styleUrls: ['../login/login.component.scss', './reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {
  email = '';
  token = '';
  newPassword = '';
  step: 'request' | 'confirm' | 'done' = 'request';
  message = '';
  error = '';
  loading = false;
  showPassword = false;
  readonly movements = EXAMEN_MOVEMENTS;
  readonly steps = [
    { key: 'request', label: 'Your email' },
    { key: 'confirm', label: 'New password' },
    { key: 'done', label: 'Complete' }
  ] as const;

  get stepIndex(): number {
    return this.steps.findIndex(s => s.key === this.step);
  }

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      // Arrived from the emailed link: go straight to choosing a new password.
      this.token = token;
      this.step = 'confirm';
    }
  }

  request(): void {
    if (!this.email.trim()) {
      this.error = 'Enter the email address of your account.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.authService.requestPasswordReset({ email: this.email.trim() }).subscribe({
      next: () => {
        this.token = '';
        this.message = 'If an account exists for that email, a reset link is on its way. Open it, or paste the code below.';
        this.step = 'confirm';
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.status === 429
          ? 'Too many reset requests. Please wait and try again.'
          : 'Could not request a password reset.';
        this.loading = false;
      }
    });
  }

  confirm(): void {
    this.error = '';
    if (!this.token.trim()) {
      this.error = 'Enter the reset code from your email.';
      return;
    }
    if (!STRONG_PASSWORD.test(this.newPassword)) {
      this.error = 'Password must include upper, lower, number, symbol and be at least 8 characters.';
      return;
    }
    this.loading = true;
    this.authService.confirmPasswordReset({ token: this.token.trim(), newPassword: this.newPassword }).subscribe({
      next: () => {
        this.message = 'Password reset successful. You can log in now.';
        this.step = 'done';
        this.newPassword = '';
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Could not reset password.';
        this.loading = false;
      }
    });
  }

  startOver(): void {
    this.step = 'request';
    this.message = '';
    this.error = '';
    this.token = '';
  }
}
