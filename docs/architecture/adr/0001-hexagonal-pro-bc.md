# ADR 0001 — Hexagonale Architektur je Bounded Context

- Status: accepted
- Datum: 2026-04-19

## Kontext

Der CLM-MVP enthaelt drei Bounded Contexts mit unterschiedlichen Lebenszyklen (Vertrag, Freigabe, Frist). Wir wollen spaetere Auslagerung in separate Microservices ohne Rework ermoeglichen und die Domaenenlogik von Frameworks entkoppeln.

## Entscheid

Wir setzen pro Bounded Context das Port-und-Adapter-Muster um: `domain`, `application.port.{in,out}`, `application.service`, `adapter.{in,out}`, plus gemeinsames `shared`. Durchsetzung per `ArchitectureTest` (ArchUnit).

## Konsequenzen

- Domaenenklassen sind rein-Java (keine Jakarta-Imports).
- Anwendungsservices tragen die Transaktionsgrenze.
- REST-Adapter kennt keine Persistenz; Persistenz-Adapter keine Anwendungsservices.
- Cross-BC-Kopplung nur via IDs oder Events.
