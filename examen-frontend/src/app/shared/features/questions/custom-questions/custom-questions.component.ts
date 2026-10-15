import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Category, CategoryService } from '../../../../core/services/category.service';
import { Question, QuestionService } from '../../../../core/services/question.service';
import { MotionDirective } from '../../../motion/motion.directive';
import { IconComponent } from '../../../ui/icon/icon.component';

@Component({
  selector: 'app-custom-questions',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MotionDirective, IconComponent],
  templateUrl: './custom-questions.component.html',
  styleUrls: ['./custom-questions.component.scss']
})
export class CustomQuestionsComponent implements OnInit {
  form!: FormGroup;
  categoryForm!: FormGroup;
  categories: Category[] = [];
  aiQuestions: Question[] = [];
  customQuestions: Question[] = [];
  loading = false;
  error = '';
  editingId: number | null = null;
  categoryEditingId: number | null = null;
  categorySaving = false;
  categoryError = '';

  /** Keyword -> movement mapping used to give categories an icon and color. */
  private readonly categoryThemes = [
    { match: /grat|thank|relish|joy|bless/i, icon: 'heart', tone: 'relish' },
    { match: /light|pray|request|grace|spirit/i, icon: 'light', tone: 'request' },
    { match: /review|reflect|day|aware|consci/i, icon: 'eye', tone: 'review' },
    { match: /repent|forgiv|sin|mercy|heal/i, icon: 'droplet', tone: 'repent' },
    { match: /resol|goal|habit|tomorrow|grow/i, icon: 'sprout', tone: 'resolve' },
    { match: /relation|family|friend|love|other/i, icon: 'user', tone: 'repent' }
  ] as const;

  categoryTheme(name: string | null | undefined): { icon: string; tone: string } {
    const theme = this.categoryThemes.find(t => t.match.test(name || ''));
    return theme ? { icon: theme.icon, tone: theme.tone } : { icon: 'layers', tone: 'review' };
  }

  responseLabel(question: Question): string {
    switch (question.responseType) {
      case 'YES_NO': return 'Yes / No';
      case 'SCALE_1_5': return 'Scale 1–5';
      default: return 'Reflection';
    }
  }

  get isEditing(): boolean {
    return this.editingId !== null;
  }

  get isCategoryEditing(): boolean {
    return this.categoryEditingId !== null;
  }

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private questionService: QuestionService
  ) {
    this.form = this.fb.group({
      text: ['', [Validators.required, Validators.maxLength(500)]],
      categoryId: [null, Validators.required]
    });
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(120)]],
      description: ['', [Validators.maxLength(240)]]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadQuestions();
  }

  loadCategories(): void {
    this.categoryError = '';
    this.categoryService.getAll().subscribe({
      next: res => (this.categories = [...res].sort((a, b) => a.name.localeCompare(b.name))),
      error: () => (this.categoryError = 'Failed to load categories')
    });
  }

  loadQuestions(): void {
    this.loading = true;
    this.error = '';

    forkJoin({
      all: this.questionService.getAll(200),
      mine: this.questionService.getMine()
    }).subscribe({
      next: res => {
        this.aiQuestions = (res.all || []).filter(q => !q.custom);
        this.customQuestions = res.mine || [];
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load questions';
        this.loading = false;
      }
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';

    const payload = this.form.value;

    const request$ = this.isEditing && this.editingId
      ? this.questionService.updateCustom(this.editingId, payload)
      : this.questionService.createCustom(payload);

    request$.subscribe({
      next: q => {
        if (this.isEditing && this.editingId) {
          this.customQuestions = this.customQuestions.map(item =>
            item.id === this.editingId ? q : item
          );
        } else {
          this.customQuestions = [q, ...this.customQuestions];
        }
        this.resetForm();
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Could not save question';
        this.loading = false;
      }
    });
  }

  startEdit(question: Question): void {
    if (!question.id) return;
    this.editingId = question.id;
    this.form.patchValue({
      text: question.text,
      categoryId: question.category?.id || null
    });
  }

  cancelEdit(): void {
    this.resetForm();
  }

  delete(question: Question): void {
    if (!question.id) return;
    if (!confirm('Delete this question?')) return;

    this.questionService.deleteCustom(question.id).subscribe({
      next: () => {
        this.customQuestions = this.customQuestions.filter(q => q.id !== question.id);
        if (this.editingId === question.id) {
          this.resetForm();
        }
      },
      error: (err) => {
        this.error = err.error?.message || 'Could not delete question';
      }
    });
  }

  submitCategory(): void {
    if (this.categoryForm.invalid) return;
    this.categorySaving = true;
    this.categoryError = '';

    const payload = this.categoryForm.value as Category;
    const request$ = this.isCategoryEditing && this.categoryEditingId
      ? this.categoryService.update(this.categoryEditingId, payload)
      : this.categoryService.create(payload);

    request$.subscribe({
      next: category => {
        if (this.isCategoryEditing && this.categoryEditingId) {
          this.categories = this.categories.map(item =>
            item.id === this.categoryEditingId ? category : item
          );
        } else {
          this.categories = [category, ...this.categories];
        }
        this.categories = [...this.categories].sort((a, b) => a.name.localeCompare(b.name));
        this.resetCategoryForm();
        this.categorySaving = false;
      },
      error: (err) => {
        this.categoryError = err.error?.message || 'Could not save category';
        this.categorySaving = false;
      }
    });
  }

  startCategoryEdit(category: Category): void {
    if (!category.id) return;
    this.categoryEditingId = category.id;
    this.categoryForm.patchValue({
      name: category.name,
      description: category.description
    });
  }

  cancelCategoryEdit(): void {
    this.resetCategoryForm();
  }

  deleteCategory(category: Category): void {
    if (!category.id) return;
    if (!confirm('Delete this category?')) return;

    this.categoryService.delete(category.id).subscribe({
      next: () => {
        this.categories = this.categories.filter(c => c.id !== category.id);
        if (this.categoryEditingId === category.id) {
          this.resetCategoryForm();
        }
        if (this.form.value.categoryId === category.id) {
          this.form.patchValue({ categoryId: null });
        }
      },
      error: (err) => {
        this.categoryError = err.error?.message || 'Could not delete category';
      }
    });
  }

  private resetForm(): void {
    this.editingId = null;
    this.form.reset();
  }

  private resetCategoryForm(): void {
    this.categoryEditingId = null;
    this.categoryForm.reset({ name: '', description: '' });
  }
}
