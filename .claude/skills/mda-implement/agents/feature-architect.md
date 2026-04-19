# Agent: feature-architect

Setzt den Plan (`plan/<slug>.md` + `/tmp/mda-feature-plan.json`) in Code ueber. Liest Dateien, bevor er sie editiert; respektiert `../../_shared/drift-guards.md` und `../../_shared/hexagonal-rules.md`.

## Regeln

- **Neue Dateien**: vollstaendige Package-Header, richtige Imports, `record`/`class`/`sealed interface` gemaess Nachbar-Codestil.
- **Bestehende Dateien**: nur unterhalb `// mda-generator: manual-edits-below` oder per gezieltem Insert zwischen klaren Markern. Fehlt der Marker → **einmalig** einfuegen (am Ende der Klasse, vor der schliessenden Klammer).
- **Domain bleibt framework-frei**: keine `jakarta.*`-, keine `io.quarkus.*`-Imports in `domain/**`.
- **REST-Endpunkte**: neue `@Path`-Methoden mit Jakarta-Validation-Annotationen (`@NotBlank`, `@Size`, ...). Response-Record oder DTO je nach Shape.
- **DTO nur wenn noetig**: wenn HTTP-Shape von Domain abweicht. Sonst direkt das Domain-`record` zurueckgeben.
- **Events**: neue sealed-Subtypen an `permits`-Liste **anfuegen**, nie ersetzen oder umbenennen.
- **Flyway**: neue Migration `V<n>__<slug>.sql`. `n` aus `ls src/main/resources/db/migration/ | sort -V | tail -1` +1. Bestehende NIE editieren.
- **JPA-Entities**: neue Felder `public` wie bestehende (Panache-Style). Nullability wie in `domain/` gespiegelt.
- **Angular**: Komponenten/Services **nur** via `ng generate` erzeugen (durch Bash). Danach Template/SCSS/Service-Call ausfuellen.
- **Cross-Aggregate-Referenzen**: nur per ID oder Event — niemals direkter Service-Call.

## Reihenfolge (zwingend)

1. Flyway-Migration (damit `./mvnw -q compile` nach dem naechsten Schritt noch klappt).
2. JPA-Entity (Column/Mapping-Updates fuer die neue Spalte).
3. Domain (Aggregate-Feld/Methode/Event/VO).
4. Application (Port-in-Interface + Command/Query-Record, Port-out, `@Transactional`-Service-Methode).
5. Adapter in (REST-Resource + Request-Record + (ggf.) DTO-Mapper).
6. Adapter out (Repository-Impl + Persistence-Mapper).
7. Frontend (`ng generate` → Template + TS + SCSS + Service-Call + Routes-Update).
8. `application.properties`: nur neue Keys anhaengen.

Nach **jedem** Schritt `./mvnw -q compile` — bei Fehler sofort Root-Cause fixen.

## Stil

- Gleiche `@Transactional`-/`@Inject`-Konventionen wie Bestand.
- ArchUnit-Regeln im Kopf: keine Service-Imports in REST-Adapter.
- Kommentar-Diaet: nur wenn WHY nicht trivial (Workaround, versteckte Invariante). Keine "Added for US-xx"-Kommentare.
- Konsistenter Namensstil (Repo nutzt Deutsch fuer Fachbegriffe).

## Nach dem Lauf

- `./mvnw -q compile` sauber.
- Report: Anzahl neuer/geaenderter Dateien, Flyway-Version, neue Angular-Komponenten.
- Wenn waehrend der Umsetzung ein Drift-Guard triggerte → **abbrechen**, Diff verwerfen, Nutzer fragen.
