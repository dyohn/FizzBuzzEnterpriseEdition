# FizzBuzzEnterpriseEdition — Refactor Experiment Plan
**Document ID:** PLAN_REVISED_2
**Supersedes:** PLAN_REVISED_1
**Branch:** `ai-refactor-experiment`
**Status:** Phase 0 COMPLETE — Phase 1 ready to begin
**Requirements Reference:** REQUIREMENTS_REFINED_1.md
**Testing Reference:** TEST_METRICS_INITIAL.md (incorporated in full)

---

## Overview

This plan describes the ordered execution of five phases required to complete the FizzBuzzEnterpriseEdition refactor experiment. Each phase has defined entry conditions, ordered steps, an exit verification checklist, and a documentation artifact committed to the repository at the end of the phase. No phase may begin until all entry conditions are met and the prior phase's artifact has been committed.

**Changes from PLAN_REVISED_1:**
- Phase 0: PMD plugin updated to include efferent coupling rule (M-D); python3 verification added; collection script created and committed
- Phase 1 & 3: Timing measurements upgraded from 3 to 5 runs; min/median/max/stdev all recorded; six additional metrics added (M-A through M-F)
- Phase 2: `FizzBuzzTest.java` expanded with 10 new test methods (T-01 through T-10); `setUp`/`tearDown` extended to save/restore `System.err`; `captureOutput()` helper extracted
- Appendix A: Measurement record template expanded for new metrics and 5-run timing
- Appendix B: Comparison table expanded with new metrics
- Appendix C (new): Collection script (`scripts/collect_metrics.sh`)
- Appendix D (new): Complete `FizzBuzzTest.java` source

---

## Phase Map

| Phase | Name | Depends On | Output Artifact |
|-------|------|-----------|-----------------|
| Phase 0 | Environment Setup & Measurement Tooling | None | `measurements/phase_00_environment.md` |
| Phase 1 | Pre-Refactor Measurement | Phase 0 complete | `measurements/phase_01_baseline.md` |
| Phase 2 | System Refactor | Phase 1 complete | `measurements/phase_02_refactor_log.md` |
| Phase 3 | Post-Refactor Measurement | Phase 2 complete | `measurements/phase_03_postrefactor.md` |
| Phase 4 | Comparative Analysis & Documentation | Phase 3 complete | `measurements/phase_04_comparison.md` |

---

## Prerequisites

- Git working directory is on branch `ai-refactor-experiment`
- No uncommitted changes in the working tree
- Network access available (Maven dependency resolution)
- A Unix-compatible shell (zsh or bash)
- `java` on the PATH

---

## Phase 0: Environment Setup & Measurement Tooling

### Purpose
Verify all required tools are present and functional, add measurement tooling to the build, create the metrics collection script, confirm the pre-refactor system builds and tests pass, and record the runtime environment.

### Entry Conditions
- Prerequisites above are met
- No prior phases have been executed

### Steps

#### Step 0.1 — Record Environment

```bash
java -version 2>&1
mvn -version
./gradlew --version
python3 --version
echo "OS: $(uname -srm)"
echo "CPU: $(sysctl -n machdep.cpu.brand_string 2>/dev/null || lscpu | grep 'Model name' | cut -d: -f2 | xargs)"
echo "RAM: $(sysctl -n hw.memsize 2>/dev/null || free -h | awk '/^Mem/{print $2}')"
echo "Shell: $SHELL"
echo "Date: $(date)"
git rev-parse HEAD
git branch --show-current
```

Record all output verbatim in `measurements/phase_00_environment.md`.

#### Step 0.2 — Verify Required Tools ✅ COMPLETE

> **Already done.** Verified 2026-04-21.
> - `cloc` 2.08 present
> - `python3` 3.13.9 present
> - `stat` present (macOS built-in)

```bash
# cloc (FR-14)
cloc --version || brew install cloc

# python3 (needed by collection script for JaCoCo XML parsing)
python3 --version || { echo "ERROR: python3 required"; exit 1; }

# stat (for JAR size — M-A)
stat --version 2>/dev/null || stat -f "%z %N" /dev/null   # macOS vs Linux
```

Record the version of each tool. If any are missing, install and re-record.

#### Step 0.3 — Verify Pre-Refactor Build (Maven) ✅ COMPLETE

> **Already done.** `mvn clean package` passes. Build modernization applied to `pom.xml` and `build.gradle`:
> - `maven-compiler-plugin` upgraded 2.3 → 3.11.0
> - `jacoco-maven-plugin` upgraded 0.5.8 → 0.8.11
> - `javax.annotation-api:1.3.2` added (Java 11 compatibility)
> - `jcenter()` replaced with `mavenCentral()` in `build.gradle`
> - `sourceCompatibility`/`targetCompatibility` = `'1.7'` added to `build.gradle`
> - Gradle wrapper upgraded 2.8 → 6.9.4

```bash
mvn clean package
```

Expected: `BUILD SUCCESS`. If this fails, diagnose and resolve before proceeding.

#### Step 0.4 — Verify Pre-Refactor Build (Gradle) ✅ COMPLETE

> **Already done.** `./gradlew clean build` passes. See Step 0.3 notes for changes made.

```bash
./gradlew clean build
```

Expected: `BUILD SUCCESSFUL`. If `jcenter()` fails, replace with `mavenCentral()` in `build.gradle` and document as a pre-existing issue.

#### Step 0.5 — Verify Pre-Refactor Tests Pass ✅ COMPLETE (with deviation)

> **Already done — with a planned deviation.** The test suite has been expanded ahead of schedule
> as part of a requirements audit conducted before Phase 0. `FizzBuzzTest.java` now contains
> 12 tests (T-00 through T-11) rather than the original 1. All 12 pass.
>
> **Deviation from plan:** The plan expected `Tests run: 1` here. The actual result is
> `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`. This is intentional — see
> TEST_METRICS_INITIAL.md §3.1 for the implementation status of all tests.
>
> **T-11 addition:** T-11 (`testOutputOrdering`, FR-02) was added beyond the original
> T-00–T-10 scope. This affects Phase 2 Step 2.3 and Step 2.9 — see notes there.

```bash
mvn test
```

Expected (updated): `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`. All assertions must pass. Do not proceed on any failure.

#### Step 0.6 — Add PMD Plugin to pom.xml ✅ COMPLETE

> **Already done.** `maven-pmd-plugin` 3.21.0 added to `pom.xml` with both
> `CyclomaticComplexity` and `CouplingBetweenObjects` rules configured.

