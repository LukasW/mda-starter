# Business Process Flow — Umsetzung

Eigener, leichtgewichtiger Zustandsautomat auf Panache (kein Camunda fuer BPFs). Siehe `mda-spec.md` §5.5 und `mda-stack.md` §2 (BPF-Engine).

## Datenmodell

```
bpf_instance
  id              uuid pk
  tenant_id       uuid not null
  process_name    text not null
  aggregate_id    uuid not null
  aggregate_type  text not null
  current_stage   text not null
  started_at      timestamp not null
  completed_at    timestamp null
  version_number  bigint not null
  unique (tenant_id, process_name, aggregate_id) where completed_at is null

bpf_transition_log
  id              uuid pk
  instance_id     uuid fk -> bpf_instance
  from_stage      text null
  to_stage        text not null
  trigger         text not null
  guard_result    text null
  occurred_at     timestamp not null
  actor           text not null
```

Flyway: `V2__bpf.sql` im Starter; Deltas additiv.

## Runtime-Regeln

- Pro Aggregat max. **eine** aktive Instanz je Prozesstyp.
- Transition ist serverseitig autoritativ. Guards nutzen dieselbe Ausdrucks-DSL wie BusinessRules.
- Jede Transition schreibt in `bpf_transition_log` in **derselben** Transaktion wie die Aggregat-Aenderung.
- Ungueltige Transition (`from` ≠ `current_stage` oder Trigger nicht erlaubt) → `DomainException MDA-BPF-001`.
- Versionsverhalten: laufende Instanzen bleiben an ihrer Ausgangsversion; neue Instanzen nutzen die aktuelle.

## API-Konvention

- `POST /api/v1/{entity}/{id}/process/{process}/trigger/{trigger}` → 200 + neues Stage.
- `GET  /api/v1/{entity}/{id}/process/{process}` → aktueller Zustand + Historie.

## Code-Pattern

```java
// domain/process/AuftragLifecycle.java — BEISPIEL (neutrale Domaene)
public final class AuftragLifecycle implements BpfDefinition<AuftragStage> {
    @Override public AuftragStage initial() { return AuftragStage.ENTWURF; }
    @Override public Map<AuftragStage, List<Transition<AuftragStage>>> transitions() {
        return Map.of(
            AuftragStage.ENTWURF,    List.of(new Transition<>("bestaetigen", AuftragStage.BESTAETIGT)),
            AuftragStage.BESTAETIGT, List.of(
                new Transition<>("versenden",  AuftragStage.VERSANDT),
                new Transition<>("stornieren", AuftragStage.STORNIERT)));
    }
}
```

Dispatch ueber `BpfService#trigger(processName, aggregateId, trigger, actor)` in `shared/process/`.

## Test-Pflicht

- Unit: vollstaendige Transition-Matrix (from/trigger → to), inkl. verbotener Uebergaenge.
- BDD (`@process`): je Prozess eine `.feature`-Datei mit Golden Path + mind. 1 verbotenem Uebergang.
