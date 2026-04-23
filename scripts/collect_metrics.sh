#!/usr/bin/env bash
# collect_metrics.sh — FizzBuzzEnterpriseEdition metric collection driver
# Usage: ./scripts/collect_metrics.sh [pre|post|pre-verify]
# Writes results to measurements/metrics_<LABEL>.md
# Run from repository root.

set -uo pipefail

LABEL=${1:-"unknown"}
OUT="measurements/metrics_${LABEL}.md"
mkdir -p measurements

echo "# Metrics Collection: $LABEL" > "$OUT"
echo "Date: $(date)" >> "$OUT"
echo "Git commit: $(git rev-parse HEAD)" >> "$OUT"
echo "Git branch: $(git branch --show-current)" >> "$OUT"
echo "" >> "$OUT"

# ── FR-14: Lines of Code ─────────────────────────────────────────────────────
echo "## FR-14: Lines of Code (cloc)" >> "$OUT"
if [ -d "resources" ]; then
  cloc src/ resources/ pom.xml build.gradle 2>/dev/null >> "$OUT" || true
else
  cloc src/ pom.xml build.gradle 2>/dev/null >> "$OUT" || true
fi
echo "" >> "$OUT"

# ── FR-15: Class and interface counts ────────────────────────────────────────
echo "## FR-15: Class and Interface Counts" >> "$OUT"
echo "Total Java files (src/main): $(find src/main -name '*.java' 2>/dev/null | wc -l | tr -d ' ')" >> "$OUT"
echo "Interfaces:       $(grep -rl '^public interface' src/main/java 2>/dev/null | wc -l | tr -d ' ')" >> "$OUT"
echo "Abstract classes: $(grep -rl 'abstract class' src/main/java 2>/dev/null | wc -l | tr -d ' ')" >> "$OUT"
echo "Enums:            $(grep -rl '^public enum\|^enum ' src/main/java 2>/dev/null | wc -l | tr -d ' ')" >> "$OUT"
echo "" >> "$OUT"

# ── FR-16 + M-D: PMD (cyclomatic complexity + coupling) ─────────────────────
echo "## FR-16 + M-D: PMD Analysis" >> "$OUT"
mvn pmd:pmd -q 2>/dev/null || true
if [ -f target/pmd.xml ]; then
  python3 - <<'PYEOF' >> "$OUT" 2>/dev/null || echo "(PMD XML parse failed)" >> "$OUT"
import xml.etree.ElementTree as ET, re
tree = ET.parse('target/pmd.xml')
cc, ce = [], []
for v in tree.findall('.//violation'):
    rule = v.get('rule', '')
    msg  = v.text or ''
    if rule == 'CyclomaticComplexity':
        m = re.search(r'complexity of (\d+)', msg)
        if m: cc.append(int(m.group(1)))
    elif rule == 'CouplingBetweenObjects':
        m = re.search(r'(\d+) dependencies', msg)
        if m: ce.append(int(m.group(1)))
print(f"CC: count={len(cc)} sum={sum(cc) if cc else 0} max={max(cc) if cc else 0} avg={sum(cc)/len(cc):.2f if cc else 0:.2f}")
print(f"Ce: classes={len(ce)} sum={sum(ce) if ce else 0} max={max(ce) if ce else 0} avg={sum(ce)/len(ce):.2f if ce else 0:.2f}")
PYEOF
else
  echo "(target/pmd.xml not found — run mvn pmd:pmd first)" >> "$OUT"
fi
echo "" >> "$OUT"

# ── FR-17: Build time (5 runs) ───────────────────────────────────────────────
echo "## FR-17: Build Time (mvn clean package -DskipTests, 5 runs)" >> "$OUT"
for i in 1 2 3 4 5; do
  mvn clean -q 2>/dev/null
  result=$( { time mvn package -DskipTests -q 2>/dev/null; } 2>&1 | grep real || echo "real 0m0.000s" )
  echo "Run $i: $result" >> "$OUT"
done
echo "" >> "$OUT"

# ── FR-18 + FR-13: Test time + JaCoCo (5 runs) ───────────────────────────────
echo "## FR-18: Test Execution Time (mvn test, 5 runs)" >> "$OUT"
for i in 1 2 3 4 5; do
  mvn clean -q 2>/dev/null
  result=$( { time mvn test -q 2>/dev/null; } 2>&1 | grep real || echo "real 0m0.000s" )
  echo "Run $i: $result" >> "$OUT"
done
echo "" >> "$OUT"
echo "## FR-13: JaCoCo Instruction Coverage" >> "$OUT"
if [ -f target/site/jacoco/jacoco.xml ]; then
  python3 - <<'PYEOF' >> "$OUT" 2>/dev/null || echo "(JaCoCo XML parse failed)" >> "$OUT"