PMD is required for FR-16 (cyclomatic complexity) and M-D (efferent coupling). Add the following inside `<build><plugins>`, after the `jacoco-maven-plugin` entry:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-pmd-plugin</artifactId>
  <version>3.21.0</version>
  <configuration>
    <rulesets>
      <ruleset>category/java/design.xml/CyclomaticComplexity</ruleset>
      <ruleset>category/java/design.xml/CouplingBetweenObjects</ruleset>
    </rulesets>
    <failOnViolation>false</failOnViolation>
    <printFailingErrors>true</printFailingErrors>
  </configuration>
</plugin>
```

> `CouplingBetweenObjects` measures efferent coupling (Ce) — the number of unique external types a class references. This is metric M-D.

#### Step 0.7 — Verify PMD Runs ✅ COMPLETE

> **Already done.** `mvn pmd:pmd` succeeds; `target/pmd.xml` confirmed present.

```bash
mvn pmd:pmd
```

Expected: `BUILD SUCCESS` and `target/pmd.xml` exists with `<pmd>` root element and method-level violation entries.

#### Step 0.8 — Create Collection Script

Create `scripts/collect_metrics.sh` using the source in **Appendix C**. Then make it executable:

```bash
mkdir -p scripts
# (Write file contents from Appendix C)
chmod +x scripts/collect_metrics.sh
```

Verify the script runs without error against the pre-refactor codebase:

```bash
./scripts/collect_metrics.sh pre-verify
```

> This is a dry-run verification only — Phase 1 performs the official baseline collection.

#### Step 0.9 — Create measurements/ Directory and Environment Document

```bash
mkdir -p measurements
```

Create `measurements/phase_00_environment.md` using the template in Appendix A §A.1, filled with results from Steps 0.1–0.8.

#### Step 0.10 — Commit

```bash
git add pom.xml scripts/collect_metrics.sh measurements/phase_00_environment.md
git commit -m "Phase 0: Add PMD/coupling tooling, collection script, record environment"
```

### Exit Verification

- [x] `mvn test` passes with 0 failures — **12 tests, 0 failures** (2026-04-21)
- [x] `mvn pmd:pmd` succeeds and produces `target/pmd.xml` (2026-04-21)
- [x] `cloc --version` outputs a version string — cloc 2.08 (2026-04-21)
- [x] `python3 --version` outputs a version string — Python 3.13.9 (2026-04-21)
- [x] `scripts/collect_metrics.sh` is executable and committed — commit 99653c6 (2026-04-22)
- [x] `measurements/phase_00_environment.md` is committed — commit 99653c6 (2026-04-22)

---

## Phase 1: Pre-Refactor Measurement

### Purpose
Collect the empirical baseline for all required metrics (FR-13 through FR-22) and all proposed additional metrics (M-A through M-F). All measurements taken on the unmodified pre-refactor codebase.

### Entry Conditions
- Phase 0 complete and artifact committed
- `mvn test` passes with 0 failures
- `target/pmd.xml` present
- No production source files modified since Phase 0

### Statistical Protocol (applies to all timed measurements)

Run each timed command **5 times**. Record all 5 values. Report:
- **Min** — fastest observed
- **Median** — middle value (official reported result)
- **Max** — slowest observed
- **Stdev** — standard deviation (if stdev > 20% of median, collect 5 additional runs and investigate noise)

Do not run background CPU-intensive processes during timing collection. Note the CPU governor mode and any thermal throttling events.

### Steps

---

#### Step 1.1 — FR-18 + FR-13: Test Execution Time and JaCoCo Coverage

Running `mvn test` collects two metrics simultaneously.

```bash
for i in 1 2 3 4 5; do
  mvn clean -q
  { time mvn test -q; } 2>&1 | tee measurements/baseline_test_run${i}.txt
done
```

Record the `real` elapsed time from each run. Compute and record min/median/max/stdev.

The JaCoCo report is generated during the final run. After all 5 runs:

```bash
# Extract instruction coverage
python3 -c "
import xml.etree.ElementTree as ET
tree = ET.parse('target/site/jacoco/jacoco.xml')
for c in tree.findall('.//counter[@type=\"INSTRUCTION\"]'):
    covered = int(c.get('covered', 0))
    missed  = int(c.get('missed', 0))
    total   = covered + missed
    pct     = 100.0 * covered / total if total else 0
    print(f'covered={covered} missed={missed} total={total} pct={pct:.2f}%')
" | tail -1
```

Copy the final JaCoCo report:
```bash
cp target/site/jacoco/jacoco.xml measurements/baseline_jacoco.xml
```

---

#### Step 1.2 — FR-17: Build Time

```bash
for i in 1 2 3 4 5; do
  mvn clean -q
  { time mvn package -DskipTests -q; } 2>&1 | tee measurements/baseline_build_run${i}.txt
done
```

Record `real` time from each run. Compute and record min/median/max/stdev.

---

#### Step 1.3 — FR-16: Cyclomatic Complexity (PMD)

```bash
mvn pmd:pmd -q
cp target/pmd.xml measurements/baseline_pmd.xml
```

Parse `target/pmd.xml` for `CyclomaticComplexity` violations:

```bash
python3 -c "
import xml.etree.ElementTree as ET
tree = ET.parse('target/pmd.xml')
cc_vals = []
for v in tree.findall('.//violation[@rule=\"CyclomaticComplexity\"]'):
    # PMD embeds the CC value in the violation message text
    msg = v.text or ''
    import re
    m = re.search(r'complexity of (\d+)', msg)
    if m:
        cc_vals.append(int(m.group(1)))
if cc_vals:
    print(f'count={len(cc_vals)} sum={sum(cc_vals)} max={max(cc_vals)} avg={sum(cc_vals)/len(cc_vals):.2f}')
else:
    print('No CyclomaticComplexity violations found (CC <= threshold for all methods)')
"
```

Also run the keyword-count approximation for cross-validation:
```bash
grep -rh "\bif\b\|\belse if\b\|\bfor\b\|\bwhile\b\|\bcase\b\|\bcatch\b\|&&\|||" \
  src/main/java --include="*.java" | wc -l
```

---

#### Step 1.4 — FR-14: Lines of Code

```bash
cloc src/ resources/ pom.xml build.gradle \
  --out=measurements/baseline_cloc_summary.txt
cat measurements/baseline_cloc_summary.txt

