---
name: mda-init
description: Erstellt einen ersten Entwurf einer Quarkus-MDA-Applikation (modular monolith, hexagonal, Angular + Material via Quinoa). Eingabe ist entweder eine **Markdown-Beschreibung** (`specs/description.md` oder Freitext im Prompt) ODER deklarative Modelle (PlantUML-Klassen-/Activity-/State-Diagramme, BPMN, Business-Rules-DSL). Der Skill erzeugt DDD-Aggregates, Ports/Adapter, BPF-/Workflow-/Rule-Engines bei Bedarf, BDD-Tests (Cucumber + UI-Cucumber), Flyway-Migrationen, CLAUDE.md und arc42/C4. Wird ausgeloest, wenn der Nutzer "mda-init", "neues Projekt aus Beschreibung", "generiere Applikation aus PlantUML" oder "Erstentwurf" sagt, oder wenn `specs/description.md` bzw. `specs/model/*.puml` referenziert wird.
tools: Read, Glob, Grep, Bash, Edit, Write, Agent, mcp__plugin_context7_context7__resolve-library-id, mcp__plugin_context7_context7__query-docs, TaskCreate, TaskUpdate, TaskList, TaskGet
---

# MDA Init — Erstentwurf eines Quarkus-Projekts

Einmalig beim Projekt-Setup: aus einer **Markdown-Beschreibung** ODER einem Buendel deklarativer Modelle entsteht ein lauffaehiges, hexagonales Quarkus-Projekt mit Angular-SPA. Kein weiteres Projekt-Handbuch ist noetig — die verbindlichen Regeln liegen vollstaendig unter `../_shared/`.

> **Kontrakt.** Erzeugt ausschliesslich Dateien im Ziel-Repository. Templates / Agents / Referenzen des Skill-Ordners werden nur gelesen. `context7` MCP ist Pflicht fuer aktuelle Versionen.

## Normative Basis (Pflichtlektuere bevor irgendetwas geschrieben wird)

- `../_shared/mda-spec.md` — destillierte MDA-Spezifikation (Prinzipien, Schichten, Artefakte, API-Konvention).
- `../_shared/mda-stack.md` — Quarkus-Stack und Pflicht-Extensions.
- `../_shared/hexagonal-rules.md` — Paket-Layout und ArchUnit-Regeln.
- `../_shared/bpf-guide.md` — Business Process Flow Engine.
- `../_shared/testing-pyramid.md` — Testverhaeltnis und Tag-Strategie.

## Eingaben (Triage)

Der Skill akzeptiert **eine beliebige Kombination** der folgenden Quellen. Fehlen alle, fragt er aktiv nach.

| Eingabe | Erkennung | Agent | Output |
|---|---|---|---|
| **Markdown-Beschreibung** | `specs/description.md` oder Freitext im Prompt | `agents/description-parser.md` | `/tmp/mda-domain-model.json` (+ `/tmp/mda-process-model.json`, `/tmp/mda-rule-model.json`) |
| Klassendiagramm (PlantUML) | `.puml`/`.plantuml` + `class`/`enum`/Stereotyp | `plantuml-parser` → `domain-modeler` | Aggregates, VOs, Events |
| Activity-Diagramm (PlantUML) | `.puml` + `start`/`:Aktion;`/`if`/`fork` | `plantuml-parser` → `process-modeler` | BPF-Stages/Transitions |
| State-Diagramm (PlantUML) | `.puml` + `[*] -->`/`state` | `plantuml-parser` → `process-modeler` | BPF-Stages (Statemachine) |
| BPMN 2.0 | `.bpmn` XML-Root `<bpmn:definitions>` | `workflow-modeler` | Camunda-Deployment + Worker-Stubs |
| Business Rules DSL | `.rules.yaml`/`.rules.json` | `rule-engineer` | Rule-AST, Interpreter, Tests |
| DMN (optional) | `.dmn` | `rule-engineer` | DMN-Deployment + Decision-Wrapper |
| Freie Specs | `.md` unter `specs/` (ausser `description.md`) | gelesen als Kontext | Kommentare, ADR-Eintraege |

Alle gefundenen Inputs werden **gemeinsam** zu einem konsolidierten Modell gemerged. Kombinationen sind erlaubt: z. B. `specs/description.md` fuer grobe Intent + `specs/model/*.puml` fuer Detail eines Aggregats.

## Akzeptanzkriterien

