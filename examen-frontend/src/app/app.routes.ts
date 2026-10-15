import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./landing/landing.component').then(m => m.LandingComponent),
    title: 'Examen',
    data: {
      description:
        'Examen is an open-source reflective journaling and examination of conscience app with AI-assisted insights, habits, and analytics.',
      indexable: true
    }
  },

  // AUTH
  {
    path: 'auth',
    title: 'Authentication',
    data: { indexable: false },
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./auth/login/login.component').then(m => m.LoginComponent),
        title: 'Login',
        data: {
          description: 'Sign in to Examen to continue your reflective session workflow.',
          indexable: false
        }
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./auth/register/register.component').then(m => m.RegisterComponent),
        title: 'Register',
        data: {
          description: 'Create an Examen account to start daily reflection tracking.',
          indexable: false
        }
      },
      {
        path: 'reset',
        loadComponent: () =>
          import('./auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent),
        title: 'Reset Password',
        data: {
          description: 'Reset your Examen password securely.',
          indexable: false
        }
      }
    ]
  },

  // Older reset emails linked here; keep them working (query params are preserved).
  { path: 'reset-password', redirectTo: 'auth/reset' },

  // EXAMINATION
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./shared/features/dashboard/dashboard.component')
        .then(m => m.DashboardComponent),
    title: 'Dashboard',
    data: {
      description: 'Track reflective progress, sessions, todos, and prompt library metrics.',
      indexable: false
    }
  },
  {
    path: 'examination',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./shared/features/examination/examination-today/examination-today.component')
        .then(m => m.ExaminationTodayComponent),
    title: 'Examination of Conscience',
    data: {
      description: 'Complete your daily examination session with guided prompts and reflections.',
      indexable: false
    }
  },

  // TODOS
  {
    path: 'todos',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./shared/features/todos/todo-list/todo-list.component')
        .then(m => m.TodoListComponent),
    title: 'Todos',
    data: {
      description: 'Manage todos aligned with your daily reflection habits.',
      indexable: false
    }
  },

  // CATEGORIES
  {
    path: 'categories',
    canActivate: [AuthGuard],
    data: { indexable: false },
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./categories/category-list/category-list.component')
            .then(m => m.CategoryListComponent),
        title: 'Categories',
        data: {
          description: 'Organize reflection prompts with personal categories.',
          indexable: false
        }
      },
      {
        path: 'create',
        loadComponent: () =>
          import('./categories/category-create/category-create.component')
            .then(m => m.CategoryCreateComponent),
        title: 'Create Category',
        data: {
          description: 'Create a category for your prompt library.',
          indexable: false
        }
      },
      {
        path: 'edit/:id',
        loadComponent: () =>
          import('./categories/category-edit/category-edit.component')
            .then(m => m.CategoryEditComponent),
        title: 'Edit Category',
        data: {
          description: 'Update category metadata for reflection prompts.',
          indexable: false
        }
      }
    ]
  },

  // PROFILE
  {
    path: 'profile',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./shared/features/profile/profile-dashboard/profile-dashboard.component')
        .then(m => m.ProfileDashboardComponent),
    title: 'Profile',
    data: {
      description: 'Review trends, summaries, and insights from recent reflection sessions.',
      indexable: false
    }
  },
  {
    path: 'questions',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./shared/features/questions/custom-questions/custom-questions.component')
        .then(m => m.CustomQuestionsComponent),
    title: 'Questions',
    data: {
      description: 'Manage default and custom questions for your daily examen.',
      indexable: false
    }
  },
  {
    path: 'journal',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./shared/features/journal/journal.component')
        .then(m => m.JournalComponent),
    title: 'Journal',
    data: {
      description: 'Capture daily reflective notes in your private journal.',
      indexable: false
    }
  },
  {
    path: 'settings',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./settings/user-settings/user-settings.component')
        .then(m => m.UserSettingsComponent),
    title: 'Settings',
    data: {
      description: 'Configure reminders and personalization settings.',
      indexable: false
    }
  },

  // WILDCARD
  { path: '**', redirectTo: '' }
];
