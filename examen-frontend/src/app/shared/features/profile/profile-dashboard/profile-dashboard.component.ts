import { environment } from '../../../../../environments/environment';
import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ProfileService,
  ProfileSummary,
  ProgressPoint,
  ProgressResponse,
  PeriodSummary,
  ProfileAnalytics
} from '../services/profile.service';
import { SessionService, SessionResponse } from '../../../../core/services/session.service';
import { Router, RouterModule } from '@angular/router';
import { MotionDirective } from '../../../motion/motion.directive';
import { GrowthService, GratitudeEntry, HabitScoreEntry, WeeklySummary } from '../services/growth.service';
import {
  InsightsService,
  InsightsSummary,
  SessionInsight,
  WeeklyNarrative,
  SemanticTagSummary,
  SemanticTag,
  FollowUpPrompt
} from '../services/insights.service';
import { NotificationItem, NotificationService } from '../services/notification.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { IconComponent } from '../../../ui/icon/icon.component';

interface SparkChart {
  line: string;
  area: string;
  dots: { x: number; y: number; v: number | null }[];
}

@Component({
  selector: 'app-profile-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MotionDirective, IconComponent],
  templateUrl: './profile-dashboard.component.html',
  styleUrls: ['./profile-dashboard.component.scss']
})
export class ProfileDashboardComponent implements OnInit {
  readonly isDemo = environment.demo;
  summary: ProfileSummary | null = null;
  loading = false;
  sessions: SessionResponse[] = [];
  progress: ProgressPoint[] = [];
  weekly?: PeriodSummary;
  monthly?: PeriodSummary;
  analytics?: ProfileAnalytics;
  weeklyGrowth?: WeeklySummary;
  gratitudeEntries: GratitudeEntry[] = [];
  habitScores: HabitScoreEntry[] = [];
  suggestions: string[] = [];
  gratitudeDraft = '';
  habitDraft = '';
  habitScore = 3;
  growthLoading = false;
  growthError = '';
  insightsSummary?: InsightsSummary;
  sessionInsight?: SessionInsight;
  suggestedQuestions: string[] = [];
  suggestionFocus = 'gratitude';
  insightsError = '';
  showEvidence = false;
  followUpPrompts: FollowUpPrompt[] = [];
  followUpsLoading = false;
  followUpsError = '';
  weeklyNarrative?: WeeklyNarrative;
  weeklyNarrativeLoading = false;
  weeklyNarrativeError = '';
  weeklyNarrativeFeedback?: 'helpful' | 'not-helpful';
  semanticTagSummary?: SemanticTagSummary;
  tagsLoading = false;
  sessionTags: { [sessionId: number]: SemanticTag[] } = {};
  feedbackStatus: { [key: string]: string } = {};
  notifications: NotificationItem[] = [];
  notificationsError = '';
  notificationsLoading = false;

  /** Presentational metadata for semantic reflection themes (movement color + icon). */
  readonly themeMeta: Record<string, { label: string; icon: string; cls: string }> = {
    VIRTUE: { label: 'Virtues & strengths', icon: 'sprout', cls: 'm-resolve' },
    STRUGGLE: { label: 'Struggles & growth edges', icon: 'droplet', cls: 'm-repent' },
    GRATITUDE: { label: 'Gratitude & joy', icon: 'heart', cls: 'm-relish' },
    STRESSOR: { label: 'Stressors & pressures', icon: 'pulse', cls: 'm-request' }
  };

  readonly habitScale = [1, 2, 3, 4, 5] as const;

  private navCache: { key: string; value: { id: string; label: string; icon: string }[] } = { key: '', value: [] };
  private moodCache: { src?: unknown; value?: SparkChart } = {};

