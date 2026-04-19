---
name: mda-implement
description: Setzt ein Feature um, das zuvor mit `mda-plan` geplant und vom Nutzer bestaetigt wurde. Liest `specs/features/<slug>.md` + `plan/<slug>.md`, legt einen Feature-Branch (optional Worktree) an, implementiert delta-minimal gemaess Plan, schreibt Tests, laesst Reviewer-Agents laufen, commitet, startet Fail-Fast-Test-Pipeline und erstellt den Pull Request. Setzt die MDA-Regeln aus `_shared/` durch (hexagonal, DDD, BPF, ArchUnit, Testpyramide, Drift-Guards). Wird ausgeloest bei "mda-implement", "setze Feature um", "implement <slug>", oder nach bestaetigtem `/mda-plan`.
argument-hint: <slug> [--worktree]
tools: Read, Glob, Grep, Bash, Edit, Write, Agent, TaskCreate, TaskUpdate, TaskList, TaskGet
---

# MDA Implement — Feature-Umsetzung

Fuehrt den bestaetigten Plan in Code ueber, ohne das Projekt zu drifteln. Ein Lauf = ein PR = ein Feature.

> Pflichtlektuere:
> - `../_shared/mda-spec.md`, `../_shared/mda-stack.md` — MDA-Regeln.
> - `../_shared/hexagonal-rules.md` — Paket-Layout + ArchUnit.
> - `../_shared/bpf-guide.md` — BPF-Regeln.
> - `../_shared/testing-pyramid.md` — Tag-Konvention und Pyramide.
> - `../_shared/drift-guards.md` — Was **nicht** angefasst werden darf.
> - `../_shared/dod-checklist.md` — Definition of Done.
> - `../_shared/git-detection.md` — Git-Host-/CLI-Detection.

## Vorbedingungen

- `specs/features/<slug>.md` vorhanden.
- `plan/<slug>.md` **und** `/tmp/mda-feature-plan.json` vorhanden (erstellt von `mda-plan`).
- Nutzer hat den Plan bestaetigt.
- Arbeitsbaum sauber (`git status` → leer), sonst abbrechen und Nutzer fragen.

## Eingabe

`$ARGUMENTS` = `<slug> [--worktree]`.

- `<slug>` = Feature-Slug (ohne Datei-Endung); matched `specs/features/<slug>.md`.
- `--worktree` = Isolation in `.trees/feature/<slug>`; sonst normaler Feature-Branch.

## Ablauf

### 1. Git-Host ermitteln

Lies `../_shared/git-detection.md`. Halte Host-Info und CLI im Kontext.

### 2. Plan laden

- `specs/features/<slug>.md` + `plan/<slug>.md` lesen.
- `/tmp/mda-feature-plan.json` laden. Wenn fehlend → `/mda-plan <slug>` zuerst laufen lassen (Hinweis an Nutzer).
- `AskUserQuestion`, wenn Plan offene Fragen (`## Offene Fragen`) enthaelt.

### 3. Branch / Worktree anlegen

```bash
git fetch origin
git checkout main && git pull --ff-only
```

- **Ohne `--worktree`**:
  ```bash
  git checkout -b feature/<slug> origin/main
  ```
- **Mit `--worktree`**:
  ```bash
  git worktree add -b feature/<slug> .trees/feature/<slug> origin/main
  ```
  In den Worktree wechseln.

Bei Bedarf `./mvnw install -DskipTests` oder `npm ci --prefix src/main/webui`.

### 4. Delta-Umsetzung

Agent: `agents/feature-architect.md`. Schreibt streng nach Plan. Reihenfolge **zwingend**:

1. Flyway-Migration (additiv).
2. JPA-Entity-Update (damit `./mvnw compile` noch kompiliert).
3. Domain (Aggregate, VO, Event, BPF-Definition).
4. Application (Port-in/out, Service).
5. Adapter in (REST-Resource, Request-Record, DTO-Mapper).
6. Adapter out (Repository-Impl, Persistence-Mapper).
7. Frontend (`ng generate component/service ...` via Bash, dann Templates + Signals + FormControls).
8. `application.properties`: nur neue Keys anhaengen.

