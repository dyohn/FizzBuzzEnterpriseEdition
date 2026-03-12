# FizzBuzzEnterpriseEdition — Revised Refactor Plan
**Document ID:** PLAN_REVISED_1
**Supersedes:** PLAN_INITIAL
**Branch:** `ai-refactor-experiment`
**Status:** Draft — under revision
**Requirements Reference:** REQUIREMENTS_REFINED_1.md

---

## Overview

This plan describes the ordered execution of four independent phases required to complete the FizzBuzzEnterpriseEdition refactor experiment. Each phase has defined entry conditions, ordered steps, an exit verification procedure, and a documentation artifact committed to the repository at the end of the phase. No phase may begin until all entry conditions are met and the prior phase's documentation artifact has been committed.

The three core areas of the experiment are:
1. **Pre-refactor measurement** — establish the empirical baseline (Phase 1)
2. **System refactor** — reduce the codebase to its minimal implementation (Phase 2)
3. **Post-refactor measurement** — collect equivalent metrics on the simplified system (Phase 3)

A setup phase (Phase 0) and an analysis phase (Phase 4) bookend the experiment.

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

The following must be true before Phase 0 begins:

- Git working directory is on branch `ai-refactor-experiment`
- No uncommitted changes exist in the working tree
- Network access is available (Maven dependency resolution)
- A Unix-compatible shell is available (zsh or bash)
- `java` is on the PATH

---

## Phase 0: Environment Setup & Measurement Tooling

### Purpose
Verify that all required tools are present and functional, add measurement-only tooling to the build, confirm the pre-refactor system builds and tests pass, and record the runtime environment for the experimental record.

### Entry Conditions
- Prerequisites above are met
- No prior phases have been executed

### Steps

#### Step 0.1 — Record Environment

```bash
java -version 2>&1
mvn -version
./gradlew --version
echo "OS: $(uname -srm)"
echo "Shell: $SHELL $($SHELL --version 2>&1 | head -1)"
echo "Date: $(date)"
```

Record all output verbatim in `measurements/phase_00_environment.md`.

#### Step 0.2 — Install cloc

`cloc` is required for FR-14 (Lines of Code Measurement).

```bash
# Check if already installed
cloc --version

# Install if missing (macOS)
brew install cloc
```

Record the installed version.

#### Step 0.3 — Verify Pre-Refactor Build (Maven)

```bash
mvn clean package
```

Expected: `BUILD SUCCESS`. If this fails, do not proceed — diagnose and resolve the build failure first.

#### Step 0.4 — Verify Pre-Refactor Build (Gradle)

```bash
./gradlew clean build
```

Expected: `BUILD SUCCESSFUL`. If `jcenter()` fails to resolve dependencies, replace with `mavenCentral()` in `build.gradle` before proceeding. Document this as a pre-existing issue in the environment record.

#### Step 0.5 — Verify Pre-Refactor Tests Pass

```bash
mvn test
```

Expected: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`. All 16 assertions in `FizzBuzzTest.testFizzBuzz()` must pass. If any test fails, do not proceed.

#### Step 0.6 — Add PMD Plugin to pom.xml

PMD is required for FR-16 (Cyclomatic Complexity Measurement). Add the following plugin block inside the `<build><plugins>` section of `pom.xml`, after the existing `jacoco-maven-plugin` entry:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-pmd-plugin</artifactId>
  <version>3.21.0</version>
  <configuration>
    <rulesets>
      <ruleset>category/java/design.xml/CyclomaticComplexity</ruleset>
    </rulesets>
    <failOnViolation>false</failOnViolation>
    <printFailingErrors>true</printFailingErrors>
  </configuration>
</plugin>
```

> This is a measurement-tooling-only change. It does not alter compilation, test execution, or runtime behavior. It is intentionally introduced before baseline measurement so that the same tooling configuration is present in both the pre- and post-refactor builds.

#### Step 0.7 — Verify PMD Runs

```bash
mvn pmd:pmd
```

Expected: `BUILD SUCCESS` and the file `target/pmd.xml` exists. Open `target/pmd.xml` to confirm it contains `<pmd>` root element and method-level violation entries.

