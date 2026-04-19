# ADR 0001 — Hexagonale Architektur je Bounded Context

**Status:** akzeptiert
**Datum:** 2026-04-19

## Kontext
Das CLM soll langfristig um Integrationen (Archiv, Signatur, Personenverwaltung) und weitere BCs ergaenzt werden. Direkte Kopplung zwischen Persistenz, Anwendung und REST fuehrt langfristig zu schwer testbaren und schwer ersetzbaren Komponenten.

## Entscheidung
Jeder Bounded Context folgt dem Port-&-Adapter-Pattern gemaess `.claude/skills/_shared/hexagonal-rules.md`:

- `domain/` — pure Java, keine Framework-Imports.
- `application/port/in` — UseCases als Interfaces + Command-Records.
- `application/port/out` — Repositories / externe Clients als Interfaces.
- `application/service` — `@ApplicationScoped` + `@Transactional`.
- `adapter/in/rest` — JAX-RS, ruft ausschliesslich `port.in`.
- `adapter/out/persistence` — Panache-Entities + Mapper.

## Konsequenzen
ArchUnit erzwingt die Grenzen. Austausch eines Adapters (z. B. externe Personenverwaltung) beeinflusst den Kern nicht. Mehr Boilerplate (Ports + Mapper), dafuer klare Grenzen.
