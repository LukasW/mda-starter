# Testpyramide + Tag-Strategie

## Zielverhaeltnis

```
          ▲
          │  UI-BDD (@ui)             ← 1–2 Szenarien, Golden Path
       ───┼───
          │  BDD (@service / @process / @rules / @workflow)
       ───┼───
          │  Integration (@QuarkusTest)
       ───┼───
          │  Unit (JUnit 5)             ← Fundament
```

Mindestverhaeltnis (per `scripts/count-tests.sh`):

- `#unit ≥ 2 × #integration`
- `#integration ≥ 2 × (#bdd_service + #bdd_process + #bdd_rules + #bdd_workflow)`
- `(#bdd_service + #bdd_process + #bdd_rules + #bdd_workflow) ≥ #bdd_ui`

Dokumentiert in `docs/architecture/testing.md`.

## Runner-Trennung

- **Surefire** (`*Test.java`) — Unit + `@QuarkusTest`-Integration.
- **Failsafe** (`*IT.java`) — Cucumber-Runner + eventuelle lange IT:
  - `ServiceBddIT` (Tag `@service`) — immer aktiv.
  - `ProcessBddIT` (Tag `@process`) — wenn BPF vorhanden.
  - `RulesBddIT` (Tag `@rules`) — wenn Rules-DSL vorhanden.
  - `WorkflowBddIT` (Tag `@workflow`) — wenn BPMN vorhanden.
  - `UiBddIT` (Tag `@ui`) — immer aktiv.

## Tags

| Tag | Bedeutung | Runner |
|---|---|---|
| `@service` | REST-Assured gegen Anwendungsservice | `ServiceBddIT` |
| `@process` | BPF-Transition | `ProcessBddIT` |
| `@rules` | Rule-Snapshot / Evaluation | `RulesBddIT` |
| `@workflow` | Camunda-Workflow | `WorkflowBddIT` |
| `@ui` | UI-Golden-Path (Playwright oder REST-Proxy) | `UiBddIT` |
| `@wip` | Work in Progress, CI-ausgeschlossen | keiner |
| `@slow` | laenger als 5 s | optional gefiltert |

## UI-BDD-Strategie

1. **Primaer**: Playwright headless; Quarkus Dev Services starten ggf. PostgreSQL.
2. **Fallback**: Wenn Browser nicht verfuegbar, fallen `@ui`-Steps auf REST-Assured zurueck. Feature bleibt unveraendert; Step-Implementierung wechselt anhand `MDA_UI_MODE=playwright|rest`. Default in CI: `rest`.

## BDD-Feature-Konvention

- Dateipfad: `src/test/resources/features/<runner>/<aggregate>.feature`.
- Deutsch (`# language: de`), Titel `US-<nr> <Kurztitel>` oder `<Aggregate> <Use-Case>`.
- Gherkin: `Gegeben sei` / `Wenn` / `Dann` / `Und`.
- **Kein `@Pending`** — das Feature muss real laufen.
