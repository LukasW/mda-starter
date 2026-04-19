---
name: mda-plantuml-generator
description: Generiert aus deklarativen MDA-Modellen (PlantUML-Klassen-/Activity-/State-Diagramme, BPMN-Workflows, Business-Rules-DSL, optional DMN + Markdown-Specs) eine erste lauffähige Quarkus-Applikation gemäss MDA-Spezifikation (MDA-Spezifikation.md + MDA-Quarkus-Stack.md). Port-und-Adapter-Architektur, DDD, BPF-/Workflow-/Rule-Engines, BDD (Cucumber, UI-Cucumber), Testpyramide, mvn quarkus:dev lauffähig, CLAUDE.md + arc42/C4. Wird ausgelöst, wenn der Nutzer eine .puml/.plantuml/.bpmn/.rules.yaml-Datei als Modell-Quelle nennt ODER explizit sagt "generiere Applikation aus PlantUML" / "mda-plantuml".
tools: Read, Glob, Grep, Bash, Edit, Write, Agent, mcp__plugin_context7_context7__resolve-library-id, mcp__plugin_context7_context7__query-docs, TaskCreate, TaskUpdate, TaskList, TaskGet
---

# MDA Model → Quarkus Generator

Erzeugt aus einem Bündel deklarativer Modelle eine erste lauffähige Quarkus-Applikation gemäss `specs/MDA-Spezifikation.md` + `specs/MDA-Quarkus-Stack.md`. Quellen können sein: **Klassendiagramme** (Domänenstruktur), **Aktivitäts-/Statediagramme** (Business Process Flows, Spec 12), **BPMN** (Workflows, Spec 11), **Business-Rules-DSL** (Rule Engine, Spec 10.2 & 7.1 des Stacks), optional **DMN** (Decisions) und freie **Markdown-Specs**.

> **Kontrakt.** Der Skill erzeugt ausschliesslich Dateien im **Ziel-Repository** (ausserhalb von `~/.claude/`). Die Templates, Agents und Referenzen im Skill-Ordner sind Skill-interne Ressourcen und werden beim Lauf **gelesen**, nicht in die Zielapplikation kopiert. `context7` MCP ist zwingende Quelle für aktuelle Versionen und technische Implementierungs-Details.

## Unterstützte Eingaben (Triage)

Der Skill ermittelt anhand von Dateiendung + erster signifikanter Zeile, welcher Agent zuständig ist. Alle gefundenen Inputs werden **gemeinsam** zu einem konsolidierten Zwischenmodell `/tmp/mda-domain-model.json` + `/tmp/mda-process-model.json` + `/tmp/mda-rule-model.json` gemerged.

| Input | Erkennung | Agent | Output-Artefakte |
|---|---|---|---|
| Klassendiagramm (PlantUML) | `.puml`/`.plantuml` + `class`/`enum`/Stereotyp | `plantuml-parser` → `domain-modeler` | Aggregates, VOs, Events |
| Activity-Diagramm (PlantUML) | `.puml` + `start`/`:Aktion;`/`if`/`fork` | `plantuml-parser` → `process-modeler` | BPF-Stages/Transitions |
| State-Diagramm (PlantUML) | `.puml` + `[*] -->`/`state` | `plantuml-parser` → `process-modeler` | BPF-Stages (Statemachine) |
| BPMN 2.0 | `.bpmn`/`.bpmn.xml` XML-Root `<bpmn:definitions>` | `workflow-modeler` | Camunda 7 Deployment + Worker-Stub |
| Business Rules DSL | `.rules.yaml`/`.rules.json` | `rule-engineer` | Rule-AST, Interpreter, Tests |
| DMN (optional) | `.dmn` | `rule-engineer` | DMN-Deployment + Decision-Wrapper |
| Freie Specs | `.md` unter `specs/model/` | gelesen als Kontext aller Agents | Kommentare, ADR-Einträge |

Fehlen alle Inputs → Nutzer nach Pfad fragen; als Fallback `references/example.puml` + `references/example.rules.yaml` + `references/example-bpf.puml` vorschlagen.

## Akzeptanzkriterien

