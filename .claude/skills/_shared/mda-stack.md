# MDA Quarkus Stack (destilliert)

Normative Technologiewahl fuer alle `mda-*`-Skills. Ersetzt das frühere `specs/MDA-Quarkus-Stack.md`. Abweichungen verlangen einen ADR.

## 1. Leitplanken

- **Quarkus** (LTS 3.x) als verbindliches JVM-Framework, JVM-Mode in Prod; GraalVM Native nur fuer Edge.
- **PostgreSQL 16+** als einzige transaktionale Primaerablage.
- **Apache Kafka** als einziger fachlicher Event Bus.
- **Keycloak** als zentraler IdP.
- **Kubernetes** (AKS + On-Prem-MicroK8s) fuer Laufzeit; Helm + Argo CD.
- **Swiss Data Residency** (Switzerland North / akzeptierte EU-Regionen).
- **OpenTelemetry** als einziger Observability-Standard.
- **Angular 21+** als einziges Frontend-Framework, Material als UI-Kit, Quinoa-Integration mit Quarkus.

## 2. Stack-Uebersicht

| Schicht | Wahl |
|---|---|
| Laufzeit | Quarkus 3.x LTS, Temurin JVM 21 |
| Persistenz | PostgreSQL 16+, Hibernate ORM with Panache, Flyway |
| Metamodell-Store | PostgreSQL + JSONB + JSON-Schema-Validierung |
| REST | RESTEasy Reactive (`quarkus-rest`, `quarkus-rest-jackson`), SmallRye OpenAPI |
| GraphQL (optional) | SmallRye GraphQL |
| Validierung | Hibernate Validator (Jakarta Bean Validation) |
| Rule-Engine | Eigener deklarativer AST-Interpreter (`mda-rules-lib`); Drools nur fuer Sonderfaelle |
| Workflow-Engine | Camunda 7 Community embedded (in Quarkus) |
| BPF-Engine | Panache-Zustandsautomat (eigen, siehe `bpf-guide.md`) |
| Event Bus | Apache Kafka + Apicurio Schema Registry, Avro |
| Outbox | Debezium Outbox (Kafka Connect) auf PostgreSQL Logical Replication |
| IdP | Keycloak 24+, OIDC, SmallRye JWT |
| Authz-Fein | OPA (Rego) via eigenem Client; Cache via Caffeine |
| Secrets | Vault on-prem / Azure Key Vault; `quarkus-vault` bzw. Azure-Key-Vault-Ext |
| Jobs | JobRunr Pro + Quarkus Scheduler |
| Search | PostgreSQL tsvector (Phase 1); OpenSearch ab Phase 2 |
| Files | S3-kompatibel (MinIO / Azure Blob) + ClamAV |
| Observability | quarkus-opentelemetry, quarkus-micrometer-registry-prometheus, quarkus-smallrye-health, quarkus-logging-json |
| CI/CD | GitLab Dedicated, Argo CD, Kaniko, Trivy, Cosign |
| Policy-as-Code | OPA Gatekeeper im Cluster, Conftest in CI |
| Frontend | Angular 21+ (Standalone + Signals), Angular Material + CDK, via Quinoa (`io.quarkiverse.quinoa:quarkus-quinoa`) |

## 3. Pflicht-Extensions (minimaler Satz je Service)

```
quarkus-rest
quarkus-rest-jackson
quarkus-smallrye-openapi
quarkus-smallrye-health
quarkus-hibernate-validator
quarkus-hibernate-orm-panache
quarkus-jdbc-postgresql
quarkus-flyway
quarkus-arc
quarkus-cache               # Caffeine
quarkus-opentelemetry
quarkus-smallrye-jwt        # Authz-vorbereitend
quarkus-quinoa              # Frontend-Integration (immer)
```

Eigenbedarfsbedingt:

