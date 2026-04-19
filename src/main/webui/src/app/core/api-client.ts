import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiError, ProblemDetail } from './models';

@Injectable({ providedIn: 'root' })
export class ApiClient {
  private readonly http = inject(HttpClient);

  get<T>(path: string, params?: Record<string, string | number | boolean>): Observable<T> {
    return this.http
      .get<T>(path, { params: asHttpParams(params) })
      .pipe(catchError((err) => this.mapError(err)));
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.http.post<T>(path, body).pipe(catchError((err) => this.mapError(err)));
  }

  put<T>(path: string, body: unknown): Observable<T> {
    return this.http.put<T>(path, body).pipe(catchError((err) => this.mapError(err)));
  }

  postNoBody<T>(path: string): Observable<T> {
    return this.http
      .post<T>(path, null, { headers: { 'Content-Type': 'application/json' } })
      .pipe(catchError((err) => this.mapError(err)));
  }

  private mapError(err: HttpErrorResponse): Observable<never> {
    const pd = (err.error as ProblemDetail | undefined) ?? undefined;
    const api: ApiError = {
      code: pd?.code ?? 'MDA-NET-001',
      status: pd?.status ?? err.status,
      message: pd?.detail ?? pd?.title ?? err.message,
      fieldErrors: pd?.errors ?? [],
    };
    return throwError(() => api);
  }
}

function asHttpParams(p?: Record<string, string | number | boolean>) {
  if (!p) return undefined;
  const out: Record<string, string> = {};
  for (const [k, v] of Object.entries(p)) {
    out[k] = String(v);
  }
  return out;
}
