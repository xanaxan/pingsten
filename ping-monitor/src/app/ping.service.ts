import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PingService {
  private baseUrl = 'http://localhost:8080/api/ping2';

  constructor(private http: HttpClient) {}

  startPingMission(): Observable<{ taskId: string }> {
    return this.http.post<{ taskId: string }>(`${this.baseUrl}/start`, {});
  }

  getProgress(taskId: string): Observable<number> {
    return this.http.get<{ progress: number }>(`${this.baseUrl}/progress/${taskId}`)
      .pipe(map(res => res.progress));
  }

  getResult(taskId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/result/${taskId}`);
  }
}