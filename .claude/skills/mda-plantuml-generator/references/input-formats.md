# Eingabeformate des Generators

Der Skill akzeptiert beliebig viele dieser Artefakte in einem Lauf. Fehlt ein Format, wird das jeweilige Ziel-Artefakt schlicht **nicht erzeugt** — kein Fehler.

| Format | Dateimuster | Erkennung | Zielgenerierung |
|---|---|---|---|
| Klassendiagramm | `specs/model/*.puml` | `@startuml` + `class`/`enum`/Stereotyp | Domäne (Aggregates, VOs, Events, Use Cases) |
| Activity-Diagramm | `specs/model/*.puml` | `@startuml` + `start`/`:Aktion;` | BPF (`domain/process/`, `bpf_instance`-Tabelle) |
| State-Diagramm | `specs/model/*.puml` | `@startuml` + `[*] -->`/`state …` | BPF (wie Activity; Zustände = Stages) |
| BPMN 2.0 | `specs/model/*.bpmn` / `*.bpmn.xml` | XML `<bpmn:definitions>` | Workflow (Camunda 7 embedded) |
| Business Rules | `specs/model/rules/*.rules.yaml` / `*.rules.json` | YAML/JSON mit Top-Level `rules:` | Rule-Engine (AST, Interpreter, RulesResource) |
| DMN (optional) | `specs/model/*.dmn` | XML `<definitions xmlns="http://www.omg.org/spec/DMN/…">` | Decision-Wrapper |
| Markdown-Specs | `specs/model/**/*.md` | — | als Kontext gelesen; fliessen in ADRs/arc42 |
| Sequenz-Diagramm | `specs/model/*.puml` | `participant`/`actor …` + `->`/`->>`  | Nur Doku: eingebettet in arc42 Laufzeitsicht |

## Ablagekonventionen

```
specs/
├── MDA-Spezifikation.md          (bereits vorhanden)
├── MDA-Quarkus-Stack.md          (bereits vorhanden)
└── model/
    ├── contact.puml              (Klassendiagramm)
    ├── contact-lifecycle.puml    (Activity oder State)
    ├── case-approval.bpmn        (BPMN)
    ├── rules/
    │   └── contact.rules.yaml    (Business Rules DSL)
    └── decisions/
        └── tariff.dmn            (optional)
```

Abweichende Ablagen sind erlaubt — der Skill scannt unterhalb von `specs/` rekursiv und ordnet anhand obiger Heuristik zu.

## Konflikte & Reihenfolge

- **Mehrere Klassendiagramme** → zusammengeführt; doppelte Aggregate-Namen → Fehler.
- **Mehrere BPMN** → alle deployed; separate Worker-Klassen.
- **Regeln, die auf unbekannte Felder referenzieren** → Hard-Fail mit Fehlerliste.
- **Prozess-Transition, deren Ziel-Stage nicht definiert ist** → Hard-Fail.
