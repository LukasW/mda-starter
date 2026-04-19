# Definition of Done

Pflicht-Check vor jedem Commit / PR.

## Struktur

- [ ] Feature-Spec unter `specs/features/<slug>.md` vorhanden, Pflichtfelder gefuellt.
- [ ] Keine direkten Edits in `src/main/java/**/domain/` oder `**/application/` ausserhalb von `shared/`, `config/` oder unterhalb `// mda-generator: manual-edits-below`.
- [ ] Frontend-Artefakte ueber `ng generate` erzeugt (keine per Hand angelegten Components/Services).

## Tests

- [ ] Unit-Tests fuer neue/geaenderte Services, Aggregates, VOs, Regeln vorhanden und gruen.
- [ ] Integration-Tests (`@QuarkusTest`) fuer neue/geaenderte REST-Endpunkte (Happy + Validation-Error + 404).
- [ ] BDD-Feature je Aenderungsart:
  - [ ] `@service` fuer neue Use-Cases.
  - [ ] `@process` fuer neue BPF-Transitions.
  - [ ] `@rules` fuer neue Business Rules.
  - [ ] `@ui` fuer neue Screens (Default-Modus `rest`).
- [ ] `./scripts/count-tests.sh` gruen (Testpyramide).
- [ ] Keine `@Pending` oder `@wip`-Tags im Merge-Branch.

## Build

- [ ] `./mvnw clean verify` gruen (Surefire + Failsafe + ArchUnit).
- [ ] `./mvnw quarkus:dev` startet ohne manuellen Eingriff; `GET /q/health/ready` → `UP`.

## Architektur & Regeln

- [ ] `ArchitectureTest` gruen (hexagonal + BC-Grenzen).
- [ ] Kein Cross-BC-Call ausser per ID oder Event.
- [ ] Keine Flyway-Migration editiert (nur addiert).
- [ ] Drift-Guards (`_shared/drift-guards.md`) eingehalten.

## Dokumentation

- [ ] `docs/architecture/arc42.md` Kap. 5 (Laufzeitsicht) erweitert, falls neuer Prozess/Workflow.
- [ ] Neuer ADR unter `docs/architecture/adr/` nur bei echtem Entwurfsentscheid.
- [ ] `CLAUDE.md` nur innerhalb der `<!-- mda-generator:begin/end -->`-Bloecke angepasst.

## Qualitaet

- [ ] Keine toten Code-Pfade, keine auskommentierten Stellen.
- [ ] Keine `TODO` ohne Issue-Referenz.
- [ ] `git diff --stat` plausibel (typisches Feature: 5–20 Dateien neu, 2–5 geaendert).
