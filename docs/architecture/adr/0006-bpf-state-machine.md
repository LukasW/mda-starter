# ADR 0006 — BPF-Statemachine auf Panache (statt Camunda)

- Status: accepted
- Datum: 2026-04-19

## Kontext

Der Vertrags-Lebenszyklus (9 Stati) und der Frist-Lebenszyklus (4 Stati) sind uebersichtliche Zustandsautomaten ohne verzweigte Service-Tasks, Kompensations-Flows oder User-Tasks mit Delegation. Eine externe Workflow-Engine (Camunda) ist Overkill und bringt Deployment-/Upgrade-Komplexitaet.

## Entscheid

BPF (Business Process Flow) ist ein leichter, generischer Zustandsautomat unter `shared.process`:

- `BpfDefinition<S,T>` — typisierte Transition-Matrix, per Builder aufgebaut; Terminal-Set.
- `BpfService` — persistente Laufzeit; jede Transition schreibt in derselben Transaktion einen `bpf_transition_log`-Eintrag.
- Stage-Enums implementieren `BpfStage`; Trigger-Enums sind pro Prozess eigen.
- Ungueltige Uebergaenge werfen `DomainException` `MDA-BPF-001` → HTTP 409.

## Konsequenzen

- Null zusaetzliche Laufzeit-Abhaengigkeiten (kein Camunda-Deployment).
- Audit-Log bekommt Zustandsaenderungen automatisch.
- Komplexere Workflows (mehrstufige Freigabeketten, Delegation) muessen spaeter entweder in BPF ausgebaut oder durch Camunda 7 (ADR 0007) ergaenzt werden.
