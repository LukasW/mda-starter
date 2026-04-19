# Agent: hexagonal-reviewer

Prueft Java-Diff auf Einhaltung der hexagonalen/DDD-Regeln aus `../../_shared/hexagonal-rules.md` und `../../_shared/mda-spec.md`.

## Eingabe

- Git-Diff des aktuellen Branch gegen `origin/main`.
- Optional Pfadliste aus `/tmp/mda-feature-plan.json`.

## Checks

### Paket-Grenzen

- [ ] `domain/**` — **keine** Framework-Imports (`jakarta.*`, `io.quarkus.*`, `org.hibernate.*`, Logger-Libs).
- [ ] `application/port/**` — nur Interfaces und Records; keine Framework-Imports.
- [ ] `application/service/**` — darf `@Transactional`, `@ApplicationScoped` nutzen; **kein** direkter JPA-/REST-Import.
- [ ] `adapter/in/rest/**` — darf `jakarta.ws.rs.*`, `jakarta.validation.*`; ruft **nur** `port.in` auf, nicht `application.service` direkt und nicht `adapter.out`.
- [ ] `adapter/out/persistence/**` — darf `jakarta.persistence.*`, Panache; implementiert **nur** `port.out`.

### DDD

- [ ] Aggregate Root haelt Invarianten (keine naked setters).
- [ ] Typisierte IDs als `record XxxId(UUID value) {}` mit Compact-Constructor-Validierung.
- [ ] Value Objects als `record` mit Compact-Constructor (`Objects.requireNonNull`, Range-Check, Regex).
- [ ] Domain Events als `sealed interface ...DomainEvent permits ...`.
- [ ] Cross-Aggregate-Referenzen nur per ID.

### Transaktionen

- [ ] Nur `application/service/**` traegt `@Transactional`.
- [ ] Rollback auf `DomainException`, `ConstraintViolationException`, `PersistenceException`.

### Fehlermodell

- [ ] `DomainException`-Subklassen landen im `DomainExceptionMapper` (`@Provider`).
- [ ] Code-Konvention `MDA-<AREA>-<NR>` (z. B. `MDA-CON-001`, `MDA-BPF-001`).
- [ ] Keine Exception-Schlucker (`catch (Exception e) { }` ohne Log oder Rethrow).

### Idempotenz

- [ ] Keine Edits oberhalb `// mda-generator: manual-edits-below` in generierten Klassen.
- [ ] Flyway-Migration additiv.
- [ ] Neue sealed-Subtypen **angehaengt**, nicht ersetzt.

## Output

- **Pass/Fail** je Regel.
- Liste von Fundstellen `file:line — Regel X verletzt` bei Fehlern.
- Bei Hard-Stoppern (Framework-Import in Domain, editierte Flyway, `permits`-Entfernung): **Blockieren** des Commits und Empfehlung, wie zu loesen.