```
quarkus-messaging-kafka            # wenn Events oder eingehende Kafka-Topics
quarkus-apicurio-registry-avro     # mit Kafka gebundelt
debezium-quarkus-outbox            # wenn Outbox-Schreibung noetig
quarkus-oidc                       # sobald Keycloak aktiv
camunda-platform-7-quarkus         # bei BPMN-Workflow
quarkus-hibernate-envers           # wenn Entitaets-Audit benoetigt
```

## 4. Datenbank-Konvention

- **Zwei Schemata** in derselben PostgreSQL-Instanz:
  - `platform` — Metamodell, Solutions, Audit, Outbox, Jobs, Rollen.
  - `app` — Fach-Entitaeten (getrennt nach BC).
- **Flyway** ausschliesslich **additiv**: `V<N>__<slug>.sql`. Existierende V-Dateien **nie** editieren.
- **Optimistic Locking** ueber `version_number` (bigint, monoton).
- **Systemfelder** auf jeder Entity: `id uuid pk`, `tenant_id uuid not null`, `created_at timestamp`, `created_by text`, `modified_at timestamp`, `modified_by text`, `version_number bigint`.
- **RLS**: Row-Level-Security-Policy per Tenant aktiv (`SET LOCAL app.tenant_id = ?`).

## 5. REST-Konvention

- Basis: `/api/v1/...`. Response-Typ `application/json`, Fehler `application/problem+json`.
- URL-Pluralform in der Fachsprache (z. B. `/api/v1/vertraege`, nicht `/contracts`).
- Fehler-Schema RFC 7807 mit zusaetzlichen Feldern `code`, `correlationId`, `errors[]`.
- OpenAPI publiziert unter `/openapi` + Swagger-UI unter `/q/swagger-ui`.

## 6. Frontend-Konvention

- `src/main/webui/` — Angular-Projekt, via Quinoa in das Quarkus-Artifact gebaut.
- **Standalone Components**, `ChangeDetectionStrategy.OnPush`, `inject()`.
- **Signals** fuer lokalen State; RxJS an Service-Grenzen.
- **Reactive Forms** + Jakarta-Validation-aequivalent (required/min/maxLength).
- **Material 3** Theme (azure/blue palette, Roboto, density 0).
- **Lazy-loaded Routes** per `loadComponent: () => import(...)`.
- Dev-Proxy `/api`, `/q`, `/openapi` → `http://localhost:8080` via `proxy.conf.json`.
- `application.properties`: `quarkus.quinoa.ignored-path-prefixes=/api,/q,/openapi` (verhindert Proxy-Loop).

## 7. Observability-Pflicht

- Alle Services exportieren `q/health/{live,ready}`.
- Log-Format JSON (`quarkus-logging-json`), Felder `timestamp`, `level`, `traceId`, `spanId`, `tenantId`, `correlationId`.
- Metriken via Prometheus-Endpoint (`/q/metrics`).
- Traces ueber OTLP.

## 8. Bewusste Trade-offs (nicht neu verhandeln ohne ADR)

- **Modularer Monolith** statt feinkoernige Microservices in Phase 1.
- **Camunda 7** statt Camunda 8 (kein zusaetzlicher Zeebe-Cluster in Phase 1).
- **RLS in PostgreSQL** statt Silo-Datenbanken je Tenant.
- **Eigene Rule-Engine** statt Drools (UI-naher AST, client+server identisch).
- **Angular** statt React.
- **Eigene generische API** statt OData-Stack.
- **Quarkus Native nur punktuell** (Edge-Services).

## 9. Versionen **immer** via Context7/Release-API verifizieren

Nie aus Trainingsdaten herleiten. Bei `io.quarkiverse.quinoa:quarkus-quinoa` zusaetzlich `gh api repos/quarkiverse/quarkus-quinoa/releases` konsultieren (Maven-Central-Index manchmal veraltet; Quinoa<2.6 inkompatibel zu Quarkus≥3.20).
