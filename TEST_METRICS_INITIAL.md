# Testing and Metrics Plan — FizzBuzzEnterpriseEdition Refactor Experiment
**Document ID:** TEST_METRICS_INITIAL
**Branch:** `ai-refactor-experiment`
**Status:** Draft — initial version for review
**Sources:** REQUIREMENTS_REFINED_1.md, PLAN_REVISED_1.md

---

## Table of Contents

1. [Overview](#1-overview)
2. [Requirements Validation Strategy](#2-requirements-validation-strategy)
   - 2.1 Verification Method Taxonomy
   - 2.2 Validation Matrix
3. [Unit Testing Plan](#3-unit-testing-plan)
   - 3.1 Existing Tests (Retained)
   - 3.2 New Tests Required
   - 3.3 Test Design Notes
4. [Metrics Collection Plan](#4-metrics-collection-plan)
   - 4.1 Required Metrics (FR-13 through FR-22)
   - 4.2 Proposed Additional Metrics
   - 4.3 Collection Commands
5. [Statistical Rigor](#5-statistical-rigor)
6. [Environment Specification](#6-environment-specification)
7. [Analysis and Reporting Plan](#7-analysis-and-reporting-plan)
8. [Open Questions](#8-open-questions)

---

## 1. Overview

This plan defines two complementary activities:

1. **Requirements Validation**: A structured strategy for verifying that every requirement in `REQUIREMENTS_REFINED_1.md` is satisfied by both the pre- and post-refactor implementations. Validation uses a combination of JUnit tests (existing and new), static analysis, build verification, and manual inspection.

2. **Metrics Collection**: A plan for gathering the empirical data required by FR-13 through FR-22, plus additional metrics proposed here to enhance the scientific completeness of the experiment. Metrics are collected for both the pre-refactor (`uinverse` branch) and post-refactor (`ai-refactor-experiment` branch) codebases.

This document is complementary to `PLAN_REVISED_1.md`, which describes the phase-by-phase execution steps. This document focuses on the *what* and *why* of verification and measurement, leaving execution scheduling to the plan.

---

## 2. Requirements Validation Strategy

### 2.1 Verification Method Taxonomy

Each requirement is assigned one or more of the following verification methods:

| Code | Method | Description |
|------|--------|-------------|
| **JUnit-E** | JUnit (Existing) | Covered by the existing `FizzBuzzTest.java` oracle tests |
| **JUnit-N** | JUnit (New) | Requires a new test method in `FizzBuzzTest.java` or a new test class |
| **Build** | Build Verification | Verified by the Maven/Gradle build succeeding without error |
| **Static** | Static Analysis | Verified by code review, PMD, or inspection of source/manifest |
| **Measure** | Measurement | Collected as a numeric metric; no pass/fail criterion beyond presence |
| **Manual** | Manual Inspection | Requires human verification; not automatable |

### 2.2 Validation Matrix

#### Functional Requirements

| Req. ID | Brief Description | Verification Methods | Notes |
|---------|-------------------|----------------------|-------|
| FR-01 | Exactly N entries for `fizzBuzz(N)` | JUnit-E, JUnit-N | Oracle tests implicitly validate; add explicit cardinality assertion (see §3.2) |
| FR-02 | Ascending order i=1..N | JUnit-E, JUnit-N | Oracle tests implicitly validate correct ordering; add explicit ordering test |
| FR-03 | Fizz for i%3==0 && i%5!=0 | JUnit-E | Oracle: N=3 → "Fizz\n", N=6 → "Fizz\n", etc. |
| FR-04 | Buzz for i%5==0 && i%3!=0 | JUnit-E | Oracle: N=5 → "Buzz\n", N=10 → "Buzz\n" |
| FR-05 | FizzBuzz for i%3==0 && i%5==0 | JUnit-E | Oracle: N=15 → output contains "FizzBuzz\n" as 15th line |
| FR-06 | Integer string for all other i | JUnit-E | Oracle: N=1→"1\n", N=2→"1\n2\n", N=7→...→"7\n" |
| FR-07 | Entry termination (newline, no trailing content) | JUnit-N | Need explicit test that output ends exactly after the Nth newline |
| FR-08 | Platform line separator — not hardcoded | JUnit-N | Set a non-default line.separator property, verify output matches |
| FR-09 | Empty output for N=0 | JUnit-N | `fizzBuzz(0)` must produce zero bytes; **not covered by existing tests** |
| FR-10 | No preamble before first entry | JUnit-N | Verify captured bytes[0] is the first character of the first token |
| FR-11 | `main()` calls `fizzBuzz(N)` with same N in both systems | Manual, Static | Inspect both `Main.java` files; verify same N constant; document it |
| FR-12 | Oracle conformance N=1..16 | JUnit-E | `FizzBuzzTest.testFizzBuzz()` — all 16 assertions |
| FR-13 | Post-refactor JaCoCo coverage ≥ pre-refactor | Measure | Compare jacoco.xml `instruction` coverage percentage |
| FR-14 | LOC measurement (cloc) | Measure | `cloc src/ resources/ pom.xml build.gradle` |
| FR-15 | Class and interface count | Measure | `find` + `grep` on production source tree |
| FR-16 | Cyclomatic complexity (PMD) | Measure | `mvn pmd:pmd`, parse PMD XML report |
| FR-17 | Build time (mvn clean package, median 5 runs) | Measure | `time mvn clean package -DskipTests` |
| FR-18 | Test execution time (mvn test, median 5 runs) | Measure | `time mvn test` |
| FR-19 | Package depth | Measure | Compute max dot-segments in deepest FQN |
| FR-20 | Runtime dependency count | Measure | Parse `<dependencies>` in pom.xml, filter scope=runtime/compile |
| FR-21 | Public method count | Measure | `grep -rn "public " src/main/` with method signature filter |
| FR-22 | DI annotation count | Measure | `grep -rn "@Service\|@Autowired\|@PostConstruct\|@Component" src/main/` |

#### Semantic Requirements

| Req. ID | Brief Description | Verification Methods | Notes |
|---------|-------------------|----------------------|-------|
| SR-01 | IOException swallowed; not propagated | JUnit-N | Redirect System.out to a stream that throws IOException; verify no exception escapes `fizzBuzz()` (see §3.2) |
| SR-02 | Call independence (output independent of prior calls) | JUnit-N | Call `fizzBuzz(5)` twice; compare captured output byte-for-byte |
| SR-03 | `System.out` resolved at call time | JUnit-E | Implicit: the existing `System.setOut()` redirect mechanism proves this — if output weren't resolved at call time, oracle tests would fail |
| SR-04 | Per-entry flush | JUnit-N | Intercept flush calls via a counting OutputStream wrapper; verify flush count ≥ N after `fizzBuzz(N)` |
| SR-05 | No side effects beyond System.out | JUnit-N, Manual | Capture `System.err` before/after; verify empty; inspect source for filesystem/network/thread usage |
| SR-06 | Determinism (same N → same bytes every time) | JUnit-N | Run `fizzBuzz(16)` three times; compare captured byte arrays for equality |
| SR-07 | `fizzBuzz()` throws no exceptions | JUnit-N | Call `fizzBuzz(N)` for N in {0, 1, 15, 16, 100}; assert no exception via `assertDoesNotThrow` or try/catch |
| SR-08 | Integer arithmetic equivalent to `i % d == 0` | JUnit-N | Exhaustive check: for i in 1..100, verify Fizz/Buzz/FizzBuzz/integer token is exactly what modulo predicts |
| SR-09 | Default charset (no explicit charset) | JUnit-E, Static | Implicit: oracle tests decode via `ByteArrayOutputStream.toString()` (default charset); also inspect source for explicit charset arguments |

#### Non-Functional Requirements

| Req. ID | Brief Description | Verification Methods | Notes |
|---------|-------------------|----------------------|-------|
| NFR-01 | Maven build succeeds | Build | `mvn clean package` exits 0 |
| NFR-02 | Gradle build succeeds | Build | `./gradlew clean build` exits 0 |
| NFR-03 | Java 1.7 language target | Build, Static | Verify `<source>1.7</source><target>1.7</target>` in pom.xml compiler plugin |
| NFR-04 | JUnit 4 test framework | Build, Static | Verify `junit:junit:4.8.2` in pom.xml; `@Test` annotations use `org.junit` |
| NFR-05 | 16 test cases (N=1..16) | JUnit-E | Count assertions in `testFizzBuzz()` |
| NFR-06 | System.out capture via `System.setOut()` + `ByteArrayOutputStream` | Static | Inspect test class; verify teardown restores `System.out` |
| NFR-07 | JaCoCo plugin active during `mvn test` | Build, Measure | Verify plugin in pom.xml; confirm `target/site/jacoco/` is generated after `mvn test` |
| NFR-08 | Runnable JAR (`java -jar`) | Manual | Execute `java -jar target/*.jar`; verify exit code 0 and expected output |
| NFR-09 | No runtime deps beyond JDK (post-refactor only) | Build, Static | Verify `mvn dependency:list` shows no compile/runtime scope Spring artifacts |
| NFR-10 | Changes on `ai-refactor-experiment` branch | Manual | `git log --oneline uinverse` must not include refactor commits |

---

## 3. Unit Testing Plan

### 3.1 Existing Tests (Retained)

`FizzBuzzTest.java` contains `testFizzBuzz()`, which iterates N from 1 to 16, calls `fizzBuzz(N)` with System.out redirected to a `ByteArrayOutputStream`, and compares the captured output to oracle strings in `TestConstants.java`. This test will be **rewritten** during Phase 2 of the refactor (to remove Spring bootstrapping) but will retain the same 16-case oracle validation structure and the `System.setOut()` capture mechanism.

The existing test implicitly validates: FR-03 through FR-06 (output rules), FR-12 (oracle conformance), SR-03 (System.out at call time), SR-06 (implicitly deterministic), SR-09 (charset round-trip), and NFR-05 (16 cases).

### 3.2 New Tests Required

The following new test methods should be added to `FizzBuzzTest.java` (or a companion class). Each is mapped to the requirement(s) it verifies.

---

**T-01 — Zero Input (FR-09)**
```java
@Test
public void testFizzBuzzZeroProducesNoOutput() {
    // Redirect System.out
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    fizzBuzz(0);
    System.out.flush();
    System.setOut(originalOut);
    assertEquals(0, baos.size());
}
```
Verifies: FR-09

---

**T-02 — Output Cardinality (FR-01)**
```java
@Test
public void testOutputCardinality() {
    // Capture output of fizzBuzz(N) and count lines
    // (line separator agnostic — split on System.getProperty("line.separator"))
    for (int n = 1; n <= 20; n++) {
        String output = captureOutput(n);
        String[] lines = output.split(System.getProperty("line.separator"), -1);
        // Last element will be empty string after final separator
        int entryCount = output.isEmpty() ? 0 : lines.length - (output.endsWith(System.getProperty("line.separator")) ? 1 : 0);
        assertEquals("fizzBuzz(" + n + ") must produce exactly " + n + " entries", n, entryCount);
    }
}
```
Verifies: FR-01

---

**T-03 — No Preamble / No Trailing Content (FR-07, FR-10)**
```java
@Test
public void testNoPreambleAndNoTrailingContent() {
    String output = captureOutput(5); // "1\n2\nFizz\n4\nBuzz\n" (or platform equiv.)
    String sep = System.getProperty("line.separator");
    assertFalse("Output must not begin with line separator", output.startsWith(sep));
    assertTrue("Output must end with exactly one line separator", output.endsWith(sep));
    // Ensure the portion after the final separator is empty
    int lastSep = output.lastIndexOf(sep);
    assertEquals("No content after final separator", "", output.substring(lastSep + sep.length()));
}
```
Verifies: FR-07, FR-10

---

**T-04 — Platform Line Separator (FR-08)**
```java
@Test
public void testPlatformLineSeparator() {
    // Override line.separator to a known unusual value
    String original = System.getProperty("line.separator");
    System.setProperty("line.separator", "|||");
    try {
        String output = captureOutput(3); // "1|||2|||Fizz|||"
        assertTrue("Output must use current line.separator", output.contains("|||"));
        assertFalse("Output must not hardcode \\n", output.contains("\n") && !"|||".equals("\n"));
    } finally {
        System.setProperty("line.separator", original);
    }
}
```
Verifies: FR-08

---

**T-05 — Call Independence (SR-02)**
```java
@Test
public void testCallIndependence() {
    String first  = captureOutput(10);
    String second = captureOutput(10);
    String third  = captureOutput(10);
    assertEquals("Repeated calls to fizzBuzz(10) must produce identical output", first, second);
    assertEquals("Repeated calls to fizzBuzz(10) must produce identical output", second, third);
}
```
Verifies: SR-02, SR-06

---

**T-06 — No Exception Propagation (SR-07)**
```java
@Test
public void testNoExceptionPropagation() {
    // fizzBuzz must not throw for any N >= 0
    for (int n : new int[]{0, 1, 3, 5, 15, 16, 100}) {
        try {
            captureOutput(n);
        } catch (Throwable t) {
            fail("fizzBuzz(" + n + ") threw: " + t);
        }
    }
}
```
Verifies: SR-07

---

**T-07 — IOException Suppression (SR-01)**
```java
@Test
public void testIOExceptionSuppression() {
    // Replace System.out with a stream that always throws IOException on write
    PrintStream throwingStream = new PrintStream(new OutputStream() {
        @Override public void write(int b) throws IOException {
            throw new IOException("forced failure");
        }
        @Override public void write(byte[] b, int off, int len) throws IOException {
            throw new IOException("forced failure");
        }
    });
    System.setOut(throwingStream);
    try {
        fizzBuzz(5); // Must not throw
    } catch (Throwable t) {
        fail("fizzBuzz() must suppress IOException; instead threw: " + t);
    } finally {
        System.setOut(originalOut);
    }
}
```
Verifies: SR-01, SR-07
> *Note: This test applies only to post-refactor; in pre-refactor the IOException suppression is provided by `FizzBuzzOutputStrategyToFizzBuzzExceptionSafeOutputStrategyAdapter`.*

---

**T-08 — Per-Entry Flush (SR-04)**
```java
@Test
public void testPerEntryFlush() {
    // Count flush() calls via a wrapping OutputStream
    final int[] flushCount = {0};
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream countingStream = new PrintStream(baos) {
        @Override public void flush() { flushCount[0]++; super.flush(); }
    };
    System.setOut(countingStream);
    fizzBuzz(10);
    System.setOut(originalOut);
    // Each of 10 entries must have triggered at least one flush
    assertTrue("Expected at least 10 flush() calls for fizzBuzz(10), got: " + flushCount[0],
               flushCount[0] >= 10);
}
```
Verifies: SR-04

---

**T-09 — Exhaustive Arithmetic Correctness (SR-08, FR-03–FR-06)**
```java
@Test
public void testArithmeticCorrectnessExhaustive() {
    // For i = 1..100, verify the emitted token matches % rules exactly
    // Parse captured output of fizzBuzz(100) line by line
    String[] lines = captureOutput(100).split(System.getProperty("line.separator"), -1);
    for (int i = 1; i <= 100; i++) {
        String token = lines[i - 1];
        boolean div3 = (i % 3 == 0), div5 = (i % 5 == 0);
        if (div3 && div5)      assertEquals("i=" + i, "FizzBuzz", token);
        else if (div3)         assertEquals("i=" + i, "Fizz", token);
        else if (div5)         assertEquals("i=" + i, "Buzz", token);
        else                   assertEquals("i=" + i, Integer.toString(i), token);
    }
}
```
Verifies: FR-03, FR-04, FR-05, FR-06, SR-08

---

**T-10 — System.err Silence (SR-05)**
```java
@Test
public void testNoSystemErrOutput() {
    ByteArrayOutputStream errCapture = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errCapture));
    captureOutput(16);
    System.setErr(originalErr);
    assertEquals("fizzBuzz() must not write to System.err", 0, errCapture.size());
}
```
Verifies: SR-05 (partial — covers System.err; filesystem/network/threads require manual inspection)

---

### 3.3 Test Design Notes

- **Helper method `captureOutput(int n)`** — all new tests should share a private helper that redirects `System.out`, calls `fizzBuzz(n)`, flushes, restores `System.out`, and returns the captured string. This avoids duplication across test methods.
- **`@Before` / `@After`** — the existing teardown pattern (restoring `System.out` via `@After`) should be extended to restore `System.err` as well, given T-10.
- **T-04 (line separator)** — this test mutates a JVM system property; it must restore the original value in a `finally` block to avoid contaminating other tests. Test ordering dependencies should be assumed absent.
- **T-07 (IOException suppression)** — this test cannot verify the pre-refactor codebase in isolation because the exception suppression is buried inside the adapter chain. It primarily validates the post-refactor implementation. For pre-refactor, the mechanism is verified by static inspection of `FizzBuzzOutputStrategyToFizzBuzzExceptionSafeOutputStrategyAdapter`.
- **T-04 and T-09 conflict awareness** — T-04 changes `line.separator`; T-09 calls `fizzBuzz(100)` which is expensive (1 second+ in pre-refactor). These should be isolated and clearly documented.

---

## 4. Metrics Collection Plan

### 4.1 Required Metrics (from REQUIREMENTS_REFINED_1.md)

| Metric | Req. | Tool | Command |
|--------|------|------|---------|
| Lines of code | FR-14 | `cloc` | `cloc src/ resources/ pom.xml build.gradle` |
| Class count, interface count | FR-15 | `find` + `grep` | `find src/main -name "*.java" \| wc -l`; `grep -rl "^public interface" src/main \| wc -l` |
| Cyclomatic complexity | FR-16 | PMD | `mvn pmd:pmd`; parse `target/pmd.xml` for `<violation rule="CyclomaticComplexity">` |
| Build time | FR-17 | `time` + Maven | `time mvn clean package -DskipTests` (5 runs; report median) |
| Test execution time | FR-18 | `time` + Maven | `time mvn test` (5 runs; report median) |
| Package depth | FR-19 | `find` + `awk` | `find src/main -name "*.java" -exec grep -m1 "^package" {} \; \| awk -F'.' '{print NF}' \| sort -n \| tail -1` |
| Runtime dependency count | FR-20 | Maven | `mvn dependency:list -DincludeScope=runtime \| grep ":compile\|:runtime" \| grep -v ":test" \| wc -l` |
| Public method count | FR-21 | `grep` | `grep -rn "^\s*public\s" src/main/java --include="*.java" \| grep -v "class\|interface\|@" \| wc -l` |
| DI annotation count | FR-22 | `grep` | `grep -rn "@Service\|@Autowired\|@Component\|@Repository\|@Controller\|@PostConstruct" src/main/ \| wc -l` |
| JaCoCo instruction coverage | FR-13 | JaCoCo | `mvn test`; read `target/site/jacoco/jacoco.xml` → `<counter type="INSTRUCTION" covered="..." missed="..."/>` |

### 4.2 Proposed Additional Metrics

The following metrics are not currently required by REQUIREMENTS_REFINED_1.md but would meaningfully enhance the scientific completeness of the experiment. Each is justified by what it adds to the comparison picture.

---

#### M-A: JAR Artifact Size (bytes)

**Rationale:** The post-refactor system removes Spring Framework 3.2.13 as a runtime dependency. If the JAR is built as a fat JAR (dependencies bundled), the size difference will directly quantify the binary footprint reduction. Even if not fat-bundled, the difference in declared dependencies (FR-20) is reflected in deployment size. This is one of the most tangible operational metrics for practitioners.

**Tool:** `ls -l` or `stat`
**Command:**
```bash
mvn clean package -DskipTests -q
ls -l target/*.jar | awk '{print $5, $9}'
```
**Record:** File size in bytes; also compute reduction percentage.

---

#### M-B: Application Startup and Execution Time

**Rationale:** Spring Framework incurs significant startup overhead due to component scanning, bean instantiation, and context wiring. The pre-refactor system boots two `ClassPathXmlApplicationContext` instances per `fizzBuzz()` call. Measuring end-to-end wall-clock time for `java -jar` provides the most user-visible latency comparison.

**Tool:** `time`
**Command:**
```bash
time java -jar target/*.jar > /dev/null
```
Run 5 times; discard first run (JVM cold start); report min, median, max.

**Record:** Wall-clock elapsed time in milliseconds.

> *This metric uniquely captures the Spring bootstrap penalty, which does not appear in build time (FR-17) or test time (FR-18).*

---

#### M-C: Peak Heap Memory Usage

**Rationale:** Spring's IoC container maintains a live object graph (bean registry, proxy objects, component-scan metadata) that occupies heap throughout the process lifetime. Measuring peak heap quantifies the memory cost of the framework.

**Tool:** JVM flags + `VisualVM`, or a simpler approach using `-verbose:gc` with `PrintGCDetails`
**Command (simple):**
```bash
java -Xmx512m -verbose:gc -jar target/*.jar > /dev/null 2> gc.log
# Parse gc.log for peak heap usage
```
**Alternative (more precise):**
```bash
java -Xmx512m -XX:+PrintGCDetails -XX:+PrintGCDateStamps -jar target/*.jar > /dev/null 2> gc.log
grep -E "Heap|used" gc.log | tail -20
```
**Record:** Peak heap used (MB) as reported by GC log.

---

#### M-D: Efferent Coupling Per Class (Ce)

**Rationale:** Efferent coupling (the number of classes a given class depends on) directly measures how tightly coupled the codebase is. High efferent coupling means changes in dependencies cascade to many classes. The refactor goal is to collapse a highly coupled system to a self-contained class; Ce quantifies this reduction.

**Tool:** PMD (Coupling Between Objects rule)
**Setup:** Add to PMD ruleset:
```xml
<rule ref="category/java/design.xml/CouplingBetweenObjects"/>
```
**Command:**
```bash
mvn pmd:pmd
# Parse target/pmd.xml for CouplingBetweenObjects violations
```
**Record:** Average Ce across all production classes; maximum Ce; total Ce sum.

---

#### M-E: Number of Spring Context Initialization Events

**Rationale:** The pre-refactor system creates two independent `ClassPathXmlApplicationContext` instances per `fizzBuzz()` call — one in `Main` and one inside `LoopContext`. This is a known architectural defect (Service Locator anti-pattern). Documenting this count provides a precise, reproducible artifact of the anti-pattern that the refactor eliminates.

**Tool:** Manual code inspection + grep
**Command:**
```bash
grep -rn "ClassPathXmlApplicationContext\|new ApplicationContext" src/main/
```
**Record:** Count of `ApplicationContext` instantiation sites in production source.

> *Expected: 2 pre-refactor (Main.java + LoopContext.java), 0 post-refactor.*

---

#### M-F: Test Count and Test-to-Production Code Ratio

**Rationale:** A refactor that simplifies production code while requiring a proportionally larger test suite may not represent a net simplification. Tracking the ratio of test LOC to production LOC, and the number of test methods, provides context for interpreting coverage numbers (FR-13).

**Tool:** `cloc`, `grep`
**Commands:**
```bash
# Test LOC
cloc src/test/
# Number of @Test methods
grep -rn "@Test" src/test/ | wc -l
```
**Record:** Test LOC; number of @Test methods; ratio of test LOC to production LOC.

---

### 4.3 Collection Commands — Complete Reference

The following is a consolidated collection script outline for Phase 1 and Phase 3 execution:

```bash
#!/usr/bin/env bash
# Collect all metrics for one system (run from repo root)
# Usage: ./collect_metrics.sh [pre|post]

LABEL=${1:-"unknown"}
OUT="measurements/metrics_${LABEL}.md"

echo "## Metrics: $LABEL — $(date)" >> $OUT

# FR-14: LOC
echo "### FR-14: Lines of Code" >> $OUT
cloc src/ resources/ pom.xml build.gradle 2>/dev/null >> $OUT || \
  cloc src/ pom.xml build.gradle >> $OUT   # post-refactor (no resources/)

# FR-15: Class/interface counts
echo "### FR-15: Class and Interface Counts" >> $OUT
echo "Java files (src/main): $(find src/main -name '*.java' | wc -l | tr -d ' ')" >> $OUT
echo "Interfaces: $(grep -rl '^public interface' src/main | wc -l | tr -d ' ')" >> $OUT
echo "Classes: $(grep -rl '^public class\|^public abstract class' src/main | wc -l | tr -d ' ')" >> $OUT

# FR-16: Cyclomatic complexity
echo "### FR-16: Cyclomatic Complexity (PMD)" >> $OUT
mvn pmd:pmd -q
python3 -c "
import xml.etree.ElementTree as ET
tree = ET.parse('target/pmd.xml')
vals = [int(v.get('endline',0)) for v in tree.findall('.//violation[@rule=\"CyclomaticComplexity\"]')]
# Simpler: count total violations
print('PMD CyclomaticComplexity violations:', len(tree.findall(\".//violation\")))
" >> $OUT 2>/dev/null || echo "(Parse pmd.xml manually)" >> $OUT

# FR-17: Build time (5 runs)
echo "### FR-17: Build Time (5 runs)" >> $OUT
for i in 1 2 3 4 5; do
  { time mvn clean package -DskipTests -q; } 2>&1 | grep real >> $OUT
done

# FR-18: Test time (5 runs)
echo "### FR-18: Test Execution Time (5 runs)" >> $OUT
for i in 1 2 3 4 5; do
  { time mvn test -q; } 2>&1 | grep real >> $OUT
done

# FR-19: Package depth
echo "### FR-19: Package Depth" >> $OUT
find src/main -name "*.java" -exec grep -m1 '^package' {} \; \
  | awk -F'.' '{print NF}' | sort -n | tail -1 >> $OUT

# FR-20: Runtime dependency count
echo "### FR-20: Runtime Dependency Count" >> $OUT
mvn dependency:list -q | grep ':compile\|:runtime' | grep -v ':test' | wc -l >> $OUT

# FR-21: Public method count
echo "### FR-21: Public Method Count" >> $OUT
grep -rn '^\s*public\s' src/main/java --include="*.java" \
  | grep -v 'class\|interface\|enum\|@interface' | wc -l >> $OUT

# FR-22: DI annotation count
echo "### FR-22: DI Annotation Count" >> $OUT
grep -rn '@Service\|@Autowired\|@Component\|@Repository\|@Controller\|@PostConstruct' \
  src/main/ | wc -l >> $OUT

# FR-13: JaCoCo (requires mvn test to have just run)
echo "### FR-13: JaCoCo Coverage" >> $OUT
python3 -c "
import xml.etree.ElementTree as ET
try:
    tree = ET.parse('target/site/jacoco/jacoco.xml')
    for c in tree.findall('.//counter[@type=\"INSTRUCTION\"]'):
        covered = int(c.get('covered',0)); missed = int(c.get('missed',0))
        total = covered + missed
        pct = 100.0 * covered / total if total > 0 else 0
        print(f'Instruction coverage: {pct:.1f}% ({covered}/{total})')
except: print('(Run mvn test first to generate JaCoCo report)')
" >> $OUT 2>/dev/null

# M-A: JAR size
echo "### M-A: JAR Artifact Size" >> $OUT
ls -l target/*.jar 2>/dev/null | awk '{print $5, "bytes:", $9}' >> $OUT

# M-B: Startup/execution time (5 runs)
echo "### M-B: Application Execution Time (5 runs)" >> $OUT
for i in 1 2 3 4 5; do
  { time java -jar target/*.jar > /dev/null; } 2>&1 | grep real >> $OUT
done

# M-D: Efferent coupling (ApplicationContext instantiation sites)
echo "### M-E: Spring Context Instantiation Sites" >> $OUT
grep -rn 'ClassPathXmlApplicationContext\|new.*ApplicationContext' src/main/ \
  | wc -l >> $OUT

# M-F: Test metrics
echo "### M-F: Test Count and Ratio" >> $OUT
echo "@Test methods: $(grep -rn '@Test' src/test/ | wc -l | tr -d ' ')" >> $OUT
cloc src/test/ 2>/dev/null >> $OUT

echo "--- Collection complete: $(date) ---" >> $OUT
```

---

## 5. Statistical Rigor

The following practices apply to all timing measurements (FR-17, FR-18, M-B):

| Practice | Rationale |
|----------|-----------|
| **5 runs minimum** (upgrade from FR-17/FR-18's 3) | More runs improve median stability; reduces outlier sensitivity |
| **Discard first run for M-B (application time)** | JVM class loading and OS filesystem cache warming inflate the first run |
| **Report min, median, max, and standard deviation** | Median alone conceals variance; std dev quantifies reproducibility |
| **No background CPU load during collection** | Close browser, IDEs, and other processes; note CPU governor mode |
| **Record JVM version and hardware** | Required for reproducibility claims (see §6) |
| **Sequential runs (no parallelism)** | Parallel builds or tests would contaminate timing data |

**Recommended timing formula:** For a set of N timing observations t₁, t₂, …, tₙ (in milliseconds):
- Report: `median`, `min`, `max`, `(max - min)` as range, and `stdev`
- If stdev > 20% of median, collect additional runs and investigate noise sources

---

## 6. Environment Specification

All metric collection must be performed in a documented, consistent environment. The following metadata must be recorded in each phase measurement file (`measurements/phase_01_baseline.md` and `measurements/phase_03_postrefactor.md`):

| Field | Collection Command |
|-------|--------------------|
| OS name and version | `uname -a` |
| CPU model and core count | `sysctl -n machdep.cpu.brand_string` (macOS) or `lscpu` (Linux) |
| Available RAM | `sysctl -n hw.memsize` (macOS) or `free -h` (Linux) |
| JVM version | `java -version 2>&1` |
| Maven version | `mvn -v` |
| Gradle version | `./gradlew --version` |
| `cloc` version | `cloc --version` |
| PMD version | from `mvn help:effective-pom` or `mvn pmd:pmd -version` |
| Date and time of collection | `date` |
| Git commit SHA | `git rev-parse HEAD` |
| Git branch | `git branch --show-current` |

> **Critical:** Pre-refactor metrics must be collected from the `uinverse` branch; post-refactor metrics from the `ai-refactor-experiment` branch. The same machine, same JVM, and same environment settings must be used for both collection sessions to ensure comparability.

---

## 7. Analysis and Reporting Plan

After both metric sets are collected (Phase 1 and Phase 3), Phase 4 produces a comparison table and trade-off narrative. The structure is defined in Appendix B of `PLAN_REVISED_1.md`. The following additions are proposed here:

### 7.1 Comparison Table Format

For each metric, the comparison table should include:

| Metric | Pre-Refactor | Post-Refactor | Absolute Change | % Change | Direction |
|--------|-------------|---------------|-----------------|----------|-----------|
| LOC (source lines) | — | — | — | — | ↓ expected |
| Class count | 87 | 3 | — | — | ↓ |
| Cyclomatic complexity (total) | — | — | — | — | ↓ expected |
| Build time (median, ms) | — | — | — | — | ↓ expected |
| Test time (median, ms) | — | — | — | — | ↓ expected |
| JaCoCo instruction coverage | — | — | — | — | ↑ expected (FR-13) |
| JAR size (bytes) | — | — | — | — | ↓ expected |
| Execution time (median, ms) | — | — | — | — | ↓ expected |
| Runtime dependencies | — | 0 | — | — | ↓ |
| DI annotation count | 80+ | 0 | — | — | ↓ |
| Spring context init count | 2 | 0 | −2 | −100% | ↓ |

### 7.2 Validity Threats to Address

- **Test validity (FR-12):** If any of the 16 oracle assertions fail post-refactor, the refactor is invalid regardless of metric improvements.
- **Coverage validity (FR-13):** If post-refactor JaCoCo coverage is lower than pre-refactor, this is a requirement violation and must be addressed before reporting.
- **Measurement confounds:** If metric collection sessions occur on different machines or at different times-of-day (with different background load), timing comparisons are not valid. Document any deviations.
- **JVM warm-up:** Build-tool timing includes JVM startup for Maven and Gradle processes; this partially offsets the Spring context startup advantage in M-B. Report both M-B and FR-18 to separate JVM startup from application logic.

---

## 8. Open Questions

The following items require decision before finalizing this plan:

| # | Question | Impact |
|---|----------|--------|
| OQ-1 | Should the JAR be built as a fat JAR (all dependencies bundled) for both pre- and post-refactor? If yes, M-A captures the full dependency footprint; if no, JAR size only reflects production source. | M-A methodology |
| OQ-2 | For M-C (heap usage), is `verbose:gc` log parsing sufficient, or should a profiler (e.g., JFR, VisualVM) be used? The latter is more accurate but adds setup complexity. | M-C collection |
| OQ-3 | Should T-04 (line separator test) be included in both pre- and post-refactor test suites? The pre-refactor system uses `NewLineStringReturner` which reads `System.getProperty("line.separator")` — the test should pass there too. | T-04 scope |
| OQ-4 | Should FR-17/FR-18 be upgraded from 3 runs to 5 runs to match the statistical rigor recommendation in §5? This would require a minor update to `REQUIREMENTS_REFINED_1.md`. | FR-17, FR-18 |
| OQ-5 | Should M-D (efferent coupling via PMD) be promoted to a formal requirement (FR-23), given its direct relevance to measuring coupling reduction? | Requirements scope |
| OQ-6 | The T-09 test calls `fizzBuzz(100)`. In the pre-refactor system, this involves two Spring context bootstraps per test run. Should a lower N (e.g., 30) be used to limit the runtime cost of this test? | T-09 parameter |
