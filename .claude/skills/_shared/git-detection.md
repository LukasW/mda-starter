# Git-Host & CLI ermitteln

Einmal pro Session ausfuehren und Ergebnis im Kontext halten.

```bash
git remote get-url origin
```

Host ableiten:
- `github.com` → `gh` CLI (Owner/Repo aus URL ableiten — nie hardcoden).
- `gitlab.com` oder self-hosted GitLab → `glab` CLI.
- Andere → per `git push` + Web-UI, Hinweis an den Nutzer.

Alle PR-/Issue-/CI-Operationen laufen ueber das CLI (keine MCP-Pflicht). Falls `gh`/`glab` nicht verfuegbar, fragt der Skill den Nutzer, ob er das Tool installieren soll.

## Beispielkommandos (`gh`)

```bash
gh issue view <nr> --json title,body,labels,state
gh issue create --title "..." --body "..." --label enhancement
gh pr create --title "..." --body "..." --base main
gh pr checks <nr> --watch
gh pr merge <nr> --squash --delete-branch
```
