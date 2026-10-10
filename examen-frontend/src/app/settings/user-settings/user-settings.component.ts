import { environment } from '../../../environments/environment';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserSettingsRequest, UserSettingsResponse, UserSettingsService } from './user-settings.service';
import { MotionDirective } from '../../shared/motion/motion.directive';
import { ThemeService } from '../../core/services/theme.service';
import { AccountService } from '../../core/services/account.service';
import { AuthService } from '../../core/services/auth.service';
import { IconComponent } from '../../shared/ui/icon/icon.component';

@Component({
  selector: 'app-user-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, MotionDirective, IconComponent],
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.scss']
})
export class UserSettingsComponent implements OnInit {
  readonly isDemo = environment.demo;
  settings: UserSettingsResponse | null = null;
  loading = false;
  loadError = '';
  saveError = '';
  saved = false;

  exporting = false;
  exportError = '';
  confirmingDelete = false;
  deletePassword = '';
  deleting = false;
  deleteError = '';

  timezones = typeof Intl.supportedValuesOf === 'function' ? Intl.supportedValuesOf('timeZone') : ['UTC'];
  themes: { value: string; label: string }[] = [
    { value: 'system', label: 'Match system' },
    { value: 'light', label: 'Light' },
    { value: 'dark', label: 'Dark' }
  ];

  /** Presentation for the appearance preview tiles. */
  readonly themeMeta: Partial<Record<string, { icon: string; hint: string }>> = {
    system: { icon: 'monitor', hint: 'Follows your device' },
    light: { icon: 'sun', hint: 'Warm vellum and ink' },
    dark: { icon: 'moon', hint: 'Sanctuary night' }
  };

  readonly exportIncludes = ['Sessions', 'Answers', 'Journal', 'Gratitude', 'Habits', 'Todos', 'Custom questions', 'Insight feedback'];

  constructor(
    private settingsService: UserSettingsService,
    private theme: ThemeService,
    private accountService: AccountService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loadError = '';
    this.settingsService.get().subscribe({
      next: res => (this.settings = this.normalize(res)),
      error: () => (this.loadError = 'Failed to load settings.')
    });
  }

  save(): void {
    if (!this.settings) return;
    this.loading = true;
    this.saveError = '';
    this.saved = false;
    this.settingsService.update(this.settings as UserSettingsRequest).subscribe({
      next: res => {
        this.settings = this.normalize(res);
        this.theme.apply(this.settings.theme);
        this.loading = false;
        this.saved = true;
      },
      error: err => {
        this.saveError = err?.error?.message || 'Failed to save settings.';
        this.loading = false;
      }
    });
  }

  previewTheme(value: string): void {
    this.theme.apply(value, false);
  }

  exportData(): void {
    this.exporting = true;
    this.exportError = '';
    this.accountService.exportData().subscribe({
      next: blob => {
        this.download(blob, `examen-export-${new Date().toISOString().slice(0, 10)}.json`);
        this.exporting = false;
      },
      error: () => {
        this.exportError = 'Could not export your data. Please try again.';
        this.exporting = false;
      }
    });
  }

  deleteAccount(): void {
    if (!this.deletePassword) {
      this.deleteError = 'Enter your password to confirm.';
      return;
    }
    this.deleting = true;
    this.deleteError = '';
    this.accountService.deleteAccount(this.deletePassword).subscribe({
      next: () => {
        this.deleting = false;
        this.authService.logout();
        this.router.navigate(['/']);
      },
      error: err => {
        this.deleting = false;
        this.deleteError = err?.status === 403 ? 'Password is incorrect.' : 'Could not delete the account.';
      }
    });
  }

  cancelDelete(): void {
    this.confirmingDelete = false;
    this.deletePassword = '';
    this.deleteError = '';
  }

  /** Smooth-scrolls to a settings section (in-page nav; avoids base-href fragment navigation). */
  jumpTo(id: string): void {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  private normalize(res: UserSettingsResponse): UserSettingsResponse {
    const browserZone = Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';
    return {
      ...res,
      timeZone: res.timeZone || browserZone,
      theme: res.theme || 'system',
      emailReminder: res.emailReminder ?? false,
      inAppReminder: res.inAppReminder ?? false
    };
  }

  private download(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
  }
}
