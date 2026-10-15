import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CategoryService } from '../../core/services/category.service';
import { IconComponent } from '../../shared/ui/icon/icon.component';

@Component({
  selector: 'app-category-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, IconComponent],
  templateUrl: './category-edit.component.html',
  styleUrls: ['../category-create/category-create.component.scss', './category-edit.component.scss']
})
export class CategoryEditComponent implements OnInit {
  form!: FormGroup;
  loading = true;
  errorMessage = '';
  private id!: number;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private categoryService: CategoryService
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.id = +idParam;
        this.loadCategory();
      }
    });
  }

  loadCategory(): void {
    this.categoryService.getById(this.id).subscribe({
      next: (cat) => {
        this.form.patchValue(cat);
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load category';
        this.loading = false;
      }
    });
  }

  updateCategory(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    this.categoryService.update(this.id, this.form.value).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/categories']);
      },
      error: () => {
        this.errorMessage = 'Failed to update category';
        this.loading = false;
      }
    });
  }
}
