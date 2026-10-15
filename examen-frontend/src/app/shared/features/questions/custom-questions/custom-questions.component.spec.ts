import { TestBed } from '@angular/core/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { of } from 'rxjs';
import { CustomQuestionsComponent } from './custom-questions.component';
import { CategoryService } from '../../../../core/services/category.service';
import { QuestionService } from '../../../../core/services/question.service';
import { MotionService } from '../../../motion/motion.service';

describe('CustomQuestionsComponent', () => {
  let component: CustomQuestionsComponent;
  let questionService: {
    getAll: ReturnType<typeof vi.fn>;
    getMine: ReturnType<typeof vi.fn>;
    createCustom: ReturnType<typeof vi.fn>;
    updateCustom: ReturnType<typeof vi.fn>;
    deleteCustom: ReturnType<typeof vi.fn>;
  };
  let categoryService: { getAll: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    questionService = {
      getAll: vi.fn(),
      getMine: vi.fn(),
      createCustom: vi.fn(),
      updateCustom: vi.fn(),
      deleteCustom: vi.fn()
    };
    categoryService = {
      getAll: vi.fn()
    };

    const category = { id: 1, name: 'Reflection', description: 'Core' };
    questionService.getAll.mockReturnValue(of([
      { id: 1, text: 'AI prompt', category, custom: false }
    ]));
    questionService.getMine.mockReturnValue(of([
      { id: 2, text: 'Custom prompt', category, custom: true }
    ]));
    questionService.createCustom.mockReturnValue(of({
      id: 3,
      text: 'New prompt',
      category,
      custom: true
    }));
    categoryService.getAll.mockReturnValue(of([category]));

    TestBed.configureTestingModule({
      imports: [CustomQuestionsComponent],
      providers: [
        { provide: QuestionService, useValue: questionService },
        { provide: CategoryService, useValue: categoryService },
        { provide: MotionService, useValue: { enabled: false, register: () => () => {} } }
      ]
    });

    const fixture = TestBed.createComponent(CustomQuestionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads AI and custom questions', () => {
    expect(component.aiQuestions.length).toBe(1);
    expect(component.customQuestions.length).toBe(1);
  });

  it('creates a custom question', () => {
    component.form.setValue({ text: 'New prompt', categoryId: 1 });
    component.submit();

    expect(questionService.createCustom).toHaveBeenCalled();
    expect(component.customQuestions.some(q => q.id === 3)).toBe(true);
  });
});
