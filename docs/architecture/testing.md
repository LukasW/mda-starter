# Testing — MDA-Starter (CLM MVP V1)

## Zielverhaeltnis

`#unit ≥ 2·#integration ≥ 4·(#bdd-service + #bdd-rules + #bdd-process) ≥ 4·#bdd-ui`

Messung via `./scripts/count-tests.sh` (Exit-Code 1 bei Verletzung, 0 bei OK).

## Runner-Trennung

| Runner | Ausfuehrung | Scope |
|---|---|---|
| Surefire | `*Test.java` | Unit + `@QuarkusTest` Integration |
| Failsafe | `ServiceBddIT`, `ProcessBddIT`, `UiBddIT` | Cucumber-Feature-Suites |

Surefire wird durch `./mvnw test` ausgeloest; Failsafe zusaetzlich durch `./mvnw verify`.

## Tags

| Tag | Bedeutung | Feature-Ordner |
|---|---|---|
| `@service` | Service-Ebene (Application-Service direkt) | `src/test/resources/features/service/` |
| `@process` | BPF-Statemachine (Transition-Matrix) | `.../features/process/` |
| `@ui` | UI-Smoke (Default `rest`-Modus, `-DMDA_UI_MODE=playwright` aktiviert echten Browser) | `.../features/ui/` |
| `@rules` | Rule-Engine (im MVP nicht aktiviert) | reserviert fuer V1.1 |

## Was pro Klasse typischerweise getestet wird

- **Domain-Aggregate**: Invarianten (Leer-Pruefungen, Daten-Konsistenz), alle Zustandstransitionen inkl. verbotener Trigger.
- **BPF-Definition**: Goldener Pfad + mindestens ein verbotener Uebergang (`MDA-BPF-001`).
- **Application-Service**: in V1.1 (zurzeit durch REST-Integration und BDD-Service-Pfad abgedeckt).
- **REST-Resource** (`@QuarkusTest`): Happy Path + Validierungsfehler + Not-Found + Problem+JSON-Shape.

## BDD-Pattern fuer den Starter

Step-Definitionen rufen Domain-Klassen direkt (kein HTTP-Roundtrip, kein Quarkus-Start fuer die Cucumber-IT). Das macht `ServiceBddIT` und `ProcessBddIT` schnell und deterministisch. REST-Roundtrips decken wir ueber `@QuarkusTest`-Integrationstests ab (z. B. `VertragResourceTest`).

## Erweiterung

- UI-BDD mit Playwright: Dependency `com.microsoft.playwright:playwright` ziehen, `VertragUiSteps` erweitern, `-DMDA_UI_MODE=playwright` setzen.
- Rule-BDD: bei Einfuehrung einer Rule-DSL `RulesBddIT`-Runner + Features `@rules` nachziehen.
