import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiConfigService } from './api-config.service';

export interface Category {
  id?: number;
  name: string;
  description: string;
}

interface PageResponse<T> {
  content: T[];
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    private api: ApiConfigService
  ) {
    this.baseUrl = this.api.endpoint('categories');
  }

  getAll(): Observable<Category[]> {
    return this.http
      .get<PageResponse<Category>>(this.baseUrl, { params: { page: 0, size: 200 } })
      .pipe(map(res => res.content));
  }

  getById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.baseUrl}/${id}`);
  }

  create(c: Category): Observable<Category> {
    return this.http.post<Category>(this.baseUrl, c);
  }

  update(id: number, c: Category): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl}/${id}`, c);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
