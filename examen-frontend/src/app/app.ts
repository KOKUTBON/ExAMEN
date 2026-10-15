import { Component, HostListener, Inject, OnDestroy, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { ThemePreference, ThemeService } from './core/services/theme.service';
import { IconComponent } from './shared/ui/icon/icon.component';
import { LogoComponent } from './shared/ui/logo/logo.component';
import { UserSettingsService } from './settings/user-settings/user-settings.service';
import { DOCUMENT } from '@angular/common';
import { Meta, Title } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { environment } from '../environments/environment';

interface NavItem {
  path: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, IconComponent, LogoComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class App implements OnDestroy {
  readonly practiceNav: NavItem[] = [
    { path: '/dashboard', label: 'Today', icon: 'compass' },
    { path: '/examination', label: 'Examen', icon: 'flame' },
    { path: '/journal', label: 'Journal', icon: 'book' },
    { path: '/todos', label: 'Todos', icon: 'checklist' }
  ];
  readonly libraryNav: NavItem[] = [
    { path: '/questions', label: 'Questions', icon: 'quill' },
    { path: '/categories', label: 'Categories', icon: 'layers' }
  ];
  readonly growthNav: NavItem[] = [
    { path: '/profile', label: 'Profile', icon: 'sunrise' },
    { path: '/settings', label: 'Settings', icon: 'sliders' }
  ];
  readonly mobileNav: NavItem[] = [
    { path: '/dashboard', label: 'Today', icon: 'compass' },
    { path: '/journal', label: 'Journal', icon: 'book' },
    { path: '/examination', label: 'Examen', icon: 'flame' },
    { path: '/todos', label: 'Todos', icon: 'checklist' },
    { path: '/profile', label: 'Growth', icon: 'sunrise' }
  ];
  readonly themeOptions: { value: ThemePreference; icon: string; label: string }[] = [
    { value: 'light', icon: 'sun', label: 'Light' },
    { value: 'dark', icon: 'moon', label: 'Dark' },
    { value: 'system', icon: 'monitor', label: 'System' }
  ];

  readonly isDemo = environment.demo;
  isLoggedIn = false;
  isScrolled = false;
  isLanding = false;
  isAuthPage = false;
  menuOpen = false;
  userName = '';
  userInitial = '';
  readonly today = new Date();
  private isBrowser = false;
  private readonly defaultDescription =
    'Examen is an open-source reflective journaling and examination of conscience platform with AI-assisted insights, habit tracking, and analytics.';
  private readonly publicBaseUrl: string;
  private subscriptions = new Subscription();

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private title: Title,
    private meta: Meta,
    private themeService: ThemeService,
    private settingsService: UserSettingsService,
    @Inject(DOCUMENT) private document: Document,
    @Inject(PLATFORM_ID) platformId: object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    this.publicBaseUrl = this.resolvePublicBaseUrl();
    this.isScrolled = this.isBrowser ? window.scrollY > 16 : false;
    this.themeService.restore();

    this.subscriptions.add(
      this.authService.currentUser$.subscribe(user => {
        const wasLoggedIn = this.isLoggedIn;
        this.isLoggedIn = !!user;
        this.userName = user?.fullName?.trim() || user?.email || '';
        this.userInitial = (this.userName[0] || 'E').toUpperCase();
        if (this.isLoggedIn && !wasLoggedIn && this.isBrowser) {
          this.settingsService.get().subscribe({
            next: settings => this.themeService.apply(settings.theme),
            error: () => undefined
          });
        }
      })
    );

    this.subscriptions.add(
      this.router.events
        .pipe(
          filter(event => event instanceof NavigationEnd),
          map(() => {
            let child = this.route.firstChild;
            while (child?.firstChild) {
              child = child.firstChild;
            }
            const snapshot = child?.snapshot;
            const pageTitle = snapshot?.title;
            const description = snapshot?.data['description'] as string | undefined;
            const indexable = (snapshot?.data['indexable'] as boolean | undefined) ?? false;
            return { pageTitle, description, indexable };
          })
        )
        .subscribe(({ pageTitle, description, indexable }) => {
          const baseUrl = this.router.url.split('#')[0].split('?')[0];
          this.isLanding = baseUrl === '/' || baseUrl === '';
          this.isAuthPage = baseUrl.startsWith('/auth');
          this.menuOpen = false;
          if (pageTitle) {
            this.title.setTitle(`${pageTitle} | Examen Platform`);
          } else {
            this.title.setTitle('Examen Platform');
          }
          this.applySeoMetadata(baseUrl, pageTitle, description, indexable);
        })
    );
  }

  @HostListener('window:scroll')
  onScroll(): void {
    if (!this.isBrowser) {
      return;
    }
    this.isScrolled = window.scrollY > 16;
  }

  get theme(): ThemePreference {
    return this.themeService.current;
  }

  /** Greeting follows the hours of the day, as the examen is traditionally prayed at evening. */
  get greeting(): string {
    const hour = new Date().getHours();
    if (hour < 5) return 'Keeping vigil';
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    if (hour < 21) return 'Good evening';
    return 'Peaceful night';
  }

  get firstName(): string {
    return this.userName.split(/[\s@]/)[0] || '';
  }

  setTheme(value: ThemePreference): void {
    this.themeService.apply(value);
  }

  cycleTheme(): void {
    const order: ThemePreference[] = ['light', 'dark', 'system'];
    this.setTheme(order[(order.indexOf(this.theme) + 1) % order.length]);
  }

  get themeIcon(): string {
    return this.themeOptions.find(option => option.value === this.theme)?.icon ?? 'sun';
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  enterDemo(): void {
    if (!this.isDemo) return;
    this.authService.login({ email: 'visitor@example.invalid', password: 'demo' })
      .subscribe(() => this.router.navigate(['/dashboard']));
  }

  resetDemo(): void {
    if (!this.isDemo) return;
    Object.keys(localStorage).filter(key => key.startsWith('examen-demo:'))
      .forEach(key => localStorage.removeItem(key));
    window.location.hash = '/dashboard';
    window.location.reload();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private applySeoMetadata(
    path: string,
    pageTitle: string | undefined,
    description: string | undefined,
    indexable: boolean
  ): void {
    const fullTitle = pageTitle ? `${pageTitle} | Examen Platform` : 'Examen Platform';
    const fullDescription = description?.trim() || this.defaultDescription;
    const absoluteUrl = environment.demo
      ? `${this.publicBaseUrl}/${path === '/' ? '' : '#' + path}`
      : `${this.publicBaseUrl}${path === '/' ? '' : path}`;
    const robots = indexable ? 'index,follow' : 'noindex,nofollow';

    this.meta.updateTag({ name: 'description', content: fullDescription });
    this.meta.updateTag({ property: 'og:title', content: fullTitle });
    this.meta.updateTag({ property: 'og:description', content: fullDescription });
    this.meta.updateTag({ property: 'og:url', content: absoluteUrl });
    this.meta.updateTag({ name: 'twitter:title', content: fullTitle });
    this.meta.updateTag({ name: 'twitter:description', content: fullDescription });
    this.meta.updateTag({ name: 'robots', content: robots });

    let canonical = this.document.querySelector("link[rel='canonical']") as HTMLLinkElement | null;
    if (!canonical) {
      canonical = this.document.createElement('link');
      canonical.setAttribute('rel', 'canonical');
      this.document.head.appendChild(canonical);
    }
    canonical.setAttribute('href', absoluteUrl);
  }

  private resolvePublicBaseUrl(): string {
    if (this.isBrowser) {
      return (environment.demo
        ? new URL(this.document.baseURI).href
        : window.location.origin).replace(/\/+$/, '');
    }
    const fromEnv = (globalThis as { process?: { env?: Record<string, string | undefined> } })
      .process?.env?.['APP_PUBLIC_URL'];
    return (fromEnv || 'http://localhost:4200').replace(/\/+$/, '');
  }
}
