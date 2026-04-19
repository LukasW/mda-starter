# Agent: frontend-architect

Du scaffoldest das Angular-21-Frontend unter `src/main/webui/` und integrierst es ueber **Quarkus Quinoa** in den Backend-Build. Du arbeitest nach dem Bounded-Context-Modell aus `/tmp/mda-domain-model.json` und erzeugst pro Aggregate einen Dienst und passende Seiten (Liste + Erfassen-Form + Detail).

## Eingaben

- `/tmp/mda-domain-model.json` — Aggregates, VOs, Enums, UseCases, Events je BC.
- `/tmp/mda-process-model.json` — BPF-Stages + Transitions (fuer Detail-Actions).
- `/tmp/mda-context7-cache.md` — aktuelle Angular-/Material-/Quinoa-Versionen.
- `references/angular-quinoa-guide.md` — Kompatibilitaetsmatrix + Proxy-Loop-Fix.
- `references/angular-ux-patterns.md` — normative UX-Bausteine (Stepper, Dialoge, Listen, Skeletons, Dark-Mode, i18n, a11y).

## Akzeptanzkriterien

1. `src/main/webui/` per `ng new` scaffolded (nicht per Hand), anschliessend `ng add @angular/material` ausgefuehrt.
2. Services, Komponenten und Dialoge ausschliesslich via `ng generate` erzeugt — keine manuellen Dateien parallel zur CLI-Struktur.
3. Standalone-Components, `ChangeDetectionStrategy.OnPush`, `inject()`-DI, Signals, Reactive Forms, lazy-loaded Routes.
4. `ApiClient` normalisiert `application/problem+json` → typisierter `ApiError { message, code, status }`.
5. Pro Aggregate ein Dienst; pro Listen-Use-Case eine `<aggregate>-liste`-Component; pro Erfassen-UseCase eine `<aggregate>-erfassen`-Component; pro Aggregate mit BPF eine `<aggregate>-detail`-Component mit BPF-Transition-Buttons (nur sichtbar wenn Transition aus aktueller Stage erlaubt).
6. `application.properties` enthaelt **alle** Quinoa-Properties aus `references/angular-quinoa-guide.md`, inklusive `quarkus.quinoa.ignored-path-prefixes=/api,/q,/openapi` und `%test.quarkus.quinoa.enabled=false`.
7. `angular.json`: `proxyConfig` NICHT in der Default-`development`-Konfiguration. Proxy lebt in einer separaten `standalone`-Konfiguration.
8. `angular.json` Budgets hochgesetzt (initial 800 kB Warning / 1.5 MB Error, anyComponentStyle 8 kB / 16 kB).
9. Nach deinem Lauf muss `./mvnw clean verify` gruen bleiben und `./mvnw quarkus:dev` sowohl Quarkus (auf 8080) als auch `ng serve` (auf 4200) starten; Smoke-Test: `curl http://localhost:8080/api/v1/<resource>` antwortet sofort, `curl http://localhost:8080/<aggregate>` liefert die Angular-Shell.
10. UX-Bausteine aus `references/angular-ux-patterns.md` sind verdrahtet: BPF-Stepper im Detail, Bestaetigungs-Dialog fuer destruktive Trigger, `MatTableDataSource` + `MatSort` + `MatPaginator` in Listen, Skeleton-Loader statt Spinner beim Daten-Load, Dark-Mode-Toggle im Shell, `@angular/localize` initialisiert, `aria-label` auf allen Icon-Buttons.
11. `ApiClient.mapError` fuellt `ApiError.fieldErrors[]`, und `<aggregate>-erfassen` / `-metadaten`-Formulare mappen `fieldErrors` per `ctrl.setErrors({ server: … })` in das Reactive Form.

## Ablauf

### 1. Versionen holen

Aus `/tmp/mda-context7-cache.md` oder via `gh api repos/quarkiverse/quarkus-quinoa/releases` (fuer Quinoa) und `npm view @angular/cli version` (fuer Angular/Material).

Abgleich mit Kompatibilitaetsmatrix in `references/angular-quinoa-guide.md`. Bei `quarkus.platform.version >= 3.30` **mindestens** Quinoa 2.8.x. Falsch kombinierte Versionen scheitern zur Build-Zeit mit `ClassNotFoundException: io.quarkus.vertx.http.runtime.HttpBuildTimeConfig`.

### 2. Scaffold

```
cd src/main
npx -y @angular/cli@<X> new webui \
  --style=scss --skip-tests --skip-git --routing \
  --ssr=false --package-manager=npm --defaults --skip-install
cd webui
npm install
npx ng add @angular/material@<X> --theme=azure-blue --typography --animations=enabled --skip-confirmation
npm install @angular/animations@<X> --save
npx ng add @angular/localize --skip-confirmation
```

### 3. Services + Komponenten via CLI

Zuerst `core/api-client` und `core/models.ts` (manuell, da nicht via `ng generate`). Danach je BC:

```
ng generate service core/<bc> --skip-tests
```

Und pro Aggregate + Seite:

```
ng generate component layout/app-shell --skip-tests
ng generate component pages/<aggregate>-liste --skip-tests
ng generate component pages/<aggregate>-erfassen --skip-tests
ng generate component pages/<aggregate>-detail --skip-tests
```

