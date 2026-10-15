import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProfileService, ProfileSummary } from '../profile/services/profile.service';
import { ExaminationService } from '../examination/services/examination.service';
import { SessionSummary } from '../examination/models/examination.model';
import { Todo, TodoService } from '../todos/services/todo.service';
import { QuestionService } from '../../../core/services/question.service';
import { CategoryService } from '../../../core/services/category.service';
import { MotionDirective } from '../../motion/motion.directive';
import { IconComponent } from '../../ui/icon/icon.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MotionDirective, IconComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  loading = true;
  summary: ProfileSummary | null = null;
  activeSession: SessionSummary | null = null;
  upcomingTodos: Todo[] = [];
  overdueCount = 0;
  defaultQuestionCount = 0;
  customQuestionCount = 0;
  categoryCount = 0;

  /** The five movements of the examen, shown as tonight's path. */
  readonly movements = [
    { key: 'relish', name: 'Relish', icon: 'heart', hint: 'Give thanks' },
    { key: 'request', name: 'Request', icon: 'light', hint: 'Ask for light' },
    { key: 'review', name: 'Review', icon: 'eye', hint: 'Walk the day' },
    { key: 'repent', name: 'Repent', icon: 'droplet', hint: 'Seek mercy' },
    { key: 'resolve', name: 'Resolve', icon: 'sprout', hint: 'Look ahead' }
  ] as const;

  /** Circumference of the progress ring (r = 52). */
  readonly ringCircumference = 2 * Math.PI * 52;

  constructor(
    private profileService: ProfileService,
    private examinationService: ExaminationService,
    private todoService: TodoService,
    private questionService: QuestionService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  get todayStatus(): string {
    if (this.activeSession) {
      return 'Session in progress';
    }
    if (this.summary?.todayCompleted) {
      return 'Completed today';
    }
    return 'Not started';
  }

  get feelingDisplay(): string {
    const feeling = this.summary?.todayMood;
    return feeling != null ? `${feeling}/5` : '-';
  }

  get greeting(): string {
    const hour = new Date().getHours();
    if (hour < 5) return 'Keeping vigil';
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  }

  get statusTone(): 'active' | 'done' | 'idle' {
    if (this.activeSession) return 'active';
    return this.summary?.todayCompleted ? 'done' : 'idle';
  }

  get progressValue(): number {
    const score = this.summary?.spiritualProgressScore;
    return score != null ? Math.max(0, Math.min(100, score)) : 0;
  }

  get ringOffset(): number {
    return this.ringCircumference * (1 - this.progressValue / 100);
  }

  isTodoOverdue(todo: Todo): boolean {
    return !!todo.dueAt && new Date(todo.dueAt).getTime() < Date.now();
  }

  private load(): void {
    this.loading = true;

    forkJoin({
      summary: this.profileService.getSummaryForLast30Days().pipe(catchError(() => of(null))),
      activeSession: this.examinationService.getActiveSession().pipe(catchError(() => of(null))),
      todos: this.todoService.getAll().pipe(catchError(() => of([]))),
      allQuestions: this.questionService.getAll(200).pipe(catchError(() => of([]))),
      customQuestions: this.questionService.getMine().pipe(catchError(() => of([]))),
      categories: this.categoryService.getAll().pipe(catchError(() => of([])))
    }).subscribe(({ summary, activeSession, todos, allQuestions, customQuestions, categories }) => {
      this.summary = summary;
      this.activeSession = activeSession;
      this.customQuestionCount = customQuestions.length;
      this.defaultQuestionCount = allQuestions.filter(q => !q.custom).length;
      this.categoryCount = categories.length;

      const now = Date.now();
      this.overdueCount = todos.filter(todo => !todo.completed && new Date(todo.dueAt).getTime() < now).length;
      this.upcomingTodos = [...todos]
        .filter(todo => !todo.completed)
        .sort((a, b) => new Date(a.dueAt).getTime() - new Date(b.dueAt).getTime())
        .slice(0, 4);

      this.loading = false;
    });
  }
}