cloc src/ resources/ pom.xml build.gradle \
  --by-file --out=measurements/baseline_cloc_detail.txt
```

---

#### Step 1.5 — FR-15: Class and Interface Count

```bash
echo "Total Java files (src/main): $(find src/main/java -name '*.java' | wc -l | tr -d ' ')"
echo "Interfaces:      $(grep -rl '^public interface' src/main/java | wc -l | tr -d ' ')"
echo "Abstract classes:$(grep -rl 'abstract class' src/main/java | wc -l | tr -d ' ')"
echo "Enums:           $(grep -rl '^public enum\|^enum ' src/main/java | wc -l | tr -d ' ')"
```

---

#### Step 1.6 — FR-19: Package Depth

```bash
find src/main/java -name "*.java" | \
  sed 's|src/main/java/||; s|/[^/]*\.java$||' | \
  awk -F'/' '{print NF}' | sort -rn | head -1
```

---

#### Step 1.7 — FR-20: Runtime Dependency Count

```bash
mvn dependency:list -DincludeScope=compile -q 2>/dev/null | \
  grep ":compile" | grep -v "FizzBuzz" | wc -l | tr -d ' '
```

---

#### Step 1.8 — FR-21: Public Method Count

```bash
grep -rn "public [a-zA-Z].*(" src/main/java --include="*.java" | \
  grep -v "\bclass\b\|\binterface\b\|\benum\b\|//\|^\s*/\*" | wc -l | tr -d ' '
```

---

#### Step 1.9 — FR-22: DI Annotation Count

```bash
for ann in "@Service" "@Autowired" "@Component" "@Repository" "@Controller" "@PostConstruct"; do
  count=$(grep -rh "$ann" src/main/java --include="*.java" | wc -l | tr -d ' ')
  echo "$ann: $count"
done
echo "Total: $(grep -rh "@Service\|@Autowired\|@Component\|@Repository\|@Controller\|@PostConstruct" \
  src/main/java --include="*.java" | wc -l | tr -d ' ')"
```

---

#### Step 1.10 — M-A: JAR Artifact Size

Requires a package build. If Step 1.2 was the last build, the JAR already exists. Otherwise:

```bash
mvn clean package -DskipTests -q
```

Then:

```bash
ls -l target/*.jar
stat -f "%z bytes: %N" target/*.jar 2>/dev/null || \
  stat --printf="%s bytes: %n\n" target/*.jar
```

Record the size in bytes.

---

#### Step 1.11 — M-B: Application Execution Time

Measures the end-to-end wall-clock time for `java -jar` including JVM startup and Spring context bootstrapping. The first run is discarded (JVM cold start / OS filesystem cache); runs 2–6 are used for statistics.

```bash
for i in 1 2 3 4 5 6; do
  { time java -jar target/*.jar > /dev/null; } 2>&1 | grep real
done
```

Discard run 1. Compute min/median/max/stdev over runs 2–6.

> This metric captures Spring's bootstrap overhead (two `ClassPathXmlApplicationContext` instances per invocation), which is not visible in build time (FR-17) or test time (FR-18).

---

#### Step 1.12 — M-C: Peak Heap Memory Usage

```bash
java -Xmx512m -verbose:gc -jar target/*.jar > /dev/null 2> measurements/baseline_gc.log
# Inspect the GC log for heap usage
grep -E "Heap|used|->|GC" measurements/baseline_gc.log | tail -20
```

For a more precise measurement, if JDK Flight Recorder is available:

```bash
java -Xmx512m -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=measurements/baseline_jfr.jfr \
     -jar target/*.jar > /dev/null
```

Record the peak heap used (MB) as reported by the GC log.

---

#### Step 1.13 — M-D: Efferent Coupling (CouplingBetweenObjects via PMD)

PMD already ran in Step 1.3 and produced `target/pmd.xml`. Extract coupling data:

```bash
python3 -c "
import xml.etree.ElementTree as ET
tree = ET.parse('target/pmd.xml')
ce_vals = []
for v in tree.findall('.//violation[@rule=\"CouplingBetweenObjects\"]'):
    msg = v.text or ''
    import re
    m = re.search(r'(\d+) dependencies', msg)
    if m:
        ce_vals.append(int(m.group(1)))
if ce_vals:
    print(f'classes_with_violations={len(ce_vals)} sum={sum(ce_vals)} max={max(ce_vals)} avg={sum(ce_vals)/len(ce_vals):.2f}')
else:
    print('No CouplingBetweenObjects violations found')
"
```

---

#### Step 1.14 — M-E: Spring ApplicationContext Instantiation Count

```bash
grep -rn "ClassPathXmlApplicationContext\|new.*ApplicationContext" src/main/java \
  --include="*.java"
grep -c "ClassPathXmlApplicationContext\|new.*ApplicationContext" \
  $(find src/main/java -name "*.java") 2>/dev/null || echo 0
```

Record the count and the specific files where each instantiation appears.

---

#### Step 1.15 — M-F: Test Count and Test-to-Production LOC Ratio

```bash
echo "@Test methods: $(grep -rn '@Test' src/test/ | wc -l | tr -d ' ')"
cloc src/test/ 2>/dev/null
echo "Production LOC (from Step 1.4): [carry value from FR-14]"
```

Compute: `test_code_lines / production_code_lines` (carry values from Step 1.4).

---

#### Step 1.16 — Produce and Commit Phase 1 Artifact

Alternatively, run the collection script to verify values match manual steps:

```bash
./scripts/collect_metrics.sh pre
```

Create `measurements/phase_01_baseline.md` using the Measurement Record Template from Appendix A §A.2, populated with all values from Steps 1.1–1.15.

```bash
git add measurements/
git commit -m "Phase 1: Record pre-refactor baseline measurements"
```

### Exit Verification

- [ ] All metrics FR-13 through FR-22 recorded in `measurements/phase_01_baseline.md`
- [ ] All metrics M-A through M-F recorded in `measurements/phase_01_baseline.md`
- [ ] 5 timing runs recorded for FR-17, FR-18, M-B; min/median/max/stdev computed
- [ ] `measurements/baseline_pmd.xml`, `baseline_jacoco.xml`, `baseline_cloc_summary.txt` committed
- [ ] Artifact committed on `ai-refactor-experiment`

---

## Phase 2: System Refactor

### Purpose
Replace the 87-file enterprise implementation with a single-class minimal implementation satisfying all requirements in REQUIREMENTS_REFINED_1.md. The test suite is expanded with 10 new test methods covering requirements not addressed by the original oracle tests.

### Entry Conditions
- Phase 1 complete and artifact committed
- `measurements/phase_01_baseline.md` exists with all metrics populated
- `mvn test` currently passes with 0 failures on the pre-refactor code

### Steps

> **Important:** Follow this order exactly. Steps 2.1–2.3 create new files while old files still exist; do not run `mvn compile` between Steps 2.1 and 2.6 as the build will be in an inconsistent state.

---

#### Step 2.1 — Create the New Production Class

Create:
`src/main/java/com/seriouscompany/business/java/fizzbuzz/packagenamingpackage/impl/FizzBuzz.java`

```java
package com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl;

/**
 * FizzBuzz: minimal single-class replacement for the original 87-file enterprise edition.
 *
 * Behavioral contract (preserved from original):
 *   fizzBuzz(n) loops from 1 to n inclusive and writes to System.out, one entry
 *   per iteration followed by the platform line separator:
 *     "Fizz"     for multiples of 3
 *     "Buzz"     for multiples of 5
 *     "FizzBuzz" for multiples of both 3 and 5
 *     the integer itself otherwise
 *
 * Output mechanism mirrors SystemOutFizzBuzzOutputStrategy (SR-03, SR-04):
 *   System.out.write(bytes) + System.out.flush() per entry, so the test harness's
 *   System.setOut() redirect captures output correctly.
 * IOException is swallowed to match the original adapter behavior (SR-01).
 * Line separator uses System.getProperty("line.separator") to match
 *   NewLineStringReturner and the test's platform-normalization logic (FR-08).
 */
