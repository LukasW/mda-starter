<!-- mda-generator:begin -->
# CLAUDE.md — CLM (Contract Lifecycle Management)

Dieses Dokument orientiert Claude Code im Projekt. Die **fachliche Quelle der Wahrheit** ist `specs/model/00-spec-clm.md`. Abweichungen Code ↔ Fachspec sind Bugs.

## Was ist das?

Contract-Lifecycle-Management (CLM) auf Quarkus 3.34.5 + Angular 21 + Material (via Quinoa 2.8.1). Modularer Monolith, hexagonale Architektur (Port & Adapter), DDD, Business Process Flow (BPF) als Panache-Zustandsautomat.

Bounded Contexts:

- **`contract`** — Vertraege, Versionen, Vertragsparteien, BPF-Lifecycle (9 Stages: `ENTWURF` → … → `ARCHIVIERT` → `ABGELAUFEN|GEKUENDIGT`).
- **`person`** — Interne Personenverwaltung mit vollem CRUD (Erfassen / Aendern / Soft-Delete via `deleted_at`) + optionaler Snapshot-Cache aus einer externen Personenverwaltung (Feature-gegated per `clm.person.externe-verwaltung.enabled`). EXTERN_API-Personen sind read-only (Aendern → `MDA-PER-002`, Loeschen → `MDA-PER-003`).

Optionale externe Integrationen (Ports definiert, Adapter-Stubs) — Anbieter-Wahl steht gem. AE-02/AE-03 aus:

- externes Archiv (`ARCHIV_EXTERN` SpeicherTyp, `clm.archiv.extern.enabled`),
- Personenverwaltung (s. o.),
- Signatur-Dienstleister (`clm.signatur.anbieter=stub`).

## Schnellstart

```bash
./mvnw quarkus:dev       # Backend (:8080) + Angular dev-server (:4200) via Quinoa
./mvnw clean verify      # Unit + @QuarkusTest + Cucumber + ng build
./scripts/count-tests.sh # Testpyramide pruefen
```

Dev-Endpunkte:
- `http://localhost:8080/` — Angular SPA (Quinoa proxy auf `ng serve` :4200)
- `http://localhost:8080/api/v1/vertraege` — Vertrags-REST (`/api/v1/vertraege/{id}/process/contract/trigger/{trigger}` für BPF)
- `http://localhost:8080/api/v1/personen`
- `http://localhost:8080/q/health/ready`
- `http://localhost:8080/openapi` / `http://localhost:8080/q/swagger-ui`

## Paket-Layout

Root-Package: `ch.grudligstrasse.mda.clm`.

```
ch.grudligstrasse.mda.clm
  shared/
    events/    DomainEvent + InMemoryDomainEventPublisher
    problem/   DomainException + ProblemDetail + ExceptionMapper + ValidationExceptionMapper
    process/   BpfDefinition + BpfService + BpfInstanceEntity + BpfTransitionLogEntity + Repositories
  contract/
    domain/           Vertrag, VertragId, VertragsTyp, DokumentReferenz, SpeicherTyp,
                      VertragsPartei, VertragsVersion, ParteiRolle,
                      event/ (sealed VertragDomainEvent + 4 Records),
                      process/VertragStage + VertragLifecycle
    application/
      port/in/        VertragErstellenUseCase, VertragMetadatenSetzenUseCase,
                      VertragDokumentHochladenUseCase, VertragPersonZuordnenUseCase,
                      VertragTriggerUseCase, VertragAbrufenQuery
      port/out/       VertragRepository
      service/        VertragApplicationService
    adapter/
      in/rest/        VertragResource + VertragDto
      out/persistence/ VertragJpaEntity, VertragsParteiJpaEntity, VertragsVersionJpaEntity,
                       VertragMapper, VertragPanacheRepository
  person/
    domain/           Person, PersonId, Email, PersonenQuelle
    application/
      port/in/        PersonErfassenUseCase, PersonSuchenQuery
      port/out/       PersonRepository, ExternePersonenverwaltungClient
      service/        PersonApplicationService
    adapter/
      in/rest/        PersonResource + PersonDto
      out/persistence/ PersonJpaEntity, PersonPanacheRepository, DisabledExternePersonenverwaltungClient
```

## Frontend

```
src/main/webui/
  angular.json, tsconfig*.json, proxy.conf.json, package.json
  src/
    main.ts, index.html, styles.scss
    app/
      app.ts, app.config.ts, app.routes.ts
      core/                 api-client, contract, person, models
      layout/app-shell/     MatToolbar + MatSidenav + MatNavList Shell
      pages/                vertrag-liste, vertrag-erfassen, vertrag-detail, person-liste
```

Konventionen (erzwungen von `angular-signals-reviewer`):

- Standalone Components, `ChangeDetectionStrategy.OnPush`, `inject()`.
- Signals (`signal`, `computed`) für lokalen State; Observables an Service-Grenzen.
- Reactive Forms (`fb.nonNullable.group`) + Jakarta-Validation-aequivalente Regeln.
- Material 3 Theme (azure-blue, Roboto).
- Lazy-loaded Routes via `loadComponent: () => import(...)`.
- `ApiClient` mappt `ProblemDetail` → `ApiError`; `fieldErrors[]` werden per `ctrl.setErrors({ server: … })` in Reactive Forms eingespielt.
- Dev-Proxy `/api`, `/q`, `/openapi` → `http://localhost:8080` in `proxy.conf.json` (nur Konfiguration `standalone`).

UX-Bausteine (Pflicht, Details: `.claude/skills/mda-init/references/angular-ux-patterns.md`):

