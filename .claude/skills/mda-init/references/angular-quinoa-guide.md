# Angular + Material via Quarkus Quinoa — Implementation Guide

Bei jedem MDA-Lauf wird zusätzlich zum Backend ein **Angular-21-Frontend mit Angular Material** unter `src/main/webui/` angelegt und via **Quarkus Quinoa** in den Quarkus-Build integriert. Ziel: ein `./mvnw quarkus:dev` startet Backend **und** UI; ein `./mvnw package` baut beides in ein JAR.

## Kompatibilitätsmatrix (per context7 / gh releases verifizieren)

| Quarkus | Quinoa | Angular | Angular Material | Node |
|---|---|---|---|---|
| 3.30+    | **2.8.1+** | 21.x    | 21.x              | ≥ 22 |
| 3.20–3.29 | 2.6.x     | 20.x    | 20.x              | ≥ 20 |
| 3.10–3.19 | 2.5.x     | 19.x    | 19.x              | ≥ 20 |

**Wichtig**: Quinoa-Versionen unter 2.6.x scheitern an Quarkus 3.20+ (ClassNotFound `HttpBuildTimeConfig`). Deshalb bei `quarkus.platform.version >= 3.30` mindestens Quinoa 2.8 verwenden. Version per `gh api repos/quarkiverse/quarkus-quinoa/releases` gegenchecken.

## Scaffold-Schritte (reproduzierbar)

1. Unter `src/main/`:
   ```
   npx -y @angular/cli@{{angular_cli_version}} new webui \
     --style=scss --skip-tests --skip-git --routing \
     --ssr=false --package-manager=npm --defaults --skip-install
   ```
2. In `src/main/webui/`:
   ```
   npm install
   npx ng add @angular/material@{{angular_cli_version}} \
     --theme=azure-blue --typography --animations=enabled --skip-confirmation
   npm install @angular/animations@{{angular_version}} --save
   ```
3. Services und Komponenten via `ng generate` erzeugen (kein manuelles Anlegen von Dateistrukturen):
   - `ng generate service core/api-client --skip-tests`
   - `ng generate service core/<bc> --skip-tests` je Bounded Context
   - `ng generate component layout/app-shell --skip-tests`
   - `ng generate component pages/<aggregate>-liste --skip-tests`
   - `ng generate component pages/<aggregate>-erfassen --skip-tests`
   - `ng generate component pages/<aggregate>-detail --skip-tests`

## Pflicht-Konfiguration in `application.properties`

```properties
quarkus.quinoa.ui-dir=src/main/webui
quarkus.quinoa.build-dir=dist/webui/browser    # Angular @angular/build:application Default
quarkus.quinoa.package-manager=npm
quarkus.quinoa.package-manager-install=false   # verwendet System-Node
quarkus.quinoa.dev-server.port=4200            # Angular dev-server Default
quarkus.quinoa.enable-spa-routing=true
# KRITISCH — Backend-Pfade NIE an ng serve weiterreichen:
quarkus.quinoa.ignored-path-prefixes=/api,/q,/openapi
# @QuarkusTest soll kein ng build triggern:
%test.quarkus.quinoa.enabled=false
```

## Proxy-Loop (bekannter Fallstrick)

**Symptom**: Request auf `http://localhost:8080/api/v1/<resource>` **hängt** (keine Antwort, kein Log). Gleichzeitig erscheint im Log:
```
ERROR [io.qu.qu.QuinoaDevProxyHandler] Quinoa failed to forward request '/api/...'
```

**Ursache**: Das Angular-Scaffold erzeugt bei `ng add @angular/material` **keine** Proxy-Config, aber häufig setzen Teams **`proxyConfig` in `angular.json`** mit `proxy.conf.json`, das `/api/*` an `localhost:8080` weiterleitet. In Kombination mit Quinoa (das unbehandelte Routen an `ng serve` reicht) entsteht eine Schleife:
```
Browser → Quarkus:8080/api/...
Quarkus → ng serve:4200/api/...  (Quinoa forward, wenn Route nicht ignoriert)
ng serve → Quarkus:8080/api/... (proxy.conf.json)
→ ∞
```

**Fix (beides gleichzeitig)**:
1. In `application.properties`:
   `quarkus.quinoa.ignored-path-prefixes=/api,/q,/openapi` setzen.
2. In `angular.json` **kein** `proxyConfig` in der Default-`development`-Konfiguration des `serve`-Targets. Den Proxy in eine separate `standalone`-Konfiguration verschieben, die nur genutzt wird, wenn `ng serve` ohne Quarkus läuft:
   ```json
   "serve": {
     "builder": "@angular/build:dev-server",
     "configurations": {
       "development": { "buildTarget": "webui:build:development" },
       "standalone":  { "buildTarget": "webui:build:development",
                        "proxyConfig": "proxy.conf.json" }
     },
     "defaultConfiguration": "development"
   }
   ```

