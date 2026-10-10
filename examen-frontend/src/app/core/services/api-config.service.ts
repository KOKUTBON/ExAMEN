import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../../environments/environment';

type ExamenWindow = Window & { __EXAMEN_API_URL__?: string };

@Injectable({
  providedIn: 'root'
})
export class ApiConfigService {
  private readonly baseUrlValue: string;

  constructor(@Inject(PLATFORM_ID) platformId: object) {
    const fallback = environment.apiUrl || '/api';
    if (environment.demo) {
      this.baseUrlValue = fallback;
      return;
    }

    if (isPlatformBrowser(platformId)) {
      const win = window as ExamenWindow;
      const fromWindow = win.__EXAMEN_API_URL__;
      const derived = this.deriveFromLocation();
      this.baseUrlValue = this.normalize(fromWindow || derived || fallback);
    } else {
      const envUrl = (globalThis as any)?.process?.env?.API_URL as string | undefined;
      this.baseUrlValue = this.normalize(envUrl || fallback);
    }
  }

  get baseUrl(): string {
    return this.baseUrlValue;
  }

  endpoint(path: string): string {
    const cleaned = path.replace(/^\/+/, '');
    return `${this.baseUrlValue}/${cleaned}`;
  }

  private normalize(url: string): string {
    return url.replace(/\/+$/, '');
  }

  private deriveFromLocation(): string | null {
    if (typeof window === 'undefined') {
      return null;
    }

    const { protocol, hostname, port } = window.location;
    if (!hostname) {
      return null;
    }

    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      const apiPort = port && port !== '8080' ? '8080' : port || '8080';
      return `${protocol}//${hostname}:${apiPort}/api`;
    }

    const portPart = port ? `:${port}` : '';
    return `${protocol}//${hostname}${portPart}/api`;
  }
}
