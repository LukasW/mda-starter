# arc42 — Contract Lifecycle Management (CLM)

## 1. Einführung und Ziele

Das CLM ist ein modularer Monolith auf Quarkus 3.34 + Angular 21, der den Lebenszyklus von Verträgen unterstützt. Fachliche Quelle: `specs/model/00-spec-clm.md`. Im Erstentwurf liefert das System zwei Bounded Contexts (`contract`, `person`) samt BPF-Lifecycle und REST-API.

Qualitätsziele (laut Fachspec AE-06):

- **Verfügbarkeit:** 99.9% Backend, externe Systeme Best-Effort.
- **Konsistenz:** Deterministische BPF-Transitions mit Audit-Log.
- **Datenresidenz:** Schweiz / akzeptierte EU-Regionen.
- **Wartbarkeit:** Hexagonale Architektur + ArchUnit-Erzwingung.

## 2. Randbedingungen

- JVM 25 (Temurin), Maven 3.9+.
- PostgreSQL 16+ (prod), H2 in-memory (dev/test).
- Keycloak 24+ (später verdrahtet, AE-01).
- Kubernetes Deployment via Helm/Argo (Stack laut `.claude/skills/_shared/mda-stack.md`).

## 3. Kontextabgrenzung

```
                       ┌────────────────────────────────┐
 Sachbearbeiter/       │            CLM                 │
 Rechtsabteilung/      │                                │
 Geschaeftsfuehrung    │  ┌─────────┐   ┌───────────┐   │
     ───── HTTPS ───▶  │  │contract │   │  person   │   │
                       │  └─────────┘   └───────────┘   │
                       │        │            │          │
                       │        ▼            ▼          │
                       │    PostgreSQL (shared, schema) │
                       └───────┬─────────────┬──────────┘
                               │             │
                     Archiv (ext., opt.)  Personenverwaltung
                     Signaturprovider (opt.)     (ext., opt.)
```

## 4. Lösungsstrategie

- **Hexagonal + DDD** je BC: Aggregate Root + Value Objects, `application/port/in`-UseCases, `port/out`-Repositories, Adapter in `adapter/in/rest` und `adapter/out/persistence`.
- **BPF-Engine** (`shared/process/BpfService`) als Panache-Zustandsautomat. Definition je BC (z. B. `VertragLifecycle`) ist pure Java.
- **Events**: sealed `VertragDomainEvent` + konkrete Records. Im Erstentwurf `InMemoryDomainEventPublisher`; Kafka/Outbox als spätere Erweiterung (ADR 0005).
- **Frontend**: Angular 21 Standalone + Material 3 über Quinoa. Services kapseln REST-Zugriffe; Fehler werden von `ApiClient` auf `ApiError` normalisiert.

## 5. Bausteinsicht (C4)

### 5.1 C4 Level 1 — System

Wie in Kapitel 3 abgebildet.

### 5.2 C4 Level 2 — Container

| Container | Technologie | Zweck |
|---|---|---|
| Backend | Quarkus REST + Panache | CLM-Fachlogik, BPF-Engine, Persistenz |
| Frontend | Angular 21 + Material | SPA, eingebettet via Quinoa |
| Datenbank | PostgreSQL 16 | transaktionale Persistenz |
| IdP (später) | Keycloak | OIDC/JWT (AE-01) |

### 5.3 C4 Level 3 — Komponenten

**`contract`-BC:**

- `Vertrag` — Aggregate Root (Invarianten, Events).
- `VertragApplicationService` — transaktionale Use-Case-Orchestrierung + BPF-Start/Trigger.
- `VertragPanacheRepository` — Port-Out-Implementierung, Mapper.
- `VertragResource` — REST-Adapter (`/api/v1/vertraege`).
- `VertragLifecycle` — BPF-Definition (9 Stages, 9 Transitions).

**`person`-BC:**

