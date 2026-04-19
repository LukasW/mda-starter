# Testpyramide und Tag-Strategie

## Zielverhältnis

```
          ▲
          │  UI-BDD (@ui)           ← 1–2 Szenarien, Golden Path
       ───┼───
          │  BDD-Service (@service) ← 1 Feature pro Use Case
       ───┼───
          │  Integration (@QuarkusTest)
       ───┼───
          │  Unit (JUnit 5)           ← Fundament
```

Heuristische Mindestverhältnisse nach Anzahl Klassen:
- `#unit ≥ 2 × #integration`
- `#integration ≥ 2 × #bdd-service`
- `#bdd-service ≥ #bdd-ui`

Messung in `docs/architecture/testing.md`, Script `scripts/count-tests.sh`.

## Tag-Konvention (Cucumber)

| Tag | Bedeutung | Runner |
|---|---|---|
| `@service` | Service-BDD via REST-Assured | `ServiceBddIT` |
| `@ui` | UI-BDD via Playwright oder REST-Proxy | `UiBddIT` |
| `@wip` | Work in Progress, vom CI ausgeschlossen | keiner |
| `@slow` | länger als 5 s | optional gefiltert |

## Runner-Trennung

- Surefire (`*Test.java`) → Unit + `@QuarkusTest` Integration.
- Failsafe (`*IT.java`) → Cucumber-Runner + eventuelle lange IT.

## UI-BDD-Strategie

1. **Primär**: Playwright headless. Quarkus Dev Services startet ggf. PostgreSQL. Tests starten `quarkus:dev` NICHT – sie verwenden `@QuarkusIntegrationTest` (packages + run) und greifen auf `http://localhost:${test.url}`.
2. **Fallback**: Wenn Browser nicht verfügbar (z. B. CI ohne Chromium), fallen die `@ui`-Steps auf REST-Assured zurück und zwar so, dass semantisch die gleiche Userstory verifiziert wird. Das Feature bleibt unverändert; nur die Step-Implementierung wechselt anhand einer Systemvariable `MDA_UI_MODE=playwright|rest`.