Eine Ausführung gilt erst dann als erfolgreich, wenn alle Punkte erfüllt sind:

1. **Hexagonale Architektur (Port & Adapter)** je Bounded Context: `domain/`, `application/port/{in,out}`, `application/service/`, `adapter/{in/rest,out/persistence}`, `config/`. Keine Framework-Imports in `domain/`/`application/`. Erzwingung: `ArchitectureTest`.
2. **DDD**: Aggregate Roots, typisierte IDs (`XxxId` als `record`), Value Objects (records mit Validierung im Compact Constructor), Domain Events (sealed interface), Domain Services. Cross-Aggregate-Referenzen nur per ID.
3. **BPF (Business Process Flow)** aus Activity-/State-Diagrammen: eigener Zustandsautomat auf Panache (Spec Kap. 12), mit Stage-Tabelle `bpf_instance`, Transitions-API, Audit-Log. Ungültige Übergänge werfen `DomainException`.
4. **Workflow (BPMN)** aus BPMN-Dateien: Camunda 7 embedded Deployment, External-Task-Worker-Stubs, Retry/Backoff/Dead-Letter per Template; Kafka-Trigger-Adapter (Stub, wenn Kafka nicht aktiviert).
5. **Business-Rules-Engine**: deklarativer AST (JSON) server- und clientseitig kompatibel (Spec Stack 7.1). Serverseitiger Interpreter in `domain/rules/`. Einheitliche Aktionen: `setRequired`, `setVisible`, `setReadOnly`, `setValue`, `showError`, `showRecommendation`. Rules sind versioniert (Metamodell) und bei Publikation schemavalidiert.
6. **BDD mit Cucumber** — eigener Runner `ServiceBddIT` (Tag `@service`), Features unter `src/test/resources/features/service/`, Steps in `.../bdd/service/`. Rules & Prozess-Transitions sind **durch eigene Features** abgedeckt (`@rules`, `@process`-Tags zusätzlich).
7. **UI-BDD** — eigener Runner `UiBddIT` (Tag `@ui`), Features unter `src/test/resources/features/ui/`, Steps in `.../bdd/ui/`. Zwei Implementierungszweige via `-DMDA_UI_MODE=playwright|rest` (Default `rest` als CI-sicherer Fallback).
8. **Testpyramide**: `#unit ≥ 2·#integration ≥ 4·(#bdd-service + #bdd-rules + #bdd-process) ≥ 4·#bdd-ui`. Automatisch gemessen und in `docs/architecture/testing.md` dokumentiert.
9. **`./mvnw quarkus:dev` startet die App** ohne manuelle Eingriffe. Dev nutzt H2 in-memory (Prod-Override für PostgreSQL als Property-File bereitgestellt).
10. **`./mvnw clean verify`** läuft grün (Surefire: Unit + `@QuarkusTest`; Failsafe: `ServiceBddIT`, `UiBddIT`).
11. **CLAUDE.md** im Repo-Root neu erzeugt oder konservativ gemerged.
12. **arc42 + C4**: `docs/architecture/arc42.md` enthält C4-L1/L2/L3 **und** eine Prozess-/Workflow-/Rule-Sektion mit Laufzeit-Sequenz je identifiziertem Prozess. ADR-Startset unter `docs/architecture/adr/` (zusätzlich: `0006-bpf-state-machine.md`, `0007-camunda7-embedded.md`, `0008-business-rules-ast.md`).
13. **Versionen per context7 verifiziert**. Keine aus Trainingsdaten geratenen Versionen.

## Output-Mapping (Skill-Input → Dateien im Ziel-Repo)

Zusätzliche Artefakte (BPF / Workflow / Rules) werden **nur erzeugt, wenn das jeweilige Input vorliegt** (Zero-Config-Prinzip: was nicht modelliert ist, wird auch nicht aufgeblasen).

### Domäne / Hexagonale Basis (immer)