public final class FizzBuzz {

    private FizzBuzz() {}

    /**
     * Runs FizzBuzz from 1 to n inclusive, writing each result to System.out.
     * @param n upper limit (inclusive); must be >= 0 (FR-09: n=0 produces no output)
     */
    public static void fizzBuzz(final int n) {
        final String newLine = System.getProperty("line.separator");
        for (int i = 1; i <= n; i++) {
            final String output;
            if      (i % 3 == 0 && i % 5 == 0) { output = "FizzBuzz"; }  // FR-05
            else if (i % 3 == 0)                { output = "Fizz"; }      // FR-03
            else if (i % 5 == 0)                { output = "Buzz"; }      // FR-04
            else                                { output = Integer.toString(i); } // FR-06
            try {
                System.out.write((output + newLine).getBytes()); // SR-03, SR-09
                System.out.flush();                               // SR-04
            } catch (java.io.IOException e) {
                // swallowed intentionally — matches original adapter behavior (SR-01)
            }
        }
    }

    /**
     * Entry point. Runs FizzBuzz 1 through 100.
     * The value 100 matches DEFAULT_FIZZ_BUZZ_UPPER_LIMIT_PARAMETER_VALUE
     * from the original implementation (FR-11).
     * @param args command-line arguments (ignored)
     */
    public static void main(final String[] args) {
        fizzBuzz(100);
    }
}
```

---

#### Step 2.2 — Rewrite TestConstants.java

Replace `src/test/java/TestConstants.java` entirely. The only change from the original is removal of the two Spring-specific string constants (`STANDARD_FIZZ_BUZZ` and `SPRING_XML`):

```java
/**
 * Constants for FizzBuzz tests.
 * Spring-specific constants (STANDARD_FIZZ_BUZZ, SPRING_XML) removed.
 * All 16 oracle output strings and integer constants are preserved unchanged (FR-12, NFR-05).
 */
public class TestConstants {

    private TestConstants() { super(); }

    static final int INT_1  = 1;  static final int INT_2  = 2;
    static final int INT_3  = 3;  static final int INT_4  = 4;
    static final int INT_5  = 5;  static final int INT_6  = 6;
    static final int INT_7  = 7;  static final int INT_8  = 8;
    static final int INT_9  = 9;  static final int INT_10 = 10;
    static final int INT_11 = 11; static final int INT_12 = 12;
    static final int INT_13 = 13; static final int INT_14 = 14;
    static final int INT_15 = 15; static final int INT_16 = 16;

    static final String _1_ = "1\n";
    static final String _1_2_ = "1\n2\n";
    static final String _1_2_FIZZ = "1\n2\nFizz\n";
    static final String _1_2_FIZZ_4 = "1\n2\nFizz\n4\n";
    static final String _1_2_FIZZ_4_BUZZ = "1\n2\nFizz\n4\nBuzz\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ = "1\n2\nFizz\n4\nBuzz\nFizz\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7 = "1\n2\nFizz\n4\nBuzz\nFizz\n7\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8 = "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ = "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ = "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11 = "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ = "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13 = "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14 = "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14_FIZZ_BUZZ =
        "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14_FIZZ_BUZZ_16 =
        "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz\n16\n";
}
```

---

#### Step 2.3 — Rewrite FizzBuzzTest.java

> **Deviation from original plan:** T-00 through T-11 (12 tests) are already written and
> passing in the current pre-refactor `FizzBuzzTest.java`. **Do not use Appendix D as the
> source** — it contains only T-00 through T-10 (11 tests) and is now outdated.
>
> Instead, adapt the current `FizzBuzzTest.java` by making these changes only:
> - Remove all Spring imports (`ClassPathXmlApplicationContext`, `ApplicationContext`, etc.)
> - Remove the `fb` field
> - Rewrite `setUp()` to only save `System.out` and `System.err` (no Spring context creation)
> - Remove the `doFizzBuzz()` helper and update `testFizzBuzz()` (T-00) to call `captureOutput()`
> - Change all `this.fb.fizzBuzz(n)` calls in `captureOutput()` to `FizzBuzz.fizzBuzz(n)`
>
> All 12 test methods (T-00 through T-11), the `captureOutput()` helper, and the
> `setUp()`/`tearDown()` structure are already correct and carry over unchanged.

**Summary of changes from the original plan:**
- Spring context bootstrapping removed; `FizzBuzz.fizzBuzz(n)` called as a direct static method
- `setUp` / `tearDown` already extended to save and restore `System.err` (T-10) — done in pre-refactor
- `captureOutput(int n)` already extracted — done in pre-refactor
- 12 test methods (T-00 through T-11) already written — carry over from pre-refactor

---

#### Step 2.4 — Delete Old Production Source Directories

```bash
BASE=src/main/java/com/seriouscompany/business/java/fizzbuzz/packagenamingpackage

