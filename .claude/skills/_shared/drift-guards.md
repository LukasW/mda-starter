# Drift-Guards — Was `mda-plan` und `mda-implement` NICHT anfassen duerfen

Die Skills duerfen nur Dateien aendern, die durch die aktuelle `specs/features/<slug>.md` gedeckt sind. Alles darunter ist Hard-Fail — bei Unsicherheit **fragen**, nicht raten.

## Harte Stopper (Abbruch sofort)

- Aggregate-Root-**Public-API** umbenennen oder entfernen (bricht Consumer).
- Flyway-Migration editieren oder umnummerieren.
- `sealed`-Subtyp aus `permits`-Liste entfernen.
- ArchUnit-Regel aufweichen.
- Cucumber-Runner oder Test-Tag loeschen.
- `/api/v1/...`-Endpunkt-Pfad umbenennen ohne Versions-Bump (`/api/v2/...`).
- `// mda-generator: manual-edits-below` loeschen oder darunter ueberschreiben.

## Regel-Guards je Bereich

### pom.xml / Migration / Architektur

- `pom.xml`: nur neue Dependencies hinzufuegen; bestehende Versionen nicht aendern. Versionssprung = ADR.
- Flyway: nur **neue** `V<n>__<slug>.sql`. Bestehende sind unveraenderlich.
- `ArchitectureTest`: darf erweitert werden (neuer BC-Namespace); nie aufgeweicht.
- `application.properties`: neue Keys anhaengen; bestehende nur mit Spec-Begruendung aendern.

### Domain

- **Aggregate Root**: neue Methode / Feld oberhalb `// mda-generator: manual-edits-below` **nur**, wenn `affects_aggregate` in der Spec passt. Sonst Hard-Fail.
- **Value Objects / Enums**: Erweiterung verlangt `kind: add-field` (oder aehnlich) **und** alle Konsumenten (Switches, sealed-Match) zeitgleich aktualisiert. Konsumenten-Liste per Grep vor dem Edit.
- **Domain Events**: `permits`-Liste erweitern OK; Umbenennung untersagt (Kafka/Outbox-Consumers).
- **BPF-Definition**: nur **neue** Transitions oder Stages; bestehende Stage-Semantik bleibt stabil.

### Application

- Ports: neue Interfaces / Records frei.
- ApplicationService: neue `@Transactional`-Methode frei. Bestehende Methoden duerfen zusaetzliche Seiteneffekte bekommen, wenn sie idempotent bleiben; sonst neuen Use-Case anlegen.
- Bestehende Records: Pflichtfelder nur mit dokumentiertem Migrationsplan aendern.

### Adapter

- REST-Resource: neue `@Path`-Methoden im selben BC OK. Signatur-Break an existierendem Endpunkt → Versions-Bump.
- Persistence-Adapter: neue Entity-Felder frei (+ Flyway). Bestehende Felder umbenennen = Hard-Fail.

### Frontend

- Components: neue Seiten / Services via `ng generate`. Bestehende Components **nur**, wenn Spec `ui[]` das Target nennt.
- Routen: immer lazy; bestehende Routen nur umlenken, wenn Spec das begruendet.
- `angular.json`: Budget-Erhoehungen nur mit Notiz in Spec; `proxyConfig` bleibt in der `standalone`-Konfiguration.
- `package.json`: neue Deps per `npm install <pkg>@<ver>`; Majors mit ADR.

### Tests

- Cucumber-Runner (`ServiceBddIT`, `ProcessBddIT`, `UiBddIT` ...): nicht umbenennen.
- Feature-Dateien: neue hinzufuegen; bestehende nur erweitern, nicht umschreiben.
- `scripts/count-tests.sh`: Verhaeltnis gruen halten; Hard-Fail, wenn rot.

### Dokumentation

- ADR-Nummern strikt monoton steigend.
- `arc42.md` Kap. 5 (Laufzeitsicht) bei neuen Prozessen erweitern, nicht ersetzen.
- `CLAUDE.md` nur generator-verwaltete Bloecke `<!-- mda-generator:begin -->` / `<!-- mda-generator:end -->` anpassen.

## Scope-Check vor jedem Edit

Vor dem Schreiben eines Files:

1. Liegt es innerhalb eines in der Spec genannten Aggregates / Adapters / Screens?
2. Faellt es unter `shared/`, `config/` (erlaubt) oder unter `// mda-generator: manual-edits-below` (erlaubt)?
3. Ist es eine additive Flyway-Datei (erlaubt)?

Wenn **kein** Punkt zutrifft: zurueck an den Nutzer, Spec erweitern oder Edit ablehnen.
