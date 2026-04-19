# ADR 0009 — Frontend per Quarkus Quinoa + Angular 21 + Material

**Status:** akzeptiert
**Datum:** 2026-04-19

## Kontext
Das Frontend soll in den Quarkus-Build integriert und als einzelnes Artifact deploybar sein. Fuer Dev-Produktivitaet brauchen wir Hot-Reload ohne manuelles `ng serve`-Handling.

## Entscheidung
- Angular 21 Standalone + Material 3 unter `src/main/webui/`.
- Integration ueber `io.quarkiverse.quinoa:quarkus-quinoa` (2.8.1, kompatibel zu Quarkus >= 3.20).
- Proxy-Loop-Prevention:
  - `quarkus.quinoa.ignored-path-prefixes=/api,/q,/openapi`.
  - `proxyConfig` in `angular.json` nur in separater `standalone`-Konfiguration (kein Default).
- Tests (`@QuarkusTest`) umgehen den Quinoa-Build via `%test.quarkus.quinoa.enabled=false`.

## Konsequenzen
- `./mvnw quarkus:dev` startet Backend (:8080) + `ng serve` (:4200) automatisch.
- `./mvnw clean package` laesst Quinoa `ng build` (production) durchlaufen; das Bundle liegt in `src/main/webui/dist/webui/browser/` und wird ins JAR eingebettet.
- Ein Versionsprung von Quinoa / Angular verlangt einen neuen ADR (aktuell Quarkus 3.34.5 + Quinoa 2.8.1 + Angular 21.2.x).
