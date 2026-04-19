import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClient } from './api-client';
import { PersonDto } from './models';

@Injectable({ providedIn: 'root' })
export class PersonService {
  private readonly api = inject(ApiClient);
  private readonly basePath = '/api/v1/personen';

  search(query: string, limit = 20): Observable<PersonDto[]> {
    return this.api.get<PersonDto[]>(this.basePath, { query, limit });
  }

  byId(id: string): Observable<PersonDto> {
    return this.api.get<PersonDto>(`${this.basePath}/${id}`);
  }

  erfassen(payload: {
    vorname: string;
    nachname: string;
    email: string;
    organisation?: string | null;
    funktion?: string | null;
    tenantId?: string;
  }): Observable<{ id: string }> {
    return this.api.post<{ id: string }>(this.basePath, payload);
  }
}
