#!/usr/bin/env bash
# Blockt destruktive Bash-Kommandos bevor sie ausgefuehrt werden.
# Exit 2 = Abbruch mit stderr-Nachricht an Claude.

set -euo pipefail

payload="$(cat)"
tool_name="$(printf '%s' "$payload" | jq -r '.tool_name // empty')"
[ "$tool_name" != "Bash" ] && exit 0

cmd="$(printf '%s' "$payload" | jq -r '.tool_input.command // empty')"
[ -z "$cmd" ] && exit 0

block() {
  printf 'BASH-SAFEGUARD: %s\n' "$1" >&2
  printf 'Kommando: %s\n' "$cmd" >&2
  printf 'Wenn wirklich gewollt: Nutzer um explizite Freigabe bitten.\n' >&2
  exit 2
}

# Hooks bypassen
printf '%s' "$cmd" | grep -qE '(^|[[:space:]])--no-verify([[:space:]]|$)' \
  && block "--no-verify nicht erlaubt"
printf '%s' "$cmd" | grep -qE '(^|[[:space:]])--no-gpg-sign([[:space:]]|$)' \
  && block "GPG-Sign-Bypass nicht erlaubt"

# Force-push auf main/master
printf '%s' "$cmd" | grep -qE 'git[[:space:]]+push.*(--force|--force-with-lease|-f[[:space:]]).*\b(main|master)\b' \
  && block "Force-push auf main/master"

# Hart-Reset auf der Hauptlinie
printf '%s' "$cmd" | grep -qE 'git[[:space:]]+reset[[:space:]]+--hard' \
  && block "git reset --hard ist destruktiv"

# Rabiate rm -rf Ziele
printf '%s' "$cmd" | grep -qE 'rm[[:space:]]+-[a-zA-Z]*r[a-zA-Z]*f?[[:space:]]+/([[:space:]]|$)' \
  && block "rm -rf auf Root"
printf '%s' "$cmd" | grep -qE 'rm[[:space:]]+-[a-zA-Z]*r[a-zA-Z]*f?[[:space:]]+([.~]|\$HOME)([[:space:]]|$|/)' \
  && block "rm -rf auf Home/CWD-Root"
printf '%s' "$cmd" | grep -qE 'rm[[:space:]]+-[a-zA-Z]*r[a-zA-Z]*f?[[:space:]]+src(/main)?([[:space:]]|$|/\*?$)' \
  && block "rm -rf auf src/"
printf '%s' "$cmd" | grep -qE 'rm[[:space:]]+.*specs(/|[[:space:]]|$)' \
  && block "rm auf specs/"
printf '%s' "$cmd" | grep -qE 'rm[[:space:]]+.*plan/([[:space:]]|$)' \
  && block "rm auf plan/"
printf '%s' "$cmd" | grep -qE 'rm[[:space:]]+.*db/migration/V[0-9]+__' \
  && block "Flyway-Migration darf nicht geloescht werden"

# Branch-Force-Delete
printf '%s' "$cmd" | grep -qE 'git[[:space:]]+branch[[:space:]]+-D' \
  && block "git branch -D (destruktiv) — nutze -d oder frage zuerst"

# Tests skippen
printf '%s' "$cmd" | grep -qE 'mvn(w)?[[:space:]].*-DskipTests' \
  && block "Tests ueberspringen widerspricht DoD"

exit 0
