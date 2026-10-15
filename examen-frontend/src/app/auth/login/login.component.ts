import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { IconComponent } from '../../shared/ui/icon/icon.component';
import { LogoComponent } from '../../shared/ui/logo/logo.component';

/** The five movements of the Ignatian examen, shown on the candlelit auth panel. */
export const EXAMEN_MOVEMENTS = [
  { key: 'relish', name: 'Relish', icon: 'heart', hint: 'Give thanks for the gifts of today' },
  { key: 'request', name: 'Request', icon: 'light', hint: 'Ask for light to see clearly' },
  { key: 'review', name: 'Review', icon: 'eye', hint: 'Walk gently back through the day' },
  { key: 'repent', name: 'Repent', icon: 'droplet', hint: 'Meet your shortcomings with mercy' },
  { key: 'resolve', name: 'Resolve', icon: 'sprout', hint: 'Choose one grace for tomorrow' }
] as const;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, IconComponent, LogoComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm!: FormGroup;
  loading = false;
  errorMessage = '';
  showPassword = false;
  readonly movements = EXAMEN_MOVEMENTS;

  get emailCtrl() { return this.loginForm.get('email'); }
  get passwordCtrl() { return this.loginForm.get('password'); }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  login(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      this.errorMessage = 'Enter a valid email and password.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Invalid credentials';
        this.loading = false;
      }
    });
  }
}
