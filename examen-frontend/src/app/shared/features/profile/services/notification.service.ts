import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfigService } from '../../../../core/services/api-config.service';

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  type: string;
  createdAt: string;
  readAt: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    private api: ApiConfigService
  ) {
    this.baseUrl = this.api.endpoint('notifications');
  }

  list(unreadOnly = false): Observable<NotificationItem[]> {
    return this.http.get<NotificationItem[]>(this.baseUrl, { params: { unreadOnly } });
  }

  markRead(id: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/read`, {});
  }
}
