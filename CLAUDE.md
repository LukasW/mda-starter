<!-- mda-generator:begin -->
# CLAUDE.md — Quarkus MDA Starter

Dieses Dokument orientiert Claude Code im Projekt. Es beschreibt **nur** Architektur, Harness und Workflow — fachliche Inhalte stehen in der Fachspec, die pro Projekt angelegt wird.

## Zustand: leerer Starter

**Dieses Repository enthaelt noch keine Fachspec und keinen Applikationscode.** Es ist das MDA-Grundgeruest: Harness (`.claude/`), Maven-Setup (`pom.xml`, `mvnw`), Quarkus-Dockerfiles unter `src/main/docker/`, Cucumber-JUnit-Konfiguration unter `src/test/resources/` und Doku-Skelette (`specs/features/`).

Der naechste Schritt ist immer **`/mda-init`**. Eingabe:

- `specs/description.md` mit einer Freitext-/Markdown-Beschreibung der Zieldomaene, **oder**
- `specs/model/*.puml` (PlantUML-Klassen-/State-/Process-Diagramme), optional `*.bpmn`, `*.rules.yaml`.

`/mda-init` erzeugt: Fachspec unter `specs/model/00-spec-<slug>.md`, DDD-Aggregates, Ports/Adapter, BPF, Angular-App via `ng new webui`, Flyway-Start-Migrationen, ArchitectureTest, BDD-Skelette, `docs/architecture/arc42.md` + ADRs 0001..0009. Danach gilt: **Fachspec ist die einzige fachliche Quelle**, Abweichungen Code↔Spec sind Bugs.

## Was ist das?

Quarkus 3.34.5 MDA-Starter. Er *wird*, nach `mda-init`: hexagonale Architektur (Port & Adapter), DDD, Business Process Flow (BPF) als Panache-Zustandsautomat, Angular 21 + Material SPA unter `src/main/webui`, via Quarkus Quinoa 2.8.1 in den Backend-Build integriert.

Die **verbindlichen MDA-Regeln** liegen vollstaendig unter `.claude/skills/_shared/` — sie sind auch im leeren Starter bereits bindend fuer `mda-init`.

## Schnellstart

```bash
./mvnw quarkus:dev       # Backend (:8080) + Angular dev-server (:4200) via Quinoa
./mvnw clean verify      # Unit + @QuarkusTest + Cucumber + ng build
./scripts/count-tests.sh # Testpyramide pruefen
```

Dev-Endpunkte:
- `http://localhost:8080/` — Angular SPA (Quinoa proxy auf ng dev-server :4200)
- `http://localhost:8080/q/health/ready`
- `http://localhost:8080/openapi` / `http://localhost:8080/q/swagger-ui`

REST-Pfade: siehe Fachspec.

## Paket-Layout (je BC identisch, nach `mda-init`)

```
<root-package>
  shared/
    events/      DomainEvent + InMemoryDomainEventPublisher
    problem/     DomainException + ProblemDetail + ExceptionMapper
    process/     BpfDefinition + BpfService + BpfInstance/TransitionLog-Entities
  <bc>/                         # BC-Namen: siehe Fachspec
    domain/              Aggregates, VOs, Enums, sealed Domain-Events, BPF-Definition
    application/
      port/in/           UseCase-Interfaces (Command/Query-Records)
      port/out/          Repository-Interfaces
      service/           @ApplicationScoped Anwendungsservice (Transaktionsgrenze)
    adapter/
      in/rest/           JAX-RS-Resources + DTOs + Request-Records (Validation)
      in/scheduler/      Quarkus-@Scheduled-Jobs (wo fachlich benoetigt)
      out/persistence/   Panache-Entities + Repositories + Adapter (Port-Out-Impl)
```

Konkrete BC- und Root-Package-Namen: **Fachspec**.

## Frontend (Angular 21 + Material, nach `mda-init`)

