# MDA-Spezifikation (destilliert)

Normative Regeln fuer alle `mda-*`-Skills. Ersetzt das frühere `specs/MDA-Spezifikation.md`. Dies ist die **einzige** Quelle der Wahrheit fuer Fachmodellierung, nicht ein optionaler Leitfaden.

## 1. Grundprinzipien

1. **Modell vor Code** — Fachliche Wirkung entsteht durch deklarative Artefakte (Entity, BusinessRule, Workflow, ProcessFlow). Prozeduraler Code ist letzte Eskalation.
2. **Datenzentrierung** — Domänenmodell ist Single Source of Truth; Views, Forms, Navigation, Rollen leiten sich daraus ab.
3. **Konsistenz durch Interpretation** — Regeln laufen client- und serverseitig aus **einem** AST; serverseitig autoritativ.
4. **Deklarativer Vorrang** — Reihenfolge: Constraint → BusinessRule → Workflow → BPF → Plug-in-Code.
5. **Versionierbarkeit** — Jedes Artefakt (Entity, Form, Rule, BPF, Workflow) ist unabhaengig versioniert (SemVer), paketiert (Solution) und portierbar.
6. **Multi-Tenancy** — Tenant-Isolation per Default. Cross-Tenant-Zugriffe unzulaessig.
7. **Security & Governance by Design** — Rollen, Audit, Retention, Datenklassifizierung sind First-Class-Modellelemente.

## 2. Architektur-Schichten

Die Plattform hat **fuenf logische Schichten**. Jeder Bounded Context bildet sie in seinem Paketbaum ab:

1. **Persistenz** — PostgreSQL, Audit-/Outbox-Tabellen, Volltextindex.
2. **Metamodell** — Artefakt-Registry (JSONB), Versionen, Solutions.
3. **Engine** — CRUD, Rule-Engine, Workflow-Engine, BPF-Engine, Events, Authz, Audit.
4. **API** — REST (OData-kompatibel), GraphQL optional, Webhooks, Kafka.
5. **Renderer** — Angular-SPA + Material; interpretiert Modell + Daten.

Im Code abgebildet durch **hexagonale** Struktur (Port & Adapter), siehe `hexagonal-rules.md`.

## 3. Metamodell-Artefakte (Pflicht-Repertoire)

Jedes fachliche Feature besteht aus einer Kombination folgender Artefakte. Andere Konstrukte sind unzulaessig, solange nicht explizit per ADR zugelassen.

| Artefakt | Zweck | Quarkus-Umsetzung |
|---|---|---|
| **Entity** | Fachlicher Typ mit stabiler ID, Namen, Primary-Display, Owner-Modell, Lifecycle-Feldern | Aggregate-Root (DDD) + Panache-Entity |
| **Attribute** | Feld einer Entity: Typ, Constraints, Default, Klassifizierung, Lokalisierung | VO (Java `record`) + JPA-Column |
| **Relationship** | 1:1, 1:n, n:m, Hierarchie; Loesch-/Kaskadenverhalten explizit | Cross-Aggregate nur per ID |
| **View** | Liste (Spalten, Filter, Sortierung, Aggregation) | Read-only Query-Port `GetXxxList` + Angular-Listen-Component |
| **Form** | Formular (Typ, Header, Tabs, Sections, Felder, Regeln) | Request/DTO-Records + Angular-Reactive-Form |
| **BusinessRule** | Bedingung + Aktionen (setRequired, setVisible, setReadOnly, setValue, showError, showRecommendation) | JSON-AST + `RuleInterpreter` |
| **Workflow** | Ereignis-/zeitgetriebene Automation (Saga, Retry, Kompensation) | Camunda 7 embedded + Worker |
| **ProcessFlow (BPF)** | Gefuehrter Fachprozess, pro Datensatz max. 1 aktive Instanz | Panache-Zustandsautomat (`BpfService`) |
| **SiteMap** | Navigation, Rollenbindung | Angular `app.routes.ts` (lazy) |
| **Role / FieldSecurityProfile / Team** | RBAC grob + ABAC fein | Keycloak-Rollen + OPA-Policies |
| **Solution** | Paket versionierter Artefakte | Git-Monorepo-Modul; Transport via OCI-Artifact |

## 4. Datentypen fuer Attribute