rm -rf "${BASE}/interfaces"
rm -rf "${BASE}/impl/factories"
rm -rf "${BASE}/impl/loop"
rm -rf "${BASE}/impl/math"
rm -rf "${BASE}/impl/parameters"
rm -rf "${BASE}/impl/printers"
rm -rf "${BASE}/impl/strategies"
rm -rf "${BASE}/impl/stringreturners"
rm -rf "${BASE}/impl/visitors"
rm -f  "${BASE}/impl/ApplicationContextHolder.java"
rm -f  "${BASE}/impl/Constants.java"
rm -f  "${BASE}/impl/Main.java"
rm -f  "${BASE}/impl/StandardFizzBuzz.java"
```

After these deletions, the only production file remaining under `src/main/java/` is `FizzBuzz.java`.

---

#### Step 2.5 — Delete the resources/ Directory

```bash
rm -rf resources/
```

---

#### Step 2.6 — Update pom.xml

**Remove** Spring dependencies; keep only JUnit:
```xml
<dependencies>
  <dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.8.2</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

**Remove** the `<resources>` block from `<build>`:
```xml
<!-- DELETE entire block: -->
<resources>
  <resource>
    <directory>resources/assets/configuration/spring/dependencyinjection/configuration</directory>
    <filtering>true</filtering>
  </resource>
</resources>
```

**Update** `mainClass` in `maven-jar-plugin`:
```xml
<mainClass>com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl.FizzBuzz</mainClass>
```

**Keep unchanged:** `maven-compiler-plugin`, `jacoco-maven-plugin`, `maven-pmd-plugin`.

---

#### Step 2.7 — Update build.gradle

**Remove** Spring entries from `dependencies`:
```groovy
// DELETE:
compile 'org.springframework:spring-aop:3.2.13.RELEASE',
        'org.springframework:spring-beans:3.2.13.RELEASE',
        'org.springframework:spring-context:3.2.13.RELEASE',
        'org.springframework:spring-core:3.2.13.RELEASE',
        'org.springframework:spring-expression:3.2.13.RELEASE'
```

**Remove** `resources` block inside `sourceSets.main`.

**Update** `mainClassName`:
```groovy
mainClassName = 'com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl.FizzBuzz'
```

---

#### Step 2.8 — Compile Verification

```bash
mvn clean compile
```

Expected: `BUILD SUCCESS`, 1 source file compiled.

---

#### Step 2.9 — Test Verification

```bash
mvn test
```

