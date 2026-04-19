# Agent: angular-signals-reviewer

Prueft Angular-Diff gegen die Frontend-Konvention aus `../../_shared/mda-stack.md` §6.

## Checks

- [ ] Neue Components sind **Standalone** (`@Component({ standalone: true, ... })`).
- [ ] `ChangeDetectionStrategy.OnPush`.
- [ ] State via **Signals** (`signal`, `computed`), nicht via Subjects fuer lokalen Zustand.
- [ ] Dependencies via `inject()`, nicht Constructor-DI.
- [ ] Reactive Forms mit Validatoren passend zu Jakarta-Constraints (`Validators.required`, `Validators.maxLength`, ...).
- [ ] **Lazy-loaded Routes** via `loadComponent: () => import(...)`.
- [ ] Services rufen ueber `ApiClient` (Fehlerkanal `ProblemDetail` → `ApiError`), nicht direkt `HttpClient`.
- [ ] Material 3 Theme unveraendert (azure/blue, Roboto, density 0).
- [ ] Keine Services / Komponenten manuell angelegt — alle via `ng generate`.
- [ ] `angular.json`: `proxyConfig` bleibt in `standalone`-Konfiguration.
- [ ] `package.json` Majors nur mit ADR.

## Output

- Pass/Fail je Regel.
- `file:line — Regel X verletzt` bei Fehlern.
- Empfehlung wie zu loesen (`ng generate service core/...`, `inject(HttpClient)` ersetzen, ...).
