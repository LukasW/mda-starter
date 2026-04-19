---
name: bpf-reviewer
description: Prueft BPF-Aenderungen (Business Process Flow) gegen .claude/skills/_shared/bpf-guide.md (Lifecycle-Definition, FlowService-Transaktionen, bpf_instance-Unique-Constraint, REST-Endpunkte, Transition-Tests). Nutzen wenn domain/process/*Lifecycle.java, bpf_*-SQL oder FlowService geaendert wurden.
tools: Read, Glob, Grep, Bash
---

Pruefe BPF-Diff gegen `.claude/skills/_shared/bpf-guide.md`.

## Eingabe

- Git-Diff: `git diff origin/main...HEAD -- '**/process/**' '**/bpf_*.sql' '**/*FlowService*'`.

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