| Template im Skill | Ziel im Repo |
|---|---|
| `templates/pom.xml.tmpl` | `pom.xml` (merge-safe: vorhandene Tags werden respektiert, fehlende ergänzt) |
| `templates/application.properties.tmpl` | `src/main/resources/application.properties` |
| `templates/flyway-V1-init.sql.tmpl` | `src/main/resources/db/migration/V1__init.sql` |
| `templates/domain-Id.java.tmpl` | `src/main/java/<pkg>/<bc>/domain/{{Aggregate}}Id.java` |
| `templates/domain-ValueObject.java.tmpl` | `…/domain/<VO>.java` |
| `templates/domain-Aggregate.java.tmpl` | `…/domain/{{Aggregate}}.java` |
| `templates/domain-Event.java.tmpl` | `…/domain/event/DomainEvent.java` + konkrete Event-Records |
| `templates/port-in-UseCase.java.tmpl` | `…/application/port/in/*.java` (ein UseCase je identifiziertem Command/Query) |
| `templates/port-out-Repository.java.tmpl` | `…/application/port/out/{{Aggregate}}Repository.java` |
| `templates/application-Service.java.tmpl` | `…/application/service/{{Aggregate}}ApplicationService.java` |
| `templates/adapter-in-rest-Resource.java.tmpl` | `…/adapter/in/rest/{{Aggregate}}Resource.java` + DTO/Request/Mapper |
| `templates/adapter-out-persistence.java.tmpl` | `…/adapter/out/persistence/*.java` |
| `templates/shared-problem.java.tmpl` | `…/shared/problem/ProblemDetail.java` + `DomainExceptionMapper` |
| `templates/junit-platform.properties.tmpl` | `src/test/resources/junit-platform.properties` |
| `templates/bdd-ServiceRunner.java.tmpl` | `src/test/java/<pkg>/bdd/ServiceBddIT.java` |
| `templates/bdd-UiRunner.java.tmpl` | `src/test/java/<pkg>/bdd/UiBddIT.java` |
| `templates/bdd-ServiceFeature.feature.tmpl` | `src/test/resources/features/service/{{aggregate}}.feature` |
| `templates/bdd-UiFeature.feature.tmpl` | `src/test/resources/features/ui/{{aggregate}}-ui.feature` |
| `templates/bdd-ServiceSteps.java.tmpl` | `src/test/java/<pkg>/bdd/service/{{Aggregate}}ServiceSteps.java` |
| `templates/bdd-UiSteps.java.tmpl` | `src/test/java/<pkg>/bdd/ui/{{Aggregate}}UiSteps.java` |
| `templates/test-UnitDomain.java.tmpl` | `src/test/java/<pkg>/<bc>/domain/{{Aggregate}}Test.java` |
| `templates/test-ArchitectureTest.java.tmpl` | `src/test/java/<pkg>/architecture/ArchitectureTest.java` |
| `templates/docs-testing.md.tmpl` | `docs/architecture/testing.md` |
| `templates/count-tests.sh.tmpl` | `scripts/count-tests.sh` (chmod +x) |
| `templates/CLAUDE.md.tmpl` | `CLAUDE.md` (merge, nicht überschreiben) |
| `templates/arc42.md.tmpl` | `docs/architecture/arc42.md` |
| `templates/adr.md.tmpl` | `docs/architecture/adr/0001…0008-*.md` (inkl. BPF/Camunda/Rules-ADRs, sofern aktiv) |

### BPF (nur bei Activity-/State-Diagramm-Input)

| Template | Ziel |
|---|---|
| `templates/bpf-Stage.java.tmpl` | `…/<bc>/domain/process/{{Process}}Stage.java` + Transitions |
| `templates/bpf-ProcessInstance.java.tmpl` | `…/<bc>/adapter/out/persistence/{{Process}}InstanceJpaEntity.java` + `…Repository` |
| `templates/bpf-Service.java.tmpl` | `…/<bc>/application/service/{{Process}}FlowService.java` (Transition-UseCase) |
| `templates/bpf-init.sql.tmpl` | `src/main/resources/db/migration/V2__bpf.sql` |
| `templates/bpf-Feature.feature.tmpl` | `src/test/resources/features/process/{{process}}.feature` (Tag `@process`) |
| `templates/bpf-Steps.java.tmpl` | `src/test/java/<pkg>/bdd/process/{{Process}}Steps.java` |
| `templates/bpf-Runner.java.tmpl` | `src/test/java/<pkg>/bdd/ProcessBddIT.java` (eigener Runner) |