```
src/main/webui/
  angular.json, tsconfig*.json, proxy.conf.json, package.json
  src/
    main.ts, index.html, styles.scss
    app/
      app.ts, app.config.ts, app.routes.ts
      core/                 Services (siehe Fachspec)
      layout/app-shell/     MatToolbar + MatSidenav Shell
      pages/                Standalone-Components (siehe Fachspec)
```

Konventionen:
- **Standalone Components**, `ChangeDetectionStrategy.OnPush`, `inject()` statt Constructor-DI.
- **Signals** (`signal`, `computed`) fuer lokalen State; Observables an Service-Grenzen.
- **Reactive Forms** + Jakarta-Validation-aequivalente Regeln (required/min/maxLength).
- **Material 3** Theme (azure/blue palette, Roboto, density 0) in `styles.scss`.
- **Lazy-loaded Routes** via `loadComponent: () => import(...)`.
- Services gehen ueber `ApiClient` (Fehlerkanal: `ProblemDetail` → `ApiError`).
- Dev-Proxy `/api`, `/q`, `/openapi` → `http://localhost:8080` (`proxy.conf.json`).

Build-Output: `src/main/webui/dist/webui/browser/` (Angular `@angular/build:application`). Quinoa packt das in das Quarkus-Artifact.

## Regeln (erzwungen per ArchUnit)

- `domain/**` importiert **keine** Framework-Pakete (`jakarta.persistence`, `jakarta.ws.rs`, `io.quarkus`, `jakarta.inject/enterprise`).
- `application/**` kennt **keine** Persistenz-Details (nur Out-Ports).
- REST-Adapter ruft **nur** Eingangs-Ports (`port.in`), niemals direkt Anwendungsservice oder Out-Adapter.
- Bounded-Context-uebergreifende Kopplung: **nur via IDs oder Events**, nie via Services/Adaptern.

Details: `.claude/skills/_shared/hexagonal-rules.md`.

## Harness-Konfiguration (`.claude/`)

- `.claude/settings.json` — Permissions (allow/deny), `env MDA_UI_MODE=rest`, Statusline, PreToolUse-Hooks.
- `.claude/hooks/drift-guard.sh` — blockt `Edit`/`Write` bei Flyway-Re-Edit, sealed-permit-Entfernung, Loeschen des `manual-edits-below`-Markers, ArchUnit-Aufweichung, REST-Pfad-Rename ohne v2-Bump.
- `.claude/hooks/bash-safeguard.sh` — blockt `--no-verify`, `git reset --hard`, `rm -rf src/`, Force-Push auf `main`, `-DskipTests`.
- `.claude/agents/` — first-class Reviewer-Sub-Agents: `hexagonal-reviewer`, `angular-signals-reviewer`, `bdd-cucumber-author`, `bpf-reviewer`. Werden von `mda-implement` via `subagent_type` aufgerufen.
- `.claude/statusline.sh` — zeigt Branch + Feature-Slug + Plan/Impl-Status.
- MCP: `context7` ist Pflicht fuer Versions-Lookups (siehe `_shared/mda-stack.md` §9).

## BPF (Business Process Flow)

- BPF-Definitionen liegen je BC unter `<bc>/domain/` — konkrete Prozesse: **Fachspec**.
- Laufzeit in `shared/process/BpfService`. Jede Transition schreibt Audit-Log-Eintrag.
- Ungueltige Uebergaenge werfen `DomainException` mit Code `MDA-BPF-001`.
- REST-Ausspielung: zuerst Aggregat-Seiteneffekt, dann BPF-Transition in derselben Transaktion.

Details: `.claude/skills/_shared/bpf-guide.md`.

## Die fuenf Skills

Die gesamte MDA-Toolchain liegt im Repo unter `.claude/skills/` — keine globale Abhaengigkeit. Genau **fuenf** Skills, ein Workflow:

