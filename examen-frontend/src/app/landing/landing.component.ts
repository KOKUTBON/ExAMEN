import {
  AfterViewInit,
  Component,
  ElementRef,
  HostListener,
  Inject,
  OnDestroy,
  PLATFORM_ID,
  QueryList,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { environment } from '../../environments/environment';
import { IconComponent } from '../shared/ui/icon/icon.component';
import { LogoComponent } from '../shared/ui/logo/logo.component';

interface Movement {
  numeral: string;
  name: string;
  theme: string;
  icon: string;
  tone: string;
  description: string;
  prompt: string;
}

interface Practice {
  icon: string;
  title: string;
  text: string;
  wide?: boolean;
}

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink, IconComponent, LogoComponent],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent implements AfterViewInit, OnDestroy {
  readonly isDemo = environment.demo;
  readonly year = new Date().getFullYear();

  readonly movements: Movement[] = [
    {
      numeral: 'I',
      name: 'Relish',
      theme: 'Gratitude',
      icon: 'heart',
      tone: 'm-relish',
      description: 'Notice the unearned gifts of the day, small and large.',
      prompt: 'Where did I receive kindness I did not expect?'
    },
    {
      numeral: 'II',
      name: 'Request',
      theme: 'Light',
      icon: 'light',
      tone: 'm-request',
      description: 'Ask for clarity to see the day honestly, without self-deception.',
      prompt: 'What do I need to see more clearly tonight?'
    },
    {
      numeral: 'III',
      name: 'Review',
      theme: 'The day',
      icon: 'eye',
      tone: 'm-review',
      description: 'Walk through thoughts, words and actions from morning to evening.',
      prompt: 'When did I feel most alive, and most distant?'
    },
    {
      numeral: 'IV',
      name: 'Repent',
      theme: 'Forgiveness',
      icon: 'droplet',
      tone: 'm-repent',
      description: 'Name what fell short, and let it be healed rather than hidden.',
      prompt: 'Whom did I hurt, and what would repair look like?'
    },
    {
      numeral: 'V',
      name: 'Resolve',
      theme: 'Tomorrow',
      icon: 'sprout',
      tone: 'm-resolve',
      description: 'Choose one concrete, hopeful step for the day ahead.',
      prompt: 'What one small thing will I do differently tomorrow?'
    }
  ];

  readonly practices: Practice[] = [
    {
      icon: 'flame',
      title: 'Guided examen',
      text: 'One session a day. Yes/no and scale prompts grouped by category, a short note and a feeling rating. Drafts stay in your browser until you submit.',
      wide: true
    },
    {
      icon: 'book',
      title: 'Private journal',
      text: 'Free-form notes, prayers and insights outside the guided flow.'
    },
    {
      icon: 'quill',
      title: 'Your own prompts',
      text: 'A curated default library, plus questions and categories you write yourself.'
    },
    {
      icon: 'checklist',
      title: 'Commitments',
      text: 'Turn resolutions into todos with due times and gentle follow-through.'
    },
    {
      icon: 'sunrise',
      title: 'Growth over time',
      text: 'Streaks, feeling trends, category balance and weekly summaries, session by session.'
    }
  ];

  readonly trust = [
    { icon: 'database', title: 'Self-hosted', text: 'Your PostgreSQL, your server. Nothing leaves unless you send it.' },
    { icon: 'cloud-off', title: 'No trackers', text: 'Zero third-party analytics, ads or telemetry.' },
    { icon: 'download', title: 'Portable', text: 'Full JSON export and a PDF growth report, any time.' },
    { icon: 'code', title: 'Open source', text: 'MIT-licensed, auditable end to end.' }
  ];

  @ViewChildren('reveal') revealItems!: QueryList<ElementRef<HTMLElement>>;
  @ViewChild('heroVisual') heroVisual?: ElementRef<HTMLElement>;

  private observer?: IntersectionObserver;
  private isBrowser: boolean;
  private scrollTicking = false;

  constructor(@Inject(PLATFORM_ID) platformId: object) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngAfterViewInit(): void {
    if (!this.isBrowser) {
      return;
    }

    this.observer = new IntersectionObserver(
      entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            this.observer?.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.15 }
    );

    this.revealItems.forEach(item => {
      item.nativeElement.classList.add('reveal-ready');
      this.observer?.observe(item.nativeElement);
    });
    this.updateParallax();
  }

  @HostListener('window:scroll')
  onScroll(): void {
    if (!this.isBrowser || !this.heroVisual || this.scrollTicking) {
      return;
    }

    this.scrollTicking = true;
    window.requestAnimationFrame(() => {
      this.updateParallax();
      this.scrollTicking = false;
    });
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  private updateParallax(): void {
    if (!this.heroVisual) {
      return;
    }
    const offset = Math.min(window.scrollY, 600) * 0.08;
    this.heroVisual.nativeElement.style.setProperty('--parallax-offset', `${offset.toFixed(2)}px`);
  }
}
