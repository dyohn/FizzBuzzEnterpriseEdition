# FizzBuzzEnterpriseEdition Refactor Plan — Initial
**Branch:** `ai-refactor-experiment`
**Status:** Draft — under revision

---

## Context

This is Phase 1 of a scientific experiment measuring AI assistance quality during software engineering. The subject codebase is an intentionally over-engineered Java implementation of FizzBuzz — 87 files, 8+ design patterns, Spring IoC, 7-level package depth — built as satire. The goal is to reduce it to the minimum complexity required to preserve identical external behavior (correct FizzBuzz output for any given N), then compare empirical pre/post metrics as experimental data.

**What this plan covers:** Baseline measurement → code refactor → build file updates → test rewrite → post-refactor measurement → trade-off documentation.

**Scientific note:** The scope of change (84 files deleted, 1 created) is closer to a *rewrite* than a classical incremental refactoring. This distinction will be documented in the scientific record.

---

## Pre-Refactor Baseline (Phase 0)

Run all commands before touching any file. Save output verbatim.

### A. Lines of Code
```bash
# Requires: brew install cloc
cloc src/ resources/ pom.xml build.gradle --by-file --out=baseline_cloc.txt && cat baseline_cloc.txt

# Raw Java line count
find src -name "*.java" | xargs wc -l | sort -rn
```

### B. Class and Interface Count
```bash
find src -name "*.java" | wc -l                                                        # total files
grep -rl "^public interface" src --include="*.java" | wc -l                            # interfaces
grep -rhl "^public.*class\|^public final class" src --include="*.java" | wc -l        # classes
```

### C. Cyclomatic Complexity — Add PMD Plugin to pom.xml
Add the following plugin block to `pom.xml` inside `<build><plugins>` **before the baseline run**. Keep it for post-refactor measurement too (do not remove it after baseline):

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
  </configuration>
</plugin>
```

```bash
mvn pmd:pmd    # report: target/pmd.xml
```

Fallback lightweight count (counts decision keywords across production source):
```bash
grep -rh "\bif\b\|\belse if\b\|\bfor\b\|\bwhile\b\|\bcase\b\|\bcatch\b\|&&\|||" \
  src/main --include="*.java" | wc -l
```

### D. Build Time (3 runs, record median)
```bash
mvn clean && time mvn package -DskipTests 2>&1 | tee baseline_build.txt
```

### E. Test Runtime (3 runs, record median)
```bash
time mvn test 2>&1 | tee baseline_test.txt
```

---

## Target Architecture

**One production class.** Package is preserved (satirical name is part of the project identity; sub-packages below `impl` are eliminated).

**File to create:**
`src/main/java/com/seriouscompany/business/java/fizzbuzz/packagenamingpackage/impl/FizzBuzz.java`

```java
package com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl;

/**
 * FizzBuzz: minimal single-class replacement for the original 87-file enterprise edition.
 *
 * Behavioral contract (preserved from original):
 *   fizzBuzz(n) loops from 1 to n inclusive and writes to System.out, one entry per
 *   iteration followed by the platform line separator:
 *     "Fizz"     for multiples of 3
 *     "Buzz"     for multiples of 5
 *     "FizzBuzz" for multiples of both 3 and 5
 *     the integer itself otherwise
 *
 * Output mechanism mirrors SystemOutFizzBuzzOutputStrategy:
 *   System.out.write(bytes) + System.out.flush() per entry, so the test's
 *   System.setOut() redirect captures output correctly.
 * IOException is swallowed to match FizzBuzzOutputStrategyToFizzBuzzExceptionSafeOutputStrategyAdapter.
 * Line separator uses System.getProperty("line.separator") to match NewLineStringReturner
 * and the test's platform-normalization logic.
 */
public final class FizzBuzz {

