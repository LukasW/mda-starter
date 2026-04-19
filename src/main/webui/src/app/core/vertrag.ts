import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClient } from './api-client';
import type { ErfassenVertragRequest, VertragDto } from './models';

@Injectable({ providedIn: 'root' })
export class VertragService {
  private readonly api = inject(ApiClient);
  private readonly base = '/api/v1/vertraege';

  list(): Observable<VertragDto[]> {
    return this.api.get<VertragDto[]>(this.base);
  }

  byId(id: string): Observable<VertragDto> {
    return this.api.get<VertragDto>(`${this.base}/${id}`);
  }

  erfassen(request: ErfassenVertragRequest): Observable<VertragDto> {
    return this.api.post<VertragDto, ErfassenVertragRequest>(this.base, request);
  }

  einreichen(id: string, antragstellerId: string): Observable<VertragDto> {
    return this.api.post<VertragDto>(`${this.base}/${id}/einreichen`, { antragstellerId });
  }

  genehmigen(id: string, reviewerId: string): Observable<VertragDto> {
    return this.api.post<VertragDto>(`${this.base}/${id}/genehmigen`, { reviewerId });
  }

  ablehnen(id: string, reviewerId: string, begruendung: string): Observable<VertragDto> {
    return this.api.post<VertragDto>(`${this.base}/${id}/ablehnen`, { reviewerId, begruendung });
  }
}
