# Business Rules DSL

Deklarative, deterministische Regel-Beschreibung. Client- und serverseitig identisch interpretiert (Spec 10.2 & Stack 7.1). YAML oder JSON.

## JSON-Schema (Auszug)

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "required": ["version", "entity", "rules"],
  "properties": {
    "version": { "type": "string" },
    "entity":  { "type": "string" },
    "rules":   { "type": "array", "items": { "$ref": "#/$defs/rule" } }
  },
  "$defs": {
    "rule": {
      "type": "object",
      "required": ["id", "when", "actions"],
      "properties": {
        "id":      { "type": "string" },
        "form":    { "type": "string" },
        "when":    { "$ref": "#/$defs/expr" },
        "actions": { "type": "array", "items": { "$ref": "#/$defs/action" } }
      }
    },
    "expr": {
      "oneOf": [
        { "properties": { "const":  {} }, "required": ["const"] },
        { "properties": { "ref":    { "type": "string" } }, "required": ["ref"] },
        { "properties": { "op":     { "enum": ["eq","neq","gt","gte","lt","lte","in","notIn","isNull","isNotNull","and","or","not","between","contains","startsWith","endsWith"] } }, "required": ["op"] }
      ]
    },
    "action": {
      "type": "object",
      "required": ["type"],
      "properties": {
        "type":    { "enum": ["setRequired","setVisible","setReadOnly","setValue","showError","showRecommendation"] },
        "field":   { "type": "string" },
        "value":   { "$ref": "#/$defs/expr" },
        "level":   { "enum": ["Required","Recommended","Optional"] },
        "messageKey": { "type": "string" },
        "when":    { "$ref": "#/$defs/expr" }
      }
    }
  }
}
```

## Ausdrücke (`when` / `value`)

- `{ "const": <scalar> }` — literaler Wert.
- `{ "ref": "fieldName" }` — Attribut des Ziel-Aggregates.
- Unary: `{ "op": "isNull", "arg": <expr> }`.
- Binary: `{ "op": "eq", "left": <expr>, "right": <expr> }`.
- N-ary: `{ "op": "and", "args": [<expr>, <expr>, ...] }`.

Unbekannte Operatoren → Schema-Fehler beim Boot.

## Aktionen

- `setRequired`: Pflichtfeld-Flag. `level` optional.
- `setVisible`: `value: {"const": true|false}`.
- `setReadOnly`: `value: {"const": true|false}`.
- `setValue`: `field` + `value`-Ausdruck.
- `showError`: fehlermeldender Event, zwingt HTTP 422 mit `ProblemDetail.errors[]`.
- `showRecommendation`: Informational, kein Abbruch.

## Beispiel

```yaml
version: "1.0"
entity: "Contact"
rules:
  - id: "RequireEmailForActive"
    form: "MainForm"
    when:
      op: "eq"
      left: { ref: "status" }
      right: { const: "ACTIVE" }
    actions:
      - type: "setRequired"
        field: "email"
        level: "Required"
      - type: "showError"
        field: "email"
        when: { op: "isNull", arg: { ref: "email" } }
        messageKey: "contact.email.requiredForActive"
```

## Nicht erlaubt

- Funktionsaufrufe mit Seiteneffekten.
- Zugriff auf andere Aggregates (würde Aggregate-Grenze verletzen; nutze stattdessen einen `Workflow`).
- Zeitabhängige Aggregationen (`lastNDays` o.ä.) — das ist Query/View-Sache.
