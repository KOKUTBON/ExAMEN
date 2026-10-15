import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Category, CategoryService } from '../../core/services/category.service';
import { IconComponent } from '../../shared/ui/icon/icon.component';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, IconComponent],
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.scss']
})
export class CategoryListComponent implements OnInit {
  categories: Category[] = [];
  loading = true;
  errorMessage = '';

  /** Row glyphs cycle through the five movement colors. */
  readonly tones = ['m-relish', 'm-request', 'm-review', 'm-repent', 'm-resolve'] as const;

  constructor(
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';

    this.categoryService.getAll().subscribe({
      next: (data: Category[]) => {
        this.categories = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load categories';
        this.loading = false;
      }
    });
  }

  onCreate(): void {
    this.router.navigate(['/categories/create']);
  }

  onEdit(category: Category): void {
    if (!category.id) return;
    this.router.navigate(['/categories/edit', category.id]);
  }

  onDelete(category: Category): void {
    if (!category.id) return;
    if (!confirm('Are you sure you want to delete this category?')) return;

    this.categoryService.delete(category.id).subscribe({
      next: () => {
        this.categories = this.categories.filter(c => c.id !== category.id);
      }
    });
  }
}
