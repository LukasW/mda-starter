---
name: mda-fast
description: Schnellstarter — plant, implementiert und shippt ein Feature in einem Durchlauf. Eingabe ist eine Beschreibung (Freitext oder Pfad zu `specs/features/<slug>.md`). Kettet `mda-plan` → Plan-Bestaetigung (nur bei Heuristik-Treffer) → `mda-implement` → `mda-ship`. Fuer nicht-triviale Features ist `mda-plan` + `mda-implement` manuell besser. Wird ausgeloest bei "mda-fast", "schnell umsetzen", "full feature-run".
argument-hint: <beschreibung-oder-slug> [--worktree]
tools: Read, Glob, Grep, Bash, Edit, Agent, TaskCreate, TaskUpdate, TaskList, TaskGet
---

# MDA Fast — Feature im Durchlauf

Kette `mda-plan` → (optional Bestaetigung) → `mda-implement` → `mda-ship`. Automatisch, mit definierten Stopps.

**Eingabe:** `$ARGUMENTS`
- Freitext-Beschreibung ODER
- Pfad/Slug zu einer bestehenden `specs/features/<slug>.md`.
- Optional `--worktree` fuer isolierte Umsetzung in `.trees/feature/<slug>`.

## Vorbedingungen

Gleiche wie `mda-plan` + `mda-implement` + `mda-ship`:
- MDA-Projekt (aus `mda-init`).
- Sauberer Arbeitsbaum.
- `gh`/`glab` verfuegbar (siehe `../_shared/git-detection.md`).

## Schritt 0 — Duplikat-Pruefung

- Codebase-Scan (Grep/Glob) auf Hinweise, dass das Feature bereits existiert.
- Issues/Pull-Requests durchsuchen:
  ```bash
  gh issue list --state all --search "<keyword>" --limit 5
  gh pr list   --state all --search "<keyword>" --limit 5
  ```

**Bei Duplikat**: Klare Meldung (Datei/Komponente + Issue/PR-Link), sofort abbrechen. **Kein neues Feature**.

## Schritt 1 — Plan erzeugen

Rufe `mda-plan` (Skill) mit der Eingabe auf. Ergebnis:
- `specs/features/<slug>.md`
- `plan/<slug>.md`
- `/tmp/mda-feature-plan.json`

## Schritt 2 — Plan-Bestaetigung (nur wenn noetig)

Normalerweise ueberspringt `mda-fast` die Bestaetigung. **Lege den Plan vor + warte**, wenn mindestens einer der folgenden Trigger zutrifft:

- Spec hat `kind: new-aggregate` oder `cross-cutting`.
- Plan beruehrt **mehr als eine Schicht** (Domain + Adapter + Frontend).
- Plan enthaelt **offene Fragen** oder mehrere valide Loesungsansaetze.
- Migration (Flyway), BPF-/Workflow-Aenderungen, Auth/Security, Breaking Changes beruehrt.
- Plan-Datei groesser als 40 Zeilen nach dem ersten Entwurf.
- Drift-Guard-Findings vorhanden.

Sonst: direkt weiter.

## Schritt 3 — Implementieren

Rufe `mda-implement <slug> [--worktree]`. Inklusive Branch-Erstellung, Tests, Reviewer, Commit, PR.

## Schritt 4 — Shippen

Rufe `mda-ship`. Inklusive CI-Warten, Squash-Merge, Issue-Close, Cleanup.

## Regeln

- Automatischer Durchlauf. Bei **echten** Unklarheiten interaktiv nachfragen (eine Frage pro Turn, mit Empfehlung + Pro/Contra).
- Bei zutreffender Heuristik (Schritt 2): Plan-Bestaetigung einholen.
- Fehler in einem Schritt: melden + abbrechen; nicht blind weitermachen.
- **Duplikat** → sofortiger Abbruch.
- **`--force`**, `git reset --hard`, `--no-verify` nur auf explizite Nutzerzustimmung.

## Warum es nicht immer der richtige Weg ist

Fuer grosse Features (Cross-Cutting, neue Aggregates, Schema-Breaking) ist `mda-plan` allein oft besser: man kann iterativ am Plan feilen, bevor Code entsteht. `mda-fast` ist fuer klare, eng umgrenzte Aenderungen (add-field, add-usecase, new-screen).
