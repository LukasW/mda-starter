---
name: mda-plan
description: Plant ein neues Feature fuer ein bestehendes MDA-Projekt (nach `mda-init`). Input ist eine Beschreibung (Freitext oder Markdown) — **kein PlantUML noetig**. Der Skill extrahiert die strukturierte Feature-Spec nach `specs/features/<slug>.md`, erstellt einen Impact-Plan unter `plan/<slug>.md` (Liste der zu erstellenden/aendernden Dateien, Tests, Rollback) und praesentiert ihn dem Nutzer zur Bestaetigung. Setzt die MDA-Regeln aus `_shared/` durch. Wird ausgeloest bei "mda-plan", "plane Feature", "neues Feature …", oder wenn `specs/features/*.md` bzw. `plan/*.md` referenziert wird.
tools: Read, Glob, Grep, Bash, Edit, Write, Agent, TaskCreate, TaskUpdate, TaskList, TaskGet
---

# MDA Plan — Feature-Planung

Erste Phase des Feature-Workflows. Nimmt einen fachlichen Wunsch entgegen (Freitext oder Markdown), destilliert ihn in eine strukturierte Feature-Spec, macht eine Impact-Analyse und legt dem Nutzer den Plan vor. **Implementiert nichts.**

> Pflichtlektuere bevor die Planung startet:
> - `../_shared/mda-spec.md` — Prinzipien, Schichten, Artefakte, API-Konvention.
> - `../_shared/mda-stack.md` — Quarkus-Stack (keine neue Technologie waehlen, die darin nicht steht).
> - `../_shared/hexagonal-rules.md` — Paket-Layout.
> - `../_shared/bpf-guide.md` — BPF-Regeln (falls `kind: add-bpf-transition` oder `new-aggregate` mit Prozess).
> - `../_shared/testing-pyramid.md` — Testverhaeltnis.
> - `../_shared/drift-guards.md` — was nie angefasst werden darf.
> - `../_shared/feature-spec-template.md` — Pflichtfelder.

## Vorbedingungen

- Projekt wurde mit `mda-init` aufgestellt (`pom.xml`, `quarkus-quinoa`, `ArchitectureTest` vorhanden). Sonst: an Nutzer zurueck mit "zuerst `/mda-init` laufen lassen".
- `specs/features/` existiert oder wird angelegt.
- `plan/` existiert oder wird angelegt.

## Ablauf

### 1. Input aufnehmen

- Wenn Argument eine Datei `specs/features/<slug>.md` ist → direkt laden und gegen das Template pruefen.
- Wenn Argument Freitext/Beschreibung ist → `agents/spec-extractor.md` destilliert `specs/features/<slug>.md` aus dem Template.
- Wenn die Beschreibung mehrere orthogonale Aenderungen enthaelt → Nutzer fragen (eine Spec = ein Feature).

Ergebnis: **genau eine** valide `specs/features/<slug>.md` mit Pflicht-Front-Matter.

### 2. Codebase erkunden

Parallel-Abfragen (Grep/Glob):

- Welcher Bounded Context ist betroffen? (Pfad `src/main/java/<root>/<bc>/`)
- Welches Aggregate / welche Ports / Services / Adapter werden angefasst?
- Gibt es aehnliche bestehende Implementierungen (Vorlage)?
- Welche Tests existieren bereits fuer den Bereich?
- Welche naechste Flyway-Nummer? (`ls src/main/resources/db/migration/ | sort | tail -1`)
- Welche Angular-Komponente / Service / Route ist zu aendern? (`src/main/webui/src/app/**`)

### 3. Impact-Analyse

Agent: `agents/impact-analyst.md`. Berechnet:

- Liste **neu** anzulegender Dateien (Domain / Application / Adapter / Frontend). **Frontend-Artefakte (Components/Services) werden ausschliesslich als `ng generate component/service …`-Befehle geplant — niemals als manuell anzulegende `.ts`/`.html`/`.scss`-Dateien.**
- Liste **zu aendernder** Dateien (inkl. ob unterhalb des `// mda-generator: manual-edits-below`-Markers).
- Naechste Flyway-Migration (additiv, nie editieren).
- Tests: Unit + Integration + BDD-Szenarien.
- BPF-Transition (wenn `kind: add-bpf-transition` oder neues Aggregate mit Prozess).
- Drift-Guards: jeder geplante Edit wird gegen `_shared/drift-guards.md` gepruefft; Verstoesse als **offene Frage** im Plan.
- Kein Speculatives: was die Spec nicht nennt, kommt nicht in den Plan.

### 4. Plan-Datei schreiben

Pfad: `plan/<slug>.md`. Pflichtstruktur (Markdown-Checklisten `- [ ]`):

