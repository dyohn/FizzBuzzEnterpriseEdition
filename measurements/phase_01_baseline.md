# Phase 01: Pre-Refactor Baseline Measurements
**Document ID:** phase_01_baseline
**Date:** 2026-04-22
**Git commit:** 6b7f98bb65590a246ac92f80726b23c760dcd4ff
**Git branch:** ai-refactor-experiment
**Collection script run:** `./scripts/collect_metrics.sh pre` → `measurements/metrics_pre.md`

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

Source: `measurements/baseline_jacoco.xml` (captured after 5th `mvn test` run)

| Metric | Value |
|--------|-------|
| Instructions covered | 928 |
| Instructions missed  | 77  |
| Instructions total   | 1005 |
| **Coverage %**       | **92.34%** |

Artifact: `measurements/baseline_jacoco.xml`

---

## FR-14: Lines of Code (cloc)

Source: `measurements/baseline_cloc_summary.txt`

| Language | Files | Blank | Comment | **Code** |
|----------|-------|-------|---------|----------|
| Java     | 89    | 518   | 814     | **1540** |
| Maven    | 1     | 6     | 0       | 113      |
| Gradle   | 1     | 6     | 0       | 27       |
| XML      | 1     | 2     | 0       | 11       |
| **SUM**  | **92**| **532**| **814**| **1691** |

Production Java LOC (src/main only, excludes test): estimated ~1290 of 1540 total Java lines.
Artifact: `measurements/baseline_cloc_detail.txt`

---

## FR-15: Class and Interface Counts

| Type | Count |
|------|-------|
| Total Java files (src/main) | 87 |
| Interfaces | 26 |
| Abstract classes | 0 |
| Enums | 1 |
| Concrete classes (derived) | 60 |

---

## FR-16: Cyclomatic Complexity (PMD)

Source: `measurements/baseline_pmd.xml`

| Metric | Value |
|--------|-------|
| CyclomaticComplexity violations (CC > 10) | **0** |
| Max CC (estimated from keyword count) | ≤ 10 |
| Keyword-branch approximation (if/for/while/case/catch/&&/\|\|) | **100** occurrences across 87 files |

PMD threshold: 10. No violations means all methods have CC ≤ 10. The codebase is intentionally decomposed into trivial single-responsibility methods — complexity is achieved via quantity of classes, not branching depth.

Artifact: `measurements/baseline_pmd.xml`

---

## FR-17: Build Time

Command: `mvn clean package -DskipTests` (5 runs)

| Run | Time (s) |
|-----|----------|
| 1   | 1.614    |
| 2   | 1.512    |
| 3   | 1.541    |
| 4   | 1.531    |
| 5   | 1.524    |

| Statistic | Value |
|-----------|-------|
| Min       | 1.512 s |
| **Median**| **1.531 s** |
| Max       | 1.614 s |
| Mean      | 1.544 s |
| Stdev     | 0.040 s |
| Stdev/Median | 2.6% ✓ (< 20% threshold) |

---

## FR-18: Test Execution Time

Command: `mvn test` (5 runs, each preceded by `mvn clean`)

| Run | Time (s) |
|-----|----------|
| 1   | 3.078    |
| 2   | 3.198    |
| 3   | 3.213    |
| 4   | 3.239    |
| 5   | 3.220    |

| Statistic | Value |
|-----------|-------|
| Min       | 3.078 s |
| **Median**| **3.213 s** |
| Max       | 3.239 s |
| Mean      | 3.190 s |
| Stdev     | 0.064 s |
| Stdev/Median | 2.0% ✓ (< 20% threshold) |

---

## FR-19: Package Depth

| Metric | Value |
|--------|-------|
| Maximum package depth | **10** |

Deepest package: `com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.*` (10 levels)

---

## FR-20: Runtime Dependency Count

Command: `mvn dependency:list -DincludeScope=compile` (without `-q` to avoid output suppression)

| Dependency | Scope |
|------------|-------|
| javax.annotation:javax.annotation-api:1.3.2 | compile |
| org.springframework:spring-aop:3.2.13.RELEASE | compile |
| aopalliance:aopalliance:1.0 | compile |
| org.springframework:spring-beans:3.2.13.RELEASE | compile |
| org.springframework:spring-context:3.2.13.RELEASE | compile |
| org.springframework:spring-core:3.2.13.RELEASE | compile |
| commons-logging:commons-logging:1.1.3 | compile |
| org.springframework:spring-expression:3.2.13.RELEASE | compile |

**Total compile-scope dependencies: 8**

Note: `collect_metrics.sh` incorrectly reported 0 due to `-q` flag suppressing dependency list output. Corrected by re-running without `-q`.

---

## FR-21: Public Method Count

| Metric | Value |
|--------|-------|
| Approximate public method count | **129** |

Counted via `grep "public [a-zA-Z].*(" src/main/java --include="*.java"` excluding class/interface/enum declarations.

---

## FR-22: DI Annotation Count

| Annotation | Count |
|------------|-------|
| @Service | 51 |
| @Autowired | 29 |
| @Component | 0 |
| @Repository | 0 |
| @Controller | 0 |
| @PostConstruct | 1 |
| **Total** | **81** |

---

## M-A: JAR Artifact Size

Command: `mvn clean package -DskipTests -q`

| Metric | Value |
|--------|-------|
| JAR file | `target/FizzBuzzEnterpriseEdition-1.0-SNAPSHOT.jar` |
| **Size** | **78,945 bytes (77.1 KB)** |

