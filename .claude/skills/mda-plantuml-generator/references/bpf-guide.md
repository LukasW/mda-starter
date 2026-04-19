# Business Process Flow — Implementation Guide

Eigener, leichtgewichtiger Zustandsautomat auf Panache (Spec 12 & Stack 7.3). **Kein** Camunda für BPFs.

## Datenmodell

```
bpf_instance
  id            uuid pk
  tenant_id     uuid not null
  process_name  text not null
  aggregate_id  uuid not null
  aggregate_type text not null
  current_stage text not null
  started_at    timestamp not null
  completed_at  timestamp null
  version_number bigint not null

bpf_transition_log
  id             uuid pk
  instance_id    uuid fk -> bpf_instance
  from_stage     text null
  to_stage       text not null
  trigger        text not null
  guard_result   text null
  occurred_at    timestamp not null
  actor          text not null
```

## Runtime

- Pro Aggregate maximal **eine** aktive Instanz je Prozesstyp.
- Transition ist serverseitig autoritativ; Guards nutzen dieselbe Ausdrucks-DSL wie Rules.
- Jede Transition schreibt in `bpf_transition_log` **in derselben Transaktion** wie die Aggregat-Änderung.
- Ungültige Transition (`from` ≠ `current_stage` oder Trigger nicht erlaubt) → `DomainException` `MDA-BPF-001`.

## API-Konvention

- `POST /api/v1/{entity}/{id}/process/{process}/trigger/{trigger}` → 200 + neues Stage.
- `GET  /api/v1/{entity}/{id}/process/{process}` → aktueller Zustand + Historie.

## Versionsverhalten

- BPF-Definition ist versioniert (Metamodell).
- Laufende Instanzen bleiben an ihrer Ausgangsversion; neue Instanzen verwenden die aktuelle.

## Test-Strategie

- Unit: Transition-Matrix (from/trigger → to) vollständig abgedeckt.
- BDD (`@process`): eine Feature-Datei pro Prozess mit Golden Path + mind. einem verbotenen Übergang.
