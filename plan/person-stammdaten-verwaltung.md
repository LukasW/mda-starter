# Feature Plan — person-stammdaten-verwaltung

Bezogen auf: `specs/features/person-stammdaten-verwaltung.md`
Bounded Context: `person`
Kind: `cross-cutting`

## Uebersicht

Vollstaendige Stammdaten-Verwaltung fuer Personen mit CRUD-UI (Auto-Load, Paginierung, Neue-Person-Dialog, Detail-/Bearbeiten-Route). Zwei neue Use-Cases (`PersonAendernUseCase`, `PersonLoeschenUseCase`) mit Guards gegen Manipulation von EXTERN_API-Snapshots. **Soft-Delete via `deleted_at`-Spalte** (neue Flyway-Migration V3). Neuer optionaler REST-Client-Adapter `RestExternePersonenverwaltungClient` hinter `@IfBuildProperty` im neuen Paket `adapter/out/rest/`. UI weist UUID-Eingaben im Suchfeld ab (nur Client-seitig).

## Betroffene Schichten

- Domain: **ja** — 1 Aggregate-Edit (Person: neue Guards + Invariante), 0 neue VO.
- Application: **ja** — 2 neue Use-Case-Ports, Service-Erweiterung.
- Adapter in (REST): **ja** — 2 neue Methoden in `PersonResource`, DTO-Feld.
- Adapter out (Persistence): **ja** — 1 neue Repo-Methode (`delete`).
- Adapter out (REST-Client): **ja (neu)** — `RestExternePersonenverwaltungClient` in neuem Paket `adapter/out/rest/`.
- Frontend: **ja** — 1 neue Page-Komponente (Detail), 1 neuer Dialog, 1 Service-Erweiterung, 1 Route, Liste erweitern.
- Migration: **ja** — `V3__person_soft_delete.sql` (ALTER + INDEX).

## Zu erstellende Dateien

### Backend
- [ ] `src/main/resources/db/migration/V3__person_soft_delete.sql` — `ALTER TABLE person ADD COLUMN deleted_at TIMESTAMP NULL` + Index.
- [ ] `src/main/java/.../person/application/port/in/PersonAendernUseCase.java` — Port + `PersonAendernCommand`-Record.
- [ ] `src/main/java/.../person/application/port/in/PersonLoeschenUseCase.java` — Port + `PersonLoeschenCommand`-Record.
- [ ] `src/main/java/.../person/adapter/out/rest/RestExternePersonenverwaltungClient.java` — JAX-RS-Client-Implementierung, `@IfBuildProperty`-gated.
- [ ] `src/main/java/.../person/adapter/out/rest/ExternePersonPayload.java` — Jackson-Record fuer JSON-Mapping.

### Tests
- [ ] `src/test/java/.../person/domain/PersonAendernGuardTest.java` — Unit: EXTERN_API-Guard, Version-Bump.
- [ ] `src/test/java/.../person/domain/PersonLoeschenGuardTest.java` — Unit: EXTERN_API-Guard.
- [ ] `src/test/java/.../person/adapter/RestExternePersonenverwaltungClientTest.java` — Integration: stub-HTTP-Endpoint oder `@QuarkusTest` mit lokal hochgezogenem Fake.
- [ ] `src/test/resources/features/service/person.feature` — BDD `@service`.
- [ ] `src/test/java/.../bdd/service/PersonServiceSteps.java` — Steps, `@ScenarioScope`.

### Frontend (nur via `ng generate`)
- [ ] `ng generate component pages/person-detail --skip-tests` — Detail/Bearbeiten-Seite.
- [ ] `ng generate component pages/person-erfassen-dialog --skip-tests` — Dialog fuer Neue Person.

## Zu aendernde Dateien

### Domain / Application
- [ ] `src/main/java/.../person/domain/Person.java` — **unterhalb** `manual-edits-below`: neue Methoden `aktualisiereSicher(...)` (mit EXTERN_API-Guard), `loeschen()` (setzt `deletedAt`), `istGeloescht()`. Zusaetzlich Feld `deletedAt: Instant` + Konstruktor-Parameter; `rehydrate(...)` um `deletedAt` erweitert (backward-kompatibel via Overload). **Wrap unterhalb Marker? ja**.
- [ ] `src/main/java/.../person/application/service/PersonApplicationService.java` — implementiert zusaetzlich `PersonAendernUseCase` + `PersonLoeschenUseCase`. **Wrap unterhalb Marker? Marker fehlt — einmalig einfuegen.**
- [ ] `src/main/java/.../person/application/port/out/PersonRepository.java` — neue Methode `void delete(PersonId id);` **Wrap unterhalb Marker? nein** (Interface, keine generator-verwaltete Klasse).

### Adapter
- [ ] `src/main/java/.../person/adapter/in/rest/PersonResource.java` — neue Methoden `@PUT /{id}` und `@DELETE /{id}` + Request-Record; `PersonDto` um `versionNumber` erweitert. **Wrap unterhalb Marker? Marker fehlt — einmalig einfuegen.**
- [ ] `src/main/java/.../person/adapter/in/rest/PersonDto.java` — Feld `long versionNumber`.
- [ ] `src/main/java/.../person/adapter/out/persistence/PersonJpaEntity.java` — neues Feld `public Instant deletedAt;` (nullable, Column `deleted_at`).
- [ ] `src/main/java/.../person/adapter/out/persistence/PersonPanacheRepository.java` — `search()` + `findById()` um `deleted_at IS NULL` erweitern; `delete(PersonId)` als Soft-Delete (setzt `deleted_at = now()`, persist). Mapping `toDomain`/`save` um `deletedAt` ergaenzt. **Wrap unterhalb Marker? Marker fehlt — einmalig einfuegen.**

