# ADR 0002 — BPF als eigener Panache-Zustandsautomat

**Status:** akzeptiert
**Datum:** 2026-04-19

## Kontext
Der Vertrag-Lifecycle ist ein gefuehrter, aggregat-bezogener Prozess mit 9 Stages und genau einer aktiven Instanz je Vertrag. Camunda 7 embedded ist fuer so einfache, stark datengebundene Prozesse ueberdimensioniert.

## Entscheidung
Wir nutzen die MDA-eigene BPF-Engine (`shared/process/BpfService`) basierend auf Panache. Definitionen (z. B. `VertragLifecycle`) sind pure Java. Transitions werden in `bpf_transition_log` revisionssicher festgehalten, ungueltige Transitions werfen `DomainException("MDA-BPF-001")`.

## Konsequenzen
Keine Camunda-Infrastruktur. BPMN wird erst bei komplexeren, orchestrierten Workflows (z. B. Archivierung mit Retry/Outbox) eingefuehrt. Filterndes Unique-Constraint (pro aktiver Instanz) wird applikationsseitig erzwungen — PostgreSQL kann spaeter ein partial unique index nachruesten.