#### Step 0.8 — Create measurements/ Directory and Environment Document

```bash
mkdir -p measurements
```

Create `measurements/phase_00_environment.md` using the template in Appendix A, filled with results from Steps 0.1–0.7.

#### Step 0.9 — Commit

```bash
git add pom.xml measurements/phase_00_environment.md
git commit -m "Phase 0: Add PMD measurement tooling and record environment"
```

### Exit Verification

- [ ] `mvn test` passes with 0 failures
- [ ] `mvn pmd:pmd` succeeds and produces `target/pmd.xml`
- [ ] `cloc --version` outputs a version string
- [ ] `measurements/phase_00_environment.md` is committed

---

## Phase 1: Pre-Refactor Measurement

### Purpose
Collect the empirical baseline for all metrics defined in FR-13 through FR-22 of REQUIREMENTS_REFINED_1.md. All measurements must be taken on the unmodified pre-refactor codebase.

### Entry Conditions
- Phase 0 is complete and its artifact is committed
- `mvn test` passes with 0 failures
- `target/pmd.xml` is present (PMD has run at least once since last `mvn clean`)
- No production source files have been modified since Phase 0

### Steps

All commands are run from the repository root. For build and test timing (FR-17, FR-18), run each command three times and record all three values; the median is the official result.

---

#### Step 1.1 — FR-18: Test Execution Time (run first — also generates JaCoCo report for FR-13)

Running `mvn test` collects two metrics simultaneously: test execution time and JaCoCo coverage.

```bash
# Run 1
mvn clean
time mvn test 2>&1 | tee measurements/baseline_test_run1.txt

# Run 2
mvn clean
time mvn test 2>&1 | tee measurements/baseline_test_run2.txt

# Run 3
mvn clean
time mvn test 2>&1 | tee measurements/baseline_test_run3.txt
```

Record the `real` time value from each run. Compute and record the median.

> Note: `mvn clean` before each run ensures a consistent starting state. JaCoCo output is written to `target/site/jacoco/` after the final run.

---

#### Step 1.2 — FR-13: Test Coverage (JaCoCo)

JaCoCo runs automatically during `mvn test` (from the existing `jacoco-maven-plugin` in `pom.xml`). After Step 1.1, the report is at `target/site/jacoco/`.

```bash
# View HTML summary
open target/site/jacoco/index.html

# Extract instruction coverage totals from XML
grep 'type="INSTRUCTION"' target/site/jacoco/jacoco.xml | tail -1
```

The XML line has the format: `<counter type="INSTRUCTION" missed="X" covered="Y"/>` at the report level. Compute:

```
coverage% = Y / (X + Y) * 100
```

Record the `missed`, `covered`, and computed percentage.

---

#### Step 1.3 — FR-17: Build Time

```bash
# Run 1
mvn clean
time mvn package -DskipTests 2>&1 | tee measurements/baseline_build_run1.txt

# Run 2
mvn clean
time mvn package -DskipTests 2>&1 | tee measurements/baseline_build_run2.txt

# Run 3
mvn clean
time mvn package -DskipTests 2>&1 | tee measurements/baseline_build_run3.txt
```

Record the `real` time value from each run. Compute and record the median.

---

#### Step 1.4 — FR-16: Cyclomatic Complexity (PMD)

```bash
mvn pmd:pmd
cp target/pmd.xml measurements/baseline_pmd.xml
```

From `target/pmd.xml`, extract all `<violation rule="CyclomaticComplexity">` entries. Each entry reports the CC value for one method. Compute:
- **Sum**: total CC across all production methods
- **Maximum**: highest CC for any single method
- **Count**: number of methods reported
- **Average**: Sum / Count (rounded to 2 decimal places)

A supplementary keyword-count approximation (for quick comparison):
```bash
grep -rh "\bif\b\|else if\b\|\bfor\b\|\bwhile\b\|\bcase\b\|\bcatch\b\|&&\|||" \
  src/main/java --include="*.java" | wc -l
```

Record both the PMD-derived values and the keyword count.

---

#### Step 1.5 — FR-14: Lines of Code

