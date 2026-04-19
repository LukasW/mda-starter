# Feature-Spec-Template

Die Datei `specs/features/<slug>.md` ist der **einzige** Eingabe-Schnitt fuer `mda-plan` und `mda-implement`. Egal ob du sie vorher per Hand schreibst oder `mda-plan` sie aus Freitext extrahiert — am Ende liegt genau diese Form im Repo.

## Minimalbeispiel

```markdown
---
slug: vertrag-kommentar
title: Kommentar beim Einreichen
bounded_context: contract
kind: add-field
affects_aggregate: Vertrag
created: 2026-04-19
author: lukas
---

## Warum
Reviewer moechten den Kontext des Antragstellers beim Einreichen kennen.

## Fachliche Regel
Beim Einreichen darf — optional — ein Kommentar (max. 2000 Zeichen) mitgegeben werden. Der Kommentar ist Teil des Vertrags-Aggregats.

## Use-Cases
### Erweiterung `VertragEinreichenUseCase`
- Command: `Command(vertragId, antragstellerId, kommentar?)`
- REST: Feld optional im bestehenden `POST /api/v1/vertraege/{id}/einreichen`.

## Daten
- Neues Feld `Vertrag.einreichungsKommentar: String?` (nullable, max 2000).
- Flyway `V3__vertrag_einreichungskommentar.sql`:
  `ALTER TABLE vertrag ADD COLUMN einreichungs_kommentar VARCHAR(2000) NULL;`

## UI
- `vertrag-detail` bekommt Textarea fuer Kommentar, read-only ab Status `IN_PRUEFUNG`.

## Tests
- Unit: `vertrag_speichert_einreichungskommentar`.
- Integration: `VertragResourceTest` — POST einreichen mit Kommentar.
- BDD `@service`: "Antragsteller reicht mit Kommentar ein".

## Rollback
- Rollback-Migration `V<n>__undo_einreichungskommentar.sql`: `DROP COLUMN`.
- Feld aus `Vertrag.java` + JPA-Entity entfernen. DTO/UI zurueck.
```

## Pflichtfelder (YAML-Front-Matter)

| Feld | Typ | Bedeutung |
|---|---|---|
| `slug` | kebab-case | Datei- und Migration-Suffix |
| `title` | String | sprechend |
| `bounded_context` | Name eines BC oder `new:<name>` | |
| `kind` | `new-aggregate` / `add-usecase` / `add-bpf-transition` / `add-field` / `new-screen` / `cross-cutting` | steuert Pipeline |
| `affects_aggregate` | String oder leer | Pflicht bei `add-usecase`/`add-field`/`add-bpf-transition` |
| `created` | ISO-Datum | |
| `author` | String | |

## Optionale Front-Matter

- `related_adr: ADR-00XX` — wenn Entwurfsentscheid neu noetig.
- `feature_flag: some.flag` — Toggle.
- `requires_puml_delta: true` — wenn Klassendiagramm in `specs/model/` erweitert werden soll.

## Struktur der Markdown-Sektionen

| Sektion | Pflicht? | Inhalt |
|---|---|---|
| `## Warum` | ja | 2-4 Saetze fachliche Motivation |
| `## Fachliche Regel` | ja | Invariante(n), 1-3 Absaetze |
| `## Use-Cases` | ja | Command/Query-Sicht; neu vs. erweitert |
| `## Daten` | wenn Datenmodell angefasst | Felder + Flyway-Migration |
| `## UI` | wenn Frontend betroffen | Screens, Komponenten, Material-Widgets |
| `## BPF` | wenn `add-bpf-transition` | `from → trigger → to` |
| `## Tests` | ja | Kategorie × Szenario |
| `## Rollback` | ja | Wie rueckgaengig (Migration, Flag) |
| `## Offene Fragen` | optional | |

## Prinzipien

- **Eine Datei = ein Feature**. Orthogonale Aenderungen → mehrere Dateien.
- **Nicht ueberladen**. Eine Umbenennung ist kein neues Aggregat.
- **Git-reviewbar**. PRs referenzieren genau eine Feature-Spec.
- **Audit-Trail**. Alte Specs werden nicht geloescht; sie sind die Projekt-Geschichte.
