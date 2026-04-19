<!-- mda-generator:begin -->
# CLAUDE.md — mda-starter (CLM MVP V1)

Dieses Dokument orientiert Claude Code im Projekt. Es ersetzt kein ausfuehrliches Architektur-Dokument (siehe `docs/architecture/arc42.md`).

## Was ist das?

Referenz-Starter fuer **Contract Lifecycle Management (CLM) MVP V1** auf Quarkus 3.34.5 gemaess MDA-Ansatz (`specs/MDA-Spezifikation.md`, `specs/MDA-Quarkus-Stack.md`). Drei Bounded Contexts: `contract`, `approval`, `obligation`. Hexagonale Architektur (Port & Adapter), DDD, Business Process Flow (BPF) als Panache-Zustandsautomat. Angular 21 + Material SPA unter `src/main/webui`, via Quarkus Quinoa 2.8.1 in den Backend-Build integriert.

## Schnellstart

```bash
./mvnw quarkus:dev      # Backend (:8080) + Angular dev-server (:4200) via Quinoa
./mvnw clean verify     # Unit + @QuarkusTest + Cucumber + ng build
./scripts/count-tests.sh # Testpyramide pruefen
```

Dev-Endpunkte:
- `http://localhost:8080/` — Angular SPA (Quinoa proxy auf ng dev-server :4200)
- `http://localhost:8080/q/health/ready`
- `http://localhost:8080/openapi` / `http://localhost:8080/q/swagger-ui`
- REST: `/api/v1/vertraege`, `/api/v1/freigaben`, `/api/v1/fristen`

## Paket-Layout (je BC identisch)

```
ch.grudligstrasse.mda.starter
  shared/
    events/      DomainEvent + InMemoryDomainEventPublisher
    problem/     DomainException + ProblemDetail + ExceptionMapper
    process/     BpfDefinition + BpfService + BpfInstance/TransitionLog-Entities
  contract/  approval/  obligation/
    domain/              Aggregates, VOs, Enums, sealed Domain-Events, BPF-Definition
    application/
      port/in/           UseCase-Interfaces (Command/Query-Records)
      port/out/          Repository-Interfaces
      service/           @ApplicationScoped Anwendungsservice (Transaktionsgrenze)
    adapter/
      in/rest/           JAX-RS-Resources + DTOs + Request-Records (Validation)
      in/scheduler/      (nur obligation) Quarkus-@Scheduled-Jobs
      out/persistence/   Panache-Entities + Repositories + Adapter (Port-Out-Impl)
```

## Frontend (Angular 21 + Material)

```
src/main/webui/
  angular.json, tsconfig*.json, proxy.conf.json, package.json
  src/
    main.ts, index.html, styles.scss
    app/
      app.ts, app.config.ts, app.routes.ts
      core/                 Services: api-client, vertrag, frist, freigabe, models.ts
      layout/app-shell/     MatToolbar + MatSidenav Shell
      pages/                Standalone-Components: vertrag-liste, vertrag-erfassen,
                            vertrag-detail, frist-liste
```

Konventionen:
- **Standalone Components**, `ChangeDetectionStrategy.OnPush`, `inject()` statt Constructor-DI.
- **Signals** (`signal`, `computed`) fuer lokalen State; Observables an Service-Grenzen.
- **Reactive Forms** + Jakarta-Validation-aequivalente Regeln (required/min/maxLength).
- **Material 3** Theme (azure/blue palette, Roboto, density 0) in `styles.scss`.
- **Lazy-loaded Routes** via `loadComponent: () => import(...)`.
- Services gehen ueber `ApiClient` (Fehlerkanal: `ProblemDetail` → `ApiError`).
- Dev-Proxy `/api`, `/q`, `/openapi` → `http://localhost:8080` (`proxy.conf.json`).

Build-Output: `src/main/webui/dist/webui/browser/` (Angular @angular/build:application).
Quinoa packt das in das Quarkus-Artifact und serviert es unter `/`.

## Regeln (erzwungen per ArchUnit)

- `domain/**` importiert **keine** Framework-Pakete (`jakarta.persistence`, `jakarta.ws.rs`, `io.quarkus`, `jakarta.inject/enterprise`).
- `application/**` kennt **keine** Persistenz-Details (nur Out-Ports).
- REST-Adapter ruft **nur** Eingangs-Ports (`port.in`), niemals direkt Anwendungsservice oder Out-Adapter.
- Bounded-Context-uebergreifende Kopplung: **nur via IDs oder Events**, nie via Services/Adaptern.

## BPF — Vertragslifecycle + Fristenerinnerung

- Definitionen in `…domain/VertragLifecycle.java` bzw. `…domain/FristenErinnerungProcess.java`.
- Laufzeit in `shared/process/BpfService`. Jede Transition schreibt Audit-Log-Eintrag.
- Ungueltige Uebergaenge werfen `DomainException` mit Code `MDA-BPF-001`.
- REST-Ausspielung: zuerst Aggregat-Seiteneffekt (einreichen/genehmigen/…), dann BPF-Transition in derselben Transaktion.

## Out of MVP Scope (bewusst weggelassen)

- Camunda 7 Workflow-Engine (keine BPMN-Inputs → Zero-Config-Prinzip).
- Business-Rules-DSL mit AST-Interpreter (keine `.rules.yaml`-Inputs).
- Playwright UI-BDD (Default-Modus ist `rest` ueber Domain-Aufrufe).
- Kafka-Producer; Events werden in-process ueber `InMemoryDomainEventPublisher` geloggt.

## Tests

- Surefire: Unit-Tests (`*Test.java`) inkl. `@QuarkusTest`.
- Failsafe: `ServiceBddIT`, `ProcessBddIT`, `UiBddIT` (Cucumber).
- ArchitectureTest prueft Schichten- und BC-Grenzen.
- Pyramiden-Heuristik: `./scripts/count-tests.sh`.

## Datenmodell

Flyway-Migrationen:
- `V1__init.sql` — `vertrag`, `dokument_version`, `freigabe`, `frist`.
- `V2__bpf.sql` — `bpf_instance`, `bpf_transition_log`.

## Weitere Dokumente

- `docs/architecture/arc42.md` — C4-Modell + Laufzeitsicht je Prozess.
- `docs/architecture/adr/*.md` — Architekturentscheide.
- `specs/model/*.puml` — Modell-Quellen, aus denen dieser Code generiert wurde.
<!-- mda-generator:end -->