| Skill | Zweck | Phase |
|---|---|---|
| `/mda-init` | Erstentwurf des Projekts aus **Beschreibung** (Freitext/Markdown) ODER **PlantUML/BPMN/Rules-DSL** | einmalig |
| `/mda-plan <beschreibung>` | Feature planen: `specs/features/<slug>.md` + `plan/<slug>.md`, wartet auf Bestaetigung | pro Feature |
| `/mda-implement <slug> [--worktree]` | Plan umsetzen: Branch, Delta, Tests, Reviewer-Agents, Commit, PR | pro Feature |
| `/mda-ship` | CI abwarten, PR squash-mergen, Issue schliessen, Cleanup | pro Feature |
| `/mda-fast <beschreibung>` | `mda-plan` → (Bestaetigung wenn noetig) → `mda-implement` → `mda-ship` | bei klaren Features |

**Shared-Regeln** (normativ, gelten fuer alle Skills):

- `.claude/skills/_shared/mda-spec.md` — MDA-Spezifikation (Prinzipien, Artefakte, API-Konvention).
- `.claude/skills/_shared/mda-stack.md` — Quarkus-Stack (Versionen, Extensions).
- `.claude/skills/_shared/hexagonal-rules.md` — Paket-Layout.
- `.claude/skills/_shared/bpf-guide.md` — BPF-Engine.
- `.claude/skills/_shared/testing-pyramid.md` — Testverhaeltnis + Tags.
- `.claude/skills/_shared/drift-guards.md` — Was nie angefasst werden darf.
- `.claude/skills/_shared/feature-spec-template.md` — Feature-Spec-Template.
- `.claude/skills/_shared/dod-checklist.md` — Definition of Done.

## Feature-First-Workflow (verpflichtend)

Jede fachliche Erweiterung (neues Aggregate, neuer Use-Case, neue BPF-Transition, neuer Screen, neues Feld) laeuft **immer** ueber den `mda-plan` → `mda-implement` → `mda-ship`-Workflow, niemals direkt ueber Hand-Edit.

**Direkter Edit in `src/main/java/…/domain/` oder `…/application/` ist nicht erlaubt** — ausser:

- unterhalb eines `// mda-generator: manual-edits-below`-Markers (wird beim Regenerieren nicht ueberschrieben); oder
- in `config/`, `shared/` (dort sind Rahmen-Entscheide erlaubt).

**Gleiches fuer das Frontend**: neue Seiten per `ng generate component pages/<name>`, neue Services per `ng generate service core/<name>` — nicht manuell anlegen.

**Drift-Guards** (siehe `.claude/skills/_shared/drift-guards.md`): bestehende Flyway-Migrationen, Aggregate-Public-API-Umbenennungen, sealed-permit-Entfernungen, Aufweichung von ArchUnit-Regeln, Umbenennung von REST-Pfaden ohne Versions-Bump.

**Bei Unsicherheit**: zuerst Fachspec lesen, dann fragen — nicht einfach loscoden.

## Tests

- Surefire: Unit-Tests (`*Test.java`) inkl. `@QuarkusTest`.
- Failsafe: `ServiceBddIT`, `ProcessBddIT`, `UiBddIT` (Cucumber).
- ArchitectureTest prueft Schichten- und BC-Grenzen.
- Pyramiden-Heuristik: `./scripts/count-tests.sh`.

Tag-Konvention + Verhaeltnis: `.claude/skills/_shared/testing-pyramid.md`.

## Persistenz

Flyway-Migrationen sind **additiv**: `V<n>__<slug>.sql`. Bestehende V-Dateien sind unveraenderlich. Konkrete Tabellen und Migrationsstand: **Fachspec**.

## Weitere Dokumente (werden von `/mda-init` erzeugt, solange der Starter leer ist)

- `specs/model/00-spec-<slug>.md` — **Fachliche Spezifikation (Pflicht)**, einzige fachliche Quelle.
- `specs/model/*.puml` — PlantUML-Modelle (Input fuer `mda-init` oder Output nach Initialisierung).
- `specs/features/*.md` — Feature-Specs, je eine pro Feature (fuer `mda-plan`/`mda-implement`).
- `plan/*.md` — Impact-Plaene (fuer `mda-implement`).
- `docs/architecture/arc42.md` — C4-Modell + Laufzeitsicht je Prozess.
- `docs/architecture/adr/*.md` — Architekturentscheide.
<!-- mda-generator:end -->
