import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfigService } from '../../../../core/services/api-config.service';

export interface Todo {
  id: number;
  title: string;
  completed: boolean;
  dueAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class TodoService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    private api: ApiConfigService
  ) {
    this.baseUrl = this.api.endpoint('todos');
  }

  getAll(): Observable<Todo[]> {
    return this.http.get<Todo[]>(this.baseUrl);
  }

  create(todo: Partial<Todo>): Observable<Todo> {
    return this.http.post<Todo>(this.baseUrl, todo);
  }

  update(id: number, todo: Partial<Todo>): Observable<Todo> {
    return this.http.patch<Todo>(`${this.baseUrl}/${id}`, todo);
  }

  toggle(id: number): Observable<Todo> {
    return this.http.patch<Todo>(`${this.baseUrl}/${id}/toggle`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
