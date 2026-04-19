# `specs/model/`

Input- und spaeter auch Output-Verzeichnis fuer die **fachliche Modellierung**.

## Input fuer `/mda-init`

`/mda-init` akzeptiert **einen** der folgenden Wege:

1. **Freitext / Markdown** unter `specs/description.md` — einfachster Einstieg, der Skill extrahiert Aggregates, Use-Cases und BPF-Kandidaten selbst.
2. **Deklarative Modelle** in diesem Verzeichnis:
   - `*.puml` — PlantUML-Klassendiagramme (Aggregates, VOs), State-Diagramme (BPF), Activity-Diagramme (Use-Cases).
   - `*.bpmn` — BPMN 2.0 fuer komplexere Prozesse (optional).
   - `*.rules.yaml` — Business-Rules-DSL (optional, fuer regelbasierte Validierungen).

Beide Wege koennen kombiniert werden: Freitext fuer den Ueberblick, PlantUML fuer Praezision an kritischen Stellen.

## Output nach `/mda-init`

`/mda-init` schreibt hierher zusaetzlich:

- `00-spec-<slug>.md` — die **Fachspec** (einzige fachliche Quelle nach Initialisierung).
- ggf. normalisierte/ergaenzte `*.puml`-Dateien, damit Code und Modell synchron bleiben.

## Danach

Weitere fachliche Aenderungen laufen **nicht** hier, sondern ueber `/mda-plan` → `specs/features/<slug>.md`. Die Dateien unter `specs/model/` werden nur durch `/mda-init` oder explizite Re-Modellierungen angefasst.
