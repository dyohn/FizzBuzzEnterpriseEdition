# Phase 4: Pre/Post Refactor Metric Comparison
**Date:** 2026-04-22
**Branch:** ai-refactor-experiment
**Pre-refactor commit:** 6b7f98bb (Phase 1 collection)
**Post-refactor commit:** 58dcc6f6 (Phase 3 collection)

---

## Comparison Table

| Metric (Req.) | Pre-Refactor | Post-Refactor | Δ Absolute | Δ % | Direction |
|---------------|-------------|---------------|-----------|-----|-----------|
| Test time — median (FR-18) | 3.213 s | 1.509 s | −1.704 s | −53.1% | ↓ ✓ |
| JaCoCo coverage % (FR-13) | 92.34% | 92.98% | +0.64 pp | +0.7% | ≥ ✓ |
| Build time — median (FR-17) | 1.531 s | 1.263 s | −0.268 s | −17.5% | ↓ ✓ |
| CC violations > threshold (FR-16) | 0 | 0 | 0 | — | = ✓ |
| Code lines — Java (FR-14) | 1,540 | 255 | −1,285 | −83.4% | ↓ ✓ |
| Total .java files (FR-15) | 87 | 1 | −86 | −98.9% | ↓ ✓ |
| Interfaces (FR-15) | 26 | 0 | −26 | −100% | ↓ ✓ |
| Max package depth (FR-19) | 10 | 7 | −3 | −30.0% | ↓ ✓ |
| Runtime dependencies (FR-20) | 8 | 0 | −8 | −100% | ↓ ✓ |
| Public methods (FR-21) | ~129 | 2 | ~−127 | ~−98.4% | ↓ ✓ |
| DI annotations — total (FR-22) | 81 | 0 | −81 | −100% | ↓ ✓ |
| JAR artifact size (M-A) | 78,945 B | 4,510 B | −74,435 B | −94.3% | ↓ ✓ |
| Execution time — median (M-B) | 0.335 s | 0.045 s | −0.290 s | −86.6% | ↓ ✓ |
| Peak Metaspace (M-C) | ~16.0 MB | ~4.4 MB | ~−11.6 MB | ~−72.5% | ↓ ✓ |
| Peak Java heap (M-C) | ~6 MB | ~0 MB (no GC) | ~−6 MB | ~−100% | ↓ ✓ |
| Ce violations (M-D) | 0 | 0 | 0 | — | = ✓ |
| Spring context init sites (M-E) | 2 | 0 | −2 | −100% | ↓ ✓ |
| @Test method count (M-F) | 12 | 12 | 0 | 0% | = (pre already expanded) |
| Test/production LOC ratio (M-F) | 0.162 | 10.6 (main-only) | +10.4 | +6,400% | ↑ ✓ |

**All metrics moved in the expected direction. FR-13 satisfied (post ≥ pre).**

---

## FR-13 Verification

| | Pre-Refactor | Post-Refactor | Requirement |
|-|-------------|---------------|-------------|
| Instructions covered | 928 / 1005 | 53 / 57 | post ≥ pre |
| Coverage % | 92.34% | **92.98%** | ≥ 92.34% ✓ |

The 4 missed post-refactor instructions are structurally unreachable:
- 3 instructions: `private FizzBuzz()` constructor (inaccessible by design)
- 1 instruction: `catch(IOException)` block (PrintStream absorbs the exception before it propagates; impossible to reach in a passing test)

---

## Timing Summary

### FR-17: Build Time

| Statistic | Pre | Post | Change |
|-----------|-----|------|--------|
| Min | 1.512 s | 1.251 s | −17.3% |
| **Median** | **1.531 s** | **1.263 s** | **−17.5%** |
| Max | 1.614 s | 1.276 s | −20.9% |
| Stdev | 0.040 s | 0.010 s | −75.0% |

Build time improvement is modest because Maven lifecycle overhead (dependency resolution, JaCoCo instrumentation setup) dominates, not source compilation. With 1 file vs 87, the compiler itself runs faster but is not the bottleneck.

### FR-18: Test Execution Time

| Statistic | Pre | Post | Change |
|-----------|-----|------|--------|
| Min | 3.078 s | 1.500 s | −51.2% |
| **Median** | **3.213 s** | **1.509 s** | **−53.1%** |
| Max | 3.239 s | 1.527 s | −52.8% |
| Stdev | 0.064 s | 0.010 s | −84.4% |

The ~1.7 s saving is almost entirely Spring context bootstrap elimination. Each pre-refactor test method triggered `new ClassPathXmlApplicationContext(...)` in setUp(), loading ~58 beans across 2 contexts. Post-refactor: direct static call, no framework. The remaining ~1.5 s is Maven startup overhead.

Actual JUnit execution time (from Maven output): **0.017 s** post-refactor vs. **not separately reported** pre-refactor (obscured by Spring logging).

### M-B: Application Execution Time

| Statistic | Pre | Post | Change |
|-----------|-----|------|--------|
| Min | 0.320 s | 0.045 s | −85.9% |
| **Median** | **0.335 s** | **0.045 s** | **−86.6%** |
| Max | 0.359 s | 0.048 s | −86.6% |
| Stdev | 0.014 s | 0.001 s | −92.9% |

The pre-refactor application instantiated two `ClassPathXmlApplicationContext` objects (in `Main.java` and `LoopContext.java`), each loading the full Spring XML configuration and performing component scan. This overhead (~0.29 s) is eliminated entirely.

