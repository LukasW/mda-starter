---
slug: person-stammdaten-verwaltung
title: Personen-Stammdatenverwaltung mit optionalem externem Adapter
bounded_context: person
kind: cross-cutting
affects_aggregate: Person
created: 2026-04-19
author: lukas
feature_flag: clm.person.externe-verwaltung.enabled
---

## Warum

Die aktuelle UI (`pages/person-liste`) ist eine reine Suchmaske ohne Initial-Load, ohne Erfassen-Dialog und ohne Detail-/Bearbeiten-Ansicht. Das Backend unterstuetzt bereits Erfassen und Suchen, aber weder Aendern noch Loeschen. Sachbearbeiter brauchen eine vollwertige interne Stammdatenverwaltung; optional soll ein externer Quellsystem-Adapter zugeschaltet werden koennen (Fachspec §7.2). Die Suche darf grundsaetzlich nicht via UUID erfolgen — weder im UI noch in der REST-Listen-API.

## Fachliche Regel

1. **Eigene Verwaltung**: Interne Personen (`quelleTyp = INTERN`) duerfen erfasst, geaendert und geloescht werden.
2. **Externe Snapshots**: Personen mit `quelleTyp = EXTERN_API` sind read-only. Aenderungen an Vorname, Nachname, Email, Organisation, Funktion → `DomainException("MDA-PER-002")` (HTTP 422). Loeschen → `DomainException("MDA-PER-003")` (HTTP 422).
3. **Optimistic Concurrency**: Aendern verlangt `versionNumber`-Match. Mismatch → `MDA-CONFLICT-001` (HTTP 409).
4. **Soft-Delete**: Interne Personen werden per `deleted_at`-Zeitstempel als geloescht markiert (nicht physisch entfernt). Bereits-geloeschte bzw. nicht-existente ID → HTTP 404 `MDA-PER-404`. Suche (`GET /api/v1/personen`) und Einzel-Abruf (`GET /api/v1/personen/{id}`) liefern geloeschte Personen nicht zurueck.
5. **Suche ohne UUID**: Die Listen-REST `GET /api/v1/personen?query=...` filtert nur nach Vorname, Nachname, Email, Organisation. Eingabe einer UUID im Suchfeld wird im UI abgewiesen mit Hinweis „Bitte nach Name, Mail oder Organisation suchen". Der Backend-Filter bleibt unveraendert (LIKE auf Name/Mail/Organisation); Einzel-Abruf per ID bleibt ueber `GET /api/v1/personen/{id}` erreichbar (aber nicht ueber das Listen-Suchfeld bedienbar).
6. **Externer Adapter konfigurativ**: Bei `clm.person.externe-verwaltung.enabled=true` wird `RestExternePersonenverwaltungClient` aktiv und erreicht per HTTP-GET ein externes System gemaess Fachspec §7.2 (`GET /persons?query=…&limit=…`). Mapping zu lokalen `EXTERN_API`-Snapshots. Default bleibt `DisabledExternePersonenverwaltungClient` (leere Trefferliste).

## Use-Cases

### Neu: `PersonAendernUseCase`
- Command: `PersonAendernCommand(id: PersonId, vorname, nachname, email, organisation?, funktion?, expectedVersion: long)`.
- Regel: nur fuer `quelleTyp = INTERN`. Bei `EXTERN_API` → `DomainException MDA-PER-002` (422). Versions-Konflikt → `MDA-CONFLICT-001` (409).
- REST: `PUT /api/v1/personen/{id}` mit `application/json`-Body.

### Neu: `PersonLoeschenUseCase`
- Command: `PersonLoeschenCommand(id: PersonId, expectedVersion: long)`.
- Regel: nur `INTERN`. `EXTERN_API` → `MDA-PER-003` (422). Nicht-existent → `MDA-PER-404` (404).
- REST: `DELETE /api/v1/personen/{id}` (204 No Content).

### Erweitert: `PersonSuchenQuery`
- unverändert — Backend bleibt wie heute. Keine UUID-Validierung serverseitig (backward-compatible).

### Neu: externer Adapter `RestExternePersonenverwaltungClient`
- Port bleibt `ExternePersonenverwaltungClient`.
- Aktivierung per `@IfBuildProperty(name="clm.person.externe-verwaltung.enabled", stringValue="true")`.
- Neue Config-Keys (Runtime):
  - `clm.person.externe-verwaltung.base-url` (Pflicht bei enabled)
  - `clm.person.externe-verwaltung.timeout-ms` (Default 5000, gem. AE-06)
- HTTP-Fehler → leere Liste + Log; kein Retry in Phase 1 (Fallback nach Fachspec §7.2).

## Daten

Neue Spalte `deleted_at TIMESTAMP NULL` an Tabelle `person` fuer Soft-Delete.

Flyway-Migration (additiv, neue Nummer):

```sql
-- V3__person_soft_delete.sql
ALTER TABLE person ADD COLUMN deleted_at TIMESTAMP NULL;
CREATE INDEX ix_person_deleted_at ON person(deleted_at);
```

