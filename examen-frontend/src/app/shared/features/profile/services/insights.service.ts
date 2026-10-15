import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfigService } from '../../../../core/services/api-config.service';

export interface EvidenceSnippet {
  source: string;
  date?: string;
  snippet: string;
  rationale?: string;
}

export interface InsightsSummary {
  summary: string;
  highlights: string[];
  periodDays: number;
  sessionsCompleted: number;
  averageFeeling: number | null;
  confidenceScore?: number;
  evidenceSnippets?: EvidenceSnippet[];
  generatedAt: string;
}

export interface FollowUpPrompt {
  question: string;
  rationale: string;
  sourceContext: string;
  targetFocus?: string;
  generatedAt?: string;
}

export interface QuestionSuggestions {
  focus: string;
  suggestions: string[];
  followUps?: FollowUpPrompt[];
  generatedAt: string;
}

export interface SessionInsight {
  sessionId: number;
  summary: string;
  insights: string[];
  nextSteps: string[];
  averageFeeling: number | null;
  generatedAt: string;
}

export interface SemanticTag {
  id?: number;
  sessionId?: number;
  answerId?: number;
  category: 'VIRTUE' | 'STRUGGLE' | 'GRATITUDE' | 'STRESSOR';
  tag: string;
  confidence?: number;
  createdAt?: string;
}

export interface TagCount {
  tag: string;
  count: number;
}

export interface SemanticTagSummary {
  periodDays: number;
  totalTags: number;
  virtues: TagCount[];
  struggles: TagCount[];
  gratitude: TagCount[];
  stressors: TagCount[];
  generatedAt: string;
}

export interface WeeklyNarrative {
  title: string;
  narrative: string;
  periodStart: string;
  periodEnd: string;
  sessionsAnalyzed: number;
  averageFeeling?: number | null;
  virtuesObserved: string[];
  strugglesIdentified: string[];
  gratitudeThemes: string[];
  stressorSignals: string[];
  actionableAdvice: string[];
  generatedAt: string;
}

export interface InsightFeedbackRequest {
  targetType: string;
  targetId?: string;
  helpful: boolean;
  comments?: string;
}

export interface InsightFeedbackResponse {
  id: number;
  targetType: string;
  targetId?: string;
  helpful: boolean;
  comments?: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class InsightsService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    private api: ApiConfigService
  ) {
    this.baseUrl = this.api.endpoint('insights');
  }

  getSummary(days = 30): Observable<InsightsSummary> {
    return this.http.get<InsightsSummary>(`${this.baseUrl}/summary`, { params: { days } });
  }

  suggestQuestions(focus: string, count = 5): Observable<QuestionSuggestions> {
    return this.http.post<QuestionSuggestions>(`${this.baseUrl}/questions`, { focus, count });
  }

  getFollowUpPrompts(count = 4): Observable<FollowUpPrompt[]> {
    return this.http.get<FollowUpPrompt[]>(`${this.baseUrl}/follow-ups`, { params: { count } });
  }

  analyzeSession(sessionId: number): Observable<SessionInsight> {
    return this.http.post<SessionInsight>(`${this.baseUrl}/session`, { sessionId });
  }

  tagSession(sessionId: number): Observable<SemanticTag[]> {
    return this.http.post<SemanticTag[]>(`${this.baseUrl}/sessions/${sessionId}/tag`, {});
  }

  getSessionTags(sessionId: number): Observable<SemanticTag[]> {
    return this.http.get<SemanticTag[]>(`${this.baseUrl}/sessions/${sessionId}/tags`);
  }

  getTagSummary(days = 30): Observable<SemanticTagSummary> {
    return this.http.get<SemanticTagSummary>(`${this.baseUrl}/tags/summary`, { params: { days } });
  }

  getWeeklyNarrative(days = 7): Observable<WeeklyNarrative> {
    return this.http.get<WeeklyNarrative>(`${this.baseUrl}/weekly-narrative`, { params: { days } });
  }

  recordFeedback(request: InsightFeedbackRequest): Observable<InsightFeedbackResponse> {
    return this.http.post<InsightFeedbackResponse>(`${this.baseUrl}/feedback`, request);
  }

  getUserFeedback(): Observable<InsightFeedbackResponse[]> {
    return this.http.get<InsightFeedbackResponse[]>(`${this.baseUrl}/feedback`);
  }
}
