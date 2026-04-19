# CLM MVP V1 — Fachliche Spezifikation

Dies ist die **einzige fachliche Quelle** dieses Repos. Alle Domain-, REST-, Persistenz- und Frontend-Entscheide mussen mit diesem Dokument konsistent sein. Die PlantUML-Diagramme `01-domain.puml`, `02-state-vertrag.puml`, `03-process-erfassung.puml`, `04-process-versionierung.puml`, `05-process-fristerinnerung.puml`, `06-system-context.puml`, `07-process-retention.puml` werden aus diesem Dokument abgeleitet.

## Kontext

- Kontext: Schweizer Versicherung (KVG/VVG), nDSG-/FINMA-Compliance.
- Zielplattform: Quarkus-Microservices, PostgreSQL, Kafka, Kubernetes.
- Querschnitt: Audit-Log (Event Sourcing, append-only), Identity (Keycloak), Blob-Storage (Write-Once).
- Retention: OR 958f, 10 Jahre; nDSG-konformes Loeschen + Pseudonymisierung.

## Bounded Contexts

Drei BCs, als Java-Package-Namen verwendet:

| BC | Package | Zweck |
|---|---|---|
| Contract Management | `contract` | Vertrag anlegen, Versionen, Lifecycle |
| Approval | `approval` | Freigabe erteilen / ablehnen |
| Obligation Management | `obligation` | Fristen, Erinnerungen |

Package-Root: `ch.grudligstrasse.mda.starter`.

## Rollen

| Rolle | Hauptaktionen |
|---|---|
| Antragsteller | Vertrag anlegen, Version hochladen, Fristen pflegen |
| Reviewer | Vertrag genehmigen / ablehnen |
| Vertragsverantwortlicher | Fristerinnerungen bearbeiten |
| Compliance-Officer | Audit-Log einsehen |
| Administrator | Stammdaten pflegen |

## REST-API (v1)

- `/api/v1/vertraege` — Contract Management
- `/api/v1/freigaben` — Approval
- `/api/v1/fristen` — Obligation Management

URL-Pluralform in der Fachsprache (deutsch), nicht englische Uebersetzung.

## BPF-Prozesse (Business Process Flow)

- `contract/domain/VertragLifecycle.java` — Stages: `ENTWURF → IN_PRUEFUNG → {AKTIV | ABGELEHNT}`.
- `obligation/domain/FristenErinnerungProcess.java` — Fristen-Zustandsautomat.

Ungueltige Uebergaenge werfen `DomainException` mit Code `MDA-BPF-001`.

## Persistenz (Flyway)

- `V1__init.sql` — Tabellen `vertrag`, `dokument_version`, `freigabe`, `frist`.
- `V2__bpf.sql` — Tabellen `bpf_instance`, `bpf_transition_log`.

Bestehende V-Dateien sind unveraenderlich; Aenderungen nur additiv als `V<n>__<slug>.sql`.

## Frontend-Seiten (Angular 21 + Material)

Standalone-Components unter `src/main/webui/src/app/pages/`:

- `vertrag-liste`, `vertrag-erfassen`, `vertrag-detail`
- `frist-liste`

Services unter `core/`: `api-client`, `vertrag`, `frist`, `freigabe`, `models.ts`.

## Ereignis-/Topic-Kontrakt (Kafka)

- `clm.contract.eingereicht.v1`
- `clm.contract.neueversion.v1`
- `clm.approval.entschieden.v1`
- `clm.obligation.fristaktiviert.v1`
- `clm.obligation.erinnerungausgeloest.v1`

Schema-Evolution: BACKWARD kompatibel, Apicurio/Confluent Registry.

## Out of MVP Scope

- Template-Authoring, Redlining, ZertES-Signatur, NLP-Obligationsextraktion, mehrstufige Freigabeketten, Partner-Portal.
- Camunda 7 Workflow-Engine (keine BPMN-Inputs → Zero-Config-Prinzip).
- Business-Rules-DSL mit AST-Interpreter (keine `.rules.yaml`-Inputs).
- Playwright UI-BDD (Default-Modus ist `rest` ueber Domain-Aufrufe).
- Kafka-Producer im MVP; Events werden in-process ueber `InMemoryDomainEventPublisher` geloggt.