Lauf gilt erst dann als erfolgreich, wenn alle Punkte erfuellt sind:

1. **Hexagonale Architektur** je Bounded Context gemaess `_shared/hexagonal-rules.md`. ArchUnit-Test enthalten und gruen.
2. **DDD**: Aggregate Roots, typisierte IDs, Value Objects (records mit Compact-Constructor), Domain Events (sealed), Cross-Aggregate-Referenzen nur per ID.
3. **BPF** (bedingt): eigener Zustandsautomat auf Panache gemaess `_shared/bpf-guide.md`, Stage-Tabelle + Transition-Log, Audit-Eintrag pro Uebergang.
4. **Workflow** (bedingt, nur bei BPMN): Camunda 7 embedded, External-Task-Worker-Stubs, Retry/Backoff/Dead-Letter.
5. **Rule-Engine** (bedingt, nur bei Rules-DSL): deklarativer AST (JSON, client+server kompatibel), server-seitiger Interpreter in `domain/rules/`, Aktionen `setRequired, setVisible, setReadOnly, setValue, showError, showRecommendation`.
6. **BDD** (`@service` Pflicht; `@rules`, `@process`, `@workflow` bedingt; `@ui` Pflicht).
7. **Testpyramide** (`_shared/testing-pyramid.md`): Script `scripts/count-tests.sh` gruen. `docs/architecture/testing.md` dokumentiert.
8. **`./mvnw quarkus:dev`** startet Backend (:8080) + Angular dev-server (:4200) via Quinoa ohne manuelle Eingriffe; Dev-H2 in-memory, Prod-PostgreSQL-Properties.
9. **`./mvnw clean verify`** gruen.
10. **CLAUDE.md** neu erzeugt oder konservativ gemerged (Marker-Bloecke).
11. **arc42 + C4**: `docs/architecture/arc42.md` mit C4-L1/L2/L3 + Prozess-/Workflow-/Rule-Sektion, ADR-Set unter `docs/architecture/adr/`.
12. **Versionen per context7 verifiziert** (Maven/NPM); Quinoa zusaetzlich per `gh api repos/quarkiverse/quarkus-quinoa/releases`.
13. **Angular 21 + Material via Quinoa** unter `src/main/webui/`. Scaffolding via `ng new` + `ng add @angular/material`. `quarkus.quinoa.ignored-path-prefixes=/api,/q,/openapi` gesetzt (Proxy-Loop-Fix siehe `references/angular-quinoa-guide.md`).

## Output-Mapping

Zusaetzliche Artefakte (BPF / Workflow / Rules) werden **nur erzeugt, wenn Input vorliegt** (Zero-Config: was nicht modelliert ist, wird nicht aufgeblasen).

### Domaene / Hexagonale Basis (immer)

| Template im Skill | Ziel im Repo |
|---|---|
| `templates/pom.xml.tmpl` | `pom.xml` (merge-safe) |
| `templates/application.properties.tmpl` | `src/main/resources/application.properties` |
| `templates/flyway-V1-init.sql.tmpl` | `src/main/resources/db/migration/V1__init.sql` |
| `templates/domain-Id.java.tmpl` | `…/domain/{{Aggregate}}Id.java` |
| `templates/domain-ValueObject.java.tmpl` | `…/domain/<VO>.java` |
| `templates/domain-Aggregate.java.tmpl` | `…/domain/{{Aggregate}}.java` |
| `templates/domain-Event.java.tmpl` | `…/domain/event/DomainEvent.java` + konkrete Events |
| `templates/port-in-UseCase.java.tmpl` | `…/application/port/in/*.java` je UseCase |
| `templates/port-out-Repository.java.tmpl` | `…/application/port/out/{{Aggregate}}Repository.java` |
| `templates/application-Service.java.tmpl` | `…/application/service/{{Aggregate}}ApplicationService.java` |
| `templates/adapter-in-rest-Resource.java.tmpl` | `…/adapter/in/rest/{{Aggregate}}Resource.java` + DTO/Mapper |
| `templates/adapter-out-persistence.java.tmpl` | `…/adapter/out/persistence/*.java` |
| `templates/shared-problem.java.tmpl` | `…/shared/problem/*.java` |
| `templates/test-ArchitectureTest.java.tmpl` | `src/test/java/<pkg>/architecture/ArchitectureTest.java` |
| `templates/test-UnitDomain.java.tmpl` | `src/test/java/<pkg>/<bc>/domain/{{Aggregate}}Test.java` |
| `templates/bdd-ServiceRunner.java.tmpl` | `src/test/java/<pkg>/bdd/ServiceBddIT.java` |
| `templates/bdd-UiRunner.java.tmpl` | `src/test/java/<pkg>/bdd/UiBddIT.java` |
| `templates/bdd-ServiceFeature.feature.tmpl` | `src/test/resources/features/service/{{aggregate}}.feature` |
| `templates/bdd-UiFeature.feature.tmpl` | `src/test/resources/features/ui/{{aggregate}}-ui.feature` |
| `templates/bdd-ServiceSteps.java.tmpl` | `src/test/java/<pkg>/bdd/service/{{Aggregate}}ServiceSteps.java` |
| `templates/bdd-UiSteps.java.tmpl` | `src/test/java/<pkg>/bdd/ui/{{Aggregate}}UiSteps.java` |
| `templates/junit-platform.properties.tmpl` | `src/test/resources/junit-platform.properties` |
| `templates/count-tests.sh.tmpl` | `scripts/count-tests.sh` (chmod +x) |
| `templates/docs-testing.md.tmpl` | `docs/architecture/testing.md` |
| `templates/CLAUDE.md.tmpl` | `CLAUDE.md` (merge) |
| `templates/arc42.md.tmpl` | `docs/architecture/arc42.md` |
| `templates/adr.md.tmpl` | `docs/architecture/adr/0001…0008-*.md` |