### Workflow (nur bei BPMN-Input)

| Template | Ziel |
|---|---|
| `templates/workflow-Camunda.bpmn.tmpl` | `src/main/resources/bpmn/{{workflow}}.bpmn` (Rohkopie aus Input, mit Namespacing) |
| `templates/workflow-JobHandler.java.tmpl` | `…/workflow/adapter/in/camunda/{{JobName}}Worker.java` |
| `templates/workflow-Deployment.java.tmpl` | `…/workflow/config/BpmnAutoDeployment.java` |
| `templates/workflow-KafkaTrigger.java.tmpl` | `…/workflow/adapter/in/kafka/{{Topic}}Consumer.java` (optional) |
| `templates/workflow-Feature.feature.tmpl` | `src/test/resources/features/workflow/{{workflow}}.feature` (Tag `@workflow`) |

### Business Rules (nur bei `*.rules.yaml`/`*.rules.json`-Input)

| Template | Ziel |
|---|---|
| `templates/rules-SchemaJson.tmpl` | `src/main/resources/rules/schema.json` (JSON-Schema für Rule-DSL) |
| `templates/rules-Interpreter.java.tmpl` | `…/<bc>/domain/rules/RuleInterpreter.java` (pure Java) |
| `templates/rules-ASTRecords.java.tmpl` | `…/<bc>/domain/rules/{Condition,Action,Rule}.java` |
| `templates/rules-Registry.java.tmpl` | `…/<bc>/application/service/RuleRegistry.java` (lädt YAML/JSON, cached) |
| `templates/rules-EvaluationService.java.tmpl` | `…/<bc>/application/service/{{Aggregate}}RuleEvaluationService.java` |
| `templates/rules-Resource.java.tmpl` | `…/<bc>/adapter/in/rest/RulesResource.java` (publiziert AST an Clients) |
| `templates/rules-Feature.feature.tmpl` | `src/test/resources/features/rules/{{aggregate}}-rules.feature` (Tag `@rules`) |
| `templates/rules-RunnerBdd.java.tmpl` | `src/test/java/<pkg>/bdd/RulesBddIT.java` |
| `templates/rules-SnapshotTest.java.tmpl` | Unit-Snapshot-Tests für Client/Server-AST-Äquivalenz |

## Voraussetzungen prüfen

```bash
test -f specs/MDA-Spezifikation.md && test -f specs/MDA-Quarkus-Stack.md
find specs -name "*.puml" -o -name "*.plantuml" 2>/dev/null
test -f pom.xml && test -x mvnw
```

Fehlt die `*.puml`-Datei, frage den Nutzer nach dem Pfad ODER verwende `references/example.puml` als Start und stelle es dem Nutzer zur Bestätigung.

## Workflow (Pipeline)

Die Pipeline ist sequentiell; einzelne Phasen werden an Agents delegiert (`Agent`-Tool mit `subagent_type: general-purpose` und dem Inhalt der Agent-Datei als System-Prompt). Agents teilen Zwischenstände über `/tmp/mda-*.{json,md}`.

### Phase 0 — Context7-Warmup (zwingend zuerst)

Aktuelle Versionen und Doku-Snippets für folgende Bibliotheken via `mcp__plugin_context7_context7__*`:

- `io.quarkus.platform:quarkus-bom` (LTS 3.x)
- `io.quarkus:quarkus-hibernate-orm-panache`
- `io.quarkus:quarkus-rest` + `quarkus-rest-jackson`
- `io.quarkus:quarkus-smallrye-openapi`, `quarkus-smallrye-health`
- `io.quarkus:quarkus-hibernate-validator`
- `io.quarkus:quarkus-flyway`
- `io.quarkus:quarkus-oidc` (sobald Keycloak aktiviert wird)
- `io.cucumber:cucumber-java` + `cucumber-junit-platform-engine` (BOM-Version)
- `org.junit:junit-bom`
- `com.tngtech.archunit:archunit-junit5`
- `com.microsoft.playwright:playwright` (nur wenn UI-Modus `playwright`)