    private FizzBuzz() {}

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
                // swallowed intentionally — matches original adapter behavior
            }
        }
    }

    public static void main(final String[] args) {
        fizzBuzz(100);   // 100 = original DEFAULT_FIZZ_BUZZ_UPPER_LIMIT_PARAMETER_VALUE
    }
}
```

**Why "FizzBuzz" for multiples of 15 is correct:** The original visitor fires `FizzStrategy` then `BuzzStrategy` independently for each number. For 15, both fire (printing "Fizz" then "Buzz" in sequence with no separator), then `NoFizzNoBuzzStrategy` does not fire. `TestConstants` confirms the expected token is `"FizzBuzz"` (a single combined string). The `i % 3 == 0 && i % 5 == 0` guard replicates this.

---

## Phase 1: Delete Production Files

All paths below `src/main/java/.../packagenamingpackage/` — **entire directories:**
```
interfaces/
impl/factories/
impl/loop/
impl/math/
impl/parameters/
impl/printers/
impl/strategies/
impl/stringreturners/
impl/visitors/
```

**Individual files in `impl/`:**
```
ApplicationContextHolder.java
Constants.java
Main.java
StandardFizzBuzz.java
```

**Also delete:**
```
resources/    (entire directory — contains only spring.xml)
```

---

## Phase 2: Update Build Files

### pom.xml — changes only
- **Remove:** all five `spring-*` `<dependency>` blocks
- **Remove:** the `<resources>` block (pointing to deleted `resources/` dir)
- **Update:** `<mainClass>` from `...impl.Main` → `...impl.FizzBuzz`
- **Keep:** `junit:junit:4.8.2`, `maven-compiler-plugin`, `maven-jar-plugin`, `jacoco-maven-plugin` (all at original versions)
- **Keep:** PMD plugin added in Phase 0

### build.gradle — changes only
- **Remove:** all five `spring-*` entries from the `compile` configuration block
- **Remove:** `resources { srcDir '...' }` block inside `sourceSets`
- **Update:** `mainClassName` from `...impl.Main` → `...impl.FizzBuzz`
- **Keep:** `testCompile 'junit:junit:4.8.2'`

**Note on `jcenter()`:** JCenter is deprecated. If dependency resolution fails, replace with `mavenCentral()`. Document in scientific record as a pre-existing issue, not introduced by this refactor.

---

## Phase 3: Rewrite Tests

### TestConstants.java
Remove only the two Spring-specific constants. Keep everything else byte-for-byte:
- **Remove:** `static final String STANDARD_FIZZ_BUZZ = "standardFizzBuzz";`
- **Remove:** `static final String SPRING_XML = "spring.xml";`
- **Keep:** all 16 expected-output string constants, all 16 integer constants

### FizzBuzzTest.java
Structural changes only — preserve System.out capture, `doFizzBuzz` helper, all 16 assertions:

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

public class FizzBuzzTest {

    private PrintStream out;

    @Before
    public void setUp() {
        this.out = System.out;
        // Spring context startup removed; no wiring needed for static method
    }

    @After
    public void tearDown() {
        System.setOut(this.out);
    }

    private void doFizzBuzz(final int n, final String s) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final BufferedOutputStream bos = new BufferedOutputStream(baos);
        System.setOut(new PrintStream(bos));

        FizzBuzz.fizzBuzz(n);   // static call replaces Spring bean dispatch

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

## Phase 4: Post-Refactor Measurement

Run the exact same commands as Phase 0 with `postrefactor_` prefixed output files.

**Expected outcomes:**

| Metric | Pre (approx.) | Post (expected) |
|--------|--------------|-----------------|
| Java files | 87 prod + 2 test = 89 | 1 prod + 2 test = 3 |
| Interfaces | 26 | 0 |
| Concrete classes | 61 | 1 (+ 2 test) |
| Cyclomatic complexity (prod) | ~50–80 decision points | 7 (one method) |
| Build time | TBD baseline | Expected significantly faster (no Spring JARs to link) |
| Test runtime | TBD baseline | Expected significantly faster (no Spring context startup) |

---

## Phase 5: Trade-off Documentation

Create `docs/refactor-tradeoffs.md` capturing:

**Lost — Extensibility:**
- Strategy pattern: New divisibility rules (e.g., "Bazz" for 7) require editing the `if/else` chain rather than implementing an interface
- Factory pattern: Output target (stdout → file) requires code change rather than factory swap
- Visitor pattern: New output contexts require structural change
- Spring DI: Implementation swapping via configuration is no longer possible

**Lost — Testability character:**
- Original: every sub-operation independently unit-testable (`FizzStrategy`, `LoopCondition`, `IntegerDivider`, etc.)
- Refactored: only integration-level testing of `fizzBuzz(n)` is structurally possible

**Gained:**
- No Spring startup cost in tests
- Zero external dependencies in production code
- Cyclomatic complexity reduced from ~50–80 to 7
- 87 files → 1 file

**Scientific classification note:**
> Deleting 84 files and creating 1 replacement is a *rewrite*, not a classical incremental refactoring (which preserves internal structure while improving it). The experiment therefore measures AI-assisted rewriting to a minimum-viable implementation, not stepwise refactoring. This distinction should be stated explicitly in the experimental methodology.

---

## Execution Order

1. **Phase 0** — collect all baseline metrics (before any file change)
2. **Add PMD plugin** to `pom.xml` (needed for CC measurement)
3. **Create** `FizzBuzz.java` (new production class)
4. **Update** `pom.xml` and `build.gradle` (remove Spring, update mainClass)
5. **Delete** all old production source files and `resources/`
6. **Rewrite** `FizzBuzzTest.java` and `TestConstants.java`
7. `mvn clean compile` — verify no compile errors
8. `mvn test` — verify all 16 assertions pass
9. **Phase 4** — collect post-refactor metrics
10. **Phase 5** — write `docs/refactor-tradeoffs.md`
11. Commit with structured message for experimental record

---

## File Summary

| Operation | Files |
|-----------|-------|
| Create | `impl/FizzBuzz.java` (1 file) |
| Replace | `pom.xml`, `build.gradle`, `FizzBuzzTest.java`, `TestConstants.java` (4 files) |
| Delete | 83 production `.java` files + `resources/spring.xml` |
| Untouched | `settings.gradle`, `gradlew*`, `gradle/`, `README.md`, `CONTRIBUTING.md`, `CLAUDE.md` |
