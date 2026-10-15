import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ExaminationService } from '../services/examination.service';
import {
  Category,
  Question,
  QuestionType,
  SessionAnswerRequest,
  SessionSummary
} from '../models/examination.model';
import { LocalStorageService } from '../../../../core/services/local-storage.service';
import { ProfileService } from '../../profile/services/profile.service';
import { MotionDirective } from '../../../motion/motion.directive';
import { IconComponent } from '../../../ui/icon/icon.component';

type QuestionStatus = 'incomplete' | 'completed';

/** Presentational mapping of a category onto one of the five movements of the examen. */
interface Movement {
  cls: string;
  icon: string;
  label: string;
}

const MOVEMENTS: { match: RegExp; movement: Movement }[] = [
  { match: /relish|gratitude|grateful|thank|gift/i, movement: { cls: 'm-relish', icon: 'heart', label: 'Relish' } },
  { match: /request|light|grace|prayer|spirit/i, movement: { cls: 'm-request', icon: 'light', label: 'Request' } },
  { match: /review|\bday\b|examin|conscience|relationship/i, movement: { cls: 'm-review', icon: 'eye', label: 'Review' } },
  { match: /repent|forgiv|sorrow|\bsin\b|vice|habit|mercy/i, movement: { cls: 'm-repent', icon: 'droplet', label: 'Repent' } },
  { match: /resolve|tomorrow|growth|grow|intention|goal/i, movement: { cls: 'm-resolve', icon: 'sprout', label: 'Resolve' } }
];

const FALLBACK_MOVEMENT: Movement = { cls: 'm-none', icon: 'quill', label: 'Reflection' };

const NUMERALS = ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII', 'XIII', 'XIV', 'XV'];

interface QuestionCard {
  id: number;
  title: string;
  text: string;
  instruction: string;
  responseType: QuestionType;
  categoryName?: string;
  answerText?: string;
  reflectionText?: string;
  feelingScore?: number;
  answeredAt?: string;
  status: QuestionStatus;
  defaultQuestion?: boolean;
  validationMessage?: string;
}

const FALLBACK_DEFAULTS: QuestionCard[] = [
  {
    id: 10001,
    title: 'Question 1',
    text: 'Where did I notice gratitude most clearly today?',
    instruction: 'Write a short reflection and rate your feeling.',
    responseType: 'SCALE_1_5',
    categoryName: 'Reflection',
    status: 'incomplete'
  },
  {
    id: 10002,
    title: 'Question 2',
    text: 'When did I act out of love versus fear?',
    instruction: 'Write a short reflection and rate your feeling.',
    responseType: 'SCALE_1_5',
    categoryName: 'Reflection',
    status: 'incomplete'
  },
  {
    id: 10003,
    title: 'Question 3',
    text: 'Which habit or vice surfaced today, and how did I respond?',
    instruction: 'Write a short reflection and rate your feeling.',
    responseType: 'SCALE_1_5',
    categoryName: 'Reflection',
    status: 'incomplete'
  },
  {
    id: 10004,
    title: 'Question 4',
    text: 'Where did I feel closest to God/meaning, and where did I feel distant?',
    instruction: 'Write a short reflection and rate your feeling.',
    responseType: 'SCALE_1_5',
    categoryName: 'Reflection',
    status: 'incomplete'
  },
  {
    id: 10005,
    title: 'Question 5',
    text: 'What is one concrete step I will take tomorrow to grow?',
    instruction: 'Write a short reflection and rate your feeling.',
    responseType: 'SCALE_1_5',
    categoryName: 'Reflection',
    status: 'incomplete'
  }
];

@Component({
  selector: 'app-examination-today',
  standalone: true,
  imports: [CommonModule, FormsModule, MotionDirective, IconComponent],
  templateUrl: './examination-today.component.html',
  styleUrls: ['./examination-today.component.scss']
})
export class ExaminationTodayComponent implements OnInit, OnDestroy {
  categories: Category[] = [];
  questions: QuestionCard[] = [];
  activeSession: SessionSummary | null = null;
  history: SessionSummary[] = [];
  loading = true;
  submitting = false;
  errorMessage = '';
  successMessage = '';
  cooldownMessage = '';
  notes = '';
  moodScore: number | null = null;
  moodScale = [1, 2, 3, 4, 5];
  feelingScale = [1, 2, 3, 4, 5];
  expandedQuestionId: number | null = null;

  feelingLabels: Record<number, string> = {
    1: 'Very low / Distracted',
    2: 'Low / Struggling',
    3: 'Neutral / Balanced',
    4: 'Good / Focused',
    5: 'Very peaceful / Grateful'
  };

  /** SVG progress ring geometry (presentational). */
  readonly ringRadius = 52;
  readonly ringCircumference = 2 * Math.PI * 52;

  liveAnnouncement = '';
  private announcementTimeout: ReturnType<typeof setTimeout> | null = null;

  private progressKey = 'examen-active-session';
  showAutosave = false;
  private autosaveTimeout: ReturnType<typeof setTimeout> | null = null;
  private autoSubmitTriggered = false;

  constructor(
    private examService: ExaminationService,
    private storage: LocalStorageService,
    private profileService: ProfileService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadActiveSession();
    this.loadQuestions();
    this.loadHistory();
  }

  ngOnDestroy(): void {
    if (this.autosaveTimeout) {
      clearTimeout(this.autosaveTimeout);
    }
    if (this.announcementTimeout) {
      clearTimeout(this.announcementTimeout);
    }
  }

  announce(message: string): void {
    this.liveAnnouncement = message;
    if (this.announcementTimeout) {
      clearTimeout(this.announcementTimeout);
    }
    this.announcementTimeout = setTimeout(() => {
      this.liveAnnouncement = '';
    }, 4000);
  }

  feelingLabel(score: number): string {
    return this.feelingLabels[score] || `Score ${score}`;
  }

  isRatingFocused(current: number | undefined, m: number): boolean {
    return current === m || (current == null && m === 1);
  }

  startSession(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.cooldownMessage = '';

    this.examService.startSession().subscribe({
      next: session => {
        this.activeSession = session;
        this.resetResponses();
        this.expandedQuestionId = null;
        this.persistProgress();
        this.announce("Daily examination session started. 5 reflection questions are ready.");
      },
      error: (err) => {
        if (err.status === 429) {
          this.cooldownMessage = err.error?.message || 'You can start a new examination after 24 hours.';
        } else if (err.status === 409) {
          this.errorMessage = 'You already have an active session.';
        } else {
          this.errorMessage = 'Failed to start a new session.';
        }
        this.announce(this.cooldownMessage || this.errorMessage);
        this.loadActiveSession();
      }
    });
  }

  setReflection(questionId: number, value: string): void {
    if (!this.activeSession) {
      this.errorMessage = 'Start a session to answer questions.';
      return;
    }
    const question = this.questions.find(q => q.id === questionId);
    if (!question) {
      return;
    }
    question.reflectionText = value;
    question.answerText = value;
    this.markQuestionDirty(question);
    this.persistProgress();
  }

  setFeelingScore(questionId: number, value: number): void {
    if (!this.activeSession) {
      this.errorMessage = 'Start a session to answer questions.';
      return;
    }
    const question = this.questions.find(q => q.id === questionId);
    if (!question) {
      return;
    }
    question.feelingScore = value;
    this.markQuestionDirty(question);
    this.persistProgress();
  }

  setMoodScore(value: number): void {
    if (!this.activeSession) {
      this.errorMessage = 'Start a session to record feeling.';
      return;
    }
    this.moodScore = value;
    this.persistProgress();
  }

  onNotesChange(value: string): void {
    this.notes = value;
    this.persistProgress();
  }

  expandQuestion(questionId: number, autoFocus = true): void {
    if (!this.activeSession) {
      this.errorMessage = 'Start a session to answer questions.';
      this.announce('Please start a session to answer questions.');
      return;
    }
    this.expandedQuestionId = questionId;
    if (autoFocus) {
      setTimeout(() => this.focusQuestionTextarea(questionId), 120);
    }
  }

  toggleQuestion(questionId: number): void {
    if (this.expandedQuestionId === questionId) {
      this.collapseQuestion(questionId, true);
    } else {
      this.expandQuestion(questionId, true);
    }
  }

  collapseQuestion(questionId: number, restoreFocus = true): void {
    if (this.expandedQuestionId === questionId) {
      this.expandedQuestionId = null;
      if (restoreFocus) {
        setTimeout(() => {
          const toggleBtn = document.getElementById(`question-toggle-${questionId}`);
          toggleBtn?.focus();
        }, 50);
      }
    }
  }

  focusQuestionTextarea(questionId: number): void {
    const textarea = document.getElementById(`question-textarea-${questionId}`) as HTMLTextAreaElement | null;
    if (textarea) {
      textarea.focus();
    }
  }

  focusQuestionHeader(index: number): void {
    if (index >= 0 && index < this.questions.length) {
      const q = this.questions[index];
      const btn = document.getElementById(`question-toggle-${q.id}`);
      btn?.focus();
    }
  }

  onQuestionHeaderKeydown(event: KeyboardEvent, questionId: number, index: number): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.toggleQuestion(questionId);
    } else if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.focusQuestionHeader(index + 1);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.focusQuestionHeader(index - 1);
    } else if (event.key === 'Home') {
      event.preventDefault();
      this.focusQuestionHeader(0);
    } else if (event.key === 'End') {
      event.preventDefault();
      this.focusQuestionHeader(this.questions.length - 1);
    }
  }

  onTextareaKeydown(event: KeyboardEvent, question: QuestionCard): void {
    if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
      event.preventDefault();
      this.submitQuestion(question);
    } else if (event.key === 'Escape') {
      event.preventDefault();
      this.collapseQuestion(question.id, true);
    }
  }

  onFeelingKeydown(event: KeyboardEvent, questionId: number, currentScore: number | undefined): void {
    let targetScore: number | null = null;
    const current = currentScore ?? 1;

    if (event.key === 'ArrowRight' || event.key === 'ArrowDown') {
      event.preventDefault();
      targetScore = current < 5 ? current + 1 : 1;
    } else if (event.key === 'ArrowLeft' || event.key === 'ArrowUp') {
      event.preventDefault();
      targetScore = current > 1 ? current - 1 : 5;
    } else if (event.key === 'Home') {
      event.preventDefault();
      targetScore = 1;
    } else if (event.key === 'End') {
      event.preventDefault();
      targetScore = 5;
    }

    if (targetScore !== null) {
      this.setFeelingScore(questionId, targetScore);
      setTimeout(() => {
        const btn = document.getElementById(`feeling-${questionId}-${targetScore}`);
        btn?.focus();
      }, 10);
    }
  }

  onMoodKeydown(event: KeyboardEvent): void {
    let targetScore: number | null = null;
    const current = this.moodScore ?? 1;

    if (event.key === 'ArrowRight' || event.key === 'ArrowDown') {
      event.preventDefault();
      targetScore = current < 5 ? current + 1 : 1;
    } else if (event.key === 'ArrowLeft' || event.key === 'ArrowUp') {
      event.preventDefault();
      targetScore = current > 1 ? current - 1 : 5;
    } else if (event.key === 'Home') {
      event.preventDefault();
      targetScore = 1;
    } else if (event.key === 'End') {
      event.preventDefault();
      targetScore = 5;
    }

    if (targetScore !== null) {
      this.setMoodScore(targetScore);
      setTimeout(() => {
        const btn = document.getElementById(`mood-${targetScore}`);
        btn?.focus();
      }, 10);
    }
  }

  @HostListener('window:keydown', ['$event'])
  onGlobalKeydown(event: KeyboardEvent): void {
    if (event.altKey && (event.key === 'n' || event.key === 'N')) {
      event.preventDefault();
      this.scrollToNextUnanswered();
    } else if ((event.ctrlKey || event.metaKey) && event.shiftKey && event.key === 'Enter') {
      if (this.submitEnabled && !this.submitting) {
        event.preventDefault();
        this.submit();
      }
    }
  }

  submitQuestion(question: QuestionCard): void {
    if (!this.activeSession) {
      this.errorMessage = 'Start a session to answer questions.';
      return;
    }
    if (this.submitting) {
      return;
    }

    const reflectionText = question.reflectionText?.trim() ?? '';
    const feelingScore = question.feelingScore;

    if (!reflectionText || feelingScore == null) {
      question.validationMessage = 'Complete the reflection and feeling rating.';
      this.announce('Please complete both the reflection and feeling rating before saving.');
      return;
    }

    question.answerText = reflectionText;
    question.reflectionText = reflectionText;
    question.status = 'completed';
    question.answeredAt = new Date().toISOString();
    question.validationMessage = '';
    this.expandedQuestionId = null;
    this.updateQuestionStatus(question.id);
    this.persistProgress();

    this.announce(`Question ${question.id} completed. ${this.answeredCount} of ${this.totalQuestions} questions answered.`);

    if (!this.submitEnabled) {
      const nextId = this.unansweredQuestionId;
      if (nextId != null) {
        this.expandQuestion(nextId, true);
        const el = document.getElementById(`question-${nextId}`);
        if (typeof el?.scrollIntoView === 'function') {
          el.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }
    }

    this.maybeAutoSubmit();
  }

  submit(): void {
    if (!this.activeSession) {
      this.errorMessage = 'Start a session before submitting.';
      return;
    }
    if (this.submitting) {
      return;
    }
    if (!this.submitEnabled) {
      this.errorMessage = 'Complete all questions before submitting.';
      this.scrollToNextUnanswered();
      return;
    }

    const sessionId = this.activeSession.id;
    const answers: SessionAnswerRequest[] = this.questions.map(q => ({
      questionId: q.id,
      answerText: q.answerText?.trim() ?? q.reflectionText?.trim() ?? '',
      correct: false,
      examinationSessionId: sessionId,
      reflectionText: q.reflectionText?.trim() ?? '',
      feelingScore: q.feelingScore ?? undefined
    }));

    const moodScore = this.moodScore ?? this.averageFeelingScore;
    this.submitting = true;
    this.autoSubmitTriggered = true;
    this.examService.submitSession(sessionId, answers, this.notes, moodScore ?? undefined).subscribe({
      next: () => {
        this.successMessage = 'Examen submitted successfully.';
        this.storage.remove(this.progressKey);
        this.activeSession = null;
        this.submitting = false;
        this.expandedQuestionId = null;
        this.loadHistory();
        this.profileService.getSummaryForLast30Days().subscribe();
        this.profileService.getAnalytics().subscribe();
        this.profileService.getProgress(14).subscribe();
        this.profileService.refresh$.next();
        setTimeout(() => this.router.navigate(['/profile']), 300);
      },
      error: () => {
        this.errorMessage = 'Failed to submit the examen.';
        this.submitting = false;
        this.autoSubmitTriggered = false;
      }
    });
  }

  movementOf(categoryName?: string): Movement {
    if (!categoryName) {
      return FALLBACK_MOVEMENT;
    }
    const found = MOVEMENTS.find(m => m.match.test(categoryName));
    return found ? found.movement : FALLBACK_MOVEMENT;
  }

  numeral(index: number): string {
    return NUMERALS[index] ?? String(index + 1);
  }

  get ringOffset(): number {
    return this.ringCircumference * (1 - this.progressPercent / 100);
  }

  /** Consecutive days (ending today or yesterday) with a completed session, derived from history. */
  get streakDays(): number {
    const days = new Set(
      this.history
        .filter(s => !!s.completedAt)
        .map(s => new Date(s.completedAt as string))
        .filter(d => !Number.isNaN(d.getTime()))
        .map(d => d.toDateString())
    );
    if (!days.size) {
      return 0;
    }
    const cursor = new Date();
    if (!days.has(cursor.toDateString())) {
      cursor.setDate(cursor.getDate() - 1);
    }
    let streak = 0;
    while (days.has(cursor.toDateString())) {
      streak += 1;
      cursor.setDate(cursor.getDate() - 1);
    }
    return streak;
  }

  get completedToday(): boolean {
    const completedAt = this.lastSession?.completedAt;
    return !!completedAt && new Date(completedAt).toDateString() === new Date().toDateString();
  }

  goToQuestion(questionId: number): void {
    this.expandQuestion(questionId, true);
    const el = document.getElementById(`question-${questionId}`);
    if (typeof el?.scrollIntoView === 'function') {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }

  trackByQuestionId(index: number, item: QuestionCard): number {
    return item?.id ?? index;
  }

  private loadQuestions(): void {
    this.loading = true;
    this.examService.getCategories().subscribe({
      next: categories => {
        this.categories = categories;
        const loaders = categories.map(category => this.examService.getQuestionsByCategory(category.id));
        if (loaders.length === 0) {
          this.ensureMinimumQuestions();
          this.loading = false;
          return;
        }

        const existingById = new Map(this.questions.map(q => [q.id, q]));
        forkJoin(loaders).subscribe({
          next: results => {
            const combined = results.flat();
            this.questions = this.mapQuestions(combined, existingById);
            this.ensureMinimumQuestions();
            this.loading = false;
          },
          error: () => {
            this.errorMessage = 'Failed to load questions.';
            this.ensureMinimumQuestions();
            this.loading = false;
          }
        });
      },
      error: () => {
        this.errorMessage = 'Failed to load categories.';
        this.ensureMinimumQuestions();
        this.loading = false;
      }
    });
  }

  private loadHistory(): void {
    this.examService.getHistory().subscribe({
      next: res => {
        this.history = res.content;
      },
      error: () => {
        this.history = [];
      }
    });
  }

  private loadActiveSession(): void {
    this.examService.getActiveSession().subscribe({
      next: session => {
        this.activeSession = session;
        const saved = this.storage.get(this.progressKey);
        if (session && saved) {
          try {
            const parsed = JSON.parse(saved) as {
              sessionId: number;
              questions: QuestionCard[];
              notes: string;
              moodScore: number | null;
            };
            if (parsed.sessionId === session.id) {
              const restored = parsed.questions || [];
              const hasTypes = restored.every(item => item.responseType);
              if (hasTypes) {
                this.questions = restored;
                this.notes = parsed.notes || '';
                this.moodScore = parsed.moodScore ?? null;
              } else {
                this.storage.remove(this.progressKey);
                this.resetResponses();
              }
            }
          } catch {
            this.storage.remove(this.progressKey);
          }
        } else if (!session) {
          this.storage.remove(this.progressKey);
          this.resetResponses();
        }
        this.expandedQuestionId = null;
      },
      error: () => {
        this.activeSession = null;
      }
    });
  }

  private persistProgress(): void {
    if (!this.activeSession) {
      return;
    }
    const payload = {
      sessionId: this.activeSession.id,
      questions: this.questions,
      notes: this.notes,
      moodScore: this.moodScore
    };
    this.storage.set(this.progressKey, JSON.stringify(payload));
    this.showAutosave = true;
    if (this.autosaveTimeout) {
      clearTimeout(this.autosaveTimeout);
    }
    this.autosaveTimeout = setTimeout(() => {
      this.showAutosave = false;
    }, 1200);
  }

  private resetResponses(): void {
    this.questions = this.questions.map(q => ({
      ...q,
      answerText: undefined,
      reflectionText: undefined,
      feelingScore: undefined,
      answeredAt: undefined,
      validationMessage: '',
      status: 'incomplete'
    }));
    this.notes = '';
    this.moodScore = null;
    this.autoSubmitTriggered = false;
  }

  get totalQuestions(): number {
    return this.questions.length;
  }

  get answeredCount(): number {
    return this.questions.filter(q => q.status === 'completed').length;
  }

  get progressPercent(): number {
    if (!this.totalQuestions) return 0;
    return Math.round((this.answeredCount / this.totalQuestions) * 100);
  }

  get unansweredQuestionId(): number | null {
    for (const q of this.questions) {
      if (q.status !== 'completed') return q.id;
    }
    return null;
  }

  scrollToNextUnanswered(): void {
    const id = this.unansweredQuestionId;
    if (id == null) {
      return;
    }
    const el = document.getElementById(`question-${id}`);
    if (el) {
      if (typeof el.scrollIntoView === 'function') {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
      el.classList.add('pulse');
      setTimeout(() => el.classList.remove('pulse'), 900);
    }
  }

  get submitEnabled(): boolean {
    return !!this.activeSession && this.questions.length > 0 && this.questions.every(q => q.status === 'completed');
  }

  get averageFeelingScore(): number | null {
    const scores = this.questions
      .map(q => q.feelingScore)
      .filter((value): value is number => value != null);
    if (!scores.length) {
      return null;
    }
    const total = scores.reduce((sum, value) => sum + value, 0);
    return Math.round(total / scores.length);
  }

  get sessionMoodScore(): number | null {
    return this.moodScore ?? this.averageFeelingScore;
  }

  get lastSession(): SessionSummary | null {
    return this.history.length ? this.history[0] : null;
  }

  get cooldownRemaining(): string | null {
    if (!this.lastSession?.completedAt) {
      return null;
    }
    const completedAt = new Date(this.lastSession.completedAt).getTime();
    if (Number.isNaN(completedAt)) {
      return null;
    }
    const nextAllowed = completedAt + 24 * 60 * 60 * 1000;
    const diffMs = nextAllowed - Date.now();
    if (diffMs <= 0) {
      return null;
    }
    const hours = Math.floor(diffMs / (60 * 60 * 1000));
    const minutes = Math.floor((diffMs % (60 * 60 * 1000)) / (60 * 1000));
    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }
    return `${minutes}m`;
  }

  get canStartSession(): boolean {
    return !this.activeSession && !this.cooldownRemaining;
  }

  private mapQuestions(questions: Question[], existingById: Map<number, QuestionCard>): QuestionCard[] {
    const sorted = [...questions].sort((a, b) => {
      const aOrder = a.orderNumber ?? 0;
      const bOrder = b.orderNumber ?? 0;
      if (aOrder !== bOrder) return aOrder - bOrder;
      return a.id - b.id;
    });

    return sorted.map((q, idx) => {
      const existing = existingById.get(q.id);
      const mapped: QuestionCard = {
        id: q.id,
        title: `Question ${idx + 1}`,
        text: q.text,
        instruction: this.instructionFor(),
        responseType: q.responseType,
        categoryName: q.category?.name,
        status: 'incomplete',
        defaultQuestion: !q.custom
      };

      if (existing?.answerText) {
        mapped.answerText = existing.answerText;
        mapped.answeredAt = existing.answeredAt;
        mapped.status = existing.status ?? 'incomplete';
        mapped.reflectionText = existing.reflectionText;
        mapped.feelingScore = existing.feelingScore;
        mapped.validationMessage = existing.validationMessage;
      }

      return mapped;
    });
  }

  private ensureMinimumQuestions(): void {
    if (this.questions.length === 0) {
      this.questions = FALLBACK_DEFAULTS.map((q, idx) => ({
        ...q,
        title: `Question ${idx + 1}`,
        defaultQuestion: true
      }));
      return;
    }
    if (this.questions.length < 5) {
      const needed = 5 - this.questions.length;
      const filler = FALLBACK_DEFAULTS.slice(0, needed).map((q, idx) => ({
        ...q,
        id: q.id + idx + 100,
        title: `Question ${this.questions.length + idx + 1}`,
        defaultQuestion: true
      }));
      this.questions = [...this.questions, ...filler];
    }
  }

  private updateQuestionStatus(questionId: number): void {
    const question = this.questions.find(q => q.id === questionId);
    if (!question) return;
    if (
      question.reflectionText?.trim() &&
      question.feelingScore != null
    ) {
      question.status = 'completed';
    } else {
      question.status = 'incomplete';
    }
  }

  private instructionFor(): string {
    return 'Write a short reflection and rate your feeling.';
  }

  private markQuestionDirty(question: QuestionCard): void {
    if (question.status === 'completed') {
      question.status = 'incomplete';
    }
    question.validationMessage = '';
  }

  private maybeAutoSubmit(): void {
    if (this.submitEnabled && !this.submitting && !this.autoSubmitTriggered) {
      this.submit();
    }
  }
}
