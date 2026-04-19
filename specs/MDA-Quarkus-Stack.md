**Technologiestack**

**Model-Driven Application Platform auf Quarkus**

*Konkreter Implementierungsvorschlag zur MDA-Referenzspezifikation v1.0*

Kontext: Swiss Insurance, KVG/VVG, FINMA, nDSG · Hybrid On-Prem \+ AKS

# **Inhaltsverzeichnis**

# **1\. Leitplanken der Technologieauswahl**

Der Stack ist entlang folgender Leitplanken zusammengestellt. Alle Auswahlentscheidungen sind gegen diese Kriterien geprüft; wo Kompromisse nötig waren, sind sie in Kapitel 13 als bewusste Trade-offs dokumentiert.

* Quarkus als verbindliches Laufzeit-Framework für alle JVM-Komponenten; JVM-Mode in Produktion, GraalVM Native nur für ausgewählte Edge-Dienste.

* PostgreSQL 16+ als einzige transaktionale Primärablage; keine zweite operative Datenbank.

* Apache Kafka (on-prem oder Confluent/Azure Event Hubs mit Kafka-Protokoll) als einziger fachlicher Event Bus.

* Keycloak als zentraler Identity Provider und Policy-Hub (OIDC, Token-Issuer); keine fachspezifischen Auth-Silos.

* Kubernetes (on-prem MicroK8s/AKS) als Laufzeitplattform; Deployment über Helm und Argo CD.

* Alles Open Source oder kommerzielle Komponenten mit klarem Exit-Pfad; keine Bindung an proprietäre Low-Code-Stacks.

* Swiss Data Residency: Primärdaten in Schweizer oder FINMA-akzeptierten EU-Regionen; AKS-Region Switzerland North bevorzugt.

* Konsistente Observability über OpenTelemetry; keine parallelen APM-Lösungen.

# **2\. Stack-Übersicht auf einen Blick**

| Schicht | Technologiewahl |
| :---- | :---- |
| Laufzeit-Framework | Quarkus 3.x LTS (JVM 21 Temurin) |
| Persistenz | PostgreSQL 16+, Hibernate ORM with Panache, Flyway |
| Metamodell-Store | PostgreSQL \+ JSONB mit JSON-Schema-Validierung |
| API / REST | RESTEasy Reactive, SmallRye OpenAPI, OData-kompatible Konventionen |
| GraphQL (optional) | SmallRye GraphQL |
| Regel- und Workflow-Engine | Eigene deklarative Engine \+ Camunda 7 Embedded für BPMN/DMN (oder Camunda 8 Zeebe extern) |
| Event Bus | Apache Kafka \+ Schema Registry (Apicurio), Avro-Schemas |
| Outbox / CDC | Debezium (Kafka Connect) auf PostgreSQL Logical Replication |
| Identity & Access | Keycloak 24+, OIDC, SmallRye JWT, OPA (Rego) für feingranulare Policies |
| Sekrete / Crypto | HashiCorp Vault, Azure Key Vault (für AKS-Workloads), Customer-Managed Keys |
| Job-Scheduler | JobRunr Pro (du nutzt ihn bereits) \+ Quarkus Scheduler |
| Search / Volltext | PostgreSQL tsvector (Phase 1), OpenSearch (Phase 2 ab Skalierungsbedarf) |
| File-Storage | S3-kompatibler Object Store (MinIO on-prem, Azure Blob in Cloud) mit Virus-Scan (ClamAV) |
| UI-Renderer | Angular 18+ (Standalone Components, Signals), Angular Material \+ CDK, Nx Monorepo |
| Designer / Authoring | Angular-SPA \+ Monaco Editor (Modell-als-Code), JSON-Schema Live-Validierung |
| API-Gateway | Kong oder Azure API Management; mTLS zwischen internen Services via Istio/Linkerd |
| Messaging-Patterns | SmallRye Reactive Messaging (Kafka-Konnektor), MicroProfile Context Propagation |
| Observability | OpenTelemetry (OTLP), Prometheus, Grafana, Loki, Tempo, SmallRye Health |
| CI/CD | GitLab Dedicated Frankfurt, Argo CD, Kaniko, Trivy, Cosign für Image Signing |
| Infrastructure-as-Code | Terraform (Azure Provider) \+ Crossplane für K8s-Ressourcen; Helm für Apps |
| Policy-as-Code | OPA Gatekeeper im Cluster, Conftest in CI |
| Data Lineage / Governance | DataHub \+ OpenLineage (passend zu deinem Data-Mesh-Ansatz) |
| Analytics-Auskopplung | Apache Iceberg auf Azure Blob, Trino für Query, Soda Core für Data Quality |

# **3\. Abbildung der Spec-Komponenten auf Quarkus-Dienste**

Die in der Spezifikation (Kap. 3.2) definierten logischen Komponenten werden auf konkrete Quarkus-Services abgebildet. Empfohlen ist ein Modular Monolith in Phase 1 mit klarer Modulgrenze, in Phase 2 Ausgliederung schreiblastiger Engines.

