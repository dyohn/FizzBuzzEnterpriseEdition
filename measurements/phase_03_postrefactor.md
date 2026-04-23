# Phase 03: Post-Refactor Measurements
**Document ID:** phase_03_postrefactor
**Date:** 2026-04-22
**Git commit:** 58dcc6f69f023c17be9662906a2c61b15e1f4618
**Git branch:** ai-refactor-experiment
**Collection script run:** `./scripts/collect_metrics.sh post` → `measurements/metrics_post.md`

---

## Environment

| Tool    | Version                           |
|---------|-----------------------------------|
| Java    | OpenJDK 11.0.25 Temurin (aarch64) |
| Maven   | 3.9.12                            |
| Gradle  | 6.9.4                             |
| python3 | 3.13.9                            |
| cloc    | 2.08                              |
| OS      | Darwin 24.6.0 (macOS, arm64)      |
| CPU     | Apple M3 Pro                      |
| RAM     | 36 GB                             |

---

## FR-13: JaCoCo Instruction Coverage

Source: `measurements/postrefactor_jacoco.xml`

| Metric | Value |
|--------|-------|
| Instructions covered | 53 |
| Instructions missed  | 4  |
| Instructions total   | 57 |
| **Coverage %**       | **92.98%** |

The 4 missed instructions are the unreachable `private FizzBuzz()` constructor body (3 instructions) and 1 instruction in the `catch(IOException)` block (PrintStream suppresses the exception before it propagates, so the catch body is never reached in testing).

**FR-13 PASS:** 92.98% ≥ 92.34% (pre-refactor baseline) ✓

Artifact: `measurements/postrefactor_jacoco.xml`

---

## FR-14: Lines of Code (cloc)

Source: `measurements/postrefactor_cloc_summary.txt`

| Language | Files | Blank | Comment | **Code** |
|----------|-------|-------|---------|----------|
| Java     | 3     | 41    | 121     | **255**  |
| Maven    | 1     | 6     | 0       | 77       |
| Gradle   | 1     | 4     | 0       | 11       |
| **SUM**  | **5** | **51**| **121** | **343**  |

Production Java LOC (src/main only, 1 file): 22 code lines.
Artifact: `measurements/postrefactor_cloc_detail.txt`

---

## FR-15: Class and Interface Counts

| Type | Count |
|------|-------|
| Total Java files (src/main) | **1** |
| Interfaces | **0** |
| Abstract classes | **0** |
| Enums | **0** |
| Concrete classes | 1 (`FizzBuzz`) |

---

## FR-16: Cyclomatic Complexity (PMD)

Source: `measurements/postrefactor_pmd.xml`

| Metric | Value |
|--------|-------|
| CyclomaticComplexity violations (CC > 10) | **0** |
| Estimated CC of `fizzBuzz()` method | ≈ 5 (1 base + 3 if-branches + 1 try/catch) |

Single method, no violations. CC well below threshold.

Artifact: `measurements/postrefactor_pmd.xml`

---

## FR-17: Build Time

Command: `mvn clean package -DskipTests` (5 runs)

| Run | Time (s) |
|-----|----------|
| 1   | 1.253    |
| 2   | 1.263    |
| 3   | 1.266    |
| 4   | 1.251    |
| 5   | 1.276    |

| Statistic | Value |
|-----------|-------|
| Min       | 1.251 s |
| **Median**| **1.263 s** |
| Max       | 1.276 s |
| Mean      | 1.262 s |
| Stdev     | 0.010 s |
| Stdev/Median | 0.8% ✓ (< 20% threshold) |

---

## FR-18: Test Execution Time

Command: `mvn test` (5 runs, each preceded by `mvn clean`)

| Run | Time (s) |
|-----|----------|
| 1   | 1.505    |
| 2   | 1.527    |
| 3   | 1.509    |
| 4   | 1.500    |
| 5   | 1.510    |

| Statistic | Value |
|-----------|-------|
| Min       | 1.500 s |
| **Median**| **1.509 s** |
| Max       | 1.527 s |
| Mean      | 1.510 s |
| Stdev     | 0.010 s |
| Stdev/Median | 0.7% ✓ (< 20% threshold) |

Note: Remaining time is Maven startup + JUnit runner overhead. The actual test execution is 0.017 s (per `mvn test` output); the remaining ~1.5 s is Maven lifecycle scaffolding.

---

## FR-19: Package Depth

| Metric | Value |
|--------|-------|
| Maximum package depth | **7** |

Package: `com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl` (7 levels — `com`, `seriouscompany`, `business`, `java`, `fizzbuzz`, `packagenamingpackage`, `impl`).

Note: Pre-refactor depth was 10 because the deepest classes lived in sub-packages of `impl` (e.g., `impl/strategies/comparators/integercomparator`). Post-refactor, `FizzBuzz.java` lives directly in `impl`.

---

## FR-20: Runtime Dependency Count

Command: `mvn dependency:list -DincludeScope=compile` (without `-q`)

| Compile-scope dependencies | **0** |
|---------------------------|-------|

No runtime dependencies beyond the JDK. Spring and all transitive dependencies removed.

---

## FR-21: Public Method Count

| Metric | Value |
|--------|-------|
| Approximate public method count | **2** |

Methods: `public static void fizzBuzz(int n)` and `public static void main(String[] args)`.

---

## FR-22: DI Annotation Count

