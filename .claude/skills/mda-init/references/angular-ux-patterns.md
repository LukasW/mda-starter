# Angular UX-Patterns — Pflichtrepertoire fuer MDA-Projekte

Ergaenzt `angular-quinoa-guide.md`. Dieses Dokument beschreibt **generische UX-Bausteine**, die jede MDA-Applikation mitbringen muss — nicht projektspezifische Designs.

Die Regeln hier sind normativ fuer `frontend-architect` (`mda-init`) und `angular-signals-reviewer` (`mda-implement`). Abweichungen verlangen einen ADR.

## 1. BPF-Stage-Visualisierung (`MatStepper`)

Jede `<aggregate>-detail`-Component mit BPF zeigt den Lebenszyklus als **horizontalen, nicht-editierbaren `MatStepper`**:

- Stages ergeben sich aus der `BpfDefinition` (via REST: `GET /api/v1/<entity>/{id}/process/{process}`).
- Aktuelle Stage ist aktiv (`MatStep.selected`).
- Finale Stages (`ABGELAUFEN`, `GEKUENDIGT`, ...) werden als `state="done"` oder `state="error"` (bei `GEKUENDIGT`) markiert.
- BPF-Action-Buttons erscheinen **nur** fuer die aktuelle Stage, geleitet von den Transitionen der Definition.

Minimalvariante ohne Stepper (sehr einfache BPFs mit 2–3 Stages): `MatChipSet` mit aktueller Stage highlighted. Dann im ADR begruenden.

## 2. Mehrstufige Formulare (`MatStepper`)

`<aggregate>-erfassen` wird dann als Stepper implementiert, wenn die Erfassung mehr als **eine** fachlich sinnvolle Stufe hat (z. B. Typ → Metadaten → Parteien → Dokument):

- Stepper: linear, mit Validierung je Stufe (`[stepControl]="stage1Form"`).
- Zusammenfassung (`review`) als letzter Schritt mit `stepLabel="Ueberpruefen"`.
- Abbruch-Navigation nur via Dialog (siehe §3).

## 3. Bestaetigungs-Dialoge (`MatDialog`)

Pflicht fuer jede **zustandsaendernde** BPF-Action, die sich nicht trivial rueckgaengig machen laesst:

- `korrekturbeantragen`, `kuendigen`, `ablaufen` → Dialog mit **Pflicht-Begruendung** (textarea, `Validators.required`, `Validators.minLength(10)`).
- `einreichen`, `freigeben`, `archivieren` → Kurzer Bestaetigungs-Dialog ohne Pflichtfeld.
- Dialog-Result muss im Audit-Trail (Service-Seite) als `actor` + `reason` transportierbar sein.

Trivial rueckgaengig (z. B. Metadaten setzen) braucht keinen Dialog — ein Snackbar-Undo-Pattern reicht.

## 4. Field-Errors aus Problem+JSON in Reactive Forms

`ApiError.fieldErrors[]` (gespiegelt von `ProblemDetail.errors[]`, siehe `shared-problem.java.tmpl`) muss direkt ins Form gemappt werden:

```ts
this.service.erstellen(...).subscribe({
  error: (err: ApiError) => {
    for (const fe of err.fieldErrors) {
      const ctrl = this.form.get(fe.field);
      if (ctrl) ctrl.setErrors({ server: fe.message });
    }
    this.snack.open(err.message, 'OK', { duration: 4000 });
  },
});
```

Im Template: `<mat-error>{{ form.controls.xxx.errors?.['server'] }}</mat-error>`.

## 5. Listen: Filter + Sort + Pagination

Alle `<aggregate>-liste`-Screens nutzen `MatTableDataSource<T>`:

