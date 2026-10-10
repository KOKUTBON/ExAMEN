import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfigService } from '../../core/services/api-config.service';

export interface UserSettingsRequest {
  timeZone: string;
  reminderTime?: string | null;
  theme?: string | null;
  emailReminder?: boolean | null;
  inAppReminder?: boolean | null;
}

export interface UserSettingsResponse extends UserSettingsRequest {}

@Injectable({ providedIn: 'root' })
export class UserSettingsService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    private api: ApiConfigService
  ) {
    this.baseUrl = this.api.endpoint('settings');
  }

  get(): Observable<UserSettingsResponse> {
    return this.http.get<UserSettingsResponse>(this.baseUrl);
  }

  update(payload: UserSettingsRequest): Observable<UserSettingsResponse> {
    return this.http.put<UserSettingsResponse>(this.baseUrl, payload);
  }
}