```bash
# Summary (totals by language)
cloc src/ resources/ pom.xml build.gradle \
  --out=measurements/baseline_cloc_summary.txt
cat measurements/baseline_cloc_summary.txt

# Detail (per file)
cloc src/ resources/ pom.xml build.gradle \
  --by-file --out=measurements/baseline_cloc_detail.txt
```

Record from the summary: Java total lines, blank lines, comment lines, code lines; XML total; and the grand total across all files.

---

#### Step 1.6 — FR-15: Class and Interface Count

```bash
# Total Java files in production source
find src/main/java -name "*.java" | wc -l

# Interfaces
grep -rl "^public interface" src/main/java --include="*.java" | wc -l

# Abstract classes
grep -rl "^public abstract class\|^abstract class" src/main/java --include="*.java" | wc -l

# Enums
grep -rl "^public enum\|^enum " src/main/java --include="*.java" | wc -l

# Concrete classes (total files minus interfaces, abstract classes, enums)
# Also verify directly:
grep -rl "^public.*class\|^public final class" src/main/java --include="*.java" | \
  grep -v abstract | wc -l
```

Record each count separately. Verify that `interfaces + abstract classes + enums + concrete classes = total files`.

---

#### Step 1.7 — FR-19: Package Depth

```bash
# Find the maximum package nesting depth in production source
find src/main/java -name "*.java" | \
  sed 's|src/main/java/||' | \
  sed 's|/[^/]*\.java$||' | \
  tr '/' '\n' | \
  awk 'BEGIN{max=0; depth=0} /^$/{if(depth>max)max=depth; depth=0} !/^$/{depth++} END{print max}'
```

Simpler alternative using Python (if available):
```bash
find src/main/java -name "*.java" | \
  sed 's|src/main/java/||; s|/[^/]*\.java$||' | \
  awk -F'/' '{print NF}' | sort -rn | head -1
```

Record the maximum depth value.

---

#### Step 1.8 — FR-20: Runtime Dependency Count

```bash
# Count compile-scope (runtime) Spring entries in pom.xml
grep -c "<artifactId>spring-" pom.xml

# Verify with Maven's dependency resolution
mvn dependency:list -DincludeScope=compile 2>/dev/null | \
  grep ":compile" | grep -v "FizzBuzz" | wc -l
```

Record both counts. The Maven dependency list count is the authoritative value.

---

#### Step 1.9 — FR-21: Public Method Count

```bash
# Count public method declarations in production source
# Matches lines with "public" followed by a return type and opening parenthesis
# Excludes class, interface, and enum declarations
grep -rn "public [a-zA-Z].*(" src/main/java --include="*.java" | \
  grep -v "\bclass\b\|\binterface\b\|\benum\b\|//\|^\s*/\*" | \
  wc -l
```

> Note: This grep approach is an approximation. It may include false positives from multi-line method signatures and may miss some edge cases. The same methodology is applied pre- and post-refactor to ensure consistent comparison.

---

#### Step 1.10 — FR-22: DI Annotation Count

```bash
# Count each Spring DI annotation type separately
echo "@Service:      $(grep -rh "@Service" src/main/java --include="*.java" | wc -l)"
echo "@Autowired:    $(grep -rh "@Autowired" src/main/java --include="*.java" | wc -l)"
echo "@Component:    $(grep -rh "@Component" src/main/java --include="*.java" | wc -l)"
echo "@Repository:   $(grep -rh "@Repository" src/main/java --include="*.java" | wc -l)"
echo "@Controller:   $(grep -rh "@Controller" src/main/java --include="*.java" | wc -l)"
echo "@PostConstruct:$(grep -rh "@PostConstruct" src/main/java --include="*.java" | wc -l)"

# Total across all annotation types
grep -rh "@Service\|@Autowired\|@Component\|@Repository\|@Controller\|@PostConstruct" \
  src/main/java --include="*.java" | wc -l
```

Record each annotation type count and the total.

---

#### Step 1.11 — Produce and Commit Phase 1 Artifact

Create `measurements/phase_01_baseline.md` using the Measurement Record Template from Appendix A, populated with all values from Steps 1.1–1.10.

```bash
git add measurements/
git commit -m "Phase 1: Record pre-refactor baseline measurements"
```