**Note on methodological consistency:** Pre-refactor used `java -cp` with Maven-generated classpath (thin JAR cannot run standalone). Post-refactor uses `java -jar` (self-contained). Both measure the same conceptual thing (wall-clock time from JVM invocation to exit), but the pre-refactor number includes classloader path scanning overhead from multiple JARs on the classpath, which may introduce a small upward bias. The post-refactor number is likely a more accurate lower bound on true execution time.

---

## Requirement Satisfaction Matrix

| Requirement | Description | Pre | Post | Status |
|-------------|-------------|-----|------|--------|
| FR-01 | N output entries | ✓ | ✓ | Maintained |
| FR-02 | Ascending order | ✓ | ✓ | Maintained |
| FR-03 | "Fizz" for mult. of 3 | ✓ | ✓ | Maintained |
| FR-04 | "Buzz" for mult. of 5 | ✓ | ✓ | Maintained |
| FR-05 | "FizzBuzz" for mult. of 15 | ✓ | ✓ | Maintained |
| FR-06 | Integer string otherwise | ✓ | ✓ | Maintained |
| FR-07 | No preamble | ✓ | ✓ | Maintained |
| FR-08 | Platform line separator | ✓ | ✓ | Maintained |
| FR-09 | N=0 produces no output | ✓ | ✓ | Maintained |
| FR-10 | No trailing content | ✓ | ✓ | Maintained |
| FR-11 | Default upper limit 100 | ✓ | ✓ | Maintained |
| FR-12 | Oracle match N=1..16 | ✓ | ✓ | Maintained |
| FR-13 | JaCoCo coverage ≥ pre | — | 92.98% ≥ 92.34% | **PASS** |
| FR-14 | LOC reduced | — | −83.4% | **PASS** |
| FR-15 | Class count reduced | — | −98.9% | **PASS** |
| FR-16 | CC ≤ threshold | ✓ (0 violations) | ✓ (0 violations) | Maintained |
| FR-17 | Build time reduced | — | −17.5% | **PASS** |
| FR-18 | Test time reduced | — | −53.1% | **PASS** |
| FR-19 | Package depth reduced | — | −30.0% | **PASS** |
| FR-20 | Runtime deps reduced | — | −100% | **PASS** |
| FR-21 | Public methods reduced | — | −98.4% | **PASS** |
| FR-22 | DI annotations reduced | — | −100% | **PASS** |
| SR-01 | IOException suppressed | ✓ | ✓ | Maintained |
| SR-02 | Call independence | ✓ | ✓ | Maintained |
| SR-03 | Output via System.out.write() | ✓ | ✓ | Maintained |
| SR-04 | Per-entry flush | ✓ | ✓ | Maintained |
| SR-05 | No System.err output | ✓ | ✓ | Maintained |
| SR-06 | Deterministic output | ✓ | ✓ | Maintained |
| SR-07 | No exception propagation | ✓ | ✓ | Maintained |
| SR-08 | Correct modulo arithmetic | ✓ | ✓ | Maintained |
| NFR-05 | Oracle constants preserved | ✓ | ✓ | Maintained |
| NFR-09 | Zero runtime dependencies | ✗ (8 deps) | ✓ (0 deps) | **ACHIEVED** |

**All 31 requirements satisfied in post-refactor implementation.**

---

## Anomalies and Observations

### 1. Package Name Preserved (Intentional)
The post-refactor `FizzBuzz.java` lives in `com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl` — the original over-engineered package name. This was a deliberate requirement choice to preserve the satirical identity of the project. Package depth 7 (vs. 10 pre-refactor) reflects the elimination of sub-packages, not renaming the root.

### 2. FR-16 / M-D — No Violations in Either State
PMD's CyclomaticComplexity threshold is 10. The pre-refactor codebase achieved complexity via class proliferation (87 files, 26 interfaces), not branching depth within any single method. The post-refactor `fizzBuzz()` method has CC ≈ 5 (1 base + 3 conditional branches + 1 try/catch), still well below threshold. The PMD metric does not capture the pre-refactor's structural complexity.

### 3. FR-18 Remaining 1.5 s After Spring Removal
Post-refactor test time is 1.509 s median, not near-zero. The floor is Maven's own startup time (process startup, plugin loading, classpath construction, JaCoCo agent initialization). The actual JUnit 4 test execution time is 0.017 s. For full elimination of Maven overhead, a tool like directly-invoked `java -cp` JUnit runner would be needed — outside the scope of this experiment.

### 4. Test Count Unchanged (12 → 12)
The plan's M-F section noted "11 tests expected post-refactor." During the pre-execution audit (Phase 0), T-11 was added, making the pre-refactor test count 12. Since we carried those 12 tests forward to the post-refactor suite, the count remained 12. The test-to-production LOC ratio improved dramatically: 0.162 (pre) → 10.6 (post, counting src/main only).

### 5. M-B Methodological Note
Pre-refactor M-B used `java -cp` (Maven classpath), post-refactor uses `java -jar` (self-contained). The numbers are directionally comparable but not methodologically identical. The observed 86.6% reduction (0.335 s → 0.045 s) is attributable to Spring context elimination, with a small confound from classpath resolution differences.

---

*See `docs/refactor_tradeoffs.md` for qualitative analysis of gains and losses.*
