# CLM MVP V1 — Fachliche Quelle

Quelle der eingebetteten PlantUML-Diagramme. Die extrahierten Diagramme liegen als `01-domain.puml`, `02-state-vertrag.puml`, `03-process-erfassung.puml`, `04-process-versionierung.puml`, `05-process-fristerinnerung.puml`, `06-system-context.puml`, `07-process-retention.puml`.

- Kontext: Schweizer Versicherung (KVG/VVG), nDSG-/FINMA-Compliance.
- Zielplattform: Quarkus-Microservices, PostgreSQL, Kafka, Kubernetes.
- Bounded Contexts (MVP): **Contract Management**, **Approval**, **Obligation Management**.
- Querschnitt: Audit-Log (Event Sourcing, append-only), Identity (Keycloak), Blob-Storage (Write-Once).
- Retention: OR 958f, 10 Jahre; nDSG-konformes Löschen + Pseudonymisierung.
- Out of Scope (MVP): Template-Authoring, Redlining, ZertES-Signatur, NLP-Obligationsextraktion, mehrstufige Freigabeketten, Partner-Portal.

## Rollen

| Rolle | Hauptaktionen |
|---|---|
| Antragsteller | Vertrag anlegen, Version hochladen, Fristen pflegen |
| Reviewer | Vertrag genehmigen / ablehnen |
| Vertragsverantwortlicher | Fristerinnerungen bearbeiten |
| Compliance-Officer | Audit-Log einsehen |
| Administrator | Stammdaten pflegen |

## Ereignis-/Topic-Kontrakt (Kafka)

- `clm.contract.eingereicht.v1`
- `clm.contract.neueversion.v1`
- `clm.approval.entschieden.v1`
- `clm.obligation.fristaktiviert.v1`
- `clm.obligation.erinnerungausgeloest.v1`

Schema-Evolution: BACKWARD kompatibel, Apicurio/Confluent Registry.