### Exit Verification

- [ ] All 10 metric values (FR-13 through FR-22) are recorded in `measurements/phase_01_baseline.md`
- [ ] `measurements/baseline_pmd.xml` is committed
- [ ] `measurements/baseline_cloc_summary.txt` and `baseline_cloc_detail.txt` are committed
- [ ] Three timing runs are recorded for both build time and test time
- [ ] Artifact is committed on `ai-refactor-experiment`

---

## Phase 2: System Refactor

### Purpose
Replace the 87-file enterprise implementation with a single-class minimal implementation that satisfies all functional and semantic requirements in REQUIREMENTS_REFINED_1.md. No measurement is taken during this phase; the output is a verified, committed refactored codebase.

### Entry Conditions
- Phase 1 is complete and its artifact is committed
- `measurements/phase_01_baseline.md` exists and all 10 metrics are populated
- `mvn test` currently passes with 0 failures on the pre-refactor code

### Steps

> **Important:** Follow this order exactly. Steps 2.1–2.3 create new files while old files still exist; this is intentional to allow incremental verification. Steps 2.4–2.5 delete old files. Step 2.6 updates build files. Steps 2.7–2.8 verify the result. Do not run `mvn compile` between Steps 2.1 and 2.6, as the build will be in an inconsistent state.

---

#### Step 2.1 — Create the New Production Class

Create the file:
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
 *   System.out.write(bytes) + System.out.flush() per entry, so the test's
 *   System.setOut() redirect captures output correctly.
 * IOException is swallowed to match the original adapter behavior (SR-01).
 * Line separator uses System.getProperty("line.separator") to match
 *   NewLineStringReturner and the test's platform-normalization logic (FR-08).
 */
public final class FizzBuzz {

    private FizzBuzz() {}

    /**
     * Runs FizzBuzz from 1 to n inclusive, writing each result to System.out.
     *
     * @param n upper limit (inclusive); must be >= 0
     */
    public static void fizzBuzz(final int n) {
        final String newLine = System.getProperty("line.separator");
        for (int i = 1; i <= n; i++) {
            final String output;
            if (i % 3 == 0 && i % 5 == 0) {
                output = "FizzBuzz";
            } else if (i % 3 == 0) {
                output = "Fizz";
            } else if (i % 5 == 0) {
                output = "Buzz";
            } else {
                output = Integer.toString(i);
            }
            try {
                System.out.write((output + newLine).getBytes());
                System.out.flush();
            } catch (java.io.IOException e) {
                // swallowed intentionally — matches original adapter behavior (SR-01)
            }
        }
    }

    /**
     * Entry point. Runs FizzBuzz 1 through 100.
     * The value 100 matches DEFAULT_FIZZ_BUZZ_UPPER_LIMIT_PARAMETER_VALUE
     * from the original implementation (FR-11).
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(final String[] args) {
        fizzBuzz(100);
    }
}
```

---

#### Step 2.2 — Rewrite TestConstants.java

Replace `src/test/java/TestConstants.java` entirely with the following. The only change from the original is removal of the two Spring-specific string constants (`STANDARD_FIZZ_BUZZ` and `SPRING_XML`):

```java
/**
 * Constants for FizzBuzz tests.
 * Spring-specific constants (STANDARD_FIZZ_BUZZ, SPRING_XML) removed.
 * All 16 oracle output strings and integer constants are preserved unchanged.
 */
public class TestConstants {

    private TestConstants() {
        super();
    }

    static final int INT_1  = 1;
    static final int INT_2  = 2;
    static final int INT_3  = 3;
    static final int INT_4  = 4;
    static final int INT_5  = 5;
    static final int INT_6  = 6;
    static final int INT_7  = 7;
    static final int INT_8  = 8;
    static final int INT_9  = 9;
    static final int INT_10 = 10;
    static final int INT_11 = 11;
    static final int INT_12 = 12;
    static final int INT_13 = 13;
    static final int INT_14 = 14;
    static final int INT_15 = 15;
    static final int INT_16 = 16;

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
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14_FIZZ_BUZZ = "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz\n";
    static final String _1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14_FIZZ_BUZZ_16 = "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz\n16\n";

}
```

---

#### Step 2.3 — Rewrite FizzBuzzTest.java

Replace `src/test/java/FizzBuzzTest.java` entirely with the following. Spring is removed; all other structure (System.out capture, `doFizzBuzz` helper, 16 assertions) is preserved:

```java
import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl.FizzBuzz;

/**
 * Tests for FizzBuzz.
 * Refactored: Spring context replaced by direct static method call.
 * All 16 original test cases (n=1 through n=16) are preserved (FR-12, NFR-05).
 * System.out capture mechanism is preserved unchanged (NFR-06).
 */
public class FizzBuzzTest {