Konsequenzen:
- `PersonJpaEntity`: Feld `Instant deletedAt` (nullable).
- `Person`-Aggregate: Feld `deletedAt`, Methode `loeschen()` (setzt Stempel), Query `istGeloescht()`. `rehydrate(...)` bekommt zusaetzlichen Parameter.
- `PersonPanacheRepository.search/findById`: Filter `deleted_at IS NULL`.
- `PersonPanacheRepository.delete(PersonId)`: laedt Entity, setzt `deleted_at = now()`, `save()` (Version-Bump). Keine physische Entfernung.
- Keine Unique-Constraint auf `email` (V1 bestaetigt) → Soft-Delete kollisionsfrei.

## UI

### `pages/person-liste` (Edit, nur unterhalb Marker)
- `ngOnInit`: Auto-Load via `search('', 25)`.
- `MatPaginator` (pageSize 25) + `MatTableDataSource`.
- Button „Neue Person erfassen" oeffnet `MatDialog` mit Komponente `PersonErfassenDialog`.
- Zeile-Klick → `router.navigate(['/personen', id])`.
- Suchfeld: Client-Validierung — wenn Input matcht `^[0-9a-f-]{32,36}$`, Fehler einspielen „Bitte nach Name, Mail oder Organisation suchen".

### `ng generate component pages/person-erfassen-dialog --skip-tests`
- Reactive Form: vorname, nachname, email, organisation?, funktion?. Validatoren: required + email + maxLength.
- Submit → `PersonService.erfassen(...)`; dialog close mit neuer ID; Liste refresht.

### `ng generate component pages/person-detail --skip-tests`
- Route `/personen/:id`, lazy.
- Reactive Form fuer Aendern; `versionNumber`-Hidden-Control.
- Button „Loeschen" sichtbar nur bei `quelleTyp = INTERN`.
- Bei `EXTERN_API`: Form disabled, Chip „Extern – read-only".

### `app.routes.ts`
- Neue Route: `{ path: 'personen/:id', loadComponent: () => import('./pages/person-detail/person-detail').then(m => m.PersonDetail) }`.

### `core/person.ts` + `core/api-client.ts`
- `PersonService.aendern(id, payload)` (PUT).
- `PersonService.loeschen(id, expectedVersion)` (DELETE).
- `ApiClient.delete<T>(path, params?)` — neue Methode.
- `PersonDto` bekommt Feld `versionNumber: number`.

## Tests

### Unit (JUnit 5)
- `PersonTest`:
  - `aktualisieren_wirftBeiExternSnapshot` — neuer Guard.
  - `kannGeloeschtWerden_nurBeiIntern` — neue Aggregate-Methode / Guard.
- `PersonLoeschenUseCaseTest` — in-memory Repository-Stub; Happy-Path + Extern-Snapshot-Error.

### Integration (`@QuarkusTest`)
- `PersonResourceTest` erweitern:
  - `putAendertInterne` (204 + verifizieren per GET).
  - `putAufExternSnapshotGibt422` (MDA-PER-002).
  - `deleteEntferntInterne` (204 + 404 beim anschliessenden GET).
  - `deleteAufExternSnapshotGibt422` (MDA-PER-003).
  - `putMitVersionKonfliktGibt409` (MDA-CONFLICT-001).
- `PersonApplicationServiceIT` erweitern: `aendern_bumptVersion`, `loeschen_entfernt`.
- `RestExternePersonenverwaltungClientTest` (nur bei Property gesetzt; WireMock oder Quarkus-HTTP-Test-Endpoint).

### BDD `@service`
- Neue Datei: `src/test/resources/features/service/person.feature` (`# language: de`).
- Szenarien:
  - „Sachbearbeiter erfasst und aendert eine Person".
  - „Person mit Quelle EXTERN_API kann nicht geloescht werden".

### ArchUnit
- `ArchitectureTest.jaxrs_onlyIn_adapterIn_rest_or_shared_problem` erweitern um `..adapter.out.rest..` (neue Namespace-Erweiterung fuer externe Adapter). Keine Regel aufgeweicht — reiner Namespace-Zugewinn.

### UI-BDD
- **keins** in Phase 1. Die UI-Aenderungen sind additiv, Golden-Path ist durch `@service`-BDDs abgedeckt.

## Rollback

- **Code**: Revert des Feature-Commits.
- **DB**: additiver Column-Zuwachs. Rollback via neue Folge-Migration `V<n+1>__undo_person_soft_delete.sql` mit `DROP COLUMN deleted_at`. Bestehende V3 bleibt unveraendert (Additiv-Regel).
- **Adapter deaktivieren**: `clm.person.externe-verwaltung.enabled=false` (Build-Property) + Rebuild → `DisabledExternePersonenverwaltungClient` ist wieder aktiv.

## Offene Fragen

Entscheidungen aus interaktiver Klaerung (2026-04-19):

1. **Delete-Mode**: Soft-Delete mit `deleted_at` (Migration V3).
2. **Adapter-Paket**: neues Paket `adapter/out/rest/` + ArchUnit-Namespace-Erweiterung.
3. **Feature-Flag**: `@IfBuildProperty` (Build-Time-Schalter); Aktivierung via Rebuild.
4. **UUID-Guard**: nur im UI (Backend bleibt permissiv, backward-compatible).

Offen fuer Folge-Features (nicht in Scope):
- Domain-Events fuer Person (`PersonAngelegt/Geaendert/Geloescht`) — separater `/mda-plan person-domain-events`-Lauf.
- Wiederherstellen geloeschter Personen (Undelete) — separate Spec.
