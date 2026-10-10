import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { defer, of } from 'rxjs';
import { DemoStore } from './demo.store';

// Never forwards requests: even an unsupported operation stays inside the browser.
export const demoInterceptor: HttpInterceptorFn = request => {
  const store = inject(DemoStore);
  return defer(() => of(new HttpResponse({ status: 200, body: structuredClone(store.handle(request)) })));
};