    private PrintStream out;

    @Before
    public void setUp() {
        this.out = System.out;
    }

    @After
    public void tearDown() {
        System.setOut(this.out);
    }

    private void doFizzBuzz(final int n, final String s) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final BufferedOutputStream bos = new BufferedOutputStream(baos);
        System.setOut(new PrintStream(bos));

        FizzBuzz.fizzBuzz(n);

        System.out.flush();
        String platformDependentExpectedResult =
            s.replaceAll("\\n", System.getProperty("line.separator"));
        assertEquals(platformDependentExpectedResult, baos.toString());
    }

    @Test
    public void testFizzBuzz() throws IOException {
        this.doFizzBuzz(TestConstants.INT_1,  TestConstants._1_);
        this.doFizzBuzz(TestConstants.INT_2,  TestConstants._1_2_);
        this.doFizzBuzz(TestConstants.INT_3,  TestConstants._1_2_FIZZ);
        this.doFizzBuzz(TestConstants.INT_4,  TestConstants._1_2_FIZZ_4);
        this.doFizzBuzz(TestConstants.INT_5,  TestConstants._1_2_FIZZ_4_BUZZ);
        this.doFizzBuzz(TestConstants.INT_6,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ);
        this.doFizzBuzz(TestConstants.INT_7,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7);
        this.doFizzBuzz(TestConstants.INT_8,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8);
        this.doFizzBuzz(TestConstants.INT_9,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ);
        this.doFizzBuzz(TestConstants.INT_10, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ);
        this.doFizzBuzz(TestConstants.INT_11, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11);
        this.doFizzBuzz(TestConstants.INT_12, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ);
        this.doFizzBuzz(TestConstants.INT_13, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13);
        this.doFizzBuzz(TestConstants.INT_14, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14);
        this.doFizzBuzz(TestConstants.INT_15,
            TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14_FIZZ_BUZZ);
        this.doFizzBuzz(TestConstants.INT_16,
            TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14_FIZZ_BUZZ_16);
    }
}
```

---

#### Step 2.4 — Delete Old Production Source Directories

```bash
BASE=src/main/java/com/seriouscompany/business/java/fizzbuzz/packagenamingpackage

# Remove all sub-packages (interfaces/ and impl/ sub-directories)
rm -rf "${BASE}/interfaces"
rm -rf "${BASE}/impl/factories"
rm -rf "${BASE}/impl/loop"
rm -rf "${BASE}/impl/math"
rm -rf "${BASE}/impl/parameters"
rm -rf "${BASE}/impl/printers"
rm -rf "${BASE}/impl/strategies"
rm -rf "${BASE}/impl/stringreturners"
rm -rf "${BASE}/impl/visitors"

# Remove individual files from impl/ root
rm "${BASE}/impl/ApplicationContextHolder.java"
rm "${BASE}/impl/Constants.java"
rm "${BASE}/impl/Main.java"
rm "${BASE}/impl/StandardFizzBuzz.java"
```

After these deletions, the only file remaining under `src/main/java/` is the new `FizzBuzz.java` created in Step 2.1.

---

#### Step 2.5 — Delete the resources/ Directory

```bash
rm -rf resources/
```

This removes `resources/assets/configuration/spring/dependencyinjection/configuration/spring.xml` and all intermediate directories.

---

#### Step 2.6 — Update pom.xml

Make the following changes to `pom.xml`:

**Remove** the entire `<dependencies>` section and replace it with a test-only dependency block (Spring entries removed):
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

**Remove** the `<resources>` block inside `<build>` (it pointed to the now-deleted `resources/` directory):
```xml
<!-- DELETE this entire block: -->
<resources>
  <resource>
    <directory>resources/assets/configuration/spring/dependencyinjection/configuration</directory>
    <filtering>true</filtering>
  </resource>
