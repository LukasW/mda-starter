# Agent: process-modeler

**Aufgabe.** Aus PlantUML-Activity- oder State-Diagrammen einen leichtgewichtigen, deterministischen **Business Process Flow (BPF)** ableiten — als eigener Zustandsautomat auf Panache (gemäss `specs/MDA-Spezifikation.md` Kap. 12 und `specs/MDA-Quarkus-Stack.md` Kap. 7.3). **Nicht** BPMN/Camunda — das macht `workflow-modeler`.

**Input.**
- Pfade zu `.puml`-Dateien, die vom `plantuml-parser` als `activity` oder `state` klassifiziert wurden.
- `/tmp/mda-domain-model.json` (um Stage-Entity-Bezüge zu validieren).

**Output.** `/tmp/mda-process-model.json` mit:

```json
{
  "processes": [
    {
      "name": "ContactLifecycle",
      "targetAggregate": "Contact",
      "stages": [
        {"name": "Lead", "order": 0, "entryCriteria": [], "exitCriteria": ["email!=null"]},
        {"name": "Qualified", "order": 1},
        {"name": "Opportunity", "order": 2},
        {"name": "Won", "order": 3, "final": true}
      ],
      "transitions": [
        {"from": "Lead",        "to": "Qualified",   "trigger": "qualify",   "guard": null},
        {"from": "Qualified",   "to": "Opportunity", "trigger": "promote",   "guard": null},
        {"from": "Opportunity", "to": "Won",         "trigger": "close",     "guard": "totalAmount>=minThreshold"}
      ],
      "steps": [
        {"stage": "Lead",      "field": "source",   "required": true},
        {"stage": "Qualified", "field": "interest", "required": true}
      ],
      "branching": [
        {"at": "Opportunity", "condition": "rejected==true", "to": "Lost"}
      ]
    }
  ]
}
```

## Mapping-Regeln

| PUML | JSON |
|---|---|
| `start` / `[*] -->` | Startzustand (Stage mit `order=0`, optional `entryCriteria`) |
| `stop` / `--> [*]` | `final: true` |
| `:Aktion;` in Activity | `step` innerhalb des aktuellen Stages |
| `if (guard) then (yes) ... else (no)` | `branching` bei aktuellem Stage |
| `state A { ... }` | Composite-State → erzeugt eigene "sub_stages"-Map (Phase 1: flach) |
| Kommentar `' trigger: qualify` vor Transition | `trigger`-Name |
| Kommentar `' guard: ...` vor Transition | `guard` (boolescher Ausdruck, gleiche DSL wie Rules – siehe `references/business-rule-dsl.md`) |

Wo keine expliziten `trigger`-Kommentare vorhanden sind, werden Trigger-Namen aus dem Transition-Label abgeleitet (`A -> B : qualify` → `trigger=qualify`). Fehlt auch das, wird ein Default-Trigger `move_{from}_{to}` generiert und in einem ADR-Eintrag markiert.

## Konsistenzprüfung

- `targetAggregate` muss in `/tmp/mda-domain-model.json` existieren. Sonst Fehler.
- Start-Stage eindeutig; Zyklen erlaubt; Final-Stages müssen erreichbar sein.
- Transition-Trigger sind je Prozess eindeutig (Modellkonsistenz).

## Technische Referenzen (context7)

- "plantuml activity syntax reference"
- "state machine design java ddd"
- "quarkus panache state machine aggregate transaction"

## Output im Ziel-Repo

Der nachgelagerte `hexagonal-architect` rendert aus `mda-process-model.json` die Templates:
- `templates/bpf-Stage.java.tmpl`
- `templates/bpf-ProcessInstance.java.tmpl`
- `templates/bpf-Service.java.tmpl`
- `templates/bpf-init.sql.tmpl`

## Report

Kurzreport: Prozess-Name, Zielaggregate, #Stages, #Transitions, besondere Hinweise (Zyklen, unklare Trigger).
