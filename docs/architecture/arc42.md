# arc42 — Contract Lifecycle Management (CLM) MVP V1

## 1 Einfuehrung und Ziele

Zentraler Dienst fuer die revisionssichere Verwaltung von Vertraegen (Schweizer Versicherung, KVG/VVG). MVP-Ziel: *keine verlorenen Vertraege, keine verpassten Fristen, nachvollziehbare Freigaben* — gemaess nDSG und OR 958f. Technischer Stack: Quarkus 3.34 LTS, PostgreSQL (Prod) / H2 (Dev), Flyway, Hibernate ORM + Panache.

**Qualitaetsziele**

| Prioritaet | Ziel | Szenario |
|---|---|---|
| 1 | Revisionssicherheit | Jede Statusaenderung am Vertrag hat einen unveraenderlichen Audit-Eintrag |
| 2 | Fristen-Treue | Jede aktive Frist loest rechtzeitig eine Erinnerung aus |
| 3 | Antwortzeit | p95 < 2 s fuer Listen; Upload < 10 s / 25 MB |

**Stakeholder**: Antragsteller, Reviewer, Vertragsverantwortliche, Compliance-Officer, Administrator.

## 2 Randbedingungen

- Datenhaltung ausschliesslich in CH / EU/EWR (Azure Region Switzerland North).
- Audit-Log 10 Jahre retainen (OR 958f).
- Quarkus-Microservice-Ansatz; ein Bounded Context pro Service spaeter; MVP = Monolith mit klaren BC-Grenzen.

## 3 Kontextabgrenzung (C4 L1)

Akteure und Nachbarsysteme siehe `specs/model/06-system-context.puml`.

- **CLM Frontend (Angular)** → REST gegen `clm`.
- **Keycloak** → OIDC (im MVP als Stub; volle Integration ab V1.1).
- **PostgreSQL** → Persistenz.
- **Blob Storage** → Dokumentinhalte (im MVP nur Referenzen, kein Upload-Pfad generiert).
- **SMTP / Kafka / OpenSearch** → ueber Events angeliefert, im MVP In-Memory-Stubs.

## 4 Bausteinsicht (C4 L2/L3)

**Bounded Contexts**

| BC | Verantwortung | Aggregate |
|---|---|---|
| contract | Vertragsstamm, Versionen, Vertrags-Lifecycle | `Vertrag`, `DokumentVersion` |
| approval | Freigabeworkflow, Reviewer-Entscheid | `Freigabe` |
| obligation | Fristen, Erinnerungen | `Frist` |

**Schichten je BC (Hexagonal)**

```
adapter.in.rest  ──▶  application.port.in  ──▶  application.service  ──▶  application.port.out  ──▶  adapter.out.persistence
adapter.in.scheduler ─▶ (nur obligation)                                  ▲
                                                                          │
                                                       domain (Aggregates, VOs, Events, BPF-Definitionen)
```

- `domain/**` ist **framework-frei**, in ArchUnit verankert.
- `application/**` kennt nur Ports; Transaktionsgrenze.
- `adapter/**` implementiert Port-Out oder konsumiert Port-In (REST, Scheduler).

**Quer-Infrastruktur**

- `shared.events` — sealed `DomainEvent`-Hierarchie + `InMemoryDomainEventPublisher`.
- `shared.problem` — `DomainException` mit fachlichem Code (`MDA-<area>-<nr>`), `DomainExceptionMapper` gibt `application/problem+json` zurueck.
- `shared.process` — BPF-Statemachine (`BpfDefinition`, `BpfService`, `BpfInstanceEntity`, `BpfTransitionLogEntity`).

**Frontend (Angular 21 + Material)**

Single-Page-Application unter `src/main/webui/`, ueber Quarkus Quinoa 2.8.1 in das Quarkus-Deployment integriert (ein JAR fuer Backend + UI). Details in ADR 0009. Schichten:

- `core/` — Services (`ApiClient`, `VertragService`, `FristService`, `FreigabeService`) + Typed Models.
- `layout/app-shell` — Material Toolbar + Sidenav + Router-Outlet.
- `pages/` — Standalone-Components je Screen (`VertragListe`, `VertragErfassen`, `VertragDetail`, `FristListe`), Routes lazy geladen.

Best Practices: Standalone-Components mit `ChangeDetectionStrategy.OnPush`, `inject()`, Signals fuer lokalen State, Reactive Forms, Material-3-Theme.

## 5 Laufzeitsicht

### 5.1 Vertragserfassung + Freigabe (BPF)

Quelle: `specs/model/03-process-erfassung.puml`.

```
Antragsteller → POST /api/v1/vertraege
  VertragApplicationService.erfassen(cmd)
  → Vertrag.erfassen() [Aggregate-Factory]
  → VertragRepository.save()
  → BpfService.startOrGet(VertragLifecycle.DEFINITION, vertragId) → stage=ENTWURF
  → publish VertragErfasst

Antragsteller → POST /api/v1/vertraege/{id}/einreichen
  service.einreichen()
  → Vertrag.markiereEingereicht() [Guard: Version hochgeladen, Status editierbar]
  → BpfService.transition(..., EINREICHEN) → IN_PRUEFUNG
  → publish VertragEingereicht

Reviewer    → POST /api/v1/vertraege/{id}/genehmigen
  → Vertrag.markiereFreigegeben()
  → BpfService.transition(..., GENEHMIGEN) → FREIGEGEBEN
  → publish VertragFreigegeben
```

