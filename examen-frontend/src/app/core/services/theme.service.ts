import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { LocalStorageService } from './local-storage.service';

export type ThemePreference = 'light' | 'dark' | 'system';

const STORAGE_KEY = 'examen.theme';

/** Applies the user's theme preference as a data-theme attribute on the root element. */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly isBrowser: boolean;
  /** The preference currently applied to the document. */
  current: ThemePreference = 'light';

  constructor(
    @Inject(DOCUMENT) private document: Document,
    @Inject(PLATFORM_ID) platformId: object,
    private storage: LocalStorageService
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  /** Re-applies the last known preference, so the page does not flash before settings load. */
  restore(): void {
    this.apply(this.normalize(this.storage.get(STORAGE_KEY)), false);
  }

  apply(theme: string | null | undefined, persist = true): void {
    const value = this.normalize(theme);
    this.current = value;
    if (!this.isBrowser) {
      return;
    }
    this.document.documentElement.setAttribute('data-theme', value);
    if (persist) {
      this.storage.set(STORAGE_KEY, value);
    }
  }

  private normalize(theme: string | null | undefined): ThemePreference {
    return theme === 'dark' || theme === 'system' ? theme : 'light';
  }
}