import xml.etree.ElementTree as ET
tree = ET.parse('target/site/jacoco/jacoco.xml')
for c in tree.findall('.//counter[@type="INSTRUCTION"]'):
    covered = int(c.get('covered', 0)); missed = int(c.get('missed', 0))
    total = covered + missed
    pct = 100.0 * covered / total if total else 0
    print(f"covered={covered} missed={missed} total={total} pct={pct:.2f}%")
PYEOF
else
  echo "(target/site/jacoco/jacoco.xml not found — run mvn test first)" >> "$OUT"
fi
echo "" >> "$OUT"

# ── FR-19: Package depth ─────────────────────────────────────────────────────
echo "## FR-19: Package Depth" >> "$OUT"
depth=$(find src/main/java -name "*.java" 2>/dev/null | \
  sed 's|src/main/java/||; s|/[^/]*\.java$||' | \
  awk -F'/' '{print NF}' | sort -rn | head -1)
echo "Maximum package depth: ${depth:-0}" >> "$OUT"
echo "" >> "$OUT"

# ── FR-20: Runtime dependency count ──────────────────────────────────────────
echo "## FR-20: Runtime Dependency Count" >> "$OUT"
count=$(mvn dependency:list -DincludeScope=compile -q 2>/dev/null | \
  grep ":compile" | grep -v "FizzBuzz" | wc -l | tr -d ' ')
echo "Compile-scope dependencies: $count" >> "$OUT"
echo "" >> "$OUT"

# ── FR-21: Public method count ───────────────────────────────────────────────
echo "## FR-21: Public Method Count" >> "$OUT"
count=$(grep -rn "public [a-zA-Z].*(" src/main/java --include="*.java" 2>/dev/null | \
  grep -v "\bclass\b\|\binterface\b\|\benum\b\|//\|^\s*/\*" | wc -l | tr -d ' ')
echo "Approximate public method count: $count" >> "$OUT"
echo "" >> "$OUT"

# ── FR-22: DI annotation count ───────────────────────────────────────────────
echo "## FR-22: DI Annotation Count" >> "$OUT"
for ann in "@Service" "@Autowired" "@Component" "@Repository" "@Controller" "@PostConstruct"; do
  c=$(grep -rh "$ann" src/main/java --include="*.java" 2>/dev/null | wc -l | tr -d ' ')
  echo "$ann: $c" >> "$OUT"
done
total=$(grep -rh "@Service\|@Autowired\|@Component\|@Repository\|@Controller\|@PostConstruct" \
  src/main/java --include="*.java" 2>/dev/null | wc -l | tr -d ' ')
echo "Total: $total" >> "$OUT"
echo "" >> "$OUT"

# ── M-A: JAR artifact size ───────────────────────────────────────────────────
echo "## M-A: JAR Artifact Size" >> "$OUT"
jar_file=$(ls target/*.jar 2>/dev/null | head -1)
if [ -n "$jar_file" ]; then
  size=$(stat -f "%z" "$jar_file" 2>/dev/null || stat --printf="%s" "$jar_file" 2>/dev/null || echo "unknown")
  echo "$jar_file: $size bytes" >> "$OUT"
else
  echo "(No JAR found — run mvn package first)" >> "$OUT"
fi
echo "" >> "$OUT"

# ── M-B: Application execution time (6 runs, discard run 1) ─────────────────
echo "## M-B: Application Execution Time (java -jar, 6 runs; run 1 discarded)" >> "$OUT"
if [ -n "${jar_file:-}" ] && [ -f "$jar_file" ]; then
  for i in 1 2 3 4 5 6; do
    result=$( { time java -jar "$jar_file" > /dev/null; } 2>&1 | grep real || echo "real 0m0.000s" )
    echo "Run $i: $result" >> "$OUT"
  done
else
  echo "(No JAR available)" >> "$OUT"
fi
echo "" >> "$OUT"

# ── M-E: Spring ApplicationContext instantiation count ───────────────────────
echo "## M-E: Spring ApplicationContext Instantiation Sites" >> "$OUT"
count=$(grep -rn "ClassPathXmlApplicationContext\|new.*ApplicationContext" src/main/java \
  --include="*.java" 2>/dev/null | wc -l | tr -d ' ')
echo "Count: $count" >> "$OUT"
grep -rn "ClassPathXmlApplicationContext\|new.*ApplicationContext" src/main/java \
  --include="*.java" 2>/dev/null >> "$OUT" || true
echo "" >> "$OUT"

# ── M-F: Test metrics ────────────────────────────────────────────────────────
echo "## M-F: Test Metrics" >> "$OUT"
test_count=$(grep -rn '@Test' src/test/ 2>/dev/null | wc -l | tr -d ' ')
echo "@Test methods: $test_count" >> "$OUT"
cloc src/test/ 2>/dev/null >> "$OUT" || true
echo "" >> "$OUT"

echo "---" >> "$OUT"
echo "Collection complete: $(date)" >> "$OUT"
echo "Output written to: $OUT"
