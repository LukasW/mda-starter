# Agent: docs-writer

**Aufgabe.** Dokumentation erstellen/aktualisieren: `CLAUDE.md` im Repo-Root, `docs/architecture/arc42.md` (kompakte arc42-Instanz) mit C4-Diagrammen (L1 Context, L2 Container, L3 Component) als eingebettete PlantUML-Blöcke, ADR-Grundgerüst unter `docs/architecture/adr/`.

## CLAUDE.md

Inhalt strikt praxisbezogen – was Claude wissen muss, um weiterzuarbeiten:

- Projektzweck (1 Absatz).
- Build-/Dev-/Test-Kommandos (`./mvnw quarkus:dev`, `./mvnw verify`, `./mvnw test -Dcucumber.filter.tags=@ui`).
- Architekturkurz: Hexagonal mit Package-Layout und Import-Regeln.
- Test-Strategie und Tag-Konvention.
- Wichtige Konventionen (IDs als VOs, Events sammeln via `pullEvents()`, RFC-7807-Fehlermodell).
- Wo das Metamodell und PlantUML liegen (`specs/`).
- Bekannte Einschränkungen / offene Punkte.

Kein Prosa-Dokument – max. 150 Zeilen. Template: `templates/CLAUDE.md.tmpl`.

## arc42

`docs/architecture/arc42.md` enthält die kompakten Abschnitte:

1. Einführung und Ziele
2. Randbedingungen
3. Kontextabgrenzung (inkl. C4-L1)
4. Lösungsstrategie
5. Bausteinsicht (C4-L2 Container + C4-L3 Components je generiertem BC)
6. Laufzeitsicht (Sequenz-Skizze für 1 Use Case)
7. Verteilungssicht (Kurzblock: JVM/PostgreSQL/Keycloak)
8. Querschnittliche Konzepte (Auth, Audit, Events)
9. Architekturentscheidungen (Link auf ADRs)
10. Qualitätsziele + SLOs (Übernahme aus `specs/MDA-Quarkus-Stack.md` Kap. 11.6)
11. Risiken
12. Glossar

C4 wird als PlantUML eingebettet:

```plantuml
@startuml C4-L1
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Context.puml
Person(user, "Benutzer")
System(mda, "MDA Quarkus Platform")
System_Ext(kc, "Keycloak")
System_Ext(db, "PostgreSQL")
Rel(user, mda, "HTTPS/REST")
Rel(mda, kc, "OIDC")
Rel(mda, db, "JDBC")
@enduml
```

Für L2/L3 je nach Bounded Context konkrete Container/Components aus der generierten Package-Struktur ableiten.

## ADRs

Startset unter `docs/architecture/adr/`:

- `0001-hexagonal-architecture.md`
- `0002-ddd-tactical-design.md`
- `0003-postgresql-rls-multi-tenancy.md` (aus Spec 5.4)
- `0004-bdd-with-cucumber.md`
- `0005-model-driven-generator.md`

Je ADR im Michael-Nygard-Format (Status, Kontext, Entscheidung, Konsequenzen).

## Technische Referenzen (context7 MCP)

- "arc42 template current"
- "c4 plantuml stdlib include reference"
- "madr vs nygard adr"

Templates liegen in `templates/` (`CLAUDE.md.tmpl`, `arc42.md.tmpl`, `adr.md.tmpl`).

## Report

Liste aller erzeugten Dateien + Hinweis, ob `CLAUDE.md` neu erzeugt oder gemerged wurde.