Pflicht-Repertoire: `String`, `Integer`, `Decimal`, `Currency`, `Boolean`, `Date`, `DateTime`, `Duration`, `Enum/OptionSet`, `Lookup`, `File/Attachment`, `Image`, `UniqueIdentifier`, `JSON/Structured`, `Calculated`, `Rollup`, `Formula`. Jedes Attribut traegt Metadaten (Pflicht, Unique, Default, Format, Klassifizierung, Datenschutz, Sichtbarkeit, Searchable, Deprecated).

## 5. Geschaeftslogik — Schichtung

Reihenfolge (haerter vor weicher):

1. **Attribut-Constraints** — Hibernate Validator auf dem Record/Entity.
2. **Formular-BusinessRules** — AST im Metamodell; Angular-Interpreter **und** Server-Interpreter. Serverseitig bei Save autoritativ.
3. **Entitaets-BusinessRules** — serverseitig fuer **jeden** Schreibweg (REST, Event, Workflow).
4. **Workflow** — asynchron, persistent protokolliert, Retry/Exponential-Backoff, Kompensation (Saga).
5. **BPF** — Zustandsautomat mit Transition-Log; ungueltige Transition → `DomainException MDA-BPF-001`.
6. **Plug-in-Code** — Last Resort, einzelne domain-spezifische Berechnung; nicht um UI zu ersetzen.

Alle serverseitigen Regeln sind **deterministisch** und **idempotent**. Einmalige Seiteneffekte per Idempotenz-Schluessel.

## 6. Konfliktbehandlung

Optimistic Concurrency ueber `versionNumber`. HTTP 409/412 mit Problem+JSON (`code: MDA-CONFLICT-001`).

## 7. API-Konvention (REST)

- Basis-URL `/api/v1/<plural-entity-snake>` (z. B. `/api/v1/vertraege`).
- Aktionen als Sub-Ressourcen: `POST /api/v1/vertraege/{id}/einreichen`.
- BPF: `POST /api/v1/{entity}/{id}/process/{process}/trigger/{trigger}` → 200 + neues Stage.
- Status-Codes einheitlich; Fehler als **RFC 7807 Problem+JSON** (`application/problem+json`), Code `MDA-<AREA>-<NR>`.
- Query-Konvention OData-kompatibel (`$filter`, `$select`, `$orderby`, `$top`, `$skip`).
- Versionierung: Pfad-Prefix `/api/v1/...`. Breaking Changes nur per Bump (`/api/v2/...`).

## 8. Sicherheit

- **AuthN**: OIDC via Keycloak (Auth Code + PKCE fuer UI, Client Credentials fuer Services). ACR-Claims fuer Step-Up.
- **AuthZ**: zweistufig.
  - RBAC grob (Keycloak-Rollen).
  - ABAC fein (OPA-Policies in Rego, gecacht).
- **Row-Level-Filter**: OPA-Entscheidung → SQL-Praedikat, in Panache-Queries injiziert.
- **Feldsicherheit**: Projektionsmaske vor JSON-Serialisierung.
- **Datenklassifizierung**: `oeffentlich | intern | vertraulich | streng_vertraulich | personenbezogen` als Attribut-Metadatum.

## 9. Audit & Eventing

- Jeder fachliche Schreibvorgang erzeugt **deterministisch** ein Domain-Event (Outbox-Muster, Debezium).
- Event-Schema: Avro, `BACKWARD TRANSITIVE`.
- Pflichtfelder: `eventId, eventType, eventTime, tenantId, correlationId, causationId, traceparent, entityName, entityId, version, changedFields, payload, actor`.
- Topic-Layout: `<tenant>.<domain>.<entity>.<event>.v<major>`.

## 10. Testpflicht

Siehe `testing-pyramid.md`. Jedes Feature braucht **mindestens** Unit + Integration + BDD (`@service` oder `@process` oder `@ui`).

## 11. Barrierefreiheit

Renderer erzeugt semantisches Markup (Labels, ARIA, Landmarks). Mindestziel **WCAG 2.2 AA**. Tastatur-Navigation ueber alle Felder/Tabs/Aktionen Pflicht.

## 12. Was explizit NICHT Teil des Modells ist

- Handgeschriebene CRUD-Controller pro Entity.
- UI-Zustand, der nicht aus dem Metamodell ableitbar ist.
- Privat gepflegte Regel-Listen im Code (alles ueber `BusinessRule`-Artefakte).
- Direktkopplung zwischen Bounded Contexts (nur IDs + Events).
