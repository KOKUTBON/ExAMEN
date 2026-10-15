// src/environments/environment.ts
export const environment = {
  production: false,
  demo: false,
  // Browser calls should hit the backend via the host port; for SSR inside Docker you can override with an env/proxy if needed.
  apiUrl: 'http://localhost:8080/api'
};