- BPF-Lifecycle als `MatStepper` (nicht-editierbar) im `*-detail`; BPF-Action-Buttons nur fuer aktuelle Stage.
- Destruktive BPF-Trigger via `MatDialog` mit Pflicht-Begruendung (`minLength(10)`); harmlose Trigger mit schlankem Bestaetigungs-Dialog.
- Listen via `MatTableDataSource` + `MatSort` + `MatPaginator` (pageSize 25) + `MatChipListbox` fuer Stage-Filter.
- Formulare mit > 1 Stufe als linearer `MatStepper`; Skeleton-Loader statt Spinner beim Daten-Load.
- Dark-Mode-Toggle im Shell (`body { color-scheme: light dark }`); alle Icon-Buttons mit `aria-label`.
- `@angular/localize` von Tag 1 eingerichtet; Texte `i18n`-markiert oder mit `// i18n: unlocalized`-Ausnahme.

## Regeln (erzwungen per ArchUnit)

- `..domain..` importiert keine Framework-Pakete (`jakarta.*`, `io.quarkus.*`, `io.smallrye.*`, `org.hibernate.*`, `com.fasterxml.*`).
- `..application.port..` importiert keine Framework-Pakete.
- `..adapter.in..` darf nicht `..adapter.out..` importieren.
- `jakarta.ws.rs.*` nur in `..adapter.in.rest..` und `shared/problem/`.
- `jakarta.persistence.*` nur in `..adapter.out.persistence..` und `shared/process/`.
- `contract/` darf `person/` **nicht** direkt importieren (Cross-BC-Kopplung nur per ID oder Event).

## BPF — Vertrag-Lifecycle

Transitions (siehe `contract/domain/process/VertragLifecycle.java`):

| Von | Trigger | Nach |
|---|---|---|
| ENTWURF | `einreichen` | IN_PRUEFUNG |
| IN_PRUEFUNG | `freigeben` | FREIGEGEBEN |
| IN_PRUEFUNG | `korrekturbeantragen` | KORREKTURBEDARF |
| KORREKTURBEDARF | `einreichen` | IN_PRUEFUNG |
| FREIGEGEBEN | `zurSignaturSenden` | ZUR_SIGNATUR |
| ZUR_SIGNATUR | `unterzeichnen` | UNTERZEICHNET |
| UNTERZEICHNET | `archivieren` | ARCHIVIERT |
| ARCHIVIERT | `ablaufen` | ABGELAUFEN |
| ARCHIVIERT | `kuendigen` | GEKUENDIGT |

Fehlermodell: ungueltige Transition → `DomainException("MDA-BPF-001")` → HTTP 422 Problem+JSON.

REST: `POST /api/v1/vertraege/{id}/process/contract/trigger/{trigger}?actor=…` → 200 `{"stage":"…"}`.

## Harness (`.claude/`)

- `.claude/settings.json` — Permissions, Statusline, Hooks.
- `.claude/hooks/drift-guard.sh` — blockt Edit auf bestehende Flyway-Migrationen, Löschen des `manual-edits-below`-Markers, ArchUnit-Aufweichung, sealed-permit-Verkleinerung, REST-Pfad-Rename ohne `/v2/`-Bump.
- `.claude/hooks/bash-safeguard.sh` — blockt `-DskipTests`, `--no-verify`, `git reset --hard`, `rm -rf src/`, Force-Push auf `main`.
- `.claude/agents/` — `hexagonal-reviewer`, `angular-signals-reviewer`, `bdd-cucumber-author`, `bpf-reviewer`.
- MCP: `context7` Pflicht für Versions-Lookups.

## Tests

- Surefire (`*Test.java`): Unit (JUnit 5) + `@QuarkusTest`-Integration.
- Failsafe (`*IT.java`):
  - `ServiceBddIT` (Tag `@service`)
  - `ProcessBddIT` (Tag `@process`)
  - `UiBddIT` (Tag `@ui`)
  - `*ApplicationServiceIT` (reine `@QuarkusTest`-Integration, ohne Cucumber)
- Cucumber via `io.quarkiverse.cucumber:quarkus-cucumber` — Runner erben von `CucumberQuarkusTest`, Step-Klassen sind `@ScenarioScope`.
- Pyramiden-Verhaeltnis: `./scripts/count-tests.sh` muss grün sein (`unit ≥ 2×int`, `int ≥ 2×bdd_non_ui`, `bdd_non_ui ≥ bdd_ui`).

## Persistenz

- Flyway-Migrationen unter `src/main/resources/db/migration/`:
  - `V1__init.sql` — `vertrag`, `vertrag_partei`, `vertrag_version`, `person`.
  - `V2__bpf.sql` — `bpf_instance`, `bpf_transition_log`.
  - `V3__person_soft_delete.sql` — `person.deleted_at` + Index.
- Dev: H2 in-memory (`%dev` Profile). Test: H2 in-memory. Prod: PostgreSQL 16+.
- Migrationen sind **additiv**: bestehende `V<n>__*.sql` nie editieren. Neue Deltas als `V<n+1>__*.sql`.

## Feature-First-Workflow

Neue fachliche Erweiterungen immer über `/mda-plan` → `/mda-implement` → `/mda-ship`. Direkter Edit in `domain/`, `application/` nur unterhalb `// mda-generator: manual-edits-below` oder innerhalb `shared/`, `config/`.

Neue Pages via `ng generate component pages/<name>`, neue Services via `ng generate service core/<name>`.

## Weitere Dokumente

- `specs/model/00-spec-clm.md` — fachliche Spezifikation (einzige fachliche Quelle).
- `docs/architecture/arc42.md` — Architekturdokumentation (C4-L1/L2/L3, Laufzeitsicht).
- `docs/architecture/adr/*.md` — Architekturentscheide (0001..0009).
- `docs/architecture/testing.md` — Testpyramide und Tags.
<!-- mda-generator:end -->