### Tests
- [ ] `src/test/java/.../architecture/ArchitectureTest.java` — Regel `jaxrs_onlyIn_adapterIn_rest_or_shared_problem` erweitern auf `..adapter.out.rest..`. **Namespace-Erweiterung, keine Aufweichung.**
- [ ] `src/test/java/.../person/adapter/PersonResourceTest.java` — neue Tests (PUT/DELETE Happy/Fehler/Konflikt).
- [ ] `src/test/java/.../person/application/PersonApplicationServiceIT.java` — `aendern_bumptVersion`, `loeschen_entfernt`.
- [ ] `src/test/java/.../person/domain/PersonTest.java` — `aktualisieren_wirftBeiExternSnapshot`.

### Frontend
- [ ] `src/main/webui/src/app/app.routes.ts` — neue Lazy-Route `/personen/:id`.
- [ ] `src/main/webui/src/app/pages/person-liste/person-liste.ts` + `.html` — Auto-Load, Paginator, Dialog-Trigger, Zeilen-Klick, UUID-Guard im Suchfeld.
- [ ] `src/main/webui/src/app/core/person.ts` — Methoden `aendern`, `loeschen`.
- [ ] `src/main/webui/src/app/core/api-client.ts` — neue Methode `delete<T>(...)`.
- [ ] `src/main/webui/src/app/core/models.ts` — `PersonDto.versionNumber: number`.

### Config / Build
- [ ] `pom.xml` — neue Dependency `io.quarkus:quarkus-rest-client-jackson`. **Neue Dep, keine Versions-Aenderung.**
- [ ] `src/main/resources/application.properties` — **anhaengen**: `clm.person.externe-verwaltung.base-url=` (leer), `clm.person.externe-verwaltung.timeout-ms=5000`.

## BPF-Delta

- **keiner**. Dieses Feature beruehrt keine BPF-Transition.

## Test-Strategie

| Ebene | Datei | Szenarien |
|---|---|---|
| Unit | `PersonTest` (bestehend) | Aktualisieren wirft bei EXTERN_API; aktualisieren bumpt version; `loeschen()` setzt deletedAt |
| Unit | `PersonAendernGuardTest` (neu) | Guard laeuft VOR Mutation (Idempotenz) |
| Unit | `PersonLoeschenGuardTest` (neu) | Extern-Snapshot wirft; doppeltes Loeschen no-op / 404 |
| Integration | `PersonResourceTest` (erweitert) | PUT Happy 204; PUT 409 bei Version-Konflikt; PUT 422 bei EXTERN_API; DELETE 204; DELETE 404 bei bereits-geloeschter ID; DELETE 422 bei EXTERN_API |
| Integration | `PersonApplicationServiceIT` (erweitert) | `aendern_bumptVersion`, `loeschen_markiert_deletedAt`, `suche_ignoriert_geloeschte` |
| Integration | `RestExternePersonenverwaltungClientTest` (neu) | Property on → HTTP-Mock; liefert 2 Eintraege → gemappt |
| BDD `@service` | `features/service/person.feature` (neu) | "Sachbearbeiter erfasst und aendert eine Person"; "Person mit Quelle EXTERN_API kann nicht geloescht werden" |
| BDD `@ui` | — | nicht in Scope Phase 1 |
| ArchUnit | `ArchitectureTest` (erweitert) | `..adapter.out.rest..` explicit in JAX-RS-Whitelist |

Pyramiden-Pruefung (`scripts/count-tests.sh`):
- Neu: 2 Unit + 3 Integration + 2 BDD-service = 7 Tests.
- Verhaeltnis bleibt gruen (bestehende Unit-Substanz ist gross).

## Drift-Guard-Check

- [x] Flyway additiv — neue `V3__person_soft_delete.sql` (ALTER ADD COLUMN + INDEX). V1/V2 bleiben unveraendert.
- [x] Aggregate-Root-Public-API bleibt stabil — neue Methoden unterhalb Marker.
- [x] Kein `permits`-Subtyp entfernt (Person hat keine Events in diesem Feature).
- [x] ArchUnit-Regel **erweitert**, nicht aufgeweicht — neuer Namespace `..adapter.out.rest..`.
- [x] Kein `/api/v1/personen`-Pfad umbenannt; neue HTTP-Methoden (PUT/DELETE) unter bestehender Ressource.
- [x] Cross-BC-Kopplung bleibt — `contract` importiert `person` nicht; unveraendert.
- [x] `pom.xml`: nur neue Dependency hinzugefuegt, kein Version-Bump.
- [x] `application.properties`: nur neue Keys angehaengt.
- [x] `ng generate` fuer alle neuen Components; kein Handanlegen von `.ts/.html/.scss`.

## Rollback-Strategie

Aus Spec uebernommen:
- **Code**: Feature-Commit reverten, PR close.
- **DB**: additive Folge-Migration `V<n+1>__undo_person_soft_delete.sql` mit `DROP COLUMN deleted_at` — V3 bleibt unveraendert (Additiv-Regel).
- **Externer Adapter**: `clm.person.externe-verwaltung.enabled=false` (Build-Property) + Rebuild → Default-Bean (Disabled) ist wieder aktiv.

## Offene Fragen (geschlossen 2026-04-19)

1. ✅ **Soft-Delete mit `deleted_at`** — Migration V3, Queries filtern `deleted_at IS NULL`.
2. ✅ **Neues Paket `adapter/out/rest/`** + ArchUnit-Namespace-Erweiterung.
3. ✅ **`@IfBuildProperty`** (Build-Time-Schalter) — Rebuild bei Aktivierung.
4. ✅ **UUID-Guard nur im UI** — Backend bleibt permissiv, backward-compatible.

Keine offenen Fragen mehr.
