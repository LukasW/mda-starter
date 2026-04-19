# Feature-Spec-Template

Die Datei `specs/features/<slug>.md` ist der **einzige** Eingabe-Schnitt fuer `mda-plan` und `mda-implement`. Egal ob du sie vorher per Hand schreibst oder `mda-plan` sie aus Freitext extrahiert — am Ende liegt genau diese Form im Repo.

## Minimalbeispiel

> **Hinweis:** Das Beispiel verwendet eine neutrale Demo-Domaene (`Auftrag`/`order`). Die projekteigene Fachsprache steht in `specs/model/*.md` (siehe CLAUDE.md).

```markdown
---
slug: auftrag-notiz
title: Notiz beim Bestaetigen
bounded_context: order
kind: add-field
affects_aggregate: Auftrag
created: 2026-04-19
author: beispiel
---

## Warum
Bearbeiter moechten beim Bestaetigen eine optionale Notiz hinterlegen.

## Fachliche Regel
Beim Bestaetigen darf — optional — eine Notiz (max. 2000 Zeichen) mitgegeben werden. Die Notiz ist Teil des Auftrags-Aggregats.

## Use-Cases
### Erweiterung `AuftragBestaetigenUseCase`
- Command: `Command(auftragId, bearbeiterId, notiz?)`
- REST: Feld optional im bestehenden `POST /api/v1/auftraege/{id}/bestaetigen`.

## Daten
- Neues Feld `Auftrag.bestaetigungsNotiz: String?` (nullable, max 2000).
- Flyway `V3__auftrag_bestaetigungsnotiz.sql`:
  `ALTER TABLE auftrag ADD COLUMN bestaetigungs_notiz VARCHAR(2000) NULL;`

## UI
- `auftrag-detail` bekommt Textarea fuer Notiz, read-only ab Status `VERSANDT`.

## Tests
- Unit: `auftrag_speichert_bestaetigungsnotiz`.
- Integration: `AuftragResourceTest` — POST bestaetigen mit Notiz.
- BDD `@service`: "Bearbeiter bestaetigt mit Notiz".

## Rollback
- Rollback-Migration `V<n>__undo_bestaetigungsnotiz.sql`: `DROP COLUMN`.
- Feld aus `Auftrag.java` + JPA-Entity entfernen. DTO/UI zurueck.
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