Strenge Regeln:
- **Neue Methoden in bestehenden Klassen** unterhalb `// mda-generator: manual-edits-below`. Marker fehlt → einmalig einfuegen.
- **Cross-Aggregate-Referenzen** nur per ID oder Event.
- **Events**: neue sealed-Subtypen **anfuegen** zu `permits`-Liste (nie entfernen/umbenennen).
- **REST**: neue `@Path`-Methoden. Kein Break an bestehendem Endpunkt ohne Versions-Bump.
- **DTO nur**, wenn UI-Shape abweicht; sonst direkt Record-API.
- **ng generate** via Bash; Komponenten-Dateien danach gezielt editieren.

Nach jedem Schritt: `./mvnw -q compile` (Fail-Fast; bei Fehler sofort Root-Cause fixen, bevor weitergegangen wird).

### 5. Tests

Agent: `agents/feature-test-writer.md`. Schreibt gemaess Plan-Matrix:

| kind | Unit | `@QuarkusTest` | BDD |
|---|---|---|---|
| `new-aggregate` | Aggregate/VO/Enum | REST Happy + 400 + 404 | `@service`: Happy |
| `add-usecase` | Aggregate-Methode | REST Happy + 400 | `@service`: Happy + 1 Fehler |
| `add-bpf-transition` | BPF-Def | — | `@process`: Golden + verbotener Uebergang |
| `add-field` | Aggregate-Invariante | REST mit neuem Feld | optional |
| `new-screen` | — | — | `@ui`: Happy (`rest`-Modus default) |
| `cross-cutting` | Infra-Unit | — | je nach Fall |

- Gherkin **deutsch** (`# language: de`).
- Step-Definitions unter dem richtigen Runner-Paket (`bdd/service/`, `bdd/process/`, `bdd/ui/`).
- `@QuarkusTest` mit RestAssured; Problem+JSON-Assertion auf `code`.
- **Kein** `@Pending` / `@wip` im Merge-Branch.
- Pyramide: `./scripts/count-tests.sh` muss gruen bleiben. Fehlt Unit-Substanz → zusaetzliche VO-/Invarianten-Tests schreiben.

### 6. Reviewer-Runde

Vor Commit parallel je nach Diff — ueber `Agent` mit `subagent_type` (die Reviewer liegen als first-class Sub-Agents unter `.claude/agents/`):

| Diff-Bereich | `subagent_type` |
|---|---|
| `src/main/java/**/domain/**` + `**/application/**` | `hexagonal-reviewer` |
| `src/main/java/**/adapter/**` | `hexagonal-reviewer` |
| `src/main/webui/src/app/**` | `angular-signals-reviewer` |
| `src/test/resources/features/**/*.feature` + Steps | `bdd-cucumber-author` |
| Aenderung an `domain/process/*Lifecycle.java` oder `bpf_*`-SQL | `bpf-reviewer` |

Parallel-Aufrufe: mehrere `Agent`-Calls in **einem** Tool-Batch. Gemeldete Findings **vor Commit** beheben. Kein `--no-verify`.

### 7. Doku-Delta

- `docs/architecture/arc42.md` Kap. 5 (Laufzeitsicht) bei neuen Prozessen erweitern.
- Neuer ADR unter `docs/architecture/adr/` nur bei echtem Entwurfsentscheid.
- `CLAUDE.md` nur innerhalb `<!-- mda-generator:begin/end -->` anpassen.

### 8. Definition of Done

Lies `../_shared/dod-checklist.md`. Jeder anwendbare Punkt erfuellt? Sonst zurueck zu 4–7.

### 9. Commit