Expected:
```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

> 12 tests = T-00 (`testFizzBuzz`) + T-01 through T-11. Original plan said 11; updated to 12
> because T-11 (`testOutputOrdering`, FR-02) was added during the pre-execution requirements
> audit. See TEST_METRICS_INITIAL.md §3.2 for T-11 details.

All assertions must pass. If any fail, diagnose and fix before proceeding.

---

#### Step 2.10 — Gradle Verification

```bash
./gradlew clean build
```

Expected: `BUILD SUCCESSFUL`.

---

#### Step 2.11 — JAR Execution Spot-Check (V-03)

```bash
java -jar target/FizzBuzzEnterpriseEdition-1.0-SNAPSHOT.jar | head -20
```

Expected first 20 lines:
```
1
2
Fizz
4
Buzz
Fizz
7
8
Fizz
Buzz
11
Fizz
13
14
FizzBuzz
16
17
Fizz
19
Buzz
```

---

#### Step 2.12 — Produce and Commit Phase 2 Artifact

Create `measurements/phase_02_refactor_log.md` documenting:
- Files created (1 production: `FizzBuzz.java`; test files rewritten: 2)
- Files modified (2: `pom.xml`, `build.gradle`)
- Files/directories deleted (83 production `.java` files + `resources/` tree)
- `mvn test` summary output
- `./gradlew build` status line
- JAR spot-check output from Step 2.11
- Git commit SHA (add after committing)

```bash
git add -A
git commit -m "Phase 2: Refactor to single-class minimal implementation"
HASH=$(git rev-parse HEAD)
echo "Commit: $HASH" >> measurements/phase_02_refactor_log.md
git add measurements/phase_02_refactor_log.md
git commit -m "Phase 2: Add refactor commit hash to log"
```

### Exit Verification

- [ ] `mvn test` passes: 12 tests, 0 failures, 0 errors
- [ ] `./gradlew clean build` passes
- [ ] `java -jar target/*.jar | head -20` matches expected output
- [ ] Only 1 production `.java` file exists under `src/main/java/`
- [ ] `resources/` directory does not exist
- [ ] `measurements/phase_02_refactor_log.md` is committed with commit hash

---

## Phase 3: Post-Refactor Measurement

### Purpose
Collect the identical set of metrics gathered in Phase 1, applied to the refactored codebase. Identical methodology enables direct comparison.

### Entry Conditions
- Phase 2 complete and artifact committed
- `mvn test` passes with 0 failures (11 tests) on refactored code
- `measurements/phase_01_baseline.md` exists

### Steps

Run the collection script for convenience, then validate manually:

```bash
mvn clean package -DskipTests -q   # ensure JAR is fresh
mvn pmd:pmd -q
mvn test -q                         # generates JaCoCo report
./scripts/collect_metrics.sh post
```

The script produces `measurements/metrics_post.md`. Use its values to populate `measurements/phase_03_postrefactor.md`. The following notes describe expected differences and methodology adjustments:

**Step 3.1 (FR-18 + FR-13):** Same as Step 1.1. Spring startup eliminated; test time expected significantly lower. Run 5 times; record min/median/max/stdev.

**Step 3.2 (FR-17):** Same as Step 1.2. 5 runs; min/median/max/stdev.

**Step 3.3 (FR-16):** Same as Step 1.3. Expected: zero or near-zero PMD CyclomaticComplexity violations (post-refactor `fizzBuzz()` has CC ≈ 5).

**Step 3.4 (FR-14):** Remove `resources/` from cloc (no longer exists):
```bash
cloc src/ pom.xml build.gradle \
  --out=measurements/postrefactor_cloc_summary.txt
cloc src/ pom.xml build.gradle \
  --by-file --out=measurements/postrefactor_cloc_detail.txt
```

**Step 3.5 (FR-15):** Expected: 1 production file, 0 interfaces, 0 abstract classes, 0 enums.

**Step 3.6 (FR-19):** Expected depth = number of segments in `com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl` = 9.

**Step 3.7 (FR-20):** Expected: 0 runtime Spring dependencies.

**Step 3.8 (FR-21):** Expected: 2 public methods (`fizzBuzz` and `main`).

**Step 3.9 (FR-22):** Expected: 0 across all annotation types.

**Step 3.10 (M-A):** JAR size — expected significantly smaller (Spring JARs removed).

**Step 3.11 (M-B):** Application execution time — 6 runs, discard first, record min/median/max/stdev for runs 2–6. Expected much faster; Spring context bootstrap eliminated.

**Step 3.12 (M-C):** Peak heap — expected lower; Spring bean registry, proxy objects, and component-scan metadata no longer held in heap.

**Step 3.13 (M-D):** Efferent coupling — expected: 0 violations (single class with only JDK dependencies).

**Step 3.14 (M-E):** Spring context instantiation count — expected: 0.

**Step 3.15 (M-F):** Test count and ratio — expected: 11 `@Test` methods (up from 1). Test-to-production LOC ratio expected to increase (more tests per line of production code).

**Step 3.16 — FR-13 Verification:**
```bash
python3 -c "
import xml.etree.ElementTree as ET
tree = ET.parse('target/site/jacoco/jacoco.xml')
for c in tree.findall('.//counter[@type=\"INSTRUCTION\"]'):
    covered = int(c.get('covered', 0))
    missed  = int(c.get('missed', 0))
    total   = covered + missed
    pct     = 100.0 * covered / total if total else 0
    print(f'Post-refactor instruction coverage: {pct:.2f}%')
"
```

Compare to baseline value from `measurements/phase_01_baseline.md`. FR-13 requires post-refactor ≥ pre-refactor.

```bash
cp target/site/jacoco/jacoco.xml measurements/postrefactor_jacoco.xml
cp target/pmd.xml measurements/postrefactor_pmd.xml
```

---

#### Step 3.17 — Produce and Commit Phase 3 Artifact

Create `measurements/phase_03_postrefactor.md` using the same Measurement Record Template as Phase 1 (Appendix A §A.2).

```bash
git add measurements/
git commit -m "Phase 3: Record post-refactor measurements"
```

### Exit Verification

- [ ] All metrics FR-13 through FR-22 and M-A through M-F recorded in `measurements/phase_03_postrefactor.md`
- [ ] FR-13 verified: post-refactor JaCoCo coverage ≥ baseline value
- [ ] 5-run timing recorded for FR-17, FR-18, M-B; statistics computed
- [ ] `measurements/postrefactor_pmd.xml`, `postrefactor_jacoco.xml`, `postrefactor_cloc_summary.txt` committed
- [ ] Artifact committed on `ai-refactor-experiment`

---

## Phase 4: Comparative Analysis & Trade-off Documentation

### Purpose
Produce the scientific record: side-by-side metric comparison, percentage-change summary, and trade-off analysis.

### Entry Conditions
- Phase 3 complete and artifact committed
- Both `measurements/phase_01_baseline.md` and `measurements/phase_03_postrefactor.md` exist

### Steps

#### Step 4.1 — Populate Comparison Table

Fill the comparison table in Appendix B with Phase 1 and Phase 3 values. Compute absolute and percentage change for each metric. Flag any metrics where the direction of change is unexpected (e.g., if post-refactor JaCoCo < pre-refactor, FR-13 is violated).

#### Step 4.2 — Write Trade-off Analysis

Create `docs/refactor_tradeoffs.md` covering:

**Lost — Extensibility:**
- Strategy pattern: adding new divisibility rules now requires modifying the `if/else` chain (Open/Closed Principle no longer satisfied)
- Factory pattern: changing the output target requires code modification, not configuration
- Spring DI: runtime implementation swapping eliminated

**Lost — Testability:**
- Pre-refactor: each of 61 classes independently unit-testable in isolation
- Post-refactor: only integration-level testing of `fizzBuzz(n)` is structurally possible

**Lost — Structural Documentation:**
- The design pattern inventory (Strategy, Factory, Visitor, Adapter, Template Method) served as executable architecture documentation; the refactored code expresses none of this

**Gained:**
- Zero runtime dependencies (NFR-09 met)
- Spring startup overhead eliminated from test suite (~90%+ test time reduction expected)
- Measurable complexity reduction across all FR-14 through FR-22 metrics
- Higher JaCoCo branch coverage per line of production code (FR-13 expected to be satisfied)
- Elimination of Service Locator anti-pattern (`ApplicationContextHolder` + second Spring context in `LoopContext`)

**Scientific classification note:**
> The scope of change (83 production files deleted, 1 created) constitutes a *rewrite* rather than a classical incremental refactoring. The experiment measures AI-assisted rewriting to a minimum-viable implementation. This distinction should be stated explicitly in any published experimental methodology.

#### Step 4.3 — Produce and Commit Phase 4 Artifact

Create `measurements/phase_04_comparison.md` using the template in Appendix B.

```bash
mkdir -p docs
git add measurements/phase_04_comparison.md docs/refactor_tradeoffs.md
git commit -m "Phase 4: Comparative analysis and trade-off documentation"
```

---

## Appendix A: Phase Documentation Templates

### A.1 — Environment Record Template (`phase_00_environment.md`)

```markdown
# Phase 0: Environment Record
Date: [DATE]
Git branch: [BRANCH]
Git commit: [SHA]

## Tool Versions
- Java: [java -version output]
- Maven: [mvn -version output]
- Gradle: [./gradlew --version | head -3]
- python3: [python3 --version]
- cloc: [cloc --version]
- OS: [uname -srm]
- CPU: [sysctl -n machdep.cpu.brand_string or lscpu Model name]
- RAM: [hw.memsize or free -h]
- Shell: [SHELL + version]

## Pre-Flight Verification
- [ ] mvn test: PASS / FAIL
- [ ] ./gradlew build: PASS / FAIL (note if jcenter() replaced with mavenCentral())
- [ ] mvn pmd:pmd: PASS / FAIL — target/pmd.xml present: YES / NO
- [ ] scripts/collect_metrics.sh: executable YES / NO

## Notes
[Any deviations from the standard procedure]
```

### A.2 — Measurement Record Template (`phase_01_baseline.md` / `phase_03_postrefactor.md`)

```markdown
# Phase [1/3]: [Pre/Post]-Refactor Measurements
Date: [DATE]
Git branch: [BRANCH]
Git commit: [git rev-parse HEAD]

---

## FR-18: Test Execution Time (mvn test)
| Run | Real Time (s) |
|-----|--------------|
| 1   | |
| 2   | |
| 3   | |
| 4   | |
| 5   | |
| **Min** | |
| **Median** | |
| **Max** | |
| **Stdev** | |

## FR-13: JaCoCo Instruction Coverage
- Covered instructions: [Y]
- Missed instructions:  [X]
- Total instructions:   [X+Y]
- **Coverage %:** [Y/(X+Y)*100]%

## FR-17: Build Time (mvn clean package -DskipTests)
| Run | Real Time (s) |
|-----|--------------|
| 1   | |
| 2   | |
| 3   | |
| 4   | |
| 5   | |
| **Min** | |
| **Median** | |
| **Max** | |
| **Stdev** | |

## FR-16: Cyclomatic Complexity (PMD)
- Methods reported: [COUNT]
- Total CC (sum):   [VALUE]
- Maximum CC:       [VALUE] (method: [NAME])
- Average CC:       [VALUE]
- Keyword approximation count: [VALUE]

## FR-14: Lines of Code (cloc)
| Category      | Java | XML | Other | Total |
|---------------|------|-----|-------|-------|
| Code lines    |      |     |       |       |
| Comment lines |      |     |       |       |
| Blank lines   |      |     |       |       |
| **Total**     |      |     |       |       |

## FR-15: Class and Interface Count
- Total production .java files: [VALUE]
- Interfaces:                   [VALUE]
- Abstract classes:             [VALUE]
- Enums:                        [VALUE]
- Concrete classes:             [VALUE]

## FR-19: Package Depth
- Maximum nesting depth: [VALUE]
- Deepest package: [PACKAGE]

## FR-20: Runtime Dependency Count
- Maven compile-scope dependencies: [VALUE]

## FR-21: Public Method Count
- Approximate count: [VALUE]

## FR-22: DI Annotation Count
| Annotation     | Count |
|----------------|-------|
| @Service       |       |
| @Autowired     |       |
| @Component     |       |
| @Repository    |       |
| @Controller    |       |
| @PostConstruct |       |
| **Total**      |       |

---

## M-A: JAR Artifact Size
- File: [JAR filename]
- Size: [BYTES] bytes ([MB] MB)

## M-B: Application Execution Time (java -jar, run 1 discarded)
| Run | Real Time (s) |
|-----|--------------|
| 1 (discarded) | |
| 2   | |
| 3   | |
| 4   | |
| 5   | |
| 6   | |
| **Min (runs 2–6)** | |
| **Median** | |
| **Max** | |
| **Stdev** | |

## M-C: Peak Heap Memory Usage
- Peak heap used: [VALUE] MB
- Measurement method: [verbose:gc / JFR]

## M-D: Efferent Coupling (CouplingBetweenObjects via PMD)
- Classes with violations: [COUNT]
- Total Ce (sum): [VALUE]
- Maximum Ce: [VALUE] (class: [NAME])
- Average Ce: [VALUE]

## M-E: Spring ApplicationContext Instantiation Sites
- Count: [VALUE]
- Locations: [FILE:LINE for each]

## M-F: Test Metrics
- @Test method count: [VALUE]
- Test code lines (cloc): [VALUE]
- Production code lines (from FR-14): [VALUE]
- Test-to-production LOC ratio: [VALUE]

---

## Notes
[Any deviations, anomalies, or caveats observed during collection]
```

---

## Appendix B: Comparison Table Template (`phase_04_comparison.md`)

```markdown
# Phase 4: Pre/Post Refactor Metric Comparison
Date: [DATE]

| Metric (Req.) | Pre-Refactor | Post-Refactor | Δ Absolute | Δ % | Direction |
|---------------|-------------|---------------|-----------|-----|-----------|
| Test time — median (FR-18) | | | | | ↓ expected |
| JaCoCo coverage % (FR-13) | | | | | ≥ required |
| Build time — median (FR-17) | | | | | ↓ expected |
| CC total sum (FR-16) | | | | | ↓ expected |
| CC maximum (FR-16) | | | | | ↓ expected |
| Code lines — Java (FR-14) | | | | | ↓ expected |
| Total .java files (FR-15) | | | | | ↓ expected |
| Interfaces (FR-15) | | | | | ↓ expected |
| Max package depth (FR-19) | | | | | ↓ expected |
| Runtime dependencies (FR-20) | | | | | ↓ expected |
| Public methods (FR-21) | | | | | ↓ expected |
| DI annotations (FR-22) | | | | | ↓ expected |
| JAR artifact size — bytes (M-A) | | | | | ↓ expected |
| Execution time — median (M-B) | | | | | ↓ expected |
| Peak heap — MB (M-C) | | | | | ↓ expected |
| Avg efferent coupling (M-D) | | | | | ↓ expected |
| Spring context init sites (M-E) | | | | | ↓ expected |
| @Test method count (M-F) | | | | | ↑ expected |
| Test/production LOC ratio (M-F) | | | | | ↑ expected |
```

---

## Appendix C: Metrics Collection Script (`scripts/collect_metrics.sh`)

```bash
#!/usr/bin/env bash
# collect_metrics.sh — FizzBuzzEnterpriseEdition metric collection driver
# Usage: ./scripts/collect_metrics.sh [pre|post|pre-verify]
# Writes results to measurements/metrics_<LABEL>.md
# Run from repository root.

set -euo pipefail

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
```

---

## Appendix D: Complete FizzBuzzTest.java

```java
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl.FizzBuzz;

/**
 * Tests for FizzBuzz.
 *
 * Refactored from original:
 *   - Spring context bootstrapping removed; FizzBuzz.fizzBuzz(n) called directly.
 *   - setUp/tearDown extended to save/restore System.err (required by T-10).
 *   - captureOutput(n) helper extracted for reuse across all tests.
 *   - 10 new test methods added (T-01 through T-10).
 *
 * Original oracle test (testFizzBuzz / T-00) preserved with all 16 assertions (FR-12, NFR-05).
 */