| Spec-Komponente | Quarkus-Service | Kern-Extensions |
| :---- | :---- | :---- |
| Metadata Service | mda-metadata-svc | Panache, REST Reactive, Hibernate Validator, JSON-Schema-Validator |
| Data Service (CRUD) | mda-data-svc | Panache, REST Reactive, Smallrye-JWT, Hibernate Envers (Audit) |
| Query/Search | mda-query-svc | REST Reactive, JDBC, Apache Lucene / OpenSearch-Client |
| Rule Engine | mda-rules-svc (Library \+ Service) | Eigene AST-Engine, optional Drools für komplexe Fälle |
| Workflow Engine | mda-workflow-svc | Camunda 7 Embedded (Quarkus-Extension) oder Zeebe-Client |
| Process Engine (BPF) | mda-process-svc | Eigener Zustandsautomat auf Panache, Event-getrieben |
| Security / Policy | mda-policy-svc | Keycloak-Admin-Client, OPA-Client, Caffeine-Cache |
| Audit & Compliance | mda-audit-svc | Kafka Consumer, Panache, Iceberg-Export-Job |
| Event Bus Adapter | Library mda-events-lib | SmallRye Reactive Messaging, Debezium Outbox |
| Notification | mda-notify-svc | Qute Templates, Mailer, Web Push, SmallRye Reactive Messaging |
| Job Scheduler | mda-jobs-svc | JobRunr Pro, Quarkus Scheduler, Kafka Trigger |
| Renderer (Client) | mda-renderer (Angular) | Nicht Quarkus; Angular 18 SPA, deployt via Nginx in K8s |
| Designer (Authoring) | mda-designer (Angular) | Eigenes Frontend, nutzt Metadata-Service-API |
| Solution Manager | mda-solution-svc | REST Reactive, Panache, OCI-Artifact-Client (für Solution-Pakete) |

## **3.1 Schnittgrössen und Deployment-Einheiten**

Phase 1 (Go-Live): deploybare Einheiten sind (a) mda-core-api als modularer Monolith, der Metadata, Data, Query, Rules, Process und Policy vereint, (b) mda-workflow-svc separat (wegen Camunda-Lifecycle), (c) mda-notify-svc, (d) mda-jobs-svc, (e) mda-audit-svc, (f) mda-designer und (g) mda-renderer. Phase 2: Ausgliederung von Data und Query aus dem Monolithen, sobald Lastprofile es rechtfertigen.

Begründung: Ein modularer Monolith reduziert Betriebskomplexität in der risikoreichsten Einführungsphase, erhält aber die logische Trennung für eine spätere Ausgliederung. Das entspricht deiner üblichen Linie: scharfe Modulgrenzen per DDD, physische Trennung erst bei messbarem Bedarf.

# **4\. Kern-Extensions und Bibliotheken**

## **4.1 Web / API**

* quarkus-rest (RESTEasy Reactive) – reaktiver HTTP-Stack, bessere Latenz und Thread-Nutzung als klassisches RESTEasy.

* quarkus-rest-jackson – JSON-Serialisierung.

* quarkus-smallrye-openapi – automatisch generierte OpenAPI 3.1-Definition, essenziell für die modellgetriebene Kern-API.

* quarkus-smallrye-graphql – optional, für Clients, die tiefe, typisierte Queries bevorzugen.

* quarkus-hibernate-validator – deklarative Validierung über Jakarta Bean Validation.

## **4.2 Persistenz**

* quarkus-hibernate-orm-panache – ORM mit repository-freier, typsicherer API; passt gut zu DDD-Aggregaten.

* quarkus-jdbc-postgresql – Treiber.

* quarkus-flyway – schemabasierte, versionierte Datenbankmigration. Alternative Liquibase nur wenn du bestehende Changelogs hast.

* quarkus-hibernate-envers – automatisches historisches Audit auf Entitätsebene, ergänzt das fachliche Audit-Log.

* hibernate-types (Vlad Mihalcea) – JSONB-Mapping für das Metamodell.

## **4.3 Messaging und Events**

* quarkus-messaging-kafka (SmallRye Reactive Messaging) – reaktiver Kafka-Producer/Consumer.

* quarkus-apicurio-registry-avro – Schema-Registry-Integration für Avro-Schemas.

* debezium-quarkus-outbox – Outbox-Pattern mit Debezium-Kompatibilität; ersetzt händisches Outbox-Relay.

## **4.4 Security**

* quarkus-oidc – OIDC-Integration mit Keycloak; Bearer-Token-Validation, User-Info, RBAC.

* quarkus-smallrye-jwt – JWT-Verarbeitung, Rollen-/Claim-Extraktion.

* quarkus-keycloak-authorization – Fine-grained Permissions gegen Keycloak RPT (optional zu OPA).

* quarkus-opa (Community-Extension) oder eigener OPA-Client – ABAC-Policies als Rego-Code.

* quarkus-vault oder Azure-Key-Vault-Extension – Secret-Management.

## **4.5 Observability**

* quarkus-opentelemetry – Traces, Metrics, Logs via OTLP.

* quarkus-micrometer-registry-prometheus – Metriken im Prometheus-Format.

* quarkus-smallrye-health – Liveness/Readiness.

