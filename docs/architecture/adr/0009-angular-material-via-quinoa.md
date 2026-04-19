# ADR 0009 — Angular 21 + Material via Quarkus Quinoa

- Status: accepted
- Datum: 2026-04-19

## Kontext

Die CLM-Spezifikation sieht ein Angular-Frontend vor. Der MDA-Starter soll Backend und UI aus **einer** Codebasis baubar machen (ein JAR, ein Deployment-Artefakt in Prod), ohne ein separates Repo pflegen zu muessen. Entwickler-Experience: ein `./mvnw quarkus:dev` soll sowohl REST-Service als auch UI mit HMR starten.

## Entscheid

- Frontend-Stack: **Angular 21.2 + Angular Material 21.2** (Azure/Blue-Palette, M3-Theme).
- Gesamte Angular-App liegt unter `src/main/webui/` und wurde per `ng new webui …` gescaffoldet; alle Artefakte via `ng generate`.
- Integration: **Quarkus Quinoa 2.8.1** (Quarkiverse). Quinoa startet Angulars `ng serve` (Port 4200) als Dev-Service in `quarkus:dev`, proxyt unbehandelte HTTP-Requests dorthin, und fuehrt `ng build` im Maven-Package-Lauf aus. Build-Output (`dist/webui/browser/`) wird als statische Resources ins Quarkus-Artifact eingebettet.

Angular-Best-Practices im Starter:

- **Standalone Components** mit `ChangeDetectionStrategy.OnPush`.
- **`inject()`** statt Constructor-Dependency-Injection.
- **Signals** (`signal`, `computed`) fuer lokalen Component-State; Observables an Service-Grenzen.
- **Reactive Forms** mit typisierten `FormBuilder.nonNullable`-Groups; serverseitige `DomainException`-Codes (`MDA-*`) werden ueber `ApiClient` → `ApiError` an die UI transportiert.
- **Lazy Routes** (`loadComponent: () => import(...)`) fuer jede Seite.
- **Material 3 Theme** in `src/styles.scss` via `mat.theme()` Mixin; Komponenten-Styles lokal per SCSS.
- **Dev-Proxy** (`proxy.conf.json`) fuer `/api`, `/q`, `/openapi` auf `http://localhost:8080`, damit auch `ng serve` alleine funktionsfaehig ist.

Services:

- `ApiClient` — kapselt `HttpClient`, normalisiert Server-Fehler auf `ApiError { message, code, status }` (RFC-7807-konform).
- `VertragService`, `FristService`, `FreigabeService` — typed Calls pro BC-REST-Endpoint.

Components:

- `AppShell` (Layout, Toolbar + Sidenav + Router-Outlet).
- `VertragListe`, `VertragErfassen`, `VertragDetail` (inkl. `AblehnenDialog`), `FristListe`.

## Konsequenzen

- Ein `./mvnw clean verify` macht jetzt **auch** `ng build` (ca. +5 s; binnen Budget).
- Produktions-Artefakt ist ein einzelnes JAR mit eingebetteter SPA.
- Zero-Install-Skeleton erwartet System-Node >= 22. Fuer isolierte Builds: `quarkus.quinoa.package-manager-install=true` + `node-version` in Properties setzen.
- Angular SSR bewusst deaktiviert (`--ssr=false` beim Scaffold); CLM-MVP braucht kein serverseitiges Rendering.
- Budget im `angular.json` auf 800 kB / 1.5 MB (initial) angehoben, um Material-Theme + Vendor-Bundles zu tragen.
- OpenAPI-Codegen fuer die TypeScript-Models ist noch nicht verdrahtet; `src/app/core/models.ts` spiegelt die Java-DTOs manuell. V1.1: `openapi-generator-cli` oder `orval` einbinden.
