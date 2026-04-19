#!/usr/bin/env bash
# Testpyramide fuer MDA-Starter zaehlen.
#   - Integration = Testklassen mit @QuarkusTest
#   - Unit        = Testklassen mit @Test, die weder @QuarkusTest noch ArchitectureTest sind
#   - BDD         = Feature-Dateien unter src/test/resources/features/{service,process,rules,ui}

root="$(cd "$(dirname "$0")/.." && pwd)"
tests="$root/src/test/java"
features="$root/src/test/resources/features"

classify() {
  local file="$1"
  case "$file" in *"/bdd/"*) echo bdd; return ;; esac
  if grep -q '@QuarkusTest' "$file" 2>/dev/null; then echo integration; return; fi
  if grep -q 'ArchitectureTest' "$file" 2>/dev/null; then echo architecture; return; fi
  if grep -qE '(@Test|@ParameterizedTest)' "$file" 2>/dev/null; then echo unit; return; fi
  echo other
}

unit=0
integration=0
arch=0
while IFS= read -r f; do
  case "$(classify "$f")" in
    unit)         unit=$((unit+1)) ;;
    integration)  integration=$((integration+1)) ;;
    architecture) arch=$((arch+1)) ;;
  esac
done < <(find "$tests" -name '*.java' -type f 2>/dev/null | sort -u)

count_features() { find "$1" -name '*.feature' -type f 2>/dev/null | wc -l | tr -d ' '; }
bdd_service=$(count_features "$features/service")
bdd_process=$(count_features "$features/process")
bdd_rules=$(count_features "$features/rules")
bdd_ui=$(count_features "$features/ui")

printf 'Unit tests:        %s\n' "$unit"
printf 'Architecture tests:%s\n' "$arch"
printf 'Integration tests: %s (@QuarkusTest)\n' "$integration"
printf 'BDD service:       %s feature(s)\n' "$bdd_service"
printf 'BDD process:       %s feature(s)\n' "$bdd_process"
printf 'BDD rules:         %s feature(s)\n' "$bdd_rules"
printf 'BDD ui:            %s feature(s)\n' "$bdd_ui"

bdd_srp=$((bdd_service + bdd_process + bdd_rules))
fail=0
[ "$unit" -lt $((2 * integration)) ] && fail=1
[ $((2 * integration)) -lt $((4 * bdd_srp)) ] && fail=1
[ $((4 * bdd_srp)) -lt $((4 * bdd_ui)) ] && fail=1

if [ "$fail" = "0" ]; then
  echo "Pyramide: OK"
  exit 0
fi
echo "Pyramide: FAIL (unit>=2*integration>=4*(service+process+rules)>=4*ui)"
exit 1