* quarkus-logging-json – strukturierte Logs; erzwingt Korrelations- und Trace-IDs.

## **4.6 Produktivität und Qualität**

* quarkus-arc – CDI-Container, Grundlage für saubere Modulgrenzen.

* quarkus-cache (Caffeine) – Entscheidungs-Cache für Policy- und Metadata-Lookups.

* quarkus-config-yaml – YAML-Konfiguration; für Modell-Overlays pro Umgebung.

* quarkus-test-h2 / quarkus-test-containers – Integrationstests mit realer PostgreSQL via Testcontainers.

* quarkus-jacoco – Testabdeckung im Build.

# **5\. Datenschicht im Detail**

## **5.1 Schemastrategie**

Die Plattform nutzt zwei physische Schema-Ebenen innerhalb derselben PostgreSQL-Instanz:

* platform – statisches Schema der Plattform selbst: Metamodell, Solutions, Audit, Outbox, Jobs, Berechtigungen.

* tenant\_\<id\> oder Row-Level-Security im Schema app – dynamisch erzeugte Tabellen pro Entität.

Empfohlen ist das Pool-Modell mit Row-Level-Security (RLS): Eine Tabelle pro Entität, Spalte tenant\_id, RLS-Policy erzwingt Isolation. Das skaliert betrieblich besser als Schema-pro-Tenant, genügt Swiss Insurance-typischen Tenant-Mengen (wenige bis dutzende logische Mandanten) und ist mit Hibernate Filters gut kombinierbar. Für besonders sensible Tenants (z. B. Rückversicherungs-Cluster) kann auf Silo-Datenbanken umgeschaltet werden.

## **5.2 Dynamische Tabellenerzeugung**

Beim Publizieren einer neuen Entität im Metadata Service wird eine Flyway-Migration generiert, in Git eingecheckt und per GitOps ausgerollt. Alternativ zur reinen Runtime-DDL-Erzeugung hat dieser Pfad drei entscheidende Vorteile: DDL ist versioniert, reviewbar und mit den üblichen Change-Management-Prozessen kompatibel. Laufzeit-DDL bleibt die Ausnahme (z. B. für reine Entwicklerumgebungen).

## **5.3 Mapping Attribut-Typen auf PostgreSQL**

| Spec-Typ | PostgreSQL-Typ | Anmerkungen |
| :---- | :---- | :---- |
| String | text \+ CHECK length | Kein varchar(n); PostgreSQL ist mit text optimal. |
| Integer | bigint |  |
| Decimal | numeric(28, s) | Skala s aus Metamodell. |
| Currency | numeric(28,4) \+ text | Wert \+ ISO-4217-Code separat. |
| Boolean | boolean |  |
| Date | date |  |
| DateTime | timestamptz | Immer UTC speichern. |
| Duration | interval oder bigint |  |
| Enum | text \+ CHECK IN (...) | Kein natives enum, wegen Migrationsschmerz. |
| Lookup | uuid \+ FK | Polymorphic: ziel\_entity\_id zusätzlich. |
| File | uuid \+ Metadaten-Tabelle | Binärdaten im Object Store, nicht in PG. |
| JSON | jsonb | Mit optionalem JSON-Schema-Check. |
| Calculated | GENERATED ALWAYS AS ... STORED | Wenn deterministisch und immutable; sonst im Service. |

## **5.4 Row-Level-Security-Policy**

Beispiel-Policy für jede Entitätstabelle:

ALTER TABLE app.contact ENABLE ROW LEVEL SECURITY;CREATE POLICY contact\_tenant\_isolation ON app.contact  USING (tenant\_id \= current\_setting('app.tenant\_id')::uuid)  WITH CHECK (tenant\_id \= current\_setting('app.tenant\_id')::uuid);CREATE POLICY contact\_owner\_scope ON app.contact  USING (    current\_setting('app.scope') \= 'organization'    OR (current\_setting('app.scope') \= 'businessunit'        AND owning\_bu\_id \= current\_setting('app.bu\_id')::uuid)    OR (current\_setting('app.scope') \= 'user'        AND owner\_id \= current\_setting('app.user\_id')::uuid)  );

Der Data Service setzt die Session-Parameter app.tenant\_id, app.scope, app.user\_id und app.bu\_id pro Request via SET LOCAL in einer Interceptor-Methode. Damit ist die Tenant-/Scope-Isolation auf Datenbankebene erzwungen, zusätzlich zur Applikations-Policy – Defense in Depth.

## **5.5 Audit-Log und Change-Feed**

* Hibernate Envers protokolliert Attribute-Level-Änderungen pro Entität in \_aud-Tabellen.

* Zusätzlich schreibt der Data Service fachliche Audit-Events (wer, wann, was, warum, Korrelations-ID) in eine dedizierte platform.audit\_log-Tabelle (append-only, partitioniert nach Monat).

* Debezium liest Logical Replication von PostgreSQL und veröffentlicht CDC-Events für Analytics und Data Mesh, getrennt vom fachlichen Event-Stream.

## **5.6 Retention und Legal Hold**