Ergebnis als `/tmp/mda-context7-cache.md` speichern. Versionen fliessen in `templates/pom.xml.tmpl`-Ausprägung.

Context7 ist auch die **Standardquelle für technische Implementierungs-Details** (z. B. "quarkus panache repository 3.x", "cucumber jvm junit platform engine setup", "rest assured shared state cucumber"). Agents nutzen es proaktiv vor dem Code-Write.

### Phase 1 — Input-Triage & Modell-Extraktion

Alle Inputs aus `specs/model/` werden zunächst klassifiziert (siehe Triage-Tabelle). Für jeden Input läuft der passende Agent **parallel**, sofern unabhängig:

| Agent | Verantwortet |
|---|---|
| `agents/plantuml-parser.md` | Klassifikation der PUML-Diagrammart + Delegation |
| `agents/domain-modeler.md` | Klassendiagramm → `/tmp/mda-domain-model.json` |
| `agents/process-modeler.md` | Activity-/State-PUML → `/tmp/mda-process-model.json` |
| `agents/workflow-modeler.md` | BPMN → `/tmp/mda-workflow-model.json` (+ BPMN-Rohdateien) |
| `agents/rule-engineer.md` | Rules-DSL → `/tmp/mda-rule-model.json` (AST + Bindings) |

Am Phasenende: Konsistenzprüfung der Modelle:
- Prozess-Stages referenzieren existierende Aggregates (sonst ADR-Eintrag + Warnung).
- Business Rules binden an vorhandene Attribute oder Use-Cases.
- Workflow-Trigger verweisen auf definierte Domain Events.

### Phase 2 — Hexagonale Architektur

Agent: `agents/hexagonal-architect.md`.

- Paket-Layout gemäss `references/port-adapter-guide.md`.
- Templates rendern und an Zielpfade schreiben.
- ArchUnit-Test aus `templates/test-ArchitectureTest.java.tmpl`.

### Phase 3 — Persistenz, REST, Engines

- Flyway `V1__init.sql` je Aggregate; `V2__bpf.sql` falls BPF vorhanden.
- Panache-Entity + Mapper + Repository-Impl je Aggregate.
- REST-Resource + DTO + Validation + Problem+JSON.
- **BPF-Engine** (bedingt): Zustandsautomat `…domain/process/{{Process}}Stage.java`, `{{Process}}FlowService` mit Transition-UseCase, persistente `bpf_instance`-Tabelle, Audit-Eintrag je Übergang (Spec Kap. 12).
- **Workflow-Engine** (bedingt): Camunda 7 embedded Extension aktivieren, BPMN-Dateien in `src/main/resources/bpmn/`, Deployment-Bean, External-Task-Worker je `serviceTask externalTask`-Definition. Retry/Backoff via Stub-Annotation konfiguriert.
- **Rule-Engine** (bedingt): `RuleInterpreter` (pure Java, side-effect-frei), `RuleRegistry` lädt YAML beim Boot und cached (Caffeine). `RulesResource` publiziert kompaktes AST unter `/api/v1/metadata/rules/{entity}` — passgenau zum Renderer-Client der Spec (Kap. 10 + Stack 7.1). `{{Aggregate}}RuleEvaluationService` wird in Anwendungsservices **vor** persist-Aufruf aufgerufen, akkumuliert Field-Errors und bricht bei `showError` ab.

### Phase 4 — Testpyramide & BDD

Agent: `agents/bdd-test-engineer.md`.

- Unit-Tests je Aggregate/VO **und je Rule** (Snapshot-Tests gegen erwartete AST-Evaluation — garantiert Client-/Server-Äquivalenz).
- `@QuarkusTest`-Integrationstests je REST-Resource.
- Cucumber-Runner je Bereich:
  - `ServiceBddIT` (Tag `@service`)
  - `RulesBddIT` (Tag `@rules`) — nur wenn Rules-Input vorhanden
  - `ProcessBddIT` (Tag `@process`) — nur wenn BPF-Input vorhanden
  - `WorkflowBddIT` (Tag `@workflow`) — nur wenn BPMN-Input vorhanden
  - `UiBddIT` (Tag `@ui`)
