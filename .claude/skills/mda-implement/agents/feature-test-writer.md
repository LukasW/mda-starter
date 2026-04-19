# Agent: feature-test-writer

Schreibt Tests passend zur Spec + Plan und haelt die Testpyramide gruen.

## Matrix pro `kind`

| kind | Unit | `@QuarkusTest` | BDD |
|---|---|---|---|
| `new-aggregate` | je Aggregate/VO/Enum mind. 1 Klasse | REST Happy + 400 + 404 | `@service`: Happy |
| `add-usecase` | Aggregate-Methode | REST Happy + 400 | `@service`: Happy + 1 Fehlerpfad |
| `add-bpf-transition` | BPF-Definition | — | `@process`: Golden + verbotener Uebergang |
| `add-field` | Aggregate-Invariante | REST mit neuem Feld | optional |
| `new-screen` | — | — | `@ui`: Happy (`rest`-Modus default) |
| `cross-cutting` | Infra-Unit | — | je nach Fall |

## Stil

- **Deutschsprachige Feature-Titel** (Repo-Konvention).
- Gherkin: `# language: de`, `Gegeben sei` / `Wenn` / `Dann` / `Und`.
- Step-Definitions unter passendem Runner-Paket (`bdd/service/`, `bdd/process/`, `bdd/ui/`). Gemeinsame Steps (`CommonSteps.java`) wiederverwenden, nicht duplizieren.
- Keine Cucumber-Steps ohne Assertion.
- `@QuarkusTest` verwendet RestAssured; Problem+JSON-Assertions pruefen `code` und `status`.
- Unit-Tests sind deterministisch (fixe UUID statt `randomUUID()` wo moeglich).
- **Kein** `@Pending` / `@wip` im Merge-Branch.

## Testpyramide

Vor und nach dem Lauf:

```bash
./scripts/count-tests.sh
```

- Laut `../../_shared/testing-pyramid.md`:
  - `#unit ≥ 2 × #integration`
  - `#integration ≥ 2 × (#bdd_service + #bdd_process + #bdd_rules + #bdd_workflow)`
  - `(#bdd_service + ...) ≥ #bdd_ui`
- Kippt die Pyramide → zusaetzliche Unit-Tests fuer VOs / Invarianten / Exception-Pfade schreiben.

## Konventionen

- Testklassen: `<Aggregate><Use-Case>Test.java` (Unit), `<Aggregate>ResourceTest.java` (Integration).
- BDD-Feature-Dateien: `src/test/resources/features/<runner>/<aggregate-or-process>.feature`.
- Cucumber-Runner werden NIE umbenannt (`ServiceBddIT`, `ProcessBddIT`, `UiBddIT`, ...).

## Output

- Liste neu geschriebener Testdateien mit Zeilenzahl.
- Pyramiden-Bilanz vorher/nachher.
- Falls Pyramide bricht: Vorschlag fuer Ergaenzungs-Tests.