### 5.2 Versionierung (BPF)

Quelle: `specs/model/04-process-versionierung.puml`. Aenderungskommentar ist Pflicht (`MDA-CON-021`). Neue Version setzt Status aus `UEBERARBEITUNG` oder `FREIGEGEBEN` zurueck auf `IN_PRUEFUNG`; aus `UEBERARBEITUNG` wird zusaetzlich die BPF-Transition `NEUE_VERSION_HOCHLADEN` gefeuert.

### 5.3 Fristenerinnerung (BPF + Scheduler)

Quelle: `specs/model/05-process-fristerinnerung.puml`. Scheduler (`FristScheduler`, `@Scheduled("0 0 2 * * ?")`) laeuft taeglich um 02:00 CET (in Dev/Test deaktiviert ueber Config `clm.obligation.scheduler.enabled`). `FristApplicationService.erinnere(stichtag)` listet `OFFEN`-Fristen mit `erinnerungsDatum ≤ stichtag`, setzt sie auf `ERINNERT`, publiziert `FristErinnerungAusgeloest`. Sichten durch Verantwortlichen → `ERLEDIGT`.

### 5.4 Retention (nicht im Code-Pfad des MVP automatisiert)

Quelle: `specs/model/07-process-retention.puml`. Uebergang `RETENTION_FRIST_LAEUFT` → `ARCHIVIERT` ist in `VertragLifecycle.DEFINITION` bereits vorgesehen; Ausloeser (Cron-Job oder manueller Trigger) ist nicht Teil des MVP-Skeletts und wird in V1.1 implementiert.

## 6 Verteilungssicht

- Single Quarkus-Deployment (JVM-Container). Native-Build-Profil `-P native` vorbereitet.
- DB-Skript via Flyway beim Boot.
- Kubernetes-Deployment wird separat unter `deploy/` gepflegt (nicht Teil des MVP-Starters).

## 7 Querschnittliche Konzepte

- **Fehlermodell**: `DomainException` → `application/problem+json` (RFC 7807). Codeschema `MDA-<BC>-<3-stellige Nummer>`, `MDA-BPF-001` fuer verbotene Transitions. Konkrete Codes vergibt das Aggregate selbst.
- **Transaktionen**: `@Transactional` ausschliesslich auf Anwendungsservices. Adapter und Domain sind transaktionsfrei.
- **Audit**: `bpf_transition_log` haelt jede Zustandsaenderung der BPF-Instanzen; voll-umfaengliches Audit-Event-Streaming folgt ueber Kafka in V1.1.
- **Sicherheit**: Im MVP noch kein Keycloak-Integrations-Code (OIDC-Extension erst ab V1.1 aktiviert); REST-Endpunkte nehmen Nutzer-IDs im Payload entgegen (Stub).

## 8 Entwurfsentscheide

Siehe `docs/architecture/adr/`:

- 0001 Hexagonale Architektur je BC.
- 0002 Quarkus 3.34 LTS + Java 25.
- 0003 Flyway-basierte DB-Schema-Verwaltung.
- 0004 ArchUnit zur Architektur-Assertion.
- 0005 Cucumber-JUnit-Platform-Suites fuer BDD.
- 0006 BPF-Statemachine auf Panache statt Camunda.
- 0007 Camunda 7 embedded ausdruecklich **nicht** im MVP (Zero-Config).
- 0008 Business-Rules-Engine ausdruecklich **nicht** im MVP (Zero-Config; Validierung in Compact Constructors).
- 0009 Angular 21 + Material ueber Quarkus Quinoa (ein JAR fuer Backend + UI).

## 9 Qualitaetsanforderungen

| Kategorie | Ziel |
|---|---|
| Startup | `quarkus:dev` < 10 s lokal |
| Deployment | Native-Build optional via `-P native` |
| Verfuegbarkeit | 99.5 % Bueroszeiten (Spec §6) |
| Datenhaltung | CH / EU/EWR |
| RPO/RTO | 24 h / 4 h |

## 10 Risiken und technische Schuld

- Event-Bus ist In-Memory — bei Multi-Instance-Deployment gehen Events verloren. V1.1: Outbox-Tabelle + Kafka-Producer.
- Scheduler laeuft in-process; Multi-Instance braucht `@Scheduled(concurrentExecution=SKIP)` oder ShedLock. Fuer MVP (Single-Instance) ausreichend.
- Keine OIDC-Absicherung der REST-Endpunkte; MVP setzt Vertrauensgrenzen des internen Netzes voraus.

## 11 Glossar

- **BPF** — Business Process Flow, leichter Zustandsautomat (Spec §12) als Alternative zu Camunda bei einfachen Status-Lebenszyklen.
- **Aggregate Root** — DDD-Einheit, die Invarianten garantiert. Cross-Aggregate-Kopplung nur via IDs.
- **Port / Adapter** — Hexagonal: Ports sind Interfaces; Adapter implementieren oder konsumieren sie.
