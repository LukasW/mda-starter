# ADR 0005 — BDD mit Cucumber + JUnit Platform Suites

- Status: accepted
- Datum: 2026-04-19

## Kontext

Fachliche Szenarien sollen in Gherkin lesbar sein und automatisch ausfuehrbar bleiben. Testpyramide verlangt eindeutige Runner-Trennung.

## Entscheid

Wir nutzen `io.cucumber:cucumber-junit-platform-engine` (Cucumber-BOM 7.34.0) mit JUnit Platform Suite. Drei Runner:

- `ServiceBddIT` — Tag `@service`, Features unter `src/test/resources/features/service/`.
- `ProcessBddIT` — Tag `@process`, Features unter `.../features/process/`.
- `UiBddIT` — Tag `@ui`, Features unter `.../features/ui/`. Default-Modus `rest` (Step-Defs rufen Domain direkt), `playwright` als spaeterer Ausbau.

Steps sind framework-frei und rufen Domain-Code direkt, damit `mvn verify` ohne laufenden HTTP-Server auskommt.

## Konsequenzen

- Keine Bootstrap-Komplexitaet in Cucumber; Quarkus wird nicht ein zweites Mal fuer BDD gestartet.
- UI-BDD im MVP deckt nur die Datenintegritaet ab; echter Browser-Test folgt in V2.
