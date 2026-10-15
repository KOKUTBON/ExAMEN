import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfigService } from './api-config.service';

/** Data portability and erasure for the signed-in account. */
@Injectable({ providedIn: 'root' })
export class AccountService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    private api: ApiConfigService
  ) {
    this.baseUrl = this.api.endpoint('users/me');
  }

  /** Everything stored for this account, as a JSON file. */
  exportData(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export`, { responseType: 'blob' });
  }

  deleteAccount(password: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/delete`, { password });
  }
}