</resources>
```

**Update** the `<mainClass>` value inside `maven-jar-plugin`:
```xml
<!-- Change FROM: -->
<mainClass>com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl.Main</mainClass>
<!-- Change TO: -->
<mainClass>com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl.FizzBuzz</mainClass>
```

**Keep unchanged:** `maven-compiler-plugin`, `maven-jar-plugin` (other settings), `jacoco-maven-plugin`, `maven-pmd-plugin`.

---

#### Step 2.7 — Update build.gradle

Make the following changes to `build.gradle`:

**Remove** the Spring entries from `dependencies`:
```groovy
// DELETE these lines:
compile 'org.springframework:spring-aop:3.2.13.RELEASE',
        'org.springframework:spring-beans:3.2.13.RELEASE',
        'org.springframework:spring-context:3.2.13.RELEASE',
        'org.springframework:spring-core:3.2.13.RELEASE',
        'org.springframework:spring-expression:3.2.13.RELEASE'
```

**Remove** the `resources` block inside `sourceSets.main`:
```groovy
// DELETE this block:
resources {
    srcDir 'resources/assets/configuration/spring/dependencyinjection/configuration/'
}
```

**Update** `mainClassName`:
```groovy
// Change FROM:
mainClassName = 'com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl.Main'
// Change TO:
mainClassName = 'com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl.FizzBuzz'
```

**Keep unchanged:** `apply plugin` lines, `repositories`, `sourceSets.main.java`, `testCompile 'junit:junit:4.8.2'`.

---

#### Step 2.8 — Compile Verification

```bash
mvn clean compile
```

Expected: `BUILD SUCCESS` with output indicating `1 source file to compile`. If compilation fails, diagnose — do not proceed to Step 2.9.

---

#### Step 2.9 — Test Verification

```bash
mvn test
```

Expected output:
```
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

All 16 assertions in `testFizzBuzz()` must pass. If any assertion fails, diagnose and fix before proceeding.

---

#### Step 2.10 — Gradle Verification

```bash
./gradlew clean build
```

Expected: `BUILD SUCCESSFUL`. If `jcenter()` fails, replace with `mavenCentral()` and document in the refactor log.

---

#### Step 2.11 — JAR Execution Spot-Check

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
- List of files created (1)
- List of files modified (4: `pom.xml`, `build.gradle`, `FizzBuzzTest.java`, `TestConstants.java`)
- List of files/directories deleted (83 production `.java` files + `resources/` tree)
- Output of `mvn test` (truncated to the summary line)
- Output of `./gradlew clean build` (truncated to the status line)
- Output of the JAR spot-check from Step 2.11

```bash
git add -A
git commit -m "Phase 2: Refactor to single-class minimal implementation"
```

Record the commit hash in `measurements/phase_02_refactor_log.md` and amend or create a follow-up commit to include it:

```bash
git rev-parse HEAD
# Record this hash in phase_02_refactor_log.md
git add measurements/phase_02_refactor_log.md
git commit -m "Phase 2: Add refactor log with commit hash"
```

### Exit Verification

- [ ] `mvn test` passes with 0 failures, 0 errors
- [ ] `./gradlew clean build` passes
- [ ] `java -jar target/FizzBuzzEnterpriseEdition-1.0-SNAPSHOT.jar | head -20` matches expected output
- [ ] Only 1 production `.java` file exists under `src/main/java/`
- [ ] `resources/` directory does not exist
- [ ] `measurements/phase_02_refactor_log.md` is committed

---

## Phase 3: Post-Refactor Measurement

### Purpose
Collect the same set of metrics gathered in Phase 1, applied to the refactored codebase. Values are recorded using identical methodology to enable direct comparison.

### Entry Conditions
- Phase 2 is complete and its artifact is committed
- `mvn test` passes with 0 failures on the refactored code
- `measurements/phase_01_baseline.md` exists (required for FR-13 comparison)

