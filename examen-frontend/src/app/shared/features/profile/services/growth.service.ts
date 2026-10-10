import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfigService } from '../../../../core/services/api-config.service';

export interface WeeklySummary {
  sessionsCompleted: number;
  todosCompleted: number;
  habitsScored: number;
  averageHabitScore: number;
  gratitudeCount: number;
}

export interface GratitudeEntry {
  id: number;
  content: string;
  createdAt: string;
}

export interface HabitScoreEntry {
  id: number;
  habit: string;
  score: number;
  scoreDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class GrowthService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    private api: ApiConfigService
  ) {
    this.baseUrl = this.api.endpoint('growth');
  }

  getWeeklySummary(): Observable<WeeklySummary> {
    return this.http.get<WeeklySummary>(`${this.baseUrl}/weekly-summary`);
  }

  getGratitude(): Observable<GratitudeEntry[]> {
    return this.http.get<GratitudeEntry[]>(`${this.baseUrl}/gratitude`);
  }

  addGratitude(content: string): Observable<GratitudeEntry> {
    return this.http.post<GratitudeEntry>(`${this.baseUrl}/gratitude`, { content });
  }

  getHabits(days = 30): Observable<HabitScoreEntry[]> {
    const params = new HttpParams().set('days', days);
    return this.http.get<HabitScoreEntry[]>(`${this.baseUrl}/habits`, { params });
  }

  addHabitScore(habit: string, score: number): Observable<HabitScoreEntry> {
    return this.http.post<HabitScoreEntry>(`${this.baseUrl}/habits`, { habit, score });
  }

  getSuggestions(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/meditation-suggestions`);
  }

  exportPdf(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export/pdf`, { responseType: 'blob' });
  }
}
