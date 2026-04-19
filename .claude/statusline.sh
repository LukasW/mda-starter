#!/usr/bin/env bash
# Statusline fuer mda-starter.
# Format: <model> | <branch> | <feature-status>
# Feature-Status: plan ✓ wenn specs/features/<slug>.md existiert; impl ● wenn auf feature/<slug>-Branch; ship - solange PR offen.

set -euo pipefail
payload="$(cat)"

cwd="$(printf '%s' "$payload" | jq -r '.workspace.current_dir // .cwd // empty')"
model="$(printf '%s' "$payload" | jq -r '.model.display_name // .model.id // "claude"')"
[ -n "$cwd" ] && cd "$cwd" 2>/dev/null || true

branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo '-')"

slug=""
plan_mark="-"
impl_mark="-"
ship_mark="-"

case "$branch" in
  feature/*)
    slug="${branch#feature/}"
    impl_mark="●"
    ;;
esac

if [ -n "$slug" ]; then
  if [ -f "specs/features/${slug}.md" ]; then plan_mark="✓"; fi
  if [ -f "plan/${slug}.md" ]; then plan_mark="✓"; fi
fi

# Dirty-Indicator
dirty=""
if [ -n "$(git status --porcelain 2>/dev/null)" ]; then dirty="*"; fi

if [ -n "$slug" ]; then
  printf '%s | %s%s | mda:%s [plan %s | impl %s | ship %s]' \
    "$model" "$branch" "$dirty" "$slug" "$plan_mark" "$impl_mark" "$ship_mark"
else
  printf '%s | %s%s' "$model" "$branch" "$dirty"
fi