Note: This is a thin JAR (application classes only; Spring dependencies not bundled).

---

## M-B: Application Execution Time

Command: `java -cp target/FizzBuzzEnterpriseEdition-1.0-SNAPSHOT.jar:<maven-classpath> com...Main`
(6 runs; run 1 discarded as cold-start; statistics computed over runs 2–6)

**Methodological note:** Pre-refactor uses `java -cp` with full Maven dependency classpath because the thin JAR does not bundle Spring. Post-refactor will use a self-contained approach. The measurement captures JVM startup + Spring `ClassPathXmlApplicationContext` initialization (×2) + FizzBuzz execution.

| Run | Time (s) |
|-----|----------|
| 1 (discarded) | 0.338 |
| 2   | 0.320    |
| 3   | 0.335    |
| 4   | 0.335    |
| 5   | 0.359    |
| 6   | 0.338    |

| Statistic | Value |
|-----------|-------|
| Min       | 0.320 s |
| **Median**| **0.335 s** |
| Max       | 0.359 s |
| Mean      | 0.337 s |
| Stdev     | 0.014 s |
| Stdev/Median | 4.2% ✓ (< 20% threshold) |

---

## M-C: Peak Heap Memory Usage

Command: `java -Xmx64m -Xms8m -Xlog:gc* -cp ... com...Main`

| Metric | Value |
|--------|-------|
| GC events triggered | 17 |
| Peak heap before GC | **6 MB** |
| Peak heap after GC  | 4 MB |
| Peak Metaspace (class data) | **~15.8 MB** (16,221 KB) |
| Heap ceiling (-Xmx) | 64 MB |

Spring's component scan loads ~58 beans and ~16 MB of class metadata into Metaspace. Java heap peaks at ~6 MB. Total JVM memory footprint ≈ **22 MB**.

Artifact: `measurements/baseline_gc_unified.log`

---

## M-D: Efferent Coupling (CouplingBetweenObjects via PMD)

Source: `measurements/baseline_pmd.xml`

| Metric | Value |
|--------|-------|
| CouplingBetweenObjects violations | **0** |

PMD default threshold: 10 dependencies per class. No violations — each class couples to ≤ 10 others. Similar to FR-16, the codebase achieves complexity via proliferation of thin classes rather than high coupling within any individual class.

---

## M-E: Spring ApplicationContext Instantiation Count

| Metric | Value |
|--------|-------|
| `new ClassPathXmlApplicationContext()` call sites | **2** |

Sites:
1. `Main.java:20` — top-level application entry point
2. `LoopContext.java:28` — creates a second context inside the loop

Note: `grep` matched 5 lines total (includes 2 import statements and 1 inner-class `ApplicationContextReferenceUpdater` name). Actual instantiation count = 2.

---

## M-F: Test Metrics

| Metric | Value |
|--------|-------|
| `@Test` methods | **12** |
| Test source files | 2 (`FizzBuzzTest.java`, `TestConstants.java`) |
| Test code lines (cloc) | 250 |
| Production Java code lines (cloc total) | 1540 |
| **Test-to-production LOC ratio** | **250 / 1540 = 0.162** |

---

## Summary Table

| Metric ID | Metric | Pre-Refactor Value |
|-----------|--------|-------------------|
| FR-13 | JaCoCo instruction coverage | 92.34% (928/1005) |
| FR-14 | Lines of code (Java, cloc) | 1540 |
| FR-14 | Lines of code (all sources) | 1691 |
| FR-15 | Java source files (src/main) | 87 |
| FR-15 | Interfaces | 26 |
| FR-15 | Enums | 1 |
| FR-16 | PMD CyclomaticComplexity violations | 0 (all methods CC ≤ 10) |
| FR-17 | Build time (median, 5 runs) | 1.531 s |
| FR-18 | Test execution time (median, 5 runs) | 3.213 s |
| FR-19 | Maximum package depth | 10 |
| FR-20 | Compile-scope runtime dependencies | 8 |
| FR-21 | Approximate public method count | 129 |
| FR-22 | DI annotation total | 81 (@Service:51, @Autowired:29, @PostConstruct:1) |
| M-A | JAR artifact size | 78,945 bytes (77.1 KB) |
| M-B | Application execution time (median) | 0.335 s |
| M-C | Peak heap memory | ~6 MB Java heap + ~16 MB Metaspace |
| M-D | PMD CouplingBetweenObjects violations | 0 (all classes Ce ≤ 10) |
| M-E | Spring ApplicationContext instantiations | 2 (Main.java + LoopContext.java) |
| M-F | @Test method count | 12 |
| M-F | Test-to-production LOC ratio | 0.162 |

---

## Artifacts Committed

| File | Contents |
|------|----------|
| `measurements/metrics_pre.md` | Raw collection script output |
| `measurements/baseline_cloc_summary.txt` | cloc language summary |
| `measurements/baseline_cloc_detail.txt` | cloc per-file breakdown |
| `measurements/baseline_pmd.xml` | Full PMD violation report |
| `measurements/baseline_jacoco.xml` | JaCoCo instruction coverage XML |
| `measurements/baseline_gc_unified.log` | JVM GC log (heap/Metaspace data) |
| `measurements/baseline_deps.txt` | Maven compile-scope dependency list |
| `measurements/baseline_classpath.txt` | Maven runtime classpath (for M-B runs) |
| `measurements/baseline_mb_runs.txt` | Raw M-B timing output |

---

*Collection complete: 2026-04-22*