Eine nightly Quarkus-Scheduler-Routine konsultiert Retention-Regeln im Metamodell und markiert Datensätze zur Löschung/Anonymisierung. Legal Holds werden als separate Tabelle modelliert und blockieren die Löschung transaktional. Löschprotokolle landen im Audit-Log und werden separat retention-sicher archiviert (WORM-fähiger Storage, z. B. Azure Immutable Blob mit Policy-Lock).

# **6\. API-Schicht**

## **6.1 URL-Konvention (OData-kompatibel)**

Die generische Kern-API folgt einer OData-ähnlichen Konvention, ohne den vollen OData-Stack zu verlangen. Das erleichtert spätere Tool-Kompatibilität (Excel-Konnektor, Power Query), ohne die Komplexität von OData v4 vollständig tragen zu müssen.

GET    /api/v1/entities/{entity}                 \# Liste mit $filter, $select, $expand, $top, $skip, $orderbyGET    /api/v1/entities/{entity}/{id}POST   /api/v1/entities/{entity}PATCH  /api/v1/entities/{entity}/{id}DELETE /api/v1/entities/{entity}/{id}POST   /api/v1/entities/{entity}/$batch          \# BulkPOST   /api/v1/entities/{entity}/{id}/actions/{action}GET    /api/v1/views/{view}                      \# Gespeicherte Ansicht ausführenGET    /api/v1/metadata/{entity}                 \# Metamodell-Fragment für Renderer

## **6.2 Filter-Ausdruckssprache**

Die Ausdruckssprache für $filter wird als typisierter AST (Jakarta JSON \+ ANTLR4-Parser) implementiert. Sie ist mit einer Untermenge von OData $filter und dem FetchXML-Dialekt kompatibel, aber unabhängig davon spezifiziert. Der Parser produziert einen AST, der über einen Visitor sowohl zu JPA Criteria (für Panache-Queries) als auch zu OpenSearch-Queries (für Volltext) kompiliert wird.

## **6.3 Versionierung**

* URL-Pfad-Versionierung /api/v1, /api/v2 – nur bei API-Major-Breaks.

* Modell-Evolution (neue Entitäten/Felder) ist additiv und benötigt keine API-Major-Änderung.

* Deprecation-Header Sunset: \<date\> mindestens 12 Monate im Voraus.

## **6.4 Fehlermodell**

RFC 7807 Problem+JSON mit Erweiterungen für Feld-Fehler und Korrelations-IDs:

{  "type": "https://mda.example.ch/errors/validation",  "title": "Validation failed",  "status": 400,  "code": "MDA-VAL-001",  "correlationId": "01HZ...",  "errors": \[    { "field": "email", "code": "required", "message": "E-Mail ist Pflicht, wenn Status=Aktiv." }  \]}

# **7\. Regel-, Workflow- und Prozess-Engines**

## **7.1 Business-Rule-Engine**

Für Business Rules der Spec (Feld setzen, Pflicht, Sichtbarkeit, Validierung) wird eine eigene, leichtgewichtige deklarative Engine implementiert. Begründung: Die Regeln sind UI-nah, müssen client- und serverseitig identisch interpretiert werden, und ein vollständiger Drools-Einsatz wäre überdimensioniert.

* Regeln liegen als JSON (AST) im Metamodell.

* Serverseitig: Java-Interpreter in mda-rules-lib (pure library, keine externe Engine).

* Clientseitig: TypeScript-Interpreter im Renderer (shared AST-Schema).

* Snapshot-Tests stellen die semantische Äquivalenz beider Implementierungen sicher.

* Für komplexe, deklarative Entscheidungslogik (z. B. Tarifrechner) ist DMN über Camunda vorgesehen – getrennt von Formular-BRs.

## **7.2 Workflow-Engine**

Empfehlung: Camunda 7 Community Edition embedded im Quarkus-Workflow-Service. Grund: Camunda 7 lässt sich als Bibliothek einbinden, teilt die PostgreSQL-Instanz, unterstützt BPMN und DMN, hat ausgezeichnete Java-APIs. Camunda 8 (Zeebe) ist leistungsstärker und reaktiver, bringt aber eigene Infrastruktur (Zeebe-Cluster, Elasticsearch, Operate) mit – Overhead, der in Phase 1 nicht gerechtfertigt ist.

* Trigger-Adapter: Kafka-Consumer konvertiert fachliche Events zu BPMN-Message-Events.

* Externe Tasks werden über externen Worker-Pattern aufgerufen, nicht synchron im Engine-Thread.

* Retries, Exponential Backoff und Dead-Letter-Queues sind Teil des Camunda-Bridge-Adapters.

* Migration-Pfad: Bei Skalierungsbedarf kann auf Camunda 8 Zeebe gewechselt werden; die BPMN-Modelle bleiben weitgehend portabel.

## **7.3 Business-Process-Flow-Engine**

BPFs sind einfacher als BPMN (geführte Zustandsmaschine pro Datensatz). Empfehlung: Eigene Implementierung auf Panache – eine bpf\_instance-Tabelle pro aktive Prozessinstanz, Zustandsübergänge über eine Service-Methode, die deklarative Stage-Definitionen aus dem Metamodell interpretiert. Keine Engine-Abhängigkeit. Das hält die fachliche Intention ('Lead → Qualified → Opportunity → Won') sichtbar und trivial auditierbar.

