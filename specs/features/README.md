# `specs/features/`

Hier liegt **eine Markdown-Datei pro Feature** — der Audit-Trail der fachlichen Weiterentwicklung.

- Dateiformat: `<kebab-slug>.md`, Front-Matter + Pflicht-Sektionen gemaess `.claude/skills/_shared/feature-spec-template.md`.
- Workflow: `/mda-plan <beschreibung>` legt Spec + Plan an → `/mda-implement <slug>` setzt um → `/mda-ship` merged.
- **Nicht loeschen**: bestehende Specs bleiben als Historie erhalten, auch wenn das Feature spaeter erweitert oder rueckgebaut wird.

Ersterfassung des Projekts ist nicht hier, sondern ueber `/mda-init` (Input: Beschreibung oder `specs/model/*.puml`).