| Annotation | Count |
|------------|-------|
| @Service | 0 |
| @Autowired | 0 |
| @Component | 0 |
| @Repository | 0 |
| @Controller | 0 |
| @PostConstruct | 0 |
| **Total** | **0** |

---

## M-A: JAR Artifact Size

| Metric | Value |
|--------|-------|
| JAR file | `target/FizzBuzzEnterpriseEdition-1.0-SNAPSHOT.jar` |
| **Size** | **4,510 bytes (4.4 KB)** |

Self-contained executable JAR (no external dependencies required at runtime).

---

## M-B: Application Execution Time

Command: `java -jar target/FizzBuzzEnterpriseEdition-1.0-SNAPSHOT.jar`
(6 runs; run 1 discarded; statistics over runs 2–6)

**Methodological note:** Post-refactor uses `java -jar` directly (self-contained JAR). Pre-refactor required `java -cp` with Maven classpath.

| Run | Time (s) |
|-----|----------|
| 1 (discarded) | 0.048 |
| 2   | 0.045    |
| 3   | 0.045    |
| 4   | 0.045    |
| 5   | 0.046    |
| 6   | 0.048    |

| Statistic | Value |
|-----------|-------|
| Min       | 0.045 s |
| **Median**| **0.045 s** |
| Max       | 0.048 s |
| Mean      | 0.046 s |
| Stdev     | 0.001 s |
| Stdev/Median | 2.9% ✓ (< 20% threshold) |

---

## M-C: Peak Heap Memory Usage

Command: `java -Xmx64m -Xms4m -Xlog:gc*:... -Xlog:gc+heap+exit -jar target/*.jar`

| Metric | Value |
|--------|-------|
| GC events triggered | **0** |
| Java heap at exit (total/used) | 8,192K total / **0K used** |
| **Peak Metaspace (class data)** | **4,482K (~4.4 MB)** |
| Peak class space | 393K |

No garbage collection was triggered — the entire application ran within the initial 4 MB heap allocation without filling even one G1 region. Total JVM footprint ≈ **4.4 MB** (Metaspace only; heap usage negligible).

Artifact: `measurements/postrefactor_gc.log`, `measurements/postrefactor_gc2.log`

---

## M-D: Efferent Coupling (CouplingBetweenObjects via PMD)

Source: `measurements/postrefactor_pmd.xml`

| Metric | Value |
|--------|-------|
| CouplingBetweenObjects violations | **0** |

Single class with only JDK dependencies (`java.lang`, `java.io`). Ce = 0.

---

## M-E: Spring ApplicationContext Instantiation Count

| Metric | Value |
|--------|-------|
| `new ClassPathXmlApplicationContext()` call sites | **0** |

Spring completely removed.

---

## M-F: Test Metrics

| Metric | Value |
|--------|-------|
| `@Test` methods | **12** |
| Test source files | 2 (`FizzBuzzTest.java`, `TestConstants.java`) |
| Test code lines (cloc) | 233 |
| Production Java code lines (cloc total) | 255 (all Java) |
| Production Java code lines (src/main only) | 22 |
| **Test-to-production LOC ratio (all Java)** | **233 / 255 = 0.914** |
| **Test-to-production LOC ratio (main only)** | **233 / 22 = 10.6** |

---

## Summary Table

| Metric ID | Metric | Post-Refactor Value |
|-----------|--------|---------------------|
| FR-13 | JaCoCo instruction coverage | **92.98%** (53/57) |
| FR-14 | Lines of code (Java, cloc, all) | **255** |
| FR-14 | Lines of code (all sources) | **343** |
| FR-15 | Java source files (src/main) | **1** |
| FR-15 | Interfaces | **0** |
| FR-15 | Enums | **0** |
| FR-16 | PMD CyclomaticComplexity violations | **0** |
| FR-17 | Build time (median, 5 runs) | **1.263 s** |
| FR-18 | Test execution time (median, 5 runs) | **1.509 s** |
| FR-19 | Maximum package depth | **7** |
| FR-20 | Compile-scope runtime dependencies | **0** |
| FR-21 | Approximate public method count | **2** |
| FR-22 | DI annotation total | **0** |
| M-A | JAR artifact size | **4,510 bytes (4.4 KB)** |
| M-B | Application execution time (median) | **0.045 s** |
| M-C | Peak heap memory | **~0 KB Java heap + ~4.4 MB Metaspace** |
| M-D | PMD CouplingBetweenObjects violations | **0** |
| M-E | Spring ApplicationContext instantiations | **0** |
| M-F | @Test method count | **12** |
| M-F | Test-to-production LOC ratio (main only) | **10.6** |

---

## Artifacts Committed

| File | Contents |
|------|----------|
| `measurements/metrics_post.md` | Raw collection script output |
| `measurements/postrefactor_cloc_summary.txt` | cloc language summary |
| `measurements/postrefactor_cloc_detail.txt` | cloc per-file breakdown |
| `measurements/postrefactor_pmd.xml` | Full PMD violation report |
| `measurements/postrefactor_jacoco.xml` | JaCoCo instruction coverage XML |
| `measurements/postrefactor_gc.log` | JVM GC log (no events triggered) |
| `measurements/postrefactor_gc2.log` | JVM GC log with heap-at-exit stats |
| `measurements/postrefactor_deps.txt` | Maven compile-scope dependency list (empty) |
| `measurements/postrefactor_mb_runs.txt` | Raw M-B timing output |

---

*Collection complete: 2026-04-22*
