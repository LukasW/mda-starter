#!/usr/bin/env bash
# Erzwingt Drift-Guards aus .claude/skills/_shared/drift-guards.md
# Eingabe: Hook-JSON auf stdin. Exit 2 blockt den Tool-Call und gibt stderr an Claude.

set -euo pipefail

payload="$(cat)"
tool_name="$(printf '%s' "$payload" | jq -r '.tool_name // empty')"
file_path="$(printf '%s' "$payload" | jq -r '.tool_input.file_path // empty')"

[ -z "$file_path" ] && exit 0
[ "$tool_name" != "Edit" ] && [ "$tool_name" != "Write" ] && [ "$tool_name" != "NotebookEdit" ] && exit 0

repo_root="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
rel_path="${file_path#"$repo_root/"}"

block() {
  printf 'DRIFT-GUARD: %s\n' "$1" >&2
  printf 'Datei: %s\n' "$rel_path" >&2
  printf 'Regel: %s\n' "$2" >&2
  printf 'Siehe: .claude/skills/_shared/drift-guards.md\n' >&2
  exit 2
}

# 1. Existierende Flyway-Migration editieren -> hart blocken (nur Edit; Write einer neuen V<n> ist OK).
if [[ "$rel_path" =~ src/main/resources/db/migration/V[0-9]+__.*\.sql$ ]]; then
  if [ "$tool_name" = "Edit" ] && [ -f "$file_path" ]; then
    block "Flyway-Migration ist unveraenderlich" "neue additive V<n>__<slug>.sql anlegen, bestehende nie editieren"
  fi
fi

# 2. ArchitectureTest aufweichen: Keywords 'ignore', 'disable', 'allow' in Edit-Payload.
if [[ "$rel_path" =~ ArchitectureTest\.java$ ]] && [ "$tool_name" = "Edit" ]; then
  old_string="$(printf '%s' "$payload" | jq -r '.tool_input.old_string // empty')"
  new_string="$(printf '%s' "$payload" | jq -r '.tool_input.new_string // empty')"
  # Wenn eine Regel entfernt wird (old nicht leer, new leer oder kuerzer und Keyword verschwindet).
  if printf '%s' "$old_string" | grep -qE 'should[A-Z]|noClasses|ArchRule|classes\(\)'; then
    if ! printf '%s' "$new_string" | grep -qE 'should[A-Z]|noClasses|ArchRule|classes\(\)'; then
      block "ArchUnit-Regel aufgeweicht/entfernt" "Regel bleibt stabil; neue BC-Namespaces erweitern, bestehende nicht entfernen"
    fi
  fi
fi

# 3. sealed-permits-Liste verkuerzt.
if [[ "$rel_path" =~ \.java$ ]] && [ "$tool_name" = "Edit" ]; then
  old_string="$(printf '%s' "$payload" | jq -r '.tool_input.old_string // empty')"
  new_string="$(printf '%s' "$payload" | jq -r '.tool_input.new_string // empty')"
  if printf '%s' "$old_string" | grep -q 'permits '; then
    old_count="$(printf '%s' "$old_string" | grep -oE 'permits [^{;]+' | tr ',' '\n' | wc -l | tr -d ' ')"
    new_count="$(printf '%s' "$new_string" | grep -oE 'permits [^{;]+' | tr ',' '\n' | wc -l | tr -d ' ')"
    if [ "$new_count" -lt "$old_count" ]; then
      block "sealed permits-Subtyp entfernt" "permits nur erweitern, nie verkleinern (Kafka/Outbox-Kompatibilitaet)"
    fi
  fi
fi

# 4. manual-edits-below-Marker geloescht oder nach oben verschoben.
if [[ "$rel_path" =~ \.java$ ]] && [ "$tool_name" = "Edit" ]; then
  old_string="$(printf '%s' "$payload" | jq -r '.tool_input.old_string // empty')"
  new_string="$(printf '%s' "$payload" | jq -r '.tool_input.new_string // empty')"
  if printf '%s' "$old_string" | grep -q 'mda-generator: manual-edits-below'; then
    if ! printf '%s' "$new_string" | grep -q 'mda-generator: manual-edits-below'; then
      block "manual-edits-below-Marker entfernt" "Marker bleibt; unterhalb wird Code nicht regeneriert"
    fi
  fi
fi

# 5. REST-Pfad-Rename ohne v2-Bump (Heuristik: Edit aendert @Path("/api/v1/...").
if [[ "$rel_path" =~ adapter/in/rest/.*Resource\.java$ ]] && [ "$tool_name" = "Edit" ]; then
  old_string="$(printf '%s' "$payload" | jq -r '.tool_input.old_string // empty')"
  new_string="$(printf '%s' "$payload" | jq -r '.tool_input.new_string // empty')"
  old_path="$(printf '%s' "$old_string" | grep -oE '@Path\("/api/v[0-9]+/[^"]+"' | head -1 || true)"
  new_path="$(printf '%s' "$new_string" | grep -oE '@Path\("/api/v[0-9]+/[^"]+"' | head -1 || true)"
  if [ -n "$old_path" ] && [ -n "$new_path" ] && [ "$old_path" != "$new_path" ]; then
    if ! printf '%s' "$new_path" | grep -q '/api/v2/'; then
      block "REST-Pfad umbenannt ohne Versions-Bump" "neue @Path-Methode hinzufuegen oder /api/v2/ verwenden"
    fi
  fi
fi

exit 0