  /** Anchored sub-navigation, filtered to the sections that currently have content (memoized). */
  get navSections(): { id: string; label: string; icon: string }[] {
    const hasRhythm = !!(this.summary?.recentMoodTrend?.length || this.progress.length ||
      this.analytics?.weeklyTrend?.length || this.weekly || this.monthly);
    const all = [
      { id: 'pd-overview', label: 'Overview', icon: 'compass', show: true },
      { id: 'pd-rhythm', label: 'Rhythm', icon: 'pulse', show: hasRhythm },
      { id: 'pd-scoring', label: 'Scoring', icon: 'target', show: !!this.analytics },
      { id: 'pd-practices', label: 'Practices', icon: 'sprout', show: true },
      { id: 'pd-insights', label: 'Insights', icon: 'sparkle', show: true },
      { id: 'pd-inbox', label: 'Inbox', icon: 'bell', show: true },
      { id: 'pd-history', label: 'History', icon: 'history', show: this.sessions.length > 0 }
    ];
    const key = all.map(s => (s.show ? '1' : '0')).join('');
    if (key !== this.navCache.key) {
      this.navCache = { key, value: all.filter(s => s.show).map(({ id, label, icon }) => ({ id, label, icon })) };
    }
    return this.navCache.value;
  }

  /** Sparkline for the 14-day feeling trend (memoized on the source array). */
  get moodChart(): SparkChart | undefined {
    const src = this.summary?.recentMoodTrend;
    if (!src?.length) {
      return undefined;
    }
    if (this.moodCache.src !== src) {
      this.moodCache = { src, value: this.spark(src.map(p => p.mood), 5) };
    }
    return this.moodCache.value;
  }