### BPF (nur bei Activity-/State-Input)

| Template | Ziel |
|---|---|
| `templates/bpf-Stage.java.tmpl` | `…/<bc>/domain/process/{{Process}}Stage.java` + Transitions |
| `templates/bpf-ProcessInstance.java.tmpl` | `…/<bc>/adapter/out/persistence/{{Process}}InstanceJpaEntity.java` |
| `templates/bpf-Service.java.tmpl` | `…/<bc>/application/service/{{Process}}FlowService.java` |
| `templates/bpf-init.sql.tmpl` | `src/main/resources/db/migration/V2__bpf.sql` |
| `templates/bpf-Feature.feature.tmpl` | `src/test/resources/features/process/{{process}}.feature` (`@process`) |
| `templates/bpf-Steps.java.tmpl` | `src/test/java/<pkg>/bdd/process/{{Process}}Steps.java` |
| `templates/bpf-Runner.java.tmpl` | `src/test/java/<pkg>/bdd/ProcessBddIT.java` |

### Workflow (nur bei BPMN)

| Template | Ziel |
|---|---|
| `templates/workflow-Camunda.bpmn.tmpl` | `src/main/resources/bpmn/{{workflow}}.bpmn` |
| `templates/workflow-JobHandler.java.tmpl` | `…/workflow/adapter/in/camunda/{{JobName}}Worker.java` |
| `templates/workflow-Deployment.java.tmpl` | `…/workflow/config/BpmnAutoDeployment.java` |
| `templates/workflow-KafkaTrigger.java.tmpl` | `…/workflow/adapter/in/kafka/{{Topic}}Consumer.java` |
| `templates/workflow-Feature.feature.tmpl` | `src/test/resources/features/workflow/{{workflow}}.feature` (`@workflow`) |

### Business Rules (nur bei `*.rules.yaml`/`*.rules.json`)

| Template | Ziel |
|---|---|
| `templates/rules-SchemaJson.tmpl` | `src/main/resources/rules/schema.json` |
| `templates/rules-Interpreter.java.tmpl` | `…/<bc>/domain/rules/RuleInterpreter.java` |
| `templates/rules-ASTRecords.java.tmpl` | `…/<bc>/domain/rules/{Condition,Action,Rule}.java` |
| `templates/rules-Registry.java.tmpl` | `…/<bc>/application/service/RuleRegistry.java` |
| `templates/rules-EvaluationService.java.tmpl` | `…/<bc>/application/service/{{Aggregate}}RuleEvaluationService.java` |
| `templates/rules-Resource.java.tmpl` | `…/<bc>/adapter/in/rest/RulesResource.java` |
| `templates/rules-Feature.feature.tmpl` | `src/test/resources/features/rules/{{aggregate}}-rules.feature` (`@rules`) |
| `templates/rules-RunnerBdd.java.tmpl` | `src/test/java/<pkg>/bdd/RulesBddIT.java` |
| `templates/rules-SnapshotTest.java.tmpl` | Unit-Snapshot-Tests Client/Server-AST-Aequivalenz |