- `scripts/count-tests.sh` zählt alle Test-Arten, `docs/architecture/testing.md` dokumentiert sie.

### Phase 5 — Dokumentation

Agent: `agents/docs-writer.md`.

- `CLAUDE.md` (merge, nicht überschreiben).
- `docs/architecture/arc42.md` mit C4-Blöcken.
- 5 Start-ADRs unter `docs/architecture/adr/`.

### Phase 6 — Validierung

```bash
./mvnw -B -DskipITs=false clean verify
```

Bei Fehler: Root-Cause analysieren, Fix anwenden, erneut laufen. Max. 3 Iterationen. Danach Zwischenstand sichern und Report an Nutzer.

Optional Smoke-Check:

```bash
./mvnw -q quarkus:dev &
sleep 20
curl -sf http://localhost:8080/q/health/ready
kill %1
```

## Agent-Delegation (Übersicht)

| Phase | Agent-Datei | Aufgabe |
|---|---|---|
| 1a | `agents/plantuml-parser.md` | PUML klassifizieren + an Subagent delegieren |
| 1b | `agents/domain-modeler.md` | Klassendiagramm → DDD-Klassen |
| 1c | `agents/process-modeler.md` | Activity-/State-PUML → BPF-Zustandsautomat |
| 1d | `agents/workflow-modeler.md` | BPMN → Camunda-Deployment + Worker-Stubs |
| 1e | `agents/rule-engineer.md` | Rules-DSL → AST, Interpreter, RulesResource |
| 2–3 | `agents/hexagonal-architect.md` | Ports/Adapter/Persistenz/REST + Integration der Engines |
| 4 | `agents/bdd-test-engineer.md` | Testpyramide + Cucumber (`@service` / `@rules` / `@process` / `@workflow` / `@ui`) |
| 5 | `agents/docs-writer.md` | CLAUDE.md + arc42/C4 + ADRs (inkl. BPF/Camunda/Rules) |

## Idempotenz & Merge-Strategie

- **CLAUDE.md**: Wenn vorhanden, parsen; generator-verwaltete Sektionen (mit Kommentar-Marker `<!-- mda-generator:begin -->` / `<!-- mda-generator:end -->`) ersetzen, alles andere unberührt lassen.
- **pom.xml**: Wenn vorhanden, Dependencies zusätzlich zu bestehenden eintragen, Duplikate vermeiden; BOM-Blöcke sind idempotent.
- **application.properties**: Zeilen mit gleichem Schlüssel updaten, neue Schlüssel anhängen.
- **Java-Klassen**: Existieren gleichnamige Klassen mit manuellen Änderungen (Marker `// mda-generator: manual-edits-below`), wird unterhalb des Markers nicht überschrieben. Oberhalb wird generiert.

## Fehlerbilder & Abbruchbedingungen

- **Keine .puml-Datei** → Nutzer fragen.
- **Context7 nicht erreichbar** → Letzte aus `pom.xml` bekannte Version verwenden, ADR-Eintrag `stale-versions`, Warnung reporten.
- **Tests schlagen nach 3 Iterationen fehl** → Generator bricht ab. Zielzustand wird nicht als "fertig" gemeldet.

## Referenzen

- `references/input-formats.md` — Inputs, Dateimuster, Erkennungsregeln
- `references/plantuml-mapping.md` — PlantUML-Elemente → DDD-Konstrukte
- `references/port-adapter-guide.md` — Hexagonal-Regeln für Quarkus
- `references/business-rule-dsl.md` — JSON-Schema der Rule-DSL + erlaubte Ausdrücke
- `references/bpf-guide.md` — BPF-Zustandsautomat (Spec Kap. 12)
- `references/workflow-guide.md` — Camunda 7 embedded Patterns
- `references/testing-pyramid.md` — Zielverhältnis, Tag-Strategie (inkl. `@rules`, `@process`, `@workflow`)
- `references/example.puml` — Klassendiagramm
- `references/example-bpf.puml` — Activity-/State-Diagramm
- `references/example.bpmn` — BPMN-Auszug
- `references/example.rules.yaml` — Business-Rules-DSL-Beispiel