  jumpTo(event: Event, id: string): void {
    event.preventDefault();
    if (typeof document === 'undefined') {
      return;
    }
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  /** Builds an SVG sparkline (viewBox 0 0 w h) for values on a 0..max scale. */
  spark(values: (number | null | undefined)[], max: number, w = 300, h = 80): SparkChart {
    const n = values.length;
    const pad = 6;
    const step = n > 1 ? (w - pad * 2) / (n - 1) : 0;
    const dots = values.map((v, i) => ({
      x: n > 1 ? pad + i * step : w / 2,
      y: v == null ? h - pad : h - pad - (Math.min(v, max) / max) * (h - pad * 2),
      v: v ?? null
    }));
    const pts = dots.filter(d => d.v != null);
    const line = pts.map((d, i) => `${i ? 'L' : 'M'}${d.x.toFixed(1)},${d.y.toFixed(1)}`).join(' ');
    const area = pts.length
      ? `${line} L${pts[pts.length - 1].x.toFixed(1)},${h} L${pts[0].x.toFixed(1)},${h} Z`
      : '';
    return { line, area, dots };
  }

  /** Maps a 1..5 feeling to a 0..100 ring percentage. */
  pct5(value: number | null | undefined): number {
    return value == null ? 0 : Math.max(0, Math.min(100, value * 20));
  }

  clampPct(value: number | null | undefined): number {
    return value == null ? 0 : Math.max(0, Math.min(100, value));
  }

  get maxWeeklySessions(): number {
    return Math.max(1, ...(this.analytics?.weeklyTrend || []).map(w => w.sessions || 0));
  }

  constructor(
    private profileService: ProfileService,
    private sessionService: SessionService,
    private growthService: GrowthService,
    private insightsService: InsightsService,
    private notificationService: NotificationService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnDestroy(): void {
    // no-op
  }

  ngOnInit(): void {
    this.loading = true;
    this.profileService.refresh$.subscribe(() => this.reload());
    this.reload();
  }

  private reload(): void {
    this.loadGrowth();
    this.loadInsightsSummary();
    this.loadWeeklyNarrative();
    this.loadSemanticTagSummary();
    this.refreshQuestionSuggestions();
    this.loadFollowUpPrompts();
    this.loadNotifications();
    this.profileService.getSummaryForLast30Days().subscribe({
      next: (data) => {
        this.summary = data;
      },
      error: () => {
      },
      complete: () => {
        this.loading = false;
        this.loadSessions();
        this.loadProgress();
        this.loadPeriodSummaries();
        this.loadAnalytics();
      }
    });
  }

  private loadSessions(): void {
    this.sessionService.getMine(0, 5).subscribe({
      next: res => {
        this.sessions = res.content;
        this.loadSessionInsight();
        this.sessions.forEach(s => {
          if (s.id) {
            this.loadSessionTags(s.id);
          }
        });
      },
      error: () => (this.sessions = [])
    });
  }

  private loadProgress(): void {
    this.profileService.getProgress(14).subscribe({
      next: (res: ProgressResponse) => (this.progress = res.points || []),
      error: () => (this.progress = [])
    });
  }

  private loadPeriodSummaries(): void {
    this.profileService.getWeekly().subscribe({
      next: res => (this.weekly = res),
      error: () => {}
    });
    this.profileService.getMonthly().subscribe({
      next: res => (this.monthly = res),
      error: () => {}
    });
  }

  private loadAnalytics(): void {
    this.profileService.getAnalytics().subscribe({
      next: res => (this.analytics = res),
      error: () => (this.analytics = undefined)
    });
  }

  private loadGrowth(): void {
    this.growthLoading = true;
    this.growthError = '';

    forkJoin({
      weekly: this.growthService.getWeeklySummary().pipe(catchError(() => of(undefined))),
      gratitude: this.growthService.getGratitude().pipe(catchError(() => of([]))),
      habits: this.growthService.getHabits(30).pipe(catchError(() => of([]))),
      suggestions: this.growthService.getSuggestions().pipe(catchError(() => of([])))
    }).subscribe(res => {
      this.weeklyGrowth = res.weekly;
      this.gratitudeEntries = res.gratitude;
      this.habitScores = res.habits;
      this.suggestions = res.suggestions;
      this.growthLoading = false;
    });
  }

  addGratitude(): void {
    if (!this.gratitudeDraft.trim()) {
      return;
    }

    this.growthError = '';
    this.growthService.addGratitude(this.gratitudeDraft.trim()).subscribe({
      next: entry => {
        this.gratitudeEntries = [entry, ...this.gratitudeEntries];
        this.gratitudeDraft = '';
        if (this.weeklyGrowth) {
          this.weeklyGrowth.gratitudeCount += 1;
        }
      },
      error: () => {
        this.growthError = 'Could not save gratitude entry.';
      }
    });
  }

  addHabitScore(): void {
    if (!this.habitDraft.trim()) {
      return;
    }

    this.growthError = '';
    this.growthService.addHabitScore(this.habitDraft.trim(), this.habitScore).subscribe({
      next: entry => {
        this.habitScores = [entry, ...this.habitScores];
        this.habitDraft = '';
        this.habitScore = 3;
        if (this.weeklyGrowth) {
          const currentCount = this.weeklyGrowth.habitsScored;
          const currentTotal = this.weeklyGrowth.averageHabitScore * currentCount;
          const nextCount = currentCount + 1;
          this.weeklyGrowth.habitsScored = nextCount;
          this.weeklyGrowth.averageHabitScore = (currentTotal + entry.score) / nextCount;
        }
      },
      error: () => {
        this.growthError = 'Could not save habit score.';
      }
    });
  }

  downloadReport(): void {
    if (typeof window === 'undefined') {
      return;
    }
    this.growthError = '';
    this.growthService.exportPdf().subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'examen-summary.pdf';
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.growthError = 'Could not export the report.';
      }
    });
  }

  get gratitudePreview(): GratitudeEntry[] {
    return this.gratitudeEntries.slice(0, 4);
  }

  get habitPreview(): HabitScoreEntry[] {
    return this.habitScores.slice(0, 4);
  }

  refreshQuestionSuggestions(): void {
    const focus = this.suggestionFocus?.trim() || 'gratitude';
    this.insightsError = '';
    this.insightsService.suggestQuestions(focus, 5).subscribe({
      next: res => {
        this.suggestedQuestions = res.suggestions || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.suggestedQuestions = [];
        this.insightsError = 'Could not load question suggestions.';
        this.cdr.detectChanges();
      }
    });
  }

  markNotificationRead(notification: NotificationItem): void {
    if (notification.readAt) {
      return;
    }
    this.notificationService.markRead(notification.id).subscribe({
      next: () => {
        this.notifications = this.notifications.map(item =>
          item.id === notification.id
            ? { ...item, readAt: new Date().toISOString() }
            : item
        );
      },
      error: () => {
        this.notificationsError = 'Could not mark notification as read.';
      }
    });
  }

  private loadInsightsSummary(): void {
    this.insightsError = '';
    this.insightsService.getSummary(30).subscribe({
      next: res => {
        this.insightsSummary = res ? {
          ...res,
          highlights: res.highlights || [],
          evidenceSnippets: res.evidenceSnippets || []
        } : undefined;
        this.cdr.detectChanges();
      },
      error: () => {
        this.insightsSummary = undefined;
        this.insightsError = 'Could not load insights summary.';
        this.cdr.detectChanges();
      }
    });
  }

  private loadSessionInsight(): void {
    const latestCompleted = this.sessions.find(item => !!item.completedAt);
    if (!latestCompleted?.id) {
      this.sessionInsight = undefined;
      return;
    }
    this.insightsService.analyzeSession(latestCompleted.id).subscribe({
      next: res => {
        this.sessionInsight = res ? {
          ...res,
          insights: res.insights || [],
          nextSteps: res.nextSteps || []
        } : undefined;
        this.cdr.detectChanges();
      },
      error: () => {
        this.sessionInsight = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  loadWeeklyNarrative(): void {
    this.weeklyNarrativeLoading = true;
    this.weeklyNarrativeError = '';
    this.insightsService.getWeeklyNarrative(7).subscribe({
      next: res => {
        this.weeklyNarrative = res ? {
          ...res,
          virtuesObserved: res.virtuesObserved || [],
          strugglesIdentified: res.strugglesIdentified || [],
          gratitudeThemes: res.gratitudeThemes || [],
          stressorSignals: res.stressorSignals || [],
          actionableAdvice: res.actionableAdvice || []
        } : undefined;
        this.weeklyNarrativeLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.weeklyNarrative = undefined;
        this.weeklyNarrativeLoading = false;
        this.weeklyNarrativeError = 'Could not load weekly narrative reflection.';
        this.cdr.detectChanges();
      }
    });
  }

  loadSemanticTagSummary(): void {
    this.tagsLoading = true;
    this.insightsService.getTagSummary(30).subscribe({
      next: res => {
        this.semanticTagSummary = res ? {
          ...res,
          virtues: res.virtues || [],
          struggles: res.struggles || [],
          gratitude: res.gratitude || [],
          stressors: res.stressors || []
        } : undefined;
        this.tagsLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.semanticTagSummary = undefined;
        this.tagsLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadSessionTags(sessionId: number): void {
    this.insightsService.getSessionTags(sessionId).subscribe({
      next: tags => {
        this.sessionTags[sessionId] = tags || [];
      },
      error: () => {
        this.sessionTags[sessionId] = [];
      }
    });
  }

  rateWeeklyNarrative(helpful: boolean): void {
    this.weeklyNarrativeFeedback = helpful ? 'helpful' : 'not-helpful';
    this.feedbackStatus['narrative'] = 'Submitting feedback...';
    this.insightsService.recordFeedback({
      targetType: 'WEEKLY_NARRATIVE',
      targetId: this.weeklyNarrative?.periodStart || 'current',
      helpful,
      comments: helpful ? 'Found narrative resonant' : 'Narrative could be more concrete'
    }).subscribe({
      next: () => {
        this.feedbackStatus['narrative'] = helpful
          ? 'Thank you! We will continue tuning your reflections in this direction.'
          : 'Thank you for your feedback. We will refine future narrative guidance.';
      },
      error: () => {
        this.feedbackStatus['narrative'] = 'Could not record feedback.';
      }
    });
  }

  rateQuestionSuggestions(helpful: boolean): void {
    const key = 'suggestions';
    this.feedbackStatus[key] = helpful ? 'Rated helpful' : 'Rated not helpful';
    this.insightsService.recordFeedback({
      targetType: 'QUESTION_SUGGESTION',
      targetId: this.suggestionFocus,
      helpful
    }).subscribe({
      next: () => {
        this.feedbackStatus[key] = helpful
          ? 'Thanks! We will suggest more questions like these.'
          : 'Noted! We will tune future suggestions.';
      }
    });
  }

  rateSessionInsight(helpful: boolean): void {
    const key = 'sessionInsight';
    this.feedbackStatus[key] = helpful ? 'Rated helpful' : 'Rated not helpful';
    this.insightsService.recordFeedback({
      targetType: 'SESSION_INSIGHT',
      targetId: String(this.sessionInsight?.sessionId || ''),
      helpful
    }).subscribe({
      next: () => {
        this.feedbackStatus[key] = helpful
          ? 'Feedback noted! Glad this insight was helpful.'
          : 'Thank you. We will improve next session insights.';
      }
    });
  }

  toggleEvidence(): void {
    this.showEvidence = !this.showEvidence;
  }

  getConfidenceClass(score: number | undefined): string {
    if (score == null) return 'confidence-low';
    if (score >= 0.8) return 'confidence-high';
    if (score >= 0.5) return 'confidence-medium';
    return 'confidence-low';
  }

  getConfidenceLabel(score: number | undefined): string {
    if (score == null) return 'Preliminary';
    if (score >= 0.8) return 'High confidence';
    if (score >= 0.5) return 'Moderate confidence';
    return 'Preliminary confidence';
  }

  rateProfileSummary(helpful: boolean): void {
    const key = 'summary';
    this.feedbackStatus[key] = helpful ? 'Submitting...' : 'Submitting...';
    this.insightsService.recordFeedback({
      targetType: 'PROFILE_SUMMARY',
      targetId: 'summary-' + (this.insightsSummary?.periodDays || 30) + 'd',
      helpful
    }).subscribe({
      next: () => {
        this.feedbackStatus[key] = helpful
          ? 'Thank you! Profile summary marked as accurate.'
          : 'Thank you for your feedback. Profile analysis will adjust.';
      },
      error: () => {
        this.feedbackStatus[key] = 'Could not record feedback.';
      }
    });
  }

  private loadNotifications(): void {
    this.notificationsLoading = true;
    this.notificationsError = '';
    this.notificationService.list(false).subscribe({
      next: res => {
        this.notifications = res || [];
        this.notificationsLoading = false;
      },
      error: () => {
        this.notifications = [];
        this.notificationsLoading = false;
        this.notificationsError = 'Could not load notifications.';
      }
    });
  }

  loadFollowUpPrompts(): void {
    this.followUpsLoading = true;
    this.followUpsError = '';
    this.insightsService.getFollowUpPrompts(4).subscribe({
      next: prompts => {
        this.followUpPrompts = prompts || [];
        this.followUpsLoading = false;
      },
      error: () => {
        this.followUpPrompts = [];
        this.followUpsLoading = false;
      }
    });
  }

  getAnomalySeverityClass(severity?: string): string {
    if (severity === 'HIGH') return 'anomaly-high';
    if (severity === 'MEDIUM') return 'anomaly-medium';
    return 'anomaly-low';
  }

  getDriftClass(indicator?: string): string {
    if (indicator === 'POSITIVE_DRIFT') return 'drift-positive';
    if (indicator === 'NEGATIVE_DRIFT') return 'drift-negative';
    return 'drift-stable';
  }
}