- `MatSort` als `@ViewChild`, `dataSource.sort = this.sort`.
- `MatPaginator` als `@ViewChild`, `dataSource.paginator = this.paginator` (default pageSize 25).
- Filter-Eingabe (`MatFormField` + `MatInput`) → `dataSource.filter = …`.
- BPF-Stage-Filter ueber `MatChipListbox` mit den Enum-Werten (Mehrfachauswahl erlaubt).
- Server-seitige Pagination (`$top`/`$skip` + `$filter`) nur wenn > 1000 Datensaetze erwartet werden; sonst Client-seitig ueber `MatTableDataSource`.

Virtual-Scroll (`cdk-virtual-scroll-viewport`) wird fuer Listen > 10 000 Zeilen eingesetzt.

## 6. Loading-Skeletons

Statt `MatProgressSpinner` in Listen/Detail-Screens: **Skeleton-Platzhalter**, die die finale Struktur spiegeln (gleiche Zeilenzahl, gleiche Spaltenbreite). Nutze eine leichte CSS-Loesung oder `ngx-skeleton-loader`.

Regel: Spinner nur fuer _Actions_ (Submit-Button, Trigger-Dialog). Fuer Daten-Loads → Skeleton.

## 7. Dark-Mode

`mat.theme()` in `styles.scss` unterstuetzt Dark-Mode automatisch, wenn `body { color-scheme: light dark; }` gesetzt ist. Der User-Toggle lebt im `AppShell`:

```ts
readonly dark = signal<boolean>(false);
constructor() { effect(() => document.body.classList.toggle('dark', this.dark())); }
```

SCSS:
```scss
body.dark { color-scheme: dark; }
```

## 8. i18n von Tag 1

`@angular/localize` wird beim Scaffold aktiviert:

```bash
npx ng add @angular/localize
```

- Alle UI-Texte mit `i18n`-Attribut (`<h1 i18n="@@app.title">…</h1>`) oder `$localize`-Tagged-Template in Services.
- Initial-Release liefert nur DE — Build-Konfiguration `de-CH` als Default, `en-US` + `fr-CH` als Slots in `angular.json` vorgesehen.
- Fehlt ein ADR fuer Mehrsprachigkeit, darf Text trotzdem hardcoded bleiben — aber **nur unterhalb eines `// i18n: unlocalized`-Kommentars**, damit `angular-signals-reviewer` den Drift meldet.

## 9. Accessibility-Minimum (WCAG 2.2 AA)

- Jedes `mat-icon-button` hat `aria-label` mit deutscher Aktionsbeschreibung.
- Tastatur-Fokus sichtbar (`:focus-visible { outline: 2px solid var(--mat-sys-primary); }`).
- Landmarks: `<header>` (Toolbar), `<nav>` (Sidenav), `<main>` (Content).
- Snackbars mit `politeness="assertive"` fuer Fehler, `"polite"` fuer Info.
- Dialog-`aria-label` Pflicht; Close-Button hat `cdkFocusInitial`.

## 10. Selektor-Konvention fuer UI-BDD

Test-Selektoren Vorrang: **`aria-label` > `data-testid` > CSS-Klasse**. Jede neue UI-Komponente mit Nutzerinteraktion bekommt mindestens `aria-label` (a11y + Testbarkeit in einem).

## Checkliste Agenten-Pflicht

`frontend-architect` muss im `mda-init`-Lauf:

- [ ] `@angular/localize` installieren.
- [ ] Dark-Mode-Toggle im `AppShell` verdrahten.
- [ ] `<aggregate>-detail` mit `MatStepper`-BPF-Visualisierung **oder** `MatChipSet`-Fallback bauen.
- [ ] `<aggregate>-erfassen` bei >1 fachlichen Stufen als Stepper.
- [ ] `ApiClient.mapError` liefert `ApiError.fieldErrors[]`, Komponenten mappen ins Form.
- [ ] Listen nutzen `MatTableDataSource` + `MatSort` + `MatPaginator`.
- [ ] Skeleton-Loader fuer Daten-Loads; `MatProgressSpinner` nur fuer Actions.
- [ ] Bestaetigungs-Dialog fuer alle destruktiven BPF-Trigger.
- [ ] Alle `mat-icon-button` tragen `aria-label`.