### Steps

Run the identical commands from Phase 1, Steps 1.1 through 1.10, with the following adjustments:

**Step 3.1 (FR-18 + FR-13):** Same as Step 1.1. Note: Spring startup is eliminated; test time is expected to be significantly lower.

**Step 3.2 (FR-13):** Same as Step 1.2. JaCoCo report reflects coverage of the single `FizzBuzz.java` class. Expected: instruction coverage ≥ baseline value from phase_01_baseline.md.

**Step 3.3 (FR-17):** Same as Step 1.3.

**Step 3.4 (FR-16):** Same as Step 1.4. Expected: significantly fewer or zero PMD violations (post-refactor has one method with CC ≈ 7).

**Step 3.5 (FR-14):** Remove `resources/` from the cloc command (directory no longer exists):
```bash
cloc src/ pom.xml build.gradle \
  --out=measurements/postrefactor_cloc_summary.txt
cloc src/ pom.xml build.gradle \
  --by-file --out=measurements/postrefactor_cloc_detail.txt
```

**Step 3.6 (FR-15):** Same as Step 1.6. Expected: 1 production file, 0 interfaces, 0 abstract classes, 0 enums, 1 concrete class.

**Step 3.7 (FR-19):** Same as Step 1.7. Expected: maximum depth equal to the number of segments in `com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl`.

**Step 3.8 (FR-20):** Same as Step 1.8. Expected: 0 Spring runtime dependencies.

**Step 3.9 (FR-21):** Same as Step 1.9. Expected: 2 public methods (`fizzBuzz` and `main`).

**Step 3.10 (FR-22):** Same as Step 1.10. Expected: 0 across all annotation types.

---

#### Step 3.11 — Produce and Commit Phase 3 Artifact

Create `measurements/phase_03_postrefactor.md` using the same Measurement Record Template as Phase 1 (Appendix A), populated with all post-refactor values.

```bash
git add measurements/
git commit -m "Phase 3: Record post-refactor measurements"
```

### Exit Verification

- [ ] All 10 metric values (FR-13 through FR-22) are recorded in `measurements/phase_03_postrefactor.md`
- [ ] `measurements/postrefactor_pmd.xml` is committed
- [ ] `measurements/postrefactor_cloc_summary.txt` is committed
- [ ] Three timing runs recorded for both build and test time
- [ ] FR-13 verified: post-refactor JaCoCo coverage ≥ baseline coverage
- [ ] Artifact committed on `ai-refactor-experiment`

---

## Phase 4: Comparative Analysis & Trade-off Documentation

### Purpose
Produce the scientific record: a side-by-side comparison of all metrics, a percentage-change summary, and a trade-off analysis.

### Entry Conditions
- Phase 3 is complete and its artifact is committed
- Both `measurements/phase_01_baseline.md` and `measurements/phase_03_postrefactor.md` exist

### Steps

#### Step 4.1 — Build Comparison Table

Populate the comparison table in Appendix B with values from Phase 1 and Phase 3 artifacts. Compute the absolute and percentage change for each metric.

#### Step 4.2 — Write Trade-off Analysis

Create `docs/refactor_tradeoffs.md` covering:

**Lost — Extensibility:**
- Strategy pattern: adding new divisibility rules now requires modifying the `if/else` chain
- Factory pattern: changing the output target requires code modification, not configuration
- Spring DI: runtime implementation swapping is no longer possible

**Lost — Testability character:**
- Pre-refactor: each of 61 classes was independently unit-testable in isolation
- Post-refactor: only integration-level testing of `fizzBuzz(n)` is structurally possible

**Gained:**
- Zero runtime dependencies (NFR-09 met)
- All Spring startup overhead eliminated from test suite
- Complexity reduction (quantified by FR-14 through FR-22 measurements)

**Scientific classification:**
> The scope of change (83 production files deleted, 1 created) constitutes a *rewrite* rather than a classical incremental refactoring. The experiment measures AI-assisted rewriting to a minimum-viable implementation. This distinction should be stated in the experimental methodology.

#### Step 4.3 — Produce and Commit Phase 4 Artifact

