import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClient } from './api-client';
import type { Entscheidung, FreigabeDto } from './models';

@Injectable({ providedIn: 'root' })
export class FreigabeService {
  private readonly api = inject(ApiClient);
  private readonly base = '/api/v1/freigaben';

  fuerVertrag(vertragId: string): Observable<FreigabeDto[]> {
    return this.api.get<FreigabeDto[]>(`${this.base}?vertragId=${encodeURIComponent(vertragId)}`);
  }

  anfordern(vertragId: string, versionId: string, reviewerId: string): Observable<FreigabeDto> {
    return this.api.post<FreigabeDto>(this.base, { vertragId, versionId, reviewerId });
  }

  entscheiden(id: string, entscheidung: Entscheidung, begruendung?: string | null): Observable<FreigabeDto> {
    return this.api.post<FreigabeDto>(`${this.base}/${id}/entscheiden`, { entscheidung, begruendung });
  }
}