# **8\. Eventing-Architektur**

## **8.1 Topic-Layout**

Die Topic-Benennung folgt deinem bestehenden Data-Mesh-Schema:

\<tenant\>.\<domain\>.\<entity\>.\<event\>.v\<major\>  Beispiele:    helvetia.partner.contact.created.v1    helvetia.claims.case.status-changed.v1    helvetia.billing.invoice.issued.v1Plattform-interne Topics:  platform.audit.events.v1  platform.notifications.out.v1  platform.workflow.signals.v1

* Partitionierung: nach aggregate\_id (Datensatz-ID), stellt Reihenfolge pro Aggregat sicher.

* Retention: fachliche Events 30 Tage oder länger, je Data-Contract; Audit-Events permanent mit separater Archivierung nach Iceberg.

## **8.2 Event-Schema**

Avro mit Apicurio Schema Registry. Kompatibilitätsmodus BACKWARD TRANSITIVE. Minimalstruktur:

{  "type": "record",  "name": "EntityChangeEvent",  "fields": \[    { "name": "eventId",        "type": "string" },    { "name": "eventType",      "type": "string" },    { "name": "eventTime",      "type": { "type": "long", "logicalType": "timestamp-micros" } },    { "name": "tenantId",       "type": "string" },    { "name": "correlationId",  "type": "string" },    { "name": "causationId",    "type": \["null", "string"\], "default": null },    { "name": "traceparent",    "type": \["null", "string"\], "default": null },    { "name": "entityName",     "type": "string" },    { "name": "entityId",       "type": "string" },    { "name": "version",        "type": "long" },    { "name": "changedFields",  "type": { "type": "array", "items": "string" } },    { "name": "payload",        "type": "bytes" },    { "name": "actor",          "type": "string" }  \]}

## **8.3 Outbox**

Debezium Outbox Event Router auf PostgreSQL: Der Data Service schreibt in dieselbe Transaktion wie die fachliche Änderung einen Eintrag in platform.outbox. Debezium liest via Logical Replication und publiziert ins Kafka-Ziel. Damit ist Exactly-Once in der Publikation garantiert (unter Kafka-Idempotenz), und die Applikation bleibt frei von Messaging-Koordinationslogik.

* Outbox-Zeilen werden nach erfolgreicher Publikation gelöscht (oder soft-deletet).

* Das Outbox-Topic ist nicht das Fach-Topic; ein Router verteilt nach event.type.

## **8.4 Eingehende Integration**

* Webhook-Endpoint: generische POST /api/v1/webhooks/{integrationId} mit HMAC-Signaturprüfung und Idempotency-Key-Header.

* Eingehender Kafka: dedizierte Consumer pro Domain, die Events deklarativ auf Entitäten mappen (Mapping im Metamodell).

# **9\. Security-Architektur**

## **9.1 Authentifizierung**

* Keycloak 24+ als zentraler OIDC-Provider; Realm pro Organisation, Clients pro App-Modul.

* Token-Flow: Authorization Code \+ PKCE für interaktive UIs; Client Credentials für Service Principals.

* Token-Lebensdauer: Access Token 15 Minuten, Refresh Token 8 Stunden (UI) / 30 Tage (Desktop).

* MFA-Pflicht für alle privilegierten Rollen und alle administrativen Zugriffe, konfiguriert in Keycloak Authentication Flows.

* Step-Up-Authentifizierung für sensible Aktionen (Löschen, Massen-Export, Impersonation) über ACR-Claims.

## **9.2 Autorisierung**

Zweistufiges Modell: RBAC grob \+ ABAC fein.

* Grob: Keycloak Rollen entsprechen Spec-Rollen (z. B. CaseAgent, ClaimsManager).

* Fein: OPA-Policies in Rego entscheiden über Privileg × Scope × Datensatz. Der mda-policy-svc fragt OPA pro Entscheidung ab und cached Ergebnisse (Caffeine) mit kurzer TTL.

* Row-Level-Filter werden aus OPA-Entscheidungen in SQL-Prädikate kompiliert und in Panache-Queries injiziert.

* Feld-Sicherheit: zentrale Projektionsmaske im Data Service, angewendet vor JSON-Serialisierung.

## **9.3 Beispiel-Rego-Policy**

package mda.authzdefault allow := falseallow if {  some role in input.user.roles  privilege := data.privileges\[role\]\[input.entity\]\[input.action\]  privilege \!= null  scope\_allows(privilege.scope, input)}scope\_allows("organization", \_)scope\_allows("businessunit", input) if input.record.bu\_id \== input.user.bu\_idscope\_allows("user", input)         if input.record.owner\_id \== input.user.id\# Datenklassifizierung als zusätzliche Sperredeny\[msg\] if {  input.record.classification \== "strictly\_confidential"  not input.user.clearance in {"top\_secret", "confidential\_cleared"}  msg := "Insufficient clearance for strictly confidential record"}

## **9.4 Secrets und Schlüssel**

* HashiCorp Vault (on-prem) oder Azure Key Vault (AKS) – nie in Git, nie in ConfigMaps.

