import { TestBed } from '@angular/core/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { of } from 'rxjs';
import { TodoListComponent } from './todo-list.component';
import { TodoService, Todo } from '../services/todo.service';
import { MotionService } from '../../../motion/motion.service';

describe('TodoListComponent', () => {
  let component: TodoListComponent;
  let todoService: {
    getAll: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    toggle: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    todoService = {
      getAll: vi.fn(),
      create: vi.fn(),
      toggle: vi.fn(),
      delete: vi.fn()
    };

    const todos: Todo[] = [
      { id: 1, title: 'Morning examen', completed: false, dueAt: '2025-01-01T10:00:00Z' },
      { id: 2, title: 'Journal note', completed: true, dueAt: '2025-01-02T12:00:00Z' }
    ];

    todoService.getAll.mockReturnValue(of(todos));
    todoService.create.mockReturnValue(of({
      id: 3,
      title: 'Prayer',
      completed: false,
      dueAt: '2025-01-03T08:00:00Z'
    }));

    TestBed.configureTestingModule({
      imports: [TodoListComponent],
      providers: [
        { provide: TodoService, useValue: todoService },
        { provide: MotionService, useValue: { enabled: false, register: () => () => {} } }
      ]
    });

    const fixture = TestBed.createComponent(TodoListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads todos on init', () => {
    expect(component.totalCount).toBe(2);
    expect(component.completedCount).toBe(1);
    expect(component.pendingCount).toBe(1);
  });

  it('creates a todo when form is valid', () => {
    component.draftTitle = 'Prayer';
    component.draftDueAt = '2025-01-03T08:00:00Z';

    component.create();

    expect(todoService.create).toHaveBeenCalledWith({
      title: 'Prayer',
      dueAt: '2025-01-03T08:00:00.000Z'
    });
    expect(component.totalCount).toBe(3);
  });

  it('converts a local datetime-local value to an ISO instant', () => {
    component.draftTitle = 'Evening';
    component.draftDueAt = '2025-01-03T20:30';

    component.create();

    expect(todoService.create).toHaveBeenCalledWith({
      title: 'Evening',
      dueAt: new Date('2025-01-03T20:30').toISOString()
    });
  });

  it('filters overdue todos', () => {
    component.filterRange = 'overdue';
    expect(component.filteredTodos.map(t => t.id)).toEqual([1]);
    expect(component.overdueCount).toBe(1);
  });
});
