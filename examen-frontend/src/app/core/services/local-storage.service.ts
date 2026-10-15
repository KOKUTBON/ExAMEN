import { Injectable, inject } from '@angular/core';
import { PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LocalStorageService {
  private platformId = inject(PLATFORM_ID);

  get(key: string): string | null {
    return isPlatformBrowser(this.platformId) ? localStorage.getItem(this.key(key)) : null;
  }

  set(key: string, value: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.key(key), value);
    }
  }

  remove(key: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.key(key));
    }
  }

  private key(key: string): string {
    return environment.demo ? `examen-demo:${key}` : key;
  }

  clear(): void {
    if (isPlatformBrowser(this.platformId)) {
      if (environment.demo) {
        Object.keys(localStorage).filter(key => key.startsWith('examen-demo:'))
          .forEach(key => localStorage.removeItem(key));
      } else {
        localStorage.clear();
      }
    }
  }
}
