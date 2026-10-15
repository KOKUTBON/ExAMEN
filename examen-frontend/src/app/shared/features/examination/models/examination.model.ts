export type QuestionType = 'YES_NO' | 'SCALE_1_5';

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface Question {
  id: number;
  text: string;
  orderNumber?: number;
  difficulty?: string;
  responseType: QuestionType;
  category: Category;
  custom?: boolean;
  defaultQuestion?: boolean;
}

export interface SessionAnswerRequest {
  questionId: number;
  answerText: string;
  correct?: boolean;
  examinationSessionId: number;
  reflectionText?: string;
  feelingScore?: number;
}

export interface SessionSummary {
  id: number;
  userId: number;
  startedAt: string;
  completedAt?: string;
  notes?: string;
   moodScore?: number;
  score?: number;
  categoryScores?: Record<string, number>;
  answers?: SessionAnswerRequest[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