* Quarkus-Vault-Extension injiziert Secrets als @ConfigProperty zur Laufzeit.

* Datenbank-Passwörter rotieren automatisch via Vault Database Secret Engine.

* Customer-Managed Keys für PostgreSQL TDE und Object Store Server-Side Encryption.

* mTLS zwischen internen Services via Istio Ambient Mesh oder Linkerd; Zertifikate kurzlebig (24h).

## **9.5 nDSG- und FINMA-Spezifika**

* Datenresidenz: primäre PostgreSQL und Object Store in Switzerland North; Keycloak und Kafka ebenso.

* Zugriffsprotokolle werden WORM-archiviert (Azure Immutable Blob mit Legal Hold Policy) für mindestens 10 Jahre (VVG/FINMA-Rundschreiben).

* Subprocessor-Liste wird aus dem Modell generiert (alle externen Konnektoren sind Modellartefakte).

* DPIA-Evidence: Datenklassifizierung und Zweckbindung sind im Metamodell pro Feld hinterlegt und exportierbar.

* FINMA-Outsourcing: Cloud-Komponenten sind klar dokumentiert, inkl. Exit-Strategie (Rückmigration on-prem ist technisch vorbereitet, da derselbe Quarkus/Postgres/Kafka-Stack überall läuft).

# **10\. Renderer und Designer (Frontend)**

## **10.1 Technologiewahl**

Angular 18+ mit Standalone Components und Signals. Begründung: stärker typisiertes Ökosystem als React für umfangreiche Formular- und CRUD-Oberflächen, etablierter Dependency-Injection-Mechanismus (passt zu Enterprise-Modularisierung), und du hast bereits Angular-Erfahrung aus PhysioPlanner/Brainy.

* Angular 18 mit Standalone Components, Signals, neue Control Flow Syntax (@if, @for).

* Angular Material \+ CDK für Komponenten-Bibliothek; Angular CDK Drag\&Drop für Designer.

* Nx Monorepo – mda-renderer und mda-designer teilen sich eine Model-AST-Library, Regel-Interpreter und UI-Primitives.

* State Management: @ngrx/signals (einfacher als klassisches NgRx, passt zu Signal-Welt).

* Formulare: Reactive Forms mit dynamisch gebauten FormGroups aus dem Metamodell.

* i18n: Angular i18n \+ ICU Messages, Sprachumschaltung zur Laufzeit via Transloco.

* Accessibility: Angular CDK A11y-Utilities, verpflichtende axe-core-Checks in E2E-Tests.

## **10.2 Designer-Spezifika**

* Monaco Editor für Modell-als-Code (YAML/JSON) mit JSON-Schema-Intellisense.

* Visueller Formular-Designer mit CDK Drag\&Drop; speichert dasselbe Modell-JSON wie der Textmodus (round-trip-fähig).

* Live-Preview gegen eine Sandbox-Tenant-Instanz.

* Diff-Viewer: Monaco Diff Editor auf kanonisch serialisiertem JSON.

## **10.3 Performance**

* SSR bewusst NICHT verwendet – die Anwendung ist auth-gated und profitiert nicht von SEO/Initial-Paint-Optimierung.

* Bundle-Splitting pro App-Modul (Route-basiert).

* Virtual Scrolling (CDK) für Listen \> 200 Zeilen.

* Service Worker für Offline-Shell und Cache der Metamodell-Fragmente.

# **11\. Infrastruktur und Betrieb**

## **11.1 Kubernetes-Topologie**

* Produktion: AKS Switzerland North, 3 Availability Zones, System- und Workload-Nodepools getrennt.

* On-Prem-Fallback: MicroK8s-Cluster mit identischen Helm-Charts (deine Homelab-Linie skaliert hier zum Produkt).

* Workload-Identity: Azure AD Workload Identity für AKS, SPIFFE/SPIRE für on-prem.

* Istio Ambient Mesh für mTLS, Traffic Shifting und Observability-Injection.

## **11.2 Deployment-Pipeline**

1. GitLab CI baut Container-Image mit Kaniko, signiert mit Cosign, scannt mit Trivy.

2. Image wird in Harbor oder Azure Container Registry gepusht.

3. Helm-Chart wird aktualisiert, Tag wird ins GitOps-Repo geschrieben.

4. Argo CD reconciled den Ziel-Cluster, wartet auf Health.

5. Automatisierte Smoke-Tests laufen gegen die neue Version.

6. Canary-Rollout über Argo Rollouts (10% → 50% → 100%) mit Metrik-basiertem Abbruch.

## **11.3 Datenbankbetrieb**

* Azure Database for PostgreSQL Flexible Server (HA, Zone-Redundant) oder CloudNativePG on-prem.

* Logical Replication aktiviert für Debezium.

* Point-in-Time-Recovery 30 Tage, tägliche Backups, monatlicher Wiederherstellungstest.

* PgBouncer als Connection Pooler vor der DB; Quarkus-Pool separat mit angemessenem Sizing.

* Schemavergleich in CI via Schemalint; Diff gegen produktives Schema vor Deploy.

