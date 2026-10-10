import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TodoService, Todo } from '../services/todo.service';
import { MotionDirective } from '../../../motion/motion.directive';
import { IconComponent } from '../../../ui/icon/icon.component';

@Component({
  selector: 'app-todo-list',
  standalone: true,
  imports: [CommonModule, FormsModule, MotionDirective, IconComponent],
  templateUrl: './todo-list.component.html',
  styleUrls: ['./todo-list.component.scss']
})
export class TodoListComponent implements OnInit {
  readonly maxTitleLength = 255;
  todos: Todo[] = [];
  loading = false;
  draftTitle = '';
  draftDueAt = '';
  error = '';
  filterRange: 'all' | 'overdue' | 'hour' | 'day' | 'month' = 'all';

  /** Filter options rendered as a segmented control. */
  readonly filterOptions = [
    { value: 'all', label: 'All' },
    { value: 'overdue', label: 'Overdue' },
    { value: 'hour', label: 'Next hour' },
    { value: 'day', label: 'Today' },
    { value: 'month', label: 'This month' }
  ] as const;

  constructor(private todoService: TodoService) {}

  ngOnInit(): void {
    this.loading = true;
    this.todoService.getAll().subscribe({
      next: (data: Todo[]) => {
        this.todos = data;
        this.sortTodos();
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load your todos. Please try again.';
        this.loading = false;
      }
    });
  }

  toggleDone(todo: Todo): void {
    this.error = '';
    this.todoService.toggle(todo.id).subscribe({
      next: updated => {
        this.todos = this.todos.map(t => t.id === updated.id ? updated : t);
        this.sortTodos();
      },
      error: () => this.error = 'Could not update this todo.'
    });
  }

  create(): void {
    this.error = '';
    const title = this.draftTitle.trim();
    const due = new Date(this.draftDueAt);
    if (!title || !this.draftDueAt || Number.isNaN(due.getTime())) {
      this.error = 'Title and due time are required.';
      return;
    }
    // datetime-local is in the browser's zone; send an absolute instant.
    this.todoService.create({ title, dueAt: due.toISOString() }).subscribe({
      next: todo => {
        this.todos = [...this.todos, todo];
        this.sortTodos();
        this.draftTitle = '';
        this.draftDueAt = '';
      },
      error: () => this.error = 'Failed to create todo.'
    });
  }

  remove(todo: Todo, event: Event): void {
    event.stopPropagation();
    if (typeof window !== 'undefined' && !window.confirm(`Delete "${todo.title}"?`)) {
      return;
    }
    this.error = '';
    this.todoService.delete(todo.id).subscribe({
      next: () => {
        this.todos = this.todos.filter(t => t.id !== todo.id);
      },
      error: () => this.error = 'Could not delete this todo.'
    });
  }

  get totalCount(): number {
    return this.todos.length;
  }

  get completedCount(): number {
    return this.todos.filter(todo => todo.completed).length;
  }

  get pendingCount(): number {
    return this.todos.filter(todo => !todo.completed).length;
  }

  get dueTodayCount(): number {
    const now = new Date();
    return this.todos.filter(todo => !todo.completed && todo.dueAt && this.isSameDay(new Date(todo.dueAt), now)).length;
  }

  get overdueCount(): number {
    return this.todos.filter(todo => this.isOverdue(todo)).length;
  }

  get emptyMessage(): string {
    switch (this.filterRange) {
      case 'overdue': return 'Nothing overdue. Well done.';
      case 'hour': return 'Nothing due in the next hour.';
      case 'day': return 'Nothing due today.';
      case 'month': return 'Nothing due this month.';
      default: return 'No todos yet. Add a few tasks to stay on track.';
    }
  }

  get filteredTodos(): Todo[] {
    const now = new Date();
    return this.todos.filter(t => {
      if (this.filterRange === 'all') {
        return true;
      }
      if (!t.dueAt) {
        return false;
      }
      const due = new Date(t.dueAt);
      switch (this.filterRange) {
        case 'overdue':
          return this.isOverdue(t);
        case 'hour': {
          const diff = due.getTime() - now.getTime();
          return diff >= 0 && diff <= 60 * 60 * 1000;
        }
        case 'day':
          return this.isSameDay(due, now);
        case 'month':
          return due.getMonth() === now.getMonth() && due.getFullYear() === now.getFullYear();
      }
    });
  }

  get completionPercent(): number {
    return this.totalCount ? Math.round((this.completedCount / this.totalCount) * 100) : 0;
  }

  isOverdue(todo: Todo): boolean {
    return !todo.completed && !!todo.dueAt && new Date(todo.dueAt).getTime() < Date.now();
  }

  private isSameDay(a: Date, b: Date): boolean {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
  }

  private sortTodos(): void {
    const time = (t: Todo) => (t.dueAt ? new Date(t.dueAt).getTime() : Number.MAX_SAFE_INTEGER);
    this.todos = [...this.todos].sort((a, b) => {
      if (a.completed !== b.completed) {
        return a.completed ? 1 : -1;
      }
      return time(a) - time(b);
    });
  }
}