public class FizzBuzzTest {

    private PrintStream originalOut;
    private PrintStream originalErr;

    @Before
    public void setUp() {
        this.originalOut = System.out;
        this.originalErr = System.err;
    }

    @After
    public void tearDown() {
        System.setOut(this.originalOut);
        System.setErr(this.originalErr);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Redirects System.out, calls fizzBuzz(n), flushes, restores System.out,
     * and returns the captured output as a String using the default charset (SR-09).
     */
    private String captureOutput(final int n) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final BufferedOutputStream  bos  = new BufferedOutputStream(baos);
        System.setOut(new PrintStream(bos));
        FizzBuzz.fizzBuzz(n);
        System.out.flush();
        System.setOut(originalOut);
        return baos.toString();
    }

    private void doFizzBuzz(final int n, final String s) {
        final String platformExpected =
            s.replaceAll("\\n", System.getProperty("line.separator"));
        assertEquals(platformExpected, captureOutput(n));
    }

    // ── T-00: Oracle Conformance — FR-12, NFR-05 ─────────────────────────────

    @Test
    public void testFizzBuzz() {
        doFizzBuzz(TestConstants.INT_1,  TestConstants._1_);
        doFizzBuzz(TestConstants.INT_2,  TestConstants._1_2_);
        doFizzBuzz(TestConstants.INT_3,  TestConstants._1_2_FIZZ);
        doFizzBuzz(TestConstants.INT_4,  TestConstants._1_2_FIZZ_4);
        doFizzBuzz(TestConstants.INT_5,  TestConstants._1_2_FIZZ_4_BUZZ);
        doFizzBuzz(TestConstants.INT_6,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ);
        doFizzBuzz(TestConstants.INT_7,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7);
        doFizzBuzz(TestConstants.INT_8,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8);
        doFizzBuzz(TestConstants.INT_9,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ);
        doFizzBuzz(TestConstants.INT_10, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ);
        doFizzBuzz(TestConstants.INT_11, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11);
        doFizzBuzz(TestConstants.INT_12, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ);
        doFizzBuzz(TestConstants.INT_13, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13);
        doFizzBuzz(TestConstants.INT_14, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14);
        doFizzBuzz(TestConstants.INT_15,
            TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14_FIZZ_BUZZ);
        doFizzBuzz(TestConstants.INT_16,
            TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14_FIZZ_BUZZ_16);
    }

