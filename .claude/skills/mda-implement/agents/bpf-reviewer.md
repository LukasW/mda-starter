# Agent: bpf-reviewer

Prueft BPF-Aenderungen gegen `../../_shared/bpf-guide.md`.

## Checks

### Definition

- [ ] `domain/process/<Process>Lifecycle.java` implementiert `BpfDefinition<...Stage>`.
- [ ] Neue Transitions sind **additiv**; bestehende Stage-Semantik bleibt stabil.
- [ ] `initial()` nicht geaendert (ausser dokumentiert in Spec).

### Service

- [ ] `<Process>FlowService` im Application-Service-Layer, `@Transactional`.
- [ ] Aggregat-Seiteneffekt **und** BPF-Transition in derselben Transaktion.
- [ ] Ungueltige Transition → `DomainException MDA-BPF-001`.

### Persistenz

- [ ] `bpf_instance`-Unique-Constraint `(tenant_id, process_name, aggregate_id) WHERE completed_at IS NULL` aktiv.
- [ ] `bpf_transition_log` pro Transition geschrieben (`from_stage`, `to_stage`, `trigger`, `occurred_at`, `actor`).

### REST

- [ ] Endpunkt `POST /api/v1/{entity}/{id}/process/{process}/trigger/{trigger}` liefert 200 + neuen Stage.
- [ ] `GET /api/v1/{entity}/{id}/process/{process}` liefert aktueller Zustand + Historie.

### Tests

- [ ] Unit: vollstaendige Transition-Matrix (from/trigger → to, inkl. verbotener Uebergaenge).
- [ ] BDD `@process`: Golden Path + mind. 1 verbotener Uebergang.

## Output

- Pass/Fail je Regel.
- Fundstellen mit Empfehlung.