### Frontend (Angular 21 + Material via Quinoa, immer)

Scaffolding per CLI-Aufrufe (`ng new`, `ng add`, `ng generate`). Der `frontend-architect`-Agent orchestriert.

## Voraussetzungen pruefen

```bash
test -f ../_shared/mda-spec.md && test -f ../_shared/mda-stack.md
ls specs/description.md 2>/dev/null || find specs -name "*.puml" -o -name "*.plantuml" -o -name "*.bpmn" -o -name "*.rules.yaml" 2>/dev/null
test -f pom.xml && test -x mvnw
```

Fehlt **jede** Eingabe, frage den Nutzer:

> "Kein Input gefunden. Moechtest du eine kurze Beschreibung als Markdown geben (ich lege `specs/description.md` an) ODER ein PlantUML-Klassendiagramm (`references/example.puml` als Start)?"

## Pipeline

### Phase 0 — Context7-Warmup (zwingend zuerst)

Aktuelle Versionen + Doku-Snippets via `mcp__plugin_context7_context7__*`:

- `io.quarkus.platform:quarkus-bom` (LTS 3.x)
- `io.quarkus:quarkus-hibernate-orm-panache`
- `io.quarkus:quarkus-rest` + `quarkus-rest-jackson`
- `io.quarkus:quarkus-smallrye-openapi`, `quarkus-smallrye-health`
- `io.quarkus:quarkus-hibernate-validator`
- `io.quarkus:quarkus-flyway`
- `io.quarkus:quarkus-oidc` (wenn Keycloak aktiv)
- `io.cucumber:cucumber-java` + `cucumber-junit-platform-engine`
- `org.junit:junit-bom`, `com.tngtech.archunit:archunit-junit5`
- `com.microsoft.playwright:playwright` (nur bei UI-Modus `playwright`)
- `io.quarkiverse.quinoa:quarkus-quinoa` — zusaetzlich `gh api repos/quarkiverse/quarkus-quinoa/releases`
- `@angular/cli`, `@angular/core`, `@angular/material`, `@angular/animations` (via `npm view`)

Ergebnis als `/tmp/mda-context7-cache.md`. Context7 ist auch **Standardquelle** fuer Implementierungs-Details (proaktiv vor Code-Write).

### Phase 1 — Input-Triage & Modell-Extraktion

Klassifiziere alle Inputs. Fuer jeden Input laeuft der passende Agent **parallel** sofern unabhaengig:

| Agent | Verantwortet |
|---|---|
| `agents/description-parser.md` | Markdown-Freitext → Aggregate-/Process-/Rule-Skizze |
| `agents/plantuml-parser.md` | Klassifikation + Delegation |
| `agents/domain-modeler.md` | Klassendiagramm → Aggregates/VOs/Events |
| `agents/process-modeler.md` | Activity/State-PUML → BPF |
| `agents/workflow-modeler.md` | BPMN → Camunda-Deployment |
| `agents/rule-engineer.md` | Rules-DSL → AST + Bindings |

**Konsistenzpruefung** am Phasenende:
- Prozess-Stages referenzieren existierende Aggregates (sonst ADR + Warnung).
- Business Rules binden an vorhandene Attribute / UseCases.
- Workflow-Trigger verweisen auf definierte Domain Events.
- Bei Konflikt zwischen Description und PlantUML → Nutzer fragen (PlantUML gewinnt normalerweise; aber kein Raten).

### Phase 2 — Hexagonale Architektur

Agent: `agents/hexagonal-architect.md`. Layout aus `_shared/hexagonal-rules.md`. ArchUnit-Test aus `templates/test-ArchitectureTest.java.tmpl`.

### Phase 3 — Persistenz, REST, Engines

- Flyway `V1__init.sql` je Aggregate; `V2__bpf.sql` falls BPF vorhanden.
- Panache-Entity + Mapper + Repository-Impl je Aggregate.
- REST-Resource + DTO + Validation + Problem+JSON.
- **BPF-Engine** (bedingt) gemaess `_shared/bpf-guide.md`.
- **Workflow-Engine** (bedingt): Camunda 7 embedded, BPMN in `src/main/resources/bpmn/`, Deployment-Bean, External-Task-Worker.
- **Rule-Engine** (bedingt): `RuleInterpreter`, `RuleRegistry`, `RulesResource`, `{{Aggregate}}RuleEvaluationService`.

