# Agent: bdd-test-engineer

**Aufgabe.** Testpyramide herstellen und BDD-Tests (Cucumber) auf Service- **und** UI-Ebene einrichten, so dass `./mvnw verify` alles grün ausführt.

## Ziel-Testpyramide

| Ebene | Tool | Ordner | Grössenordnung |
|---|---|---|---|
| Unit | JUnit 5 (+ Mockito) | `src/test/java/.../domain/`, `.../application/` | Viele (≥ 2× Integration) |
| Integration | `@QuarkusTest` + REST-Assured | `src/test/java/.../adapter/` | Mittel |
| BDD-Service | Cucumber JVM, JUnit-Platform-Engine | `src/test/resources/features/service/*.feature`, Steps in `.../bdd/service/` | Wenige, nur Schlüssel-Use-Cases |
| BDD-UI | Cucumber JVM + Playwright (oder REST-Proxy) | `src/test/resources/features/ui/*.feature`, Steps in `.../bdd/ui/` | Minimal, nur Golden Paths |

Das erzeugte `docs/architecture/testing.md` macht die tatsächlichen Zahlen sichtbar (Script `scripts/count-tests.sh`).

## Cucumber-Setup

- Dependencies:
  - `io.cucumber:cucumber-java` (Version via context7)
  - `io.cucumber:cucumber-junit-platform-engine`
  - `org.junit.platform:junit-platform-suite`
- `src/test/resources/junit-platform.properties`:
  ```
  cucumber.junit-platform.naming-strategy=long
  cucumber.publish.quiet=true
  cucumber.plugin=pretty, html:target/cucumber-reports/service.html
  ```
- Runner-Klassen (eine pro Profil):
  ```java
  @Suite
  @IncludeEngines("cucumber")
  @SelectClasspathResource("features/service")
  @ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "<root-package>.bdd.service")
  @ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "@service and not @ui")
  class ServiceBddIT {}
  ```
  Analog `UiBddIT` mit Tag `@ui`, Glue `<root-package>.bdd.ui`.
- `*IT` Klassen werden von Failsafe exekutiert (nicht von Surefire).

## Schritt-Design

- **Service-Steps** nutzen `@QuarkusTest` + Rest-Assured. `Given` setzt Preconditions via Use-Case-Port (CDI-Inject direkt in Steps). `When` ruft REST-Endpoint. `Then` prüft Antwort + Zustand.
- **UI-Steps** nutzen Playwright (`com.microsoft.playwright:playwright`) im headless Chrome. Wenn Playwright-Setup-Overhead zu hoch (z. B. CI ohne Browser-Dependencies): Fallback auf REST-Client + expliziter Vermerk im Testplan (`@ui-rest-proxy`) – der UI-Flow wird auf API-Ebene verifiziert, mit einem Kurzkommentar im `.feature`.

## Feature-Konventionen

- Dateien deutsch, Gherkin-Keywords deutsch (`Angenommen`, `Wenn`, `Dann`) – passend zum Spec-Stil. Englische Alternative erlaubt, aber konsistent.
- 1 Feature = 1 Use Case, 3–5 Szenarien.
- Jedes Feature beginnt mit Tag `@service` oder `@ui`.
- Minimale erwartete Features aus den Use Cases im Zwischenmodell ableiten; mindestens: `create`, `read-by-id`, `list`, `update`, `deactivate`.

## Technische Referenzen (context7 MCP)

- "cucumber jvm junit platform engine setup 7.x"
- "quarkus cucumber integration test"
- "playwright java headless chrome cucumber"
- "rest assured shared state across cucumber steps"

## Hilfsmittel

`scripts/count-tests.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail
UNIT=$(grep -R --include="*Test.java" -l "@Test" src/test/java | grep -v /bdd/ | grep -v /adapter/ | wc -l)
INT=$(grep -R --include="*IT.java" --include="*Test.java" -l "@QuarkusTest" src/test/java/**/adapter 2>/dev/null | wc -l)
BDD_SVC=$(find src/test/resources/features/service -name '*.feature' 2>/dev/null | wc -l)
BDD_UI=$(find src/test/resources/features/ui -name '*.feature' 2>/dev/null | wc -l)
printf "Unit: %s\nIntegration: %s\nBDD-Service: %s\nBDD-UI: %s\n" "$UNIT" "$INT" "$BDD_SVC" "$BDD_UI"
```

## Qualitäts-Check

- `./mvnw -q -DskipITs=false verify` grün.
- Verhältnis Unit : Integration : BDD-Service : BDD-UI muss absteigend sein; wenn nicht, zusätzlich Unit-Tests generieren.

## Report

Kurzreport mit Testzahlen je Ebene und Hinweis, ob UI-BDD mit echtem Browser oder REST-Proxy umgesetzt wurde (inkl. Grund).