Aufruf im Quinoa-Modus: `ng serve` (default). Standalone-Modus: `ng serve --configuration standalone`.

## Angular Best-Practice-Konventionen

- **Standalone Components** mit `ChangeDetectionStrategy.OnPush`, `imports:[...]`.
- **`inject()`** statt Constructor-DI.
- **Signals** (`signal`, `computed`) für lokalen State; Observables nur an Service-Grenzen.
- **Reactive Forms** via `fb.nonNullable.group({...})`, Validators aus `@angular/forms`.
- **Lazy Routes** via `loadComponent: () => import('...').then(m => m.Foo)`.
- **`provideHttpClient(withFetch())`**, **`provideAnimationsAsync()`** in `app.config.ts`.
- **Material 3 Theme** via `@use '@angular/material'; html { @include mat.theme(...) }` in `src/styles.scss`.
- REST-Fehler serverseitig als `application/problem+json` (`ProblemDetail`) → im Client zu typisiertem `ApiError { message, code, status }` normalisieren.

## Standard-Komponentenset je BC

Pro Aggregate mit Listen-Use-Case + Erfassen-Use-Case:

- `<aggregate>-liste` — `MatTable` + `MatTableDataSource<T>` + `MatSort` + `MatPaginator` (pageSize 25), Filter via `MatFormField`, Stage-Filter via `MatChipListbox`, Skeleton-Loader beim Daten-Load.
- `<aggregate>-erfassen` — Reactive Form mit `MatFormField`, `MatSelect`, `MatDatepicker`, SnackBar bei Success/Error. Bei > 1 fachlichen Stufen als linearer `MatStepper` mit Pro-Stufe-Validierung.
- `<aggregate>-detail` — Metadaten, Kindobjekte (z. B. Versionen), **`MatStepper` (nicht-editierbar) zur BPF-Lifecycle-Anzeige**, BPF-Transition-Buttons (nur für aktuelle Stage), `MatDialog` für Bestätigungs-/Begründungs-Dialoge (Pflicht-Textarea bei destruktiven Triggern).

Plus übergeordnet:

- `layout/app-shell` — `MatToolbar` + `MatSidenav` + `MatNavList`, Sidenav-Signal, **Dark-Mode-Toggle** (Signal + `color-scheme: light dark`).
- `core/api-client` — zentrale `HttpClient`-Abstraktion mit einheitlichem Fehler-Mapping; `ApiError.fieldErrors[]` wird im Aufrufer per `ctrl.setErrors({ server: ... })` ins Reactive Form abgebildet.
- `core/models` — TypeScript-Typen, die die Java-DTOs spiegeln (bis OpenAPI-Codegen verdrahtet ist).

Normative Details zu diesen Patterns (Pflicht, nicht optional): siehe **`angular-ux-patterns.md`** (BPF-Stepper, Formular-Stepper, Field-Error-Mapping, Filter/Sort/Pagination, Skeletons, Dark-Mode, i18n, WCAG 2.2 AA).

## Budgets

Material-Themes sprengen den Standard-Initial-Budget (500 kB). In `angular.json` erhöhen:
```json
"budgets": [
  { "type": "initial",           "maximumWarning": "800kB", "maximumError": "1.5MB" },
  { "type": "anyComponentStyle", "maximumWarning": "8kB",   "maximumError": "16kB" }
]
```

## Was passiert im Build

| Phase | Aktion |
|---|---|
| `mvn compile` | nichts Frontend-spezifisches |
| `mvn package` | Quinoa ruft `npm run build` → `ng build` (production) → `dist/webui/browser/` wird ins JAR eingebettet |
| `mvn quarkus:dev` | Quinoa startet `ng serve --host=0.0.0.0 --hmr` auf :4200, proxyt unbehandelte Requests dorthin, REST-Pfade (`ignored-path-prefixes`) bleiben bei Quarkus |
| `@QuarkusTest` | `%test.quarkus.quinoa.enabled=false` → Surefire rennt ohne Frontend-Build |

## Rauch-Test (manuell)

```
./mvnw quarkus:dev
# warte bis "Quinoa is forwarding unhandled requests to port: 4200"
curl -sS http://localhost:8080/api/v1/<resource>      # REST — muss sofort antworten
curl -sS http://localhost:8080/<aggregate>            # SPA — muss index.html liefern
```

Hängt der REST-Call → `ignored-path-prefixes` prüfen und `proxyConfig` aus `angular.json` entfernen.