### Phase 4 — Testpyramide & BDD

Agent: `agents/bdd-test-engineer.md`. Regeln aus `_shared/testing-pyramid.md`.

- Unit-Tests je Aggregate/VO/Rule (Snapshot-Tests fuer Client/Server-AST-Aequivalenz).
- `@QuarkusTest` je REST-Resource.
- Cucumber-Runner je Bereich (`ServiceBddIT`, `RulesBddIT`, `ProcessBddIT`, `WorkflowBddIT`, `UiBddIT`).
- `scripts/count-tests.sh` + `docs/architecture/testing.md`.

### Phase 4b — Frontend (Angular 21 + Material via Quinoa)

Agent: `agents/frontend-architect.md`. Details + Proxy-Loop-Fix: `references/angular-quinoa-guide.md`.

- `ng new webui` + `ng add @angular/material`.
- Services + Komponenten via `ng generate` (nicht per Template-Kopie).
- Quinoa-Properties: `quarkus.quinoa.ignored-path-prefixes=/api,/q,/openapi`.
- `angular.json`: `proxyConfig` in separater `standalone`-Konfiguration.
- `pom.xml`: `io.quarkiverse.quinoa:quarkus-quinoa` kompatibel.

### Phase 5 — Dokumentation

Agent: `agents/docs-writer.md`.

- `CLAUDE.md` (merge, nicht ueberschreiben; Marker-Bloecke).
- `docs/architecture/arc42.md` mit C4 + Frontend-Baustein.
- Start-ADRs unter `docs/architecture/adr/` (Nummern 0001-0009, inkl. Quinoa).

### Phase 6 — Validierung

```bash
./mvnw -B clean verify
./scripts/count-tests.sh
./mvnw quarkus:dev -Ddebug=false -Dsuspend=false &
until grep -q "Quinoa is forwarding" target/quarkus-dev.log 2>/dev/null; do sleep 2; done
curl -sSf --max-time 10 http://localhost:8080/q/health/ready
curl -sSf --max-time 10 http://localhost:8080/api/v1/<resource>
curl -sSI --max-time 10 http://localhost:8080/<aggregate>
pkill -f quarkus
```

Max. 3 Iterationen bei Fehler. Danach Zwischenstand sichern + Report.

## Idempotenz & Merge-Strategie

- **CLAUDE.md**: Bloecke zwischen `<!-- mda-generator:begin -->` / `<!-- mda-generator:end -->` werden ersetzt; alles andere unberuehrt.
- **pom.xml**: Dependencies additiv; Duplikate vermeiden.
- **application.properties**: Keys mit gleichem Namen updaten, neue anhaengen.
- **Java-Klassen**: unterhalb `// mda-generator: manual-edits-below` NIE ueberschreiben.

## Fehlerbilder

- **Keine Eingabe** → Nutzer fragen (Beschreibung oder PlantUML).
- **Context7 nicht erreichbar** → letzte `pom.xml`-Version verwenden, ADR `stale-versions`, Warnung.
- **Tests rot nach 3 Iterationen** → Abbruch, Zielzustand nicht als "fertig" melden.
- **Quinoa-Version inkompatibel** (`ClassNotFoundException: HttpBuildTimeConfig`) → Quinoa 2.8+, Kreuzpruefung per `gh api repos/quarkiverse/quarkus-quinoa/releases`.
- **REST-Call haengt** im Dev → `quarkus.quinoa.ignored-path-prefixes` fehlt oder `proxyConfig` in Default-`development`-Konfiguration. Fix: `references/angular-quinoa-guide.md#proxy-loop`.

## Referenzen (lokal im Skill)

- `references/input-formats.md` — Dateimuster, Erkennungsregeln
- `references/plantuml-mapping.md` — PlantUML → DDD
- `references/angular-quinoa-guide.md` — Angular 21 + Material + Quinoa, Kompatibilitaet, Proxy-Fix
- `references/business-rule-dsl.md` — JSON-Schema der Rule-DSL
- `references/workflow-guide.md` — Camunda 7 embedded Patterns
- `references/example.puml`, `references/example-bpf.puml`, `references/example.bpmn`, `references/example.rules.yaml`
- `../_shared/mda-spec.md`, `../_shared/mda-stack.md`, `../_shared/hexagonal-rules.md`, `../_shared/bpf-guide.md`, `../_shared/testing-pyramid.md`
