// src/app/core/guards/auth.guard.ts
import { Injectable, Inject } from '@angular/core';
import { PLATFORM_ID } from '@angular/core';
import {
  CanActivate,
  Router,
  ActivatedRouteSnapshot,
  RouterStateSnapshot
} from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { LocalStorageService } from '../services/local-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private router: Router,
    private storage: LocalStorageService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    if (!isPlatformBrowser(this.platformId)) {
      return true; // Allow SSR builds/prerender without touching browser APIs
    }

    const token = this.storage.get('token');

    if (token) {
      return true;
    }

    this.router.navigate(['/auth/login']);
    return false;
  }
}