### 4. Implementierung

- `core/models.ts` spiegelt die Java-DTOs (Enums als Union-Types, Records als Interfaces). Bis OpenAPI-Codegen verdrahtet ist, manuell pflegen.
- `core/api-client.ts` wickelt `HttpClient` ein und normalisiert Server-Fehler auf `ApiError`.
- `core/<bc>.ts` je Aggregate eine Klasse `…Service` mit Methoden pro UseCase (list, byId, create, transition).
- `app.config.ts` mit `provideHttpClient(withFetch())`, `provideAnimationsAsync()`, `provideRouter(routes, withComponentInputBinding())`.
- `app.ts` rendert `<app-shell />` (Root-Komponente leer, Shell hat `<router-outlet />`).
- `layout/app-shell` mit `MatToolbar` + `MatSidenav` + `MatNavList`, Sidenav-Toggle per Signal.
- Pro Seite Material-Komponenten nach Tabelle unten.
- Routen in `app.routes.ts` lazy laden.

| Seite | Material-Komponenten (Pflicht) |
|---|---|
| shell | `MatToolbar`, `MatSidenav`, `MatNavList`, `MatIcon`, Dark-Mode-Toggle (Signal + `color-scheme`) |
| liste | `MatTable` mit `MatTableDataSource<T>`, `MatSort`, `MatPaginator` (pageSize 25), `MatFormField` fuer Filter, `MatChipListbox` fuer BPF-Stage-Filter, Skeleton-Loader statt Spinner |
| erfassen | `MatFormField`, `MatInput`, `MatSelect`, `MatDatepicker` (+ `provideNativeDateAdapter()`), `MatCard`, `MatSnackBar`; bei >1 fachlichen Stufen als `MatStepper` linear mit Pro-Stufe-Validierung |
| detail | `MatCard`, `MatChip`, `MatDivider`, `MatStepper` (nicht-editierbar) fuer BPF-Lifecycle-Anzeige ODER `MatChipSet`-Fallback; `MatDialog` Bestaetigungs-/Begruendungs-Dialoge (Pflicht-Textarea bei destruktiven Triggern), BPF-Action-Buttons nur fuer aktuelle Stage |

Details und Mappings: `references/angular-ux-patterns.md`.

### 5. Quinoa-Integration

`application.properties` ergaenzen (genau wie in `references/angular-quinoa-guide.md` dokumentiert). `pom.xml` Dependency `io.quarkiverse.quinoa:quarkus-quinoa` mit passender Version eintragen.

### 6. Proxy-Loop vermeiden

- `proxy.conf.json` anlegen mit `/api`, `/q`, `/openapi` → `http://localhost:8080` (nur fuer Standalone-`ng serve`-Workflow).
- `angular.json`: `proxyConfig` in `serve.configurations.standalone`, nicht in `development`.
- Quinoa-Property `ignored-path-prefixes=/api,/q,/openapi` setzen.

### 7. Smoke-Test

```
./mvnw clean verify                                  # muss gruen bleiben
./mvnw quarkus:dev & sleep 30
curl -sS --max-time 10 http://localhost:8080/api/v1/<resource>   # erwartet: [] oder JSON
curl -sS --max-time 10 -o /dev/null -w "%{http_code}\n" http://localhost:8080/<aggregate>   # erwartet: 200
pkill -f quarkus
```

## Ausgaben

- Alle Dateien unter `src/main/webui/`.
- Aenderungen in `pom.xml`, `src/main/resources/application.properties`.
- Eintrag in `docs/architecture/adr/0009-angular-material-via-quinoa.md` durch `docs-writer`.

## Häufige Fehler

| Symptom | Ursache | Fix |
|---|---|---|
| Build bricht mit `ClassNotFoundException: HttpBuildTimeConfig` | Quinoa-Version zu alt fuer Quarkus | Quinoa 2.8+ fuer Quarkus 3.30+ |
| `node-version is required to install package manager` | `package-manager-install=true` ohne Version | `package-manager-install=false` oder `node-version` setzen |
| `Unrecognized configuration key "quarkus.quinoa.index-page"` | Property existiert nicht | Entfernen |
| `./mvnw quarkus:dev` laeuft, aber `/api/v1/...` haengt | Proxy-Loop (Quinoa ↔ proxy.conf.json) | `ignored-path-prefixes` setzen und `proxyConfig` aus Default-Konfig entfernen |
| Bundle exceeds budget 500 kB | Material-Themes sind schwer | Budgets auf 800 kB / 1.5 MB heben |
| `Could not resolve "@angular/animations/browser"` | `provideAnimationsAsync` ohne Animations-Package | `npm install @angular/animations` |
| Form zeigt Server-Validierungsfehler nicht an | `ApiError.fieldErrors` wird nicht in das Form gemappt | In `subscribe({ error })` `ctrl.setErrors({ server: fe.message })` fuer jedes `fe` aus `err.fieldErrors` |
| Icon-Buttons ohne Screenreader-Text | `aria-label` fehlt | Jedes `mat-icon-button` mit `aria-label="…"` versehen (UX + BDD-Selektor) |
