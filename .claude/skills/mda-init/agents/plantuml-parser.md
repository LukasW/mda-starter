# Agent: plantuml-parser

**Aufgabe.** (1) PlantUML-Dateien **klassifizieren** (Klassen-, Activity-, State-, Sequence-Diagramm) und (2) bei Klassendiagrammen ein Zwischenmodell extrahieren. Andere Diagrammarten werden an den jeweiligen Subagent delegiert.

## Klassifikation

Erste signifikante Zeile nach `@startuml` entscheidet:

| Muster | Typ | Delegat |
|---|---|---|
| `class X`, `enum X`, Stereotyp `<<aggregate>>`/`<<vo>>`/`<<event>>` | `class` | `agents/domain-modeler.md` |
| `start`, `:Aktion;`, `if (...) then`, `fork`, `partition` | `activity` | `agents/process-modeler.md` |
| `[*] -->`, `state X {`, `hide empty description` | `state` | `agents/process-modeler.md` |
| `participant`, `actor` gefolgt von `->`, `->>`, `-->>` | `sequence` | (informational; fliesst in arc42 Laufzeitsicht) |
| Sonst | `unknown` | Fehler an Orchestrator, Nutzer fragen |

Mehrdeutige Dateien (mehrere `@startuml`-Blöcke unterschiedlicher Typen) werden pro Block behandelt.

**Input.** Pfad zu `*.puml` / `*.plantuml`.

**Output.** `/tmp/mda-domain-model.json` (überschreibend) mit folgender Struktur:

```json
{
  "boundedContext": "string",
  "aggregates": [
    {
      "name": "string",
      "root": true,
      "attributes": [
        {"name": "string", "type": "string|int|decimal|bool|date|datetime|uuid|enum:...", "required": true, "unique": false}
      ],
      "invariants": ["natürlichsprachlich"],
      "methods": [
        {"name": "string", "params": [{"name": "...", "type": "..."}], "returns": "...", "throws": []}
      ]
    }
  ],
  "valueObjects": [
    {"name": "string", "fields": [...], "immutable": true}
  ],
  "entities": [...],
  "enums": [
    {"name": "string", "values": ["..."]}
  ],
  "relationships": [
    {"from": "Aggregate.A", "to": "Aggregate.B", "kind": "1:1|1:n|n:m|composition|aggregation", "navigable": "from|to|both"}
  ],
  "domainEvents": [
    {"name": "string", "trigger": "Aggregate.method", "payload": ["field", "..."]}
  ],
  "useCases": [
    {"name": "string", "kind": "command|query", "targetAggregate": "...", "description": "..."}
  ]
}
```

## Parsing-Heuristiken

| PlantUML-Element | Mapping |
|---|---|
| `class X` (ohne Stereotyp) | Entity oder Value Object – je nach Heuristik (siehe unten) |
| `class X <<aggregate>>` / `<<aggregate root>>` | Aggregate Root |
| `class X <<value object>>` / `<<vo>>` | Value Object |
| `class X <<entity>>` | Entity (innerhalb eines Aggregates) |
| `class X <<event>>` / `<<domain event>>` | Domain Event |
| `enum X` | Enum |
| `X --> Y` | Assoziation (Navigation von X nach Y) |
| `X o-- Y` | Aggregation (Y Teil von X, Lebenszyklus unabhängig) |
| `X *-- Y` | Composition (Y Teil von X, Lebenszyklus abhängig) |
| `X .. Y : <<uses>>` | Use-Case-Beziehung |
| Kommentare `' invariant: ...` | Sammeln als Invariante des vorhergehenden Aggregates |
| Methoden `+ create(...)`, `+ confirm()` | Use-Cases (commands); Read-only Getter sind KEINE Use Cases |

**Value Object vs Entity** (falls kein Stereotyp): ohne ID-Attribut und mit nur Wert-Typ-Attributen → Value Object; alles andere → Entity. Im Zweifel Entity und ADR-Eintrag vermerken.

**Aggregate-Zuordnung**: Composition (`*--`) und Kind-Entities definieren implizit die Aggregate-Grenze, wenn kein Stereotyp vorliegt. Cross-Aggregate-Referenzen werden als `uuid`-Typ modelliert (kein Navigations-Property), siehe DDD-Regel "reference by ID".

## Technischer Modus

Dieser Agent ruft **context7** nur nötigenfalls auf (z. B. PlantUML-Syntax-Grenzfälle). Primäre Logik ist ein eigener Mini-Parser in Bash/Python. Ein kompakter Python-Parser liegt in `scripts/parse_puml.py` (falls vorhanden) – andernfalls direkt Regex-basiert in Bash.

## Abbruchbedingungen

- Datei nicht lesbar → Fehler an Orchestrator.
- Keine `class`-Deklaration gefunden → Fehler.
- Aggregate ohne mindestens ein Attribut → Warnung + ADR-Eintrag, aber weiterarbeiten.

## Report

Am Ende einen Kurzreport (≤ 10 Zeilen) an den Orchestrator:
- Anzahl Aggregates, VOs, Events, Use Cases
- Liste aller Aggregate-Namen
- Auffälligkeiten (Zyklen, fehlende IDs, namenlose Attribute)
