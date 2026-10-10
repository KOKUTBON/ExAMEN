import { ApplicationConfig } from '@angular/core';
import { provideServerRendering, RenderMode, withRoutes } from '@angular/ssr';
import { appConfig } from './app.config';

export const serverConfig: ApplicationConfig = {
  ...appConfig,
  providers: [
    // Use SSR for all routes (including parameterized ones) to avoid prerendering errors
    provideServerRendering(
      withRoutes([{ path: '**', renderMode: RenderMode.Server }])
    ),
    ...(appConfig.providers ?? [])
  ]
};
