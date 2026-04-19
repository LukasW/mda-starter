import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClient } from './api-client';
import { ParteiRolle, VertragDto, VertragsTyp } from './models';

@Injectable({ providedIn: 'root' })
export class ContractService {
  private readonly api = inject(ApiClient);
  private readonly basePath = '/api/v1/vertraege';

  list(tenantId?: string, top = 50, skip = 0): Observable<VertragDto[]> {
    const params: Record<string, string | number> = { $top: top, $skip: skip };
    if (tenantId) params['tenantId'] = tenantId;
    return this.api.get<VertragDto[]>(this.basePath, params);
  }

  byId(id: string): Observable<VertragDto> {
    return this.api.get<VertragDto>(`${this.basePath}/${id}`);
  }

  erstellen(titel: string, typ: VertragsTyp, erstellerId: string, tenantId?: string) {
    return this.api.post<{ id: string }>(this.basePath, { titel, typ, erstellerId, tenantId });
  }

  metadaten(id: string, titel: string, gueltigVon?: string | null, gueltigBis?: string | null) {
    return this.api.put<void>(`${this.basePath}/${id}/metadaten`, { titel, gueltigVon, gueltigBis });
  }

  personZuordnen(id: string, personId: string, rolle: ParteiRolle) {
    return this.api.post<void>(`${this.basePath}/${id}/parteien`, { personId, rolle });
  }

  trigger(id: string, trigger: string, actor = 'ui'): Observable<{ stage: string }> {
    return this.api.postNoBody<{ stage: string }>(
      `${this.basePath}/${id}/process/contract/trigger/${trigger}?actor=${encodeURIComponent(actor)}`
    );
  }
}