    // ── T-01: Empty output for N=0 — FR-09 ───────────────────────────────────

    @Test
    public void testZeroInputProducesNoOutput() {
        assertEquals("fizzBuzz(0) must produce zero bytes", 0, captureOutput(0).length());
    }

    // ── T-02: Output cardinality — FR-01 ─────────────────────────────────────

    @Test
    public void testOutputCardinality() {
        final String sep = System.getProperty("line.separator");
        for (int n = 1; n <= 20; n++) {
            final String output = captureOutput(n);
            // Splitting "a\nb\n" on "\n" with limit -1 yields ["a","b",""] — length = entries + 1
            final int entryCount = output.split(sep, -1).length - 1;
            assertEquals("fizzBuzz(" + n + ") must produce exactly " + n + " entries",
                         n, entryCount);
        }
    }

    // ── T-03: No preamble and no trailing content — FR-07, FR-10 ─────────────

    @Test
    public void testNoPreambleAndNoTrailingContent() {
        final String sep    = System.getProperty("line.separator");
        final String output = captureOutput(5);
        assertFalse("Output must not begin with line separator", output.startsWith(sep));
        assertTrue( "Output must end with line separator",       output.endsWith(sep));
        final int lastSep = output.lastIndexOf(sep);
        assertEquals("No content after final separator",
                     "", output.substring(lastSep + sep.length()));
    }

    // ── T-04: Platform line separator — FR-08 ────────────────────────────────

    @Test
    public void testPlatformLineSeparator() {
        final String original = System.getProperty("line.separator");
        System.setProperty("line.separator", "|||");
        try {
            final String output = captureOutput(3);
            assertTrue("Output must use current line.separator", output.contains("|||"));
        } finally {
            System.setProperty("line.separator", original);
        }
    }

    // ── T-05: Call independence and determinism — SR-02, SR-06 ───────────────

    @Test
    public void testCallIndependenceAndDeterminism() {
        final String first  = captureOutput(10);
        final String second = captureOutput(10);
        final String third  = captureOutput(10);
        assertEquals("Repeated calls must produce identical output (1v2)", first,  second);
        assertEquals("Repeated calls must produce identical output (2v3)", second, third);
    }

    // ── T-06: No exception propagation — SR-07 ───────────────────────────────

    @Test
    public void testNoExceptionPropagation() {
        for (final int n : new int[]{0, 1, 3, 5, 15, 16, 100}) {
            try {
                captureOutput(n);
            } catch (final Throwable t) {
                fail("fizzBuzz(" + n + ") must not throw; threw: " + t);
            }
        }
    }

    // ── T-07: IOException suppression — SR-01 ────────────────────────────────

    @Test
    public void testIOExceptionSuppression() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
                throw new IOException("forced failure");
            }
            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                throw new IOException("forced failure");
            }
        }));
        try {
            FizzBuzz.fizzBuzz(5);
        } catch (final Throwable t) {
            fail("fizzBuzz() must suppress IOException; threw: " + t);
        }
    }

    // ── T-08: Per-entry flush — SR-04 ────────────────────────────────────────

    @Test
    public void testPerEntryFlush() {
        final int     n          = 10;
        final int[]   flushCount = {0};
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos) {
            @Override public void flush() { flushCount[0]++; super.flush(); }
        });
        FizzBuzz.fizzBuzz(n);
        System.setOut(originalOut);
        assertTrue("Expected >= " + n + " flush() calls for fizzBuzz(" + n + "); got " + flushCount[0],
                   flushCount[0] >= n);
    }

    // ── T-09: Exhaustive arithmetic correctness i=1..100 — FR-03/04/05/06, SR-08

    @Test
    public void testArithmeticCorrectnessExhaustive() {
        final String   sep   = System.getProperty("line.separator");
        final String[] lines = captureOutput(100).split(sep, -1);
        for (int i = 1; i <= 100; i++) {
            final String  token = lines[i - 1];
            final boolean div3  = (i % 3 == 0);
            final boolean div5  = (i % 5 == 0);
            if      (div3 && div5) assertEquals("i=" + i, "FizzBuzz",       token);
            else if (div3)         assertEquals("i=" + i, "Fizz",           token);
            else if (div5)         assertEquals("i=" + i, "Buzz",           token);
            else                   assertEquals("i=" + i, Integer.toString(i), token);
        }
    }

    // ── T-10: No System.err output — SR-05 (partial) ─────────────────────────

    @Test
    public void testNoSystemErrOutput() {
        final ByteArrayOutputStream errCapture = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errCapture));
        captureOutput(16);
        System.setErr(originalErr);
        assertEquals("fizzBuzz() must not write to System.err", 0, errCapture.size());
    }
}
```
