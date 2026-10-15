import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, RegisterRequest } from '../../core/services/auth.service';
import { IconComponent } from '../../shared/ui/icon/icon.component';
import { LogoComponent } from '../../shared/ui/logo/logo.component';
import { EXAMEN_MOVEMENTS } from '../login/login.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, IconComponent, LogoComponent],
  templateUrl: './register.component.html',
  styleUrls: ['../login/login.component.scss', './register.component.scss']
})
export class RegisterComponent {

  registerForm!: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';
  showPassword = false;
  readonly movements = EXAMEN_MOVEMENTS;
  private successTimeout?: ReturnType<typeof setTimeout>;

  get firstNameCtrl() { return this.registerForm.get('firstname'); }
  get lastNameCtrl() { return this.registerForm.get('lastname'); }
  get emailCtrl() { return this.registerForm.get('email'); }
  get passwordCtrl() { return this.registerForm.get('password'); }
  get confirmPasswordCtrl() { return this.registerForm.get('confirmPassword'); }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        // Upper, lower, number, symbol
        Validators.pattern(/^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/)
      ]],
      confirmPassword: ['', Validators.required]
    });
  }

  register(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.errorMessage = this.passwordCtrl?.errors
        ? 'Password must include upper, lower, number, symbol and be at least 8 characters.'
        : 'Please complete all fields.';
      return;
    }

    if (this.passwordCtrl?.value !== this.confirmPasswordCtrl?.value) {
      this.errorMessage = 'Passwords do not match.';
      this.confirmPasswordCtrl?.setErrors({ mismatch: true });
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { firstname, lastname, email, password } = this.registerForm.value;
    const payload: RegisterRequest = {
      fullName: `${firstname} ${lastname}`.trim(),
      email,
      password
    };

    this.authService.register(payload).subscribe({
      next: () => {
        this.successMessage = 'Registration successful! Redirecting to login...';
        this.loading = false;
        this.successTimeout = setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 600);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Registration failed. Please check your details and try again.';
        this.loading = false;
      }
    });
  }

  ngOnDestroy(): void {
    if (this.successTimeout) {
      clearTimeout(this.successTimeout);
    }
  }
}
