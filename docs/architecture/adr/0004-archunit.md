# ADR 0004 — ArchUnit erzwingt Schichten- und BC-Regeln

- Status: accepted
- Datum: 2026-04-19

## Kontext

Hexagonale Architektur und saubere BC-Grenzen sind nicht durch den Compiler geschuetzt. Ohne Durchsetzung driften sie im Team schnell.

## Entscheid

`ArchitectureTest` (archunit-junit5 1.4.1) prueft im Surefire-Lauf:

1. `domain/**` importiert keine Framework-Pakete.
2. `application/**` kennt keine Persistenz-Details.
3. REST-Adapter ruft nur `port.in`.
4. Persistenz-Adapter ruft keine Application-Services.
5. Cross-BC-Kopplung vom Domain-Layer ist ausgeschlossen.

## Konsequenzen

- Neue Module werden bewusst zu den Regeln gedrueckt; sonst bricht CI.
- Ignore-Ausnahmen muessen in ADRs dokumentiert sein.
