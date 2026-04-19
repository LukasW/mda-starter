---
name: mda-ship
description: Schliesst die Arbeit am aktuellen Feature-Branch ab — wartet auf gruene CI, merged den PR (Squash), schliesst das verknuepfte Issue (falls vorhanden), raeumt Branch und ggf. Git-Worktree auf. Laeuft nach `/mda-implement`. Setzt die DoD aus `_shared/dod-checklist.md` als finales Gate durch. Wird ausgeloest bei "mda-ship", "merge ab", "schliess ab", "release feature".
tools: Read, Glob, Grep, Bash, Edit, TaskCreate, TaskUpdate, TaskList, TaskGet
---

# MDA Ship — Feature abschliessen

Abschlussphase des Feature-Workflows. Merged sauber und raeumt lokale Artefakte auf.

> Pflichtlektuere:
> - `../_shared/dod-checklist.md` — finales Gate.
> - `../_shared/git-detection.md` — Git-CLI-Detection.

## Vorbedingungen

- PR des aktuellen Branch existiert (erstellt von `/mda-implement`).
- Keine uncommitteten Aenderungen.

## Ablauf

### 1. Git-Host ermitteln

Lies `../_shared/git-detection.md`. Halte `gh`/`glab`-CLI im Kontext.

### 2. Branch & Remote

```bash
BRANCH=$(git branch --show-current)
[ "$BRANCH" = "main" ] && echo "ERROR: auf main — nichts zu mergen" && exit 1

git status --porcelain | grep . && echo "ERROR: uncommittete Aenderungen" && exit 1

git push   # falls noch nicht gepusht
```

### 3. DoD-Gate

- `./scripts/count-tests.sh` → gruen.
- `../_shared/dod-checklist.md` durchgehen. Jeder anwendbare Punkt erfuellt? Sonst abbrechen, Liste offener Punkte an Nutzer.

### 4. CI abwarten

```bash
gh pr checks "$BRANCH" --watch --interval 30
```

Bei Fehlschlag: Abbruch, Nutzer informieren, **nicht** mergen.

### 5. PR mergen (Squash)

```bash
PR_NR=$(gh pr view "$BRANCH" --json number -q .number)
gh pr merge "$PR_NR" --squash --delete-branch
```

Squash, damit auf `main` pro Feature genau **ein** Commit landet.

### 6. Issue schliessen

- Issue-Nummer aus Branch-Name extrahieren (`feature/<slug>` — falls Slug mit `<nr>-...` beginnt).
- Falls nicht automatisch ueber `Closes #N` geschlossen:
  ```bash
  gh issue close "$ISSUE_NR" --comment "Geschlossen durch PR #$PR_NR"
  ```

### 7. Lokal aufraeumen

Ist es ein Worktree?

```bash
if git rev-parse --show-toplevel | grep -q '\.trees/'; then
  IS_WORKTREE=1
else
  IS_WORKTREE=0
fi
```

- **Worktree**:
  ```bash
  MAIN=$(git worktree list --porcelain | awk '/^worktree/ {print $2; exit}')
  cd "$MAIN"
  git worktree remove ".trees/feature/$SLUG"
  git worktree prune
  ```
- **Normaler Branch**:
  ```bash
  git checkout main && git pull --ff-only
  git branch -d "$BRANCH"   # -d, nicht -D — sauberer Merge vorausgesetzt
  ```

### 8. Plan-Artefakte aufraeumen (optional)

- `plan/<slug>.md` bleibt als Historie.
- `/tmp/mda-feature-plan.json` kann entfernt werden.

### 9. Bestaetigung

Zeige:
- PR-Nummer + Merge-Status.
- Issue-Nummer + Close-Status.
- Branch-Status (geloescht ja/nein).
- Worktree-Status (entfernt ja/nein).

## Fehlerbilder

- **CI rot** → Abbruch, Nutzer muss Fix per `/mda-implement` nachschieben.
- **PR bereits gemerged** → Skip zu Schritt 6/7 (idempotent).
- **Branch hat unpublizierte Commits** → Push vorschlagen, Nutzer fragen.
- **`git branch -d` schlaegt fehl** → Branch nicht sauber gemerged. Diff zeigen, Nutzer fragen, ob `-D` (destruktiv, nur nach Bestaetigung).

## Regeln

- **Kein `--force`** ohne explizite Nutzerzustimmung.
- **Kein Merge auf `main` direkt** — immer ueber PR.
- **Squash** ist Pflicht.
- Destruktive Operationen (`git branch -D`, `rm -rf`) nur auf explizite Nutzerzustimmung.
