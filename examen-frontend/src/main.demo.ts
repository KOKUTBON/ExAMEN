import 'zone.js';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withHashLocation, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { App } from './app/app';
import { routes } from './app/app.routes';
import { demoInterceptor } from './app/demo/demo.interceptor';

// Separate entry point: no hydration, SSR providers, credentials or backend transport.
try {
  if (!localStorage.getItem('examen-demo:auth')) {
    localStorage.setItem('examen-demo:token', 'local-demo');
    localStorage.setItem('examen-demo:auth', JSON.stringify({
      userId: 1, fullName: 'Demo Visitor', email: 'visitor@example.invalid', token: 'local-demo'
    }));
  }
} catch { /* The app can still show the landing page when browser storage is unavailable. */ }

bootstrapApplication(App, {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withHashLocation(), withInMemoryScrolling({
      anchorScrolling: 'enabled', scrollPositionRestoration: 'enabled'
    })),
    provideHttpClient(withInterceptors([demoInterceptor]))
  ]
}).catch((error: unknown) => console.error(error));
