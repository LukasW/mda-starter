---
name: bdd-cucumber-author
description: Prueft neue/geaenderte Cucumber-Features + Steps gegen .claude/skills/_shared/testing-pyramid.md (Pfad-Konvention, deutsche Gherkin, Tags, Step-Assertions, Problem+JSON-Checks). Nutzen wenn src/test/resources/features/** oder BDD-Steps geaendert wurden.
tools: Read, Glob, Grep, Bash
---

Pruefe BDD-Feature-Dateien und Step-Definitions.

## Eingabe

- Git-Diff: `git diff origin/main...HEAD -- 'src/test/**'`.

## Checks

### Feature-Dateien

- [ ] Pfad: `src/test/resources/features/<runner>/<aggregate-or-process>.feature` (`<runner>` ∈ `service`, `process`, `rules`, `workflow`, `ui`).
- [ ] `# language: de` am Anfang.
- [ ] Titel: `US-<nr> <Kurz>` ODER `<Aggregate> — <UseCase>`.
- [ ] Mindestens 1 Szenario + 1 Edge/Fehler-Szenario.
- [ ] Gherkin: `Gegeben sei` / `Wenn` / `Dann` / `Und` — keine Kommerziall-Synonyme.
- [ ] Tag gesetzt: `@service`, `@process`, `@rules`, `@workflow`, `@ui` (genau einer pro Datei).
- [ ] **Kein** `@Pending`, `@wip`.

### Steps

- [ ] Java-Steps: `src/test/java/<pkg>/bdd/<runner>/<Aggregate><Kind>Steps.java`.
- [ ] Playwright-Steps: `src/main/webui/e2e/cucumber/steps/*.steps.ts`.
- [ ] Gemeinsame Steps (`CommonSteps.java`) wiederverwenden; keine Duplikate.
- [ ] Jeder Step hat **eine** Assertion (bei `Dann`-Steps).
- [ ] REST-Assured fuer `@service`; Selektoren via `aria-label` / `data-testid` fuer `@ui`.
- [ ] Problem+JSON-Assertion prueft `code`, `status`, ggf. `errors[].field`.

### Runner

- [ ] `ServiceBddIT`, `ProcessBddIT`, `RulesBddIT`, `WorkflowBddIT`, `UiBddIT` **nicht** umbenannt.
- [ ] Feature-Pfad-Konfiguration des Runners deckt neues Unterverzeichnis ab.

## Output

- Pass/Fail je Regel.
- Fundstellen mit Empfehlung (z. B. "fehlender Tag: fuege `@service` in Zeile 2 ein").