```markdown
# Feature Plan — <slug>

Bezogen auf: `specs/features/<slug>.md`
Bounded Context: <bc>
Kind: <new-aggregate | add-usecase | add-bpf-transition | add-field | new-screen | cross-cutting>

## Uebersicht
<2-4 Saetze: Was wird gebaut, warum>

## Betroffene Schichten
- Domain: ja/nein (Dateien)
- Application: ja/nein (Dateien)
- Adapter in (REST): ja/nein (Dateien)
- Adapter out (Persistence): ja/nein (Dateien)
- Frontend: ja/nein (Komponenten)
- Migration: V<n>__<slug>.sql (ja/nein)

## Zu erstellende Dateien
- [ ] `<pfad>.java` — Zweck (1 Zeile)
- [ ] `ng generate component pages/<name> --skip-tests` — neue Seite (Frontend: nur via `ng generate`, nicht per Hand)
- [ ] `ng generate service core/<name> --skip-tests` — neuer Service

## Zu aendernde Dateien
- [ ] `<pfad>.java` — Art der Aenderung; **Wrap unterhalb Marker?** ja/nein
- [ ] `angular.json` — neuer Lazy-Route-Eintrag (nur wenn Spec `ui[]` das nennt)

## BPF-Delta (nur bei `add-bpf-transition` oder neuem Prozess)
- Von `<FROM_STAGE>` per Trigger `<trigger>` nach `<TO_STAGE>`; Guard: `<ausdruck>`.

## Test-Strategie
- [ ] Unit: `<TestKlasse>` — Szenarien
- [ ] Integration (`@QuarkusTest`): `<TestKlasse>` — Endpunkte
- [ ] BDD `@service`: `<Feature>` — Szenarien
- [ ] BDD `@process`: `<Feature>` (nur wenn BPF-Delta)
- [ ] BDD `@ui`: `<Feature>` (nur wenn neuer Screen)

## Drift-Guard-Check
- [ ] Flyway additiv (neue Nummer, keine editierte Migration)
- [ ] Keine Aggregate-Root-Public-API umbenannt/entfernt
- [ ] Kein `permits`-Subtyp entfernt
- [ ] Keine ArchUnit-Regel aufgeweicht
- [ ] Kein `/api/v1/...`-Pfad ohne Versions-Bump umbenannt
- [ ] Cross-BC-Kopplung nur per ID oder Event

## Rollback-Strategie
<aus Spec uebernommen: Migration + Code + UI>

## Offene Fragen
- <keine / Liste>
```

### 5. Praesentieren und warten

Zeige:
- Spec-Datei-Pfad + Kurzfassung.
- Plan-Datei-Pfad + Kurzfassung (Anzahl neuer Files, geaenderter Files, Tests).
- **Offene Fragen** (falls vorhanden).

> **Implementiere nichts vor Bestaetigung.** Bei Korrekturen: Spec und/oder Plan anpassen, erneut vorlegen. Erst nach „OK" / „ja" / „go": `/mda-implement <slug>`.

## Eskalationspunkte (Plan-Pflicht-Check)

Zeige den Plan **immer** dem Nutzer. Ziehe ausserdem eine **AskUserQuestion**, wenn:

- Plan beruehrt mehr als eine Schicht UND mehr als ein Aggregate (Cross-Cutting-Verdacht).
- Plan verlangt neue Dependencies (Version-Bump oder neues Artifact) → ADR-Bedarf.
- Plan verlangt einen Versions-Bump eines REST-Endpunkts.
- Beschreibung erwaehnt Security/Auth/RLS-Aenderungen.
- Beschreibung erwaehnt neue Event-Typen oder Kafka-Topics.

## Fehlerbilder

- **Projekt ist kein MDA-Projekt** → zurueck an Nutzer: `/mda-init` zuerst.
- **Mehrdeutige Beschreibung** → `AskUserQuestion` mit Empfehlung + Pro/Contra (siehe `brainy`-Pattern): eine Frage pro Turn, nicht bundeln.
- **Feature wuerde Drift-Guard brechen** → Plan enthaelt **Offene Frage**; keine Umsetzung ohne Spec-Erweiterung oder ADR.
- **Aehnliches Feature existiert bereits** → Hinweis + Link zur bestehenden Spec-Datei, Nutzer kann mergen oder abbrechen.

## Referenzen

- `../_shared/feature-spec-template.md` — Pflichtfelder.
- `../_shared/drift-guards.md` — Tabu-Liste.
- `../_shared/mda-spec.md`, `../_shared/mda-stack.md` — MDA-Regeln.
- `references/plan-example.md` — ausfuehrliches Plan-Beispiel.
- `agents/spec-extractor.md`, `agents/impact-analyst.md`.