```bash
mkdir -p docs
git add measurements/phase_04_comparison.md docs/refactor_tradeoffs.md
git commit -m "Phase 4: Comparative analysis and trade-off documentation"
```

---

## Appendix A: Phase Documentation Templates

### Template: Environment Record (`phase_00_environment.md`)

```markdown
# Phase 0: Environment Record
Date: [DATE]

## Tool Versions
- Java: [OUTPUT OF java -version]
- Maven: [OUTPUT OF mvn -version]
- Gradle: [OUTPUT OF ./gradlew --version | head -3]
- cloc: [cloc --version]
- OS: [uname -srm]
- Shell: [SHELL + version]

## Pre-Flight Verification
- [ ] mvn test: PASS / FAIL
- [ ] ./gradlew build: PASS / FAIL (note if jcenter() replaced with mavenCentral())
- [ ] mvn pmd:pmd: PASS / FAIL
- [ ] target/pmd.xml present: YES / NO

## Notes
[Any deviations from the standard procedure]
```

### Template: Measurement Record (`phase_01_baseline.md` / `phase_03_postrefactor.md`)

```markdown
# Phase [1/3]: [Pre/Post]-Refactor Measurements
Date: [DATE]
Git commit: [git rev-parse HEAD]

## FR-18: Test Execution Time
| Run | Real Time |
|-----|-----------|
| 1   | |
| 2   | |
| 3   | |
| **Median** | |

## FR-13: JaCoCo Instruction Coverage
- Missed instructions: [X]
- Covered instructions: [Y]
- Coverage %: [Y/(X+Y)*100]%

## FR-17: Build Time (mvn clean package -DskipTests)
| Run | Real Time |
|-----|-----------|
| 1   | |
| 2   | |
| 3   | |
| **Median** | |

## FR-16: Cyclomatic Complexity (PMD)
- Total CC (sum): [VALUE]
- Maximum CC (single method): [VALUE]
- Method count reported: [VALUE]
- Average CC: [VALUE]
- Keyword approximation count: [VALUE]

## FR-14: Lines of Code (cloc)
| Category | Java | XML | Other | Total |
|----------|------|-----|-------|-------|
| Code lines | | | | |
| Comment lines | | | | |
| Blank lines | | | | |
| Total lines | | | | |

## FR-15: Class and Interface Count
- Total production .java files: [VALUE]
- Interfaces: [VALUE]
- Abstract classes: [VALUE]
- Enums: [VALUE]
- Concrete classes: [VALUE]

## FR-19: Package Depth
- Maximum nesting depth: [VALUE]

## FR-20: Runtime Dependency Count
- Spring entries in pom.xml: [VALUE]
- Maven compile-scope dependencies: [VALUE]

## FR-21: Public Method Count
- Approximate public method count: [VALUE]

## FR-22: DI Annotation Count
| Annotation | Count |
|-----------|-------|
| @Service | |
| @Autowired | |
| @Component | |
| @Repository | |
| @Controller | |
| @PostConstruct | |
| **Total** | |

## Notes
[Any deviations, anomalies, or caveats]
```

---

## Appendix B: Comparison Table Template (`phase_04_comparison.md`)

```markdown
# Phase 4: Pre/Post Refactor Metric Comparison

| Metric (Req.) | Pre-Refactor | Post-Refactor | Change | % Change | Direction |
|---------------|-------------|---------------|--------|----------|-----------|
| Test execution time (FR-18) | | | | | ↓ expected |
| JaCoCo coverage % (FR-13) | | | | | ≥ required |
| Build time (FR-17) | | | | | ↓ expected |
| CC sum (FR-16) | | | | | ↓ expected |
| CC max (FR-16) | | | | | ↓ expected |
| Code lines — Java (FR-14) | | | | | ↓ expected |
| Total .java files (FR-15) | | | | | ↓ expected |
| Interfaces (FR-15) | | | | | ↓ expected |
| Max package depth (FR-19) | | | | | ↓ expected |
| Runtime deps (FR-20) | | | | | ↓ expected |
| Public methods (FR-21) | | | | | ↓ expected |
| DI annotations (FR-22) | | | | | ↓ expected |
```
