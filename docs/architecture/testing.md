# Testpyramide — CLM

## Aktueller Zaehlstand (Stand Erstentwurf)

```
$ ./scripts/count-tests.sh
Unit tests:        8
Architecture tests:1
Integration tests: 4 (@QuarkusTest)
BDD service:       1 feature(s)
BDD process:       1 feature(s)
BDD rules:         0 feature(s)
BDD workflow:      0 feature(s)
BDD ui:            1 feature(s)
Pyramide: OK
```

## Regeln (erzwungen durch `scripts/count-tests.sh`)

- `#unit ≥ 2 × #integration`
- `#integration ≥ 2 × (#bdd_service + #bdd_process + #bdd_rules + #bdd_workflow)`
- `#bdd_non_ui ≥ #bdd_ui`

## Runner / Tags

| Runner | Tag | Zweck |
|---|---|---|
| Surefire | (keiner) | Unit + `@QuarkusTest` |
| `ServiceBddIT` | `@service` | REST-Assured gegen Anwendungsservice |
| `ProcessBddIT` | `@process` | BPF-Transition |
| `UiBddIT` | `@ui` | UI-Golden-Path (REST-Modus im CI) |
| `*ApplicationServiceIT` | — | Reine `@QuarkusTest`-Integration auf Service-Layer |

## Implementations-Hinweise

- Cucumber laeuft via `io.quarkiverse.cucumber:quarkus-cucumber` 1.3.0.
- Runner-Klassen erben von `CucumberQuarkusTest`, konfiguriert per `@CucumberOptions(features, glue, tags)`.
- Step-Klassen sind mit `@ScenarioScope` annotiert (State pro Szenario, automatisch von CDI-Object-Factory verwaltet).
- Alle POST-Trigger-Endpunkte nutzen `@Consumes(MediaType.WILDCARD)`, damit Aufrufer ohne Body den Status 415 vermeiden.