- **Gezielt stagen** (keine `git add -A`; Secrets/*.env ausschliessen).
- Message-Format: `<typ>(<scope>): <Beschreibung>`; Typ aus `feat|fix|refactor|test|docs|chore`.
- Ein Commit je semantisch zusammenhaengende Aenderung (meist 1 Commit pro Feature).

### 10. Fail-Fast-Test-Pipeline

Schichtenweise, jede Schicht fail-fast. Ersten Fehler fixen, erneut starten, naechste Schicht erst bei gruener Vorschicht.

1. **Unit**:
   ```bash
   ./mvnw test -Dsurefire.skipAfterFailureCount=1
   ```
2. **Integration + Cucumber**:
   ```bash
   ./mvnw verify -Dfailsafe.skipAfterFailureCount=1
   ```
3. **Smoke**:
   ```bash
   ./mvnw quarkus:dev -Ddebug=false -Dsuspend=false &
   until grep -q "Quinoa is forwarding" target/quarkus-dev.log 2>/dev/null; do sleep 2; done
   curl -sSf --max-time 10 http://localhost:8080/q/health/ready
   curl -sSf --max-time 10 http://localhost:8080/api/v1/<neue-route>
   pkill -f quarkus
   ```
4. **Abschluss** (ohne Fail-Fast):
   ```bash
   ./mvnw clean verify
   ./scripts/count-tests.sh
   ```

Muss fehlerfrei durchlaufen; Pyramide gruen.

### 11. Push & PR

```bash
git push -u origin feature/<slug>
```

PR via Git-CLI (aus `_shared/git-detection.md`):

```bash
gh pr create \
  --title "feat(<bc>): <title> (#<feature-slug>)" \
  --base main \
  --body "$(cat <<EOF
## Summary
<2-3 Saetze>

## Feature-Spec
\`specs/features/<slug>.md\`

## Plan
\`plan/<slug>.md\`

## Test plan
- [x] Unit-Tests gruen
- [x] Integration-Tests gruen
- [x] BDD-Tests gruen
- [x] ArchitectureTest gruen
- [x] scripts/count-tests.sh gruen
- [x] Smoke-Test (quarkus:dev) gruen

🤖 Generated by mda-implement
EOF
)"
```

- Bei `--worktree`: nicht zurueckwechseln; `/mda-ship` raeumt auf.
- Ohne Worktree: auf Feature-Branch bleiben.

## Erweiterte Regeln

### Idempotenz

- Ein zweiter Lauf mit identischem Input darf **keine** zusaetzlichen Diffs erzeugen.
- Re-Run nach Teilfehler: Nicht mehr-Dateien erzeugen, sondern bestehende Aenderungen **updaten**.

### Drift-Guards (Scope-Check vor jedem Edit)

Bevor eine Datei geschrieben wird: gegen `../_shared/drift-guards.md` pruefen. Verstoss → Hard-Fail, Nutzer fragen.

## Fehlerbilder

- **Plan nicht vorhanden** → `/mda-plan <slug>` zuerst.
- **Spec inkonsistent zum Plan** → Hard-Fail, Nutzer muss Plan neu ziehen.
- **ArchUnit-Regel bricht** → Delta rollbacken (Diff anzeigen), Nutzer fragen, wie weiter.
- **`./mvnw verify` rot nach 3 Iterationen** → Zwischenstand auf Branch pushen, Report, NICHT mergen.
- **Drift-Guard triggert** → Edit abbrechen, im PR-Body als "Blocker" markieren.
- **Branch existiert bereits** (feature/<slug>) → Nutzer fragen: fortfuehren oder `<slug>-2` anlegen.

## Referenzen

- `../_shared/*` — MDA-Regeln (Pflicht).
- `agents/feature-architect.md` — Delta-Umsetzung (Skill-interner Prompt).
- `agents/feature-test-writer.md` — Testpyramide (Skill-interner Prompt).
- `.claude/agents/hexagonal-reviewer.md` — first-class Sub-Agent.
- `.claude/agents/angular-signals-reviewer.md` — first-class Sub-Agent.
- `.claude/agents/bdd-cucumber-author.md` — first-class Sub-Agent.
- `.claude/agents/bpf-reviewer.md` — first-class Sub-Agent.
