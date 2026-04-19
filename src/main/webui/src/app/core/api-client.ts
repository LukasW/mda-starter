import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import type { ProblemDetail } from './models';

export interface ApiError {
  message: string;
  code?: string;
  status?: number;
}

@Injectable({ providedIn: 'root' })
export class ApiClient {
  private readonly http = inject(HttpClient);

  get<T>(path: string): Observable<T> {
    return this.http.get<T>(path).pipe(catchError(handleError));
  }

  post<T, B = unknown>(path: string, body: B): Observable<T> {
    return this.http.post<T>(path, body).pipe(catchError(handleError));
  }
}

function handleError(error: unknown): Observable<never> {
  const httpErr = error as { error?: ProblemDetail; status?: number; message?: string };
  const problem = httpErr.error;
  const apiErr: ApiError = {
    message: problem?.detail ?? httpErr.message ?? 'Unbekannter Fehler',
    code: problem?.code,
    status: httpErr.status,
  };
  return throwError(() => apiErr);
}
