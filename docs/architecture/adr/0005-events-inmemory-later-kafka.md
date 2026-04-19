# ADR 0005 — Events: In-Memory im Erstentwurf, spaeter Outbox + Kafka

**Status:** akzeptiert
**Datum:** 2026-04-19

## Kontext
Im ersten Wurf ist nur ein Consumer (CLM selbst). Outbox + Debezium + Kafka bringen betrieblichen Overhead.

## Entscheidung
- `DomainEvent`-Interface + sealed Event-Hierarchien pro BC.
- Default-Publisher: `InMemoryDomainEventPublisher` (loggt + cacht fuer Tests).
- Ein spaeteres Feature ergaenzt ein Outbox-Persistenz + Debezium-CDC + Kafka-Publisher.

## Konsequenzen
- Schemastabilitaet (Kafka-Kompatibilitaet) bleibt Leitlinie — sealed-permit-Listen duerfen nur erweitern, nie verkleinern.
- Audit-Export (CSV/PDF) muss zunaechst direkt aus `bpf_transition_log` und Log-Dateien erzeugt werden.
