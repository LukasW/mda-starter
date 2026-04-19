import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClient } from './api-client';
import type { ErfassenFristRequest, FristDto } from './models';

@Injectable({ providedIn: 'root' })
export class FristService {
  private readonly api = inject(ApiClient);
  private readonly base = '/api/v1/fristen';

  list(): Observable<FristDto[]> {
    return this.api.get<FristDto[]>(this.base);
  }

  byVertrag(vertragId: string): Observable<FristDto[]> {
    return this.api.get<FristDto[]>(`${this.base}?vertragId=${encodeURIComponent(vertragId)}`);
  }

  erfassen(request: ErfassenFristRequest): Observable<FristDto> {
    return this.api.post<FristDto, ErfassenFristRequest>(this.base, request);
  }

  sichten(id: string, sichtenderUserId: string): Observable<FristDto> {
    return this.api.post<FristDto>(`${this.base}/${id}/sichten`, { sichtenderUserId });
  }
}