- `Person` — Aggregate Root.
- `PersonApplicationService` — Erfassen + Suche (lokal + extern).
- `PersonPanacheRepository` — lokaler Cache.
- `ExternePersonenverwaltungClient` — Out-Port für externe Quelle (Stub: `DisabledExternePersonenverwaltungClient`).
- `PersonResource` — REST-Adapter (`/api/v1/personen`).

**Shared:**

- `shared/events` — Event-Publisher, In-Memory-Default.
- `shared/problem` — RFC 7807 + `DomainException`-Mapper.
- `shared/process` — BPF-Engine (Entities, Repositories, Service).

## 6. Laufzeitsicht

### 6.1 Vertrag erfassen und einreichen

```
Sachbearbeiter → POST /api/v1/vertraege (JSON)
  → VertragResource.erstellen
    → VertragApplicationService.execute(VertragErstellenCommand)
      → Vertrag.erstellen(...)                       (Domain)
      → VertragRepository.save(v)                    (Port-Out)
      → BpfService.start(VertragLifecycle, id, ...)  (BPF: ENTWURF)
      → DomainEventPublisher.publish(...)
  → 201 Created + Location
Sachbearbeiter → POST /api/v1/vertraege/{id}/process/contract/trigger/einreichen
  → BpfService.trigger(...) ⇒ IN_PRUEFUNG
  → Vertrag.stageWechseln(IN_PRUEFUNG, "einreichen", actor)
  → bpf_transition_log-Eintrag
```

### 6.2 Ungültige Transition

```
Client → POST .../trigger/unterzeichnen  (Stage=ENTWURF)
BpfService.trigger → DomainException("MDA-BPF-001") → 422 Problem+JSON
```

### 6.3 Personensuche (Hybrid)

```
Client → GET /api/v1/personen?query=meier
PersonApplicationService.suchen
  → PersonRepository.search("meier")                     (lokal)
  → ExternePersonenverwaltungClient.isEnabled() ? suchen : leer
  → merge (lokal + Snapshot EXTERN_API)
```

## 7. Verteilungssicht

Phase 1: Single-JAR (Backend + Angular-Bundle via Quinoa) auf Kubernetes. PostgreSQL separat. Phase 2: separate Worker für Outbox-Publisher (Kafka, ADR 0005).

## 8. Querschnittliche Konzepte

- **Transaktionen:** nur Application-Services tragen `@Transactional`. BPF-Transition und Aggregat-Aenderung im selben TX.
- **Audit-Log:** `bpf_transition_log` pro Transition. Domain-Events decken semantische Audit-Spuren.
- **Fehlermodell:** `DomainException(code, status, message)` → Mapper liefert `ProblemDetail`. Validation-Errors über `ValidationExceptionMapper`.
- **Multi-Tenancy:** `tenant_id`-Spalte auf allen Tabellen; Default-Tenant `00000000-0000-0000-0000-000000000001` im Erstentwurf (ADR 0006).

## 9. Architekturentscheide

Siehe `docs/architecture/adr/0001..0009-*.md`.

## 10. Qualitätsanforderungen

- `./scripts/count-tests.sh` muss grün sein (Testpyramide).
- `./mvnw clean verify` grün (Surefire + Failsafe + ArchUnit + Cucumber).
- Angular-Bundle < 1.5 MB (Budget in `angular.json`).

## 11. Risiken und technische Schulden

- OIDC/Keycloak noch nicht integriert — lokale Accounts im Erstentwurf nicht ausgereift.
- Signatur/Archiv/Personenverwaltung nur als Stubs — Anbieter ausstehend (OP-A, OP-B).
- BPF-Partielle-Uniqueness applikationsseitig (H2-Limitation); PostgreSQL kann später filterndes Unique-Index nachrüsten.
- DSFA-Begründung fehlt noch (OP-C).

## 12. Glossar

- **BPF** — Business Process Flow: Panache-Zustandsautomat für pro-Aggregat-Prozesse.
- **Aggregate Root** — DDD-Entity, die Invarianten hält.
- **Problem+JSON** — RFC 7807 Fehlerformat.