## **11.4 Kafka-Betrieb**

* Option A: Strimzi Operator auf Kubernetes (on-prem und AKS).

* Option B: Azure Event Hubs mit Kafka-Protokoll (managed, einfacher, weniger Features).

* Empfehlung: Strimzi wegen Feature-Parität, Schema-Registry-Integration und Unabhängigkeit von Azure-Lock-in.

* Apicurio Registry in derselben Umgebung, Backup via PostgreSQL.

## **11.5 Observability-Stack**

* OpenTelemetry Collector als DaemonSet und Gateway.

* Metriken: Prometheus \+ Thanos oder Azure Managed Prometheus.

* Logs: Loki mit Grafana (kostengünstiger als Elastic für Log-heavy-Workloads).

* Traces: Tempo.

* Dashboards: Grafana, mit vordefinierten SLO-Boards pro Service.

* Alerting: Alertmanager \+ PagerDuty/Opsgenie.

## **11.6 SLOs (Referenz)**

| SLO | Zielwert | Messgrundlage |
| :---- | :---- | :---- |
| API-Verfügbarkeit mda-core-api | 99.9% / Monat | Successful responses / total, excl. client errors. |
| P95-Latenz GET /entities/{x}/{id} | ≤ 300 ms | OTel-Histogramm, excl. network to client. |
| P95-Latenz POST /entities/{x} | ≤ 500 ms | Inkl. Regeln, Outbox, Audit. |
| Event Publish End-to-End | ≤ 2 s | Commit → sichtbar im Ziel-Topic. |
| Workflow Start Delay (async) | ≤ 5 s | Trigger → erste Task aktiv. |

# **12\. Implementierungs-Skizzen**

## **12.1 Generischer Entity-Controller (Auszug)**

@Path("/api/v1/entities/{entity}")@Authenticated@Produces(MediaType.APPLICATION\_JSON)@Consumes(MediaType.APPLICATION\_JSON)public class GenericEntityResource {    @Inject MetadataService metadata;    @Inject DataService data;    @Inject PolicyService policy;    @Inject SecurityIdentity identity;    @Inject TenantContext tenant;    @GET    public Uni\<PagedResult\<JsonNode\>\> list(            @PathParam("entity") String entityName,            @QueryParam("$filter")  String filter,            @QueryParam("$select")  String select,            @QueryParam("$expand")  String expand,            @QueryParam("$orderby") String orderBy,            @QueryParam("$top")     @DefaultValue("50") int top,            @QueryParam("$skip")    @DefaultValue("0")  int skip) {        EntityDef def \= metadata.resolve(entityName, tenant.id());        policy.require(def, Action.READ, identity);        FilterAst ast          \= FilterParser.parse(filter);        FilterAst scopedFilter \= policy.injectRowFilter(ast, def, identity);        Projection projection  \= policy.maskFields(def, Projection.of(select), identity);        return data.list(def, scopedFilter, projection, expand, orderBy, top, skip);    }    @POST    @WithTransaction    public Uni\<JsonNode\> create(@PathParam("entity") String entityName, JsonNode body) {        EntityDef def \= metadata.resolve(entityName, tenant.id());        policy.require(def, Action.CREATE, identity);        return data.create(def, body, identity);    }}

## **12.2 Outbox-Schreibung in derselben Transaktion**

@ApplicationScopedpublic class DataService {    @Inject EventMapper mapper;    @Inject OutboxRepository outbox;    @WithTransaction    public Uni\<JsonNode\> create(EntityDef def, JsonNode body, SecurityIdentity user) {        return Panache            .withTransaction(() \-\> persist(def, body, user)                .flatMap(saved \-\> {                    OutboxEvent ev \= mapper.toOutbox(def, saved, "created", user);                    return outbox.persist(ev).replaceWith(saved);                }));    }}

## **12.3 RLS-Context-Setzung pro Request**

@Interceptor@Priority(Priorities.USER \- 100)@TenantScopedpublic class TenantContextInterceptor {    @Inject EntityManager em;    @Inject SecurityIdentity identity;    @Inject TenantContext tenant;    @AroundInvoke    public Object around(InvocationContext ctx) throws Exception {        em.createNativeQuery(            "SELECT set\_config('app.tenant\_id', :t, true)," \+            "       set\_config('app.user\_id',  :u, true)," \+            "       set\_config('app.bu\_id',    :b, true)," \+            "       set\_config('app.scope',    :s, true)")          .setParameter("t", tenant.id().toString())          .setParameter("u", identity.getPrincipal().getName())          .setParameter("b", identity.\<String\>getAttribute("bu\_id"))          .setParameter("s", identity.\<String\>getAttribute("scope"))          .getSingleResult();        return ctx.proceed();    }}

# **13\. Bewusste Trade-offs und Risiken**

## **13.1 Modularer Monolith statt feinkörniger Microservices in Phase 1**

Pro: deutlich einfacherer Betrieb, eine Transaktion pro fachlichem Schreibvorgang, weniger Latenz. Kontra: Teamskalierung ist bis zum Refactoring limitiert. Risiko-Mitigation: klare DDD-Modulgrenzen, keine Direkt-Tabellenzugriffe zwischen Modulen, nur Interfaces. Ausstiegspfad: Data und Query zuerst ausgliedern.

