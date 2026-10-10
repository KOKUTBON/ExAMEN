import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { of, Subject, NEVER } from 'rxjs';
import { provideRouter } from '@angular/router';
import { ExaminationTodayComponent } from './examination-today.component';
import { ExaminationService } from '../services/examination.service';
import { LocalStorageService } from '../../../../core/services/local-storage.service';
import { ProfileService } from '../../profile/services/profile.service';

describe('ExaminationTodayComponent UI and Accessibility', () => {
  let component: ExaminationTodayComponent;
  let fixture: ComponentFixture<ExaminationTodayComponent>;
  let examService: {
    startSession: ReturnType<typeof vi.fn>;
    submitSession: ReturnType<typeof vi.fn>;
    getCategories: ReturnType<typeof vi.fn>;
    getQuestionsByCategory: ReturnType<typeof vi.fn>;
    getHistory: ReturnType<typeof vi.fn>;
    getActiveSession: ReturnType<typeof vi.fn>;
  };
  let storageService: {
    get: ReturnType<typeof vi.fn>;
    set: ReturnType<typeof vi.fn>;
    remove: ReturnType<typeof vi.fn>;
  };
  let profileService: {
    getSummaryForLast30Days: ReturnType<typeof vi.fn>;
    getAnalytics: ReturnType<typeof vi.fn>;
    getProgress: ReturnType<typeof vi.fn>;
    refresh$: Subject<void>;
  };

  const mockCategories = [{ id: 1, name: 'Reflection', description: 'Prompts' }];
  const mockQuestions = [
    {
      id: 101,
      text: 'Prompt 1',
      orderNumber: 1,
      responseType: 'SCALE_1_5' as const,
      custom: false,
      category: mockCategories[0]
    },
    {
      id: 102,
      text: 'Prompt 2',
      orderNumber: 2,
      responseType: 'SCALE_1_5' as const,
      custom: false,
      category: mockCategories[0]
    }
  ];

  beforeEach(() => {
    examService = {
      startSession: vi.fn().mockReturnValue(of({ id: 42, userId: 1, startedAt: new Date().toISOString() })),
      submitSession: vi.fn().mockReturnValue(of({ id: 42, completedAt: new Date().toISOString() })),
      getCategories: vi.fn().mockReturnValue(of(mockCategories)),
      getQuestionsByCategory: vi.fn().mockReturnValue(of(mockQuestions)),
      getHistory: vi.fn().mockReturnValue(of({ content: [] })),
      getActiveSession: vi.fn().mockReturnValue(of({ id: 42, userId: 1, startedAt: new Date().toISOString() }))
    };

    storageService = {
      get: vi.fn().mockReturnValue(null),
      set: vi.fn(),
      remove: vi.fn()
    };

    profileService = {
      getSummaryForLast30Days: vi.fn().mockReturnValue(of({})),
      getAnalytics: vi.fn().mockReturnValue(of({})),
      getProgress: vi.fn().mockReturnValue(of({})),
      refresh$: new Subject<void>()
    };

    TestBed.configureTestingModule({
      imports: [ExaminationTodayComponent],
      providers: [
        { provide: ExaminationService, useValue: examService },
        { provide: LocalStorageService, useValue: storageService },
        { provide: ProfileService, useValue: profileService },
        provideRouter([])
      ]
    });

    fixture = TestBed.createComponent(ExaminationTodayComponent);
    component = fixture.componentInstance;
  });

  it('renders realistic skeleton loaders with aria-busy when loading', () => {
    examService.getCategories.mockReturnValue(NEVER);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const skeletonRegion = compiled.querySelector('.skeletons');
    expect(skeletonRegion).not.toBeNull();
    expect(skeletonRegion?.getAttribute('role')).toBe('status');
    expect(skeletonRegion?.getAttribute('aria-busy')).toBe('true');
    expect(skeletonRegion?.getAttribute('aria-live')).toBe('polite');

    const skeletonCards = compiled.querySelectorAll('.skeleton-card');
    expect(skeletonCards.length).toBe(5);

    const srOnly = skeletonRegion?.querySelector('.sr-only');
    expect(srOnly?.textContent).toContain('Loading examination questions');
  });

  it('transitions from skeletons to question cards after data loads', () => {
    fixture.detectChanges();

    expect(component.loading).toBe(false);
    expect(component.questions.length).toBeGreaterThanOrEqual(5);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.skeletons')).toBeNull();
    expect(compiled.querySelectorAll('.question-card').length).toBe(component.questions.length);
  });

  it('provides accessible progressbar attributes in the hero panel', () => {
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const progressbar = compiled.querySelector('.progress-bar');
    expect(progressbar).not.toBeNull();
    expect(progressbar?.getAttribute('role')).toBe('progressbar');
    expect(progressbar?.getAttribute('aria-valuemin')).toBe('0');
    expect(progressbar?.getAttribute('aria-valuenow')).toBe('0');
    expect(progressbar?.getAttribute('aria-valuemax')).toBe(String(component.totalQuestions));
  });

  it('toggles question expansion via header trigger button with ARIA attributes', () => {
    fixture.detectChanges();

    const q = component.questions[0];
    const toggleBtn = fixture.nativeElement.querySelector(`#question-toggle-${q.id}`) as HTMLButtonElement;
    expect(toggleBtn).not.toBeNull();
    expect(toggleBtn.getAttribute('aria-expanded')).toBe('false');

    toggleBtn.click();
    fixture.detectChanges();
    expect(component.expandedQuestionId).toBe(q.id);
    expect(toggleBtn.getAttribute('aria-expanded')).toBe('true');

    toggleBtn.click();
    fixture.detectChanges();
    expect(component.expandedQuestionId).toBeNull();
    expect(toggleBtn.getAttribute('aria-expanded')).toBe('false');
  });

  it('handles Escape key to collapse question', () => {
    fixture.detectChanges();
    component.activeSession = { id: 42, userId: 1, startedAt: new Date().toISOString() };
    const q = component.questions[0];
    component.expandQuestion(q.id, false);
    expect(component.expandedQuestionId).toBe(q.id);

    const escapeEvent = new KeyboardEvent('keydown', { key: 'Escape' });
    component.onTextareaKeydown(escapeEvent, q);
    expect(component.expandedQuestionId).toBeNull();
  });

  it('handles Ctrl+Enter inside textarea to submit question response', () => {
    fixture.detectChanges();
    component.activeSession = { id: 42, userId: 1, startedAt: new Date().toISOString() };
    const q = component.questions[0];
    component.expandQuestion(q.id, false);

    q.reflectionText = 'Grateful for kindness today';
    q.feelingScore = 4;

    const ctrlEnterEvent = new KeyboardEvent('keydown', { key: 'Enter', ctrlKey: true });
    vi.spyOn(ctrlEnterEvent, 'preventDefault');

    component.onTextareaKeydown(ctrlEnterEvent, q);

    expect(ctrlEnterEvent.preventDefault).toHaveBeenCalled();
    expect(q.status).toBe('completed');
    expect(component.liveAnnouncement).toContain(`Question ${q.id} completed`);
  });

  it('navigates feeling scores using Arrow keys in feeling radiogroup', () => {
    fixture.detectChanges();
    component.activeSession = { id: 42, userId: 1, startedAt: new Date().toISOString() };
    const q = component.questions[0];

    const arrowRight = new KeyboardEvent('keydown', { key: 'ArrowRight' });
    vi.spyOn(arrowRight, 'preventDefault');
    component.onFeelingKeydown(arrowRight, q.id, 1);
    expect(arrowRight.preventDefault).toHaveBeenCalled();
    expect(q.feelingScore).toBe(2);

    const arrowLeft = new KeyboardEvent('keydown', { key: 'ArrowLeft' });
    component.onFeelingKeydown(arrowLeft, q.id, 2);
    expect(q.feelingScore).toBe(1);

    const endKey = new KeyboardEvent('keydown', { key: 'End' });
    component.onFeelingKeydown(endKey, q.id, 1);
    expect(q.feelingScore).toBe(5);
  });

  it('navigates overall session mood score using Arrow keys', () => {
    fixture.detectChanges();
    component.activeSession = { id: 42, userId: 1, startedAt: new Date().toISOString() };

    component.moodScore = 3;
    const arrowRight = new KeyboardEvent('keydown', { key: 'ArrowRight' });
    component.onMoodKeydown(arrowRight);
    expect(component.moodScore).toBe(4);

    const homeKey = new KeyboardEvent('keydown', { key: 'Home' });
    component.onMoodKeydown(homeKey);
    expect(component.moodScore).toBe(1);
  });

  it('supports Alt+N global shortcut to jump to next incomplete question', () => {
    fixture.detectChanges();
    component.activeSession = { id: 42, userId: 1, startedAt: new Date().toISOString() };

    const scrollSpy = vi.spyOn(component, 'scrollToNextUnanswered');
    const altNEvent = new KeyboardEvent('keydown', { key: 'n', altKey: true });
    vi.spyOn(altNEvent, 'preventDefault');

    component.onGlobalKeydown(altNEvent);
    expect(altNEvent.preventDefault).toHaveBeenCalled();
    expect(scrollSpy).toHaveBeenCalled();
  });

  it('updates live announcements when session starts', () => {
    fixture.detectChanges();
    component.startSession();
    expect(component.activeSession?.id).toBe(42);
    expect(component.liveAnnouncement).toContain('Daily examination session started');
  });
});
