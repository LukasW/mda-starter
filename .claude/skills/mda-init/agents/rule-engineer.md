# Agent: rule-engineer

**Aufgabe.** Aus deklarativen Business-Rules-DSL-Dateien (`*.rules.yaml` / `*.rules.json`) eine leichtgewichtige, **deterministische** Rule-Engine bauen (Spec Kap. 10 & Stack 7.1): AST im Metamodell, pure Java Interpreter, REST-Resource zur Publikation des AST an den Client-Renderer, Snapshot-Tests für Client-/Server-Äquivalenz.

**Input.**
- `.rules.yaml` / `.rules.json` unter `specs/model/rules/` oder wo vom Nutzer benannt.
- `/tmp/mda-domain-model.json` zur Feld-Validierung.

**Output.** `/tmp/mda-rule-model.json` + die erzeugten Java-Artefakte.

## DSL-Erwartung (siehe `references/business-rule-dsl.md` für Schema)

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
  - id: "PreferredLanguageDefault"
    form: "QuickCreate"
    when: { const: true }
    actions:
      - type: "setValue"
        field: "preferredLanguage"
        value: { const: "de-CH" }
```

## Parser-Schritte

1. YAML/JSON mit JSON-Schema (`templates/rules-SchemaJson.tmpl`) validieren.
2. Je `rule` zu einem `Rule`-Record des AST konvertieren.
3. Konsistenzprüfung: `field` referenziert ein Attribut des `entity` aus `mda-domain-model.json`. Unbekannte Felder → Hard-Fail.
4. AST nach `/tmp/mda-rule-model.json` schreiben.

## Java-Artefakte (via `hexagonal-architect`)

| Template | Ziel |
|---|---|
| `rules-ASTRecords.java.tmpl` | `…/domain/rules/{Condition,Action,Rule}.java` — sealed records |
| `rules-Interpreter.java.tmpl` | `…/domain/rules/RuleInterpreter.java` — `evaluate(RuleContext): EvaluationResult` |
| `rules-Registry.java.tmpl` | `…/application/service/RuleRegistry.java` — lädt YAML beim Boot + Caffeine-Cache |
| `rules-EvaluationService.java.tmpl` | `…/application/service/{{Aggregate}}RuleEvaluationService.java` — aufgerufen vor persist in jedem Write-UseCase |
| `rules-Resource.java.tmpl` | `…/adapter/in/rest/RulesResource.java` — `GET /api/v1/metadata/rules/{entity}` liefert kompaktes AST |
| `rules-SnapshotTest.java.tmpl` | Pro Rule ein Snapshot-Test (Eingabekontext → erwartete Aktionen) |
| `rules-Feature.feature.tmpl` | BDD `@rules`, Tag, je Regel ein Szenario |

## Action-Semantik

| Action | Wirkung |
|---|---|
| `setRequired` | Markiert `field` als Pflicht; leerer Wert führt zu `EvaluationResult.error`. |
| `setVisible(false)` | Feld aus Response-DTO filtern + in Rule-AST für Client markieren. |
| `setReadOnly(true)` | Write-Versuch → `422` mit `MDA-RUL-002`. |
| `setValue` | Server setzt Wert vor persist, falls aktuell null (idempotent). |
| `showError` | Ergänzt `ProblemDetail.errors`; HTTP-Status wird zu `422`. |
| `showRecommendation` | Informational (Header `Warning` oder Response-Feld `recommendations`), kein Abbruch. |

## Determinismus & Idempotenz

Der Interpreter ist **side-effect-frei**, gibt eine `EvaluationResult` zurück, die vom Anwendungsservice in das Ergebnis-Objekt gemischt wird. Mehrfachausführung liefert dasselbe Ergebnis.

## Snapshot-Tests (Client/Server-Äquivalenz)

Jede Rule wird mit mind. 2 Eingabekontexten getestet (Happy + Edge). Der Ergebnis-AST ist JSON-normalisiert und gegen eine Snapshot-Datei unter `src/test/resources/rules-snapshots/` verglichen. Der **gleiche** Snapshot muss später von einem TypeScript-Interpreter (Spec 7.1) erzeugt werden — dieser Generator bereitet die Verträge dafür vor, auch wenn der TS-Interpreter nicht Teil des ersten Outputs ist.

## Technische Referenzen (context7)

- "json schema 2020-12 validation java"
- "quarkus cache caffeine configuration"
- "deterministic expression evaluator ast java"

## Report

Kurzreport: Anzahl Rules, unbekannte Feld-Referenzen (Fehler-Liste), Schema-Version.