## **13.2 Camunda 7 statt Camunda 8**

Pro: embedded, teilt Datenbank, niedrigere Betriebskosten, stabile Java-API. Kontra: End-of-Community-Support angekündigt. Risiko-Mitigation: Workflow-Bridge-Adapter abstrahiert den Engine-Typ; Migration auf Zeebe ist geplant, sobald Last oder Community-Support es verlangen. BPMN-Modelle bleiben portabel.

## **13.3 RLS in PostgreSQL statt Silo-Datenbanken**

Pro: operativer Aufwand gering, Hibernate-kompatibel, Defense-in-Depth zur Applikations-Policy. Kontra: Performance-Overhead bei komplexen Policies, Debuggability leidet. Risiko-Mitigation: Policies bewusst einfach halten, Explain-Analyse pro Query in CI, Silo-DB für besonders sensible Tenants als Option offen lassen.

## **13.4 Eigene Regel-Engine statt Drools**

Pro: UI-Semantik-Äquivalenz zwischen Client und Server, einfacheres Mental Model. Kontra: Eigenentwicklung ist Wartungslast. Risiko-Mitigation: AST-Schema klein und stabil halten, Property-Based-Tests, Drools bleibt Option für Entscheidungslogik jenseits von Formular-BRs.

## **13.5 Angular statt React**

Pro: besseres Typmodell für grosse Formular-/CRUD-Apps, DI-Mechanismus, deine Vorerfahrung. Kontra: kleineres Ökosystem an Datagrid-Produkten. Risiko-Mitigation: AG Grid hat einen Angular-Adapter und ist de-facto-Standard; Angular Material deckt die Komponentenbibliothek ab.

## **13.6 Eigene generische API statt OData-Stack**

Pro: volle Kontrolle über Fehlermodell, Security, Performance. Kontra: Tool-Ökosystem von OData (Excel-Connector, Power Query) nicht sofort verfügbar. Risiko-Mitigation: URL- und Query-Parameter-Konvention sind OData-kompatibel gehalten, sodass ein OData-Gateway nachrüstbar ist.

## **13.7 Quarkus Native nur punktuell**

Pro: JVM-Mode ist produktiv stabil, bessere Dynamik (Hibernate-Reflection, Camunda-Proxies). Kontra: höhere Startup-Zeiten und Memory-Footprint. Native bringt Vorteile nur für reine Edge-Services. Empfehlung: JVM-Mode als Default, Native selektiv für mda-policy-svc und mda-notify-svc evaluieren.

# **14\. Einführungs-Roadmap (Vorschlag)**

| Phase | Zeitrahmen | Meilensteine |
| :---- | :---- | :---- |
| Phase 0 | 0–2 Monate | Spike: Metamodell-Schema, ein Pilot-Entity (z. B. Contact), RLS-Setup, Keycloak-Realm, Pipeline. |
| Phase 1 | 2–6 Monate | Core-API (CRUD, Filter, Paging), Formulare, Business Rules, Site Map, Audit, Outbox, Angular-Renderer MVP. |
| Phase 2 | 6–10 Monate | Workflows (Camunda 7), BPFs, Dashboards, Solution-Lifecycle, Designer-MVP, OPA-Policies, Observability. |
| Phase 3 | 10–14 Monate | Feldsicherheit, Retention, Legal Hold, DataHub/Iceberg-Auskopplung, Multi-Region, Canary-Deployments. |
| Phase 4 | ab 14 Monate | Skalierung: Ausgliederung Data/Query-Service, ggf. Camunda 8, Offline-Modus, OData-Gateway. |

## **14.1 Kritischer Pfad**

* Metamodell-Schema muss in Phase 0 stabil werden; spätere Breaking Changes sind teuer.

* Audit-Log-Format ist rechtlich relevant und rückwirkend schwer änderbar – frühe nDSG/FINMA-Abstimmung.

* Berechtigungsmodell (Rollen × Scope × Feldsicherheit) definiert API-Formfaktor – vor der API-Implementierung finalisieren.

* Event-Schema-Strategie (Kompatibilitätsmodus) muss vor dem ersten produktiven Consumer stehen.

# **15\. Zusammenfassung**

Der vorgeschlagene Stack setzt die MDA-Referenzspezifikation mit etablierten, bei dir bereits eingeführten Bausteinen um: Quarkus als JVM-Backend, PostgreSQL mit Row-Level-Security, Kafka mit Debezium-Outbox, Keycloak plus OPA für Authn/Authz, Angular 18 für Renderer und Designer, alles auf Kubernetes (AKS/MicroK8s) mit GitOps. Die Komponentenwahl optimiert für Swiss-Insurance-Constraints (Datenresidenz, FINMA, nDSG), Portabilität zwischen on-prem und Cloud, Exit-Fähigkeit und konsistente DDD-/EDA-Linien. Bewusste Vereinfachungen (modularer Monolith in Phase 1, Camunda 7 embedded, eigene UI-Regel-Engine) reduzieren Betriebskomplexität ohne strategische Sackgassen.