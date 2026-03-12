# FizzBuzzEnterpriseEdition — Equivalence Requirements
**Document ID:** REQUIREMENTS_REFINED_1
**Supersedes:** REQUIREMENTS_INITIAL
**Branch:** `ai-refactor-experiment`
**Status:** Draft — under revision

---

## Purpose

This document defines the requirements that must be satisfied by **both** the pre-refactor and post-refactor implementations of FizzBuzzEnterpriseEdition. Requirements are written as invariants: statements that are true of the original system and must remain true of the refactored system. Collectively, they constitute the proof obligations needed to demonstrate that the two systems are **semantically**, **functionally**, and **non-functionally equivalent**.

Any refactored implementation that satisfies every requirement in this document is considered a valid replacement for the original.

---

## Definitions

| Term | Definition |
|------|-----------|
| **System** | The FizzBuzzEnterpriseEdition application in either its pre- or post-refactor form |
| **fizzBuzz(N)** | The core operation: accepts a positive integer N and produces output to `System.out` |
| **Output** | The byte sequence written to the standard output stream during a single call to `fizzBuzz(N)` |
| **Line separator** | The value of `System.getProperty("line.separator")` at the time of the call |
| **Token** | A single output string ("Fizz", "Buzz", "FizzBuzz", or a decimal integer string) for one iteration |
| **Entry** | A token concatenated with the line separator |
| **Oracle** | `TestConstants.java` — the authoritative set of expected output strings for N = 1 through 16 |
| **Pre-refactor** | The original 87-file Spring-based implementation on the `uinverse` branch |
| **Post-refactor** | The simplified single-class implementation produced by this experiment |

---

## Section 1: Functional Requirements

These requirements define the exact observable output of `fizzBuzz(N)` for all valid inputs. Both systems must satisfy all of them identically.

### FR-01 — Output Cardinality
For any call `fizzBuzz(N)` where N ≥ 1, the output must contain exactly N entries.

### FR-02 — Output Ordering
Entries must appear in strictly ascending order of iteration index i, from i = 1 to i = N inclusive.

### FR-03 — Fizz Rule
For each iteration index i where `i % 3 == 0` AND `i % 5 != 0`, the token for that entry must be the string `"Fizz"`.

### FR-04 — Buzz Rule
For each iteration index i where `i % 5 == 0` AND `i % 3 != 0`, the token for that entry must be the string `"Buzz"`.

### FR-05 — FizzBuzz Rule
For each iteration index i where `i % 3 == 0` AND `i % 5 == 0`, the token for that entry must be the string `"FizzBuzz"`.
> *Note: In the original system, "FizzBuzz" is produced by the Fizz printer emitting "Fizz" immediately followed by the Buzz printer emitting "Buzz" with no separator between them. The net byte output is identical to the string `"FizzBuzz"`. Both implementations must produce this same byte sequence.*

### FR-06 — Integer Rule
For each iteration index i where `i % 3 != 0` AND `i % 5 != 0`, the token for that entry must be the decimal string representation of i with no leading zeros and no sign prefix (e.g., i=7 → `"7"`, i=14 → `"14"`).

### FR-07 — Entry Termination
Every entry (token + line separator) must be followed immediately by the platform line separator and nothing else. No entry may be preceded by a separator, and there must be no trailing content after the last entry's separator.

### FR-08 — Line Separator Platform Sensitivity
The line separator used by the system must be `System.getProperty("line.separator")` — the JVM platform default at runtime. The system must not hardcode `"\n"` or `"\r\n"`.

### FR-09 — Empty Output for N = 0
For a call `fizzBuzz(0)`, no output must be produced (zero entries, zero bytes written to `System.out`).

### FR-10 — Entry Boundary
The first entry begins at byte offset 0 in the output. No preamble, header, or prefix is emitted before the first entry.

### FR-11 — Main Entry Point Output
Invoking the system via its `main(String[] args)` method (with no arguments) must result in a call to `fizzBuzz(N)` for some positive integer N. Both the pre-refactor and post-refactor systems shall use the same value of N in their respective `main()` implementations. The output produced must conform to FR-03 through FR-08 for all N iterations. For all iterations 1 through 16, the output must be oracle-conformant per FR-12.
> *Note: Direct automated validation of `main()` output is limited to oracle conformance for N = 1 through 16. Output beyond N = 16 is not covered by the existing test suite and is verified by manual inspection only.*

### FR-12 — Oracle Conformance
For each N in {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, the output of `fizzBuzz(N)` must exactly match the corresponding expected string in `TestConstants`, after substituting all `"\n"` literals in the expected string with `System.getProperty("line.separator")`.

### FR-13 — Test Coverage Equivalence
The JaCoCo instruction coverage percentage of production source code (excluding test classes) must be measured for both systems. The post-refactor system's JaCoCo instruction coverage must be equal to or greater than the pre-refactor system's JaCoCo instruction coverage.
> *Rationale: A simplification that reduces coverage is not a valid equivalence-preserving refactor. The post-refactor codebase, having fewer classes and branches, is expected to achieve higher or equal coverage with the same test suite.*

---

### Measurement Requirements

The following requirements define the empirical data that must be collected for both the pre- and post-refactor systems. These measurements constitute the experimental record and enable quantitative comparison. Values are not required to be equivalent; differences are expected and are the subject of the experiment.

### FR-14 — Lines of Code Measurement
The following line count categories must be recorded for the `src/` and `resources/` directories, plus `pom.xml` and `build.gradle`, using `cloc` (or equivalent):
- Total lines
- Blank lines
- Comment lines
- Source (code) lines

Both pre- and post-refactor values must be recorded and reported side by side.

### FR-15 — Class and Interface Count Measurement
The following counts must be recorded for production source code only (excluding test classes):
- Total `.java` files
- Interface definitions
- Concrete class definitions (including abstract classes and enums, enumerated separately)

Both pre- and post-refactor values must be recorded and reported side by side.

### FR-16 — Cyclomatic Complexity Measurement
The aggregate and per-method cyclomatic complexity of production source code must be measured using PMD (`maven-pmd-plugin`) and recorded. At minimum the following must be reported:
- Total sum of cyclomatic complexity across all production methods
- Maximum cyclomatic complexity for any single method
- Average cyclomatic complexity per method

Both pre- and post-refactor values must be recorded and reported side by side.

### FR-17 — Build Time Measurement
The wall-clock time for a clean Maven build (`mvn clean package -DskipTests`) must be recorded. Three consecutive runs must be performed; the median value is the recorded result. Both pre- and post-refactor values must be recorded and reported side by side.

### FR-18 — Test Execution Time Measurement
The wall-clock time for a full Maven test run (`mvn test`) must be recorded. Three consecutive runs must be performed; the median value is the recorded result. Both pre- and post-refactor values must be recorded and reported side by side.

### FR-19 — Package Depth Measurement
The maximum package nesting depth of production source code must be recorded (measured as the number of dot-separated segments in the deepest fully-qualified package name). Both pre- and post-refactor values must be recorded and reported side by side.

### FR-20 — Runtime Dependency Count Measurement
The number of distinct external runtime JAR dependencies (i.e., non-JDK, non-test-scoped) declared in `pom.xml` must be recorded. Both pre- and post-refactor values must be recorded and reported side by side.

### FR-21 — Public Method Count Measurement
The total number of public methods declared in production source code (excluding test classes) must be recorded. Both pre- and post-refactor values must be recorded and reported side by side.

### FR-22 — Dependency Injection Annotation Count Measurement
The total count of Spring DI annotations present in production source code must be recorded, broken down by annotation type: `@Service`, `@Autowired`, `@Component`, `@Repository`, `@Controller`, and `@PostConstruct`. The sum across all types is the reported value. Both pre- and post-refactor values must be recorded and reported side by side.
> *Note: This metric is expected to be zero post-refactor. It quantifies one major dimension of the complexity reduction.*

---

## Section 2: Semantic Requirements

These requirements define the behavioral contracts and invariants that both systems must uphold, beyond the literal output bytes. They cover error handling, state, and execution semantics.

### SR-01 — IOException Suppression
Any `IOException` arising from the act of writing output must be silently swallowed. It must not propagate to the caller of `fizzBuzz(N)` and must not terminate the loop prematurely.
> *Rationale: The original `FizzBuzzOutputStrategyToFizzBuzzExceptionSafeOutputStrategyAdapter` wraps all output calls and discards `IOException`. This is an observable semantic: `fizzBuzz` has a void return type and declares no checked exceptions.*

### SR-02 — Call Independence
The result of any call to `fizzBuzz(N)` must be independent of any prior call.
> *Note: Implementations are not prohibited from maintaining internal state (e.g., caching or memoization) between calls, provided that such state does not alter the observable output of any call. Determinism of output for a given N is separately required by SR-06.*

### SR-03 — Output Target Is System.out at Call Time
Output must be written to `System.out` as it exists at the moment each write operation executes — not a reference captured at construction time or class load time. This is required for the test harness's `System.setOut()` redirect to function correctly.

### SR-04 — Output Flush Per Entry
Each entry's bytes must be flushed to the underlying output stream before the next entry begins. The system must not buffer multiple entries and flush at the end of `fizzBuzz(N)`.
> *Rationale: The original `SystemOutFizzBuzzOutputStrategy` calls `System.out.flush()` after every `write()`. The test flushes `System.out` once after `fizzBuzz(N)` returns, but correctness depends on the intermediate flushes within the call.*

### SR-05 — No Side Effects Beyond System.out
The system must produce no observable side effects other than writing to `System.out`. It must not write to `System.err`, modify static fields visible outside the system, interact with the filesystem, open network connections, or spawn threads.

### SR-06 — Determinism
For any given N and any given value of `System.getProperty("line.separator")`, the output of `fizzBuzz(N)` must be deterministic — the same bytes in the same order on every execution.

### SR-07 — No Exception Propagation from fizzBuzz
The method `fizzBuzz(N)` must not throw any checked or unchecked exception under any input N ≥ 0, assuming `System.out` is non-null and the JVM is in a normal operating state.

### SR-08 — Integer Arithmetic Correctness
The divisibility tests applied to each iteration index i must be arithmetically equivalent to the modulo operations `i % 3 == 0` and `i % 5 == 0`. No floating-point arithmetic, string-based comparison, or lookup-table shortcut shall produce a result that differs from these modulo operations for any integer i in [1, N].
> *Rationale: The original system computes divisibility via integer division followed by multiplication and comparison (`IntegerDivider` + `NumberIsMultipleOfAnotherNumberVerifier`). The refactored system may use the `%` operator directly, which is semantically equivalent for all positive divisors.*

### SR-09 — Charset Consistency
Bytes written to `System.out` for each token must encode the token string using the JVM's default charset (i.e., `String.getBytes()` with no explicit charset argument). The test reads back the captured bytes using the same default charset via `ByteArrayOutputStream.toString()`.

---

## Section 3: Non-Functional Requirements

These requirements define properties of the system that are not directly observable in the output but must be equivalent across both implementations for the scientific comparison to be valid.

### NFR-01 — Build System: Maven
Both systems must be buildable using Maven with `mvn package`. The resulting artifact must be a runnable JAR. The Maven build must succeed without errors or warnings that prevent artifact creation.

### NFR-02 — Build System: Gradle
Both systems must be buildable using Gradle with `./gradlew build`. The Gradle build must succeed without errors that prevent artifact creation.

### NFR-03 — Java Version Target
Both systems must compile targeting Java 1.7 (`<source>1.7</source><target>1.7</target>` in the Maven compiler plugin, or equivalent Gradle configuration). No language features above Java 7 shall be required for compilation.

### NFR-04 — Test Framework
Both systems must use JUnit 4 (`junit:junit:4.8.2`) as the test framework. Tests must be executable via `mvn test`.

### NFR-05 — Test Scope: 16 Cases
Both systems must provide a test that validates `fizzBuzz(N)` for each N in {1, 2, …, 16} against the oracle strings defined in `TestConstants`. All 16 assertions must pass.

### NFR-06 — Test Capture Mechanism
Both systems' tests must capture `System.out` using `System.setOut()` and a `ByteArrayOutputStream`-backed `PrintStream`. The test must restore the original `System.out` after each test method (via `@After` / teardown). This mechanism is the behavioral oracle and must not change.

### NFR-07 — Coverage Instrumentation
Both systems must support JaCoCo code coverage instrumentation via the `jacoco-maven-plugin`. The plugin must execute during `mvn test` and produce a coverage report.

### NFR-08 — Runnable JAR
Both systems must produce a JAR that is executable via `java -jar` with no additional classpath arguments. The manifest must declare a `Main-Class` attribute pointing to the entry point.

### NFR-09 — No Runtime Dependencies Beyond JDK (Post-Refactor)
The post-refactor system must have no runtime dependencies beyond the Java standard library. The production JAR must not require Spring or any other third-party library on the classpath at runtime.
> *This requirement applies only to the post-refactor system. The pre-refactor system requires Spring Framework 3.2.13 at runtime.*

### NFR-10 — Version Control Integrity
All changes must be committed to the `ai-refactor-experiment` branch. No changes may be made to the `uinverse` branch. The pre-refactor state is preserved in the `uinverse` branch history and must remain intact for metric collection.

---

## Section 4: Assumptions

These are conditions assumed to hold in the execution environment for both systems. They are not enforced by the code but are required for the requirements above to be meaningful.

### AS-01 — System.out Is Non-Null
`System.out` is non-null at the time `fizzBuzz(N)` is called.

### AS-02 — JVM Default Charset Is Consistent
The JVM default charset does not change between the `write()` call in production code and the `toString()` call in the test. Both operations use the same charset for encoding and decoding.

### AS-03 — Line Separator Is Stable Within a Call
`System.getProperty("line.separator")` returns the same value for all calls within a single execution of `fizzBuzz(N)`.

### AS-04 — N Is Non-Negative
`fizzBuzz(N)` is called with N ≥ 0. Behavior for negative N is undefined and not required to be equivalent.

### AS-05 — No Concurrent Modification of System.out
`System.out` is not reassigned by another thread during the execution of `fizzBuzz(N)`.

### AS-06 — Sufficient Heap and Stack
The JVM has sufficient memory to execute `fizzBuzz(N)` for any N in the range exercised by the test suite (1–16) and by the `main()` entry point. Out-of-memory conditions are out of scope.

---

## Section 5: Explicitly Out-of-Scope Equivalence

The following properties are **not** required to be equivalent between the pre- and post-refactor systems. Differences in these areas are expected and are the subject of the experimental measurement. Measurement of each property is required by the functional measurement requirements (FR-14 through FR-22).

| Property | Pre-Refactor | Post-Refactor | Measured By |
|----------|-------------|---------------|-------------|
| Class count | 87 Java files | 3 Java files | FR-15 |
| Interface count | 26 | 0 | FR-15 |
| Design patterns | 8+ explicitly named | 0 explicitly named | Qualitative (trade-off doc) |
| Spring dependency | Required | Absent | FR-20 |
| Cyclomatic complexity | ~50–80 decision points | ~7 | FR-16 |
| Build time | TBD | Expected lower | FR-17 |
| Test runtime | TBD | Expected lower | FR-18 |
| Lines of code | TBD | Expected lower | FR-14 |
| Package depth | 7 levels | 1 level (impl only) | FR-19 |
| DI annotation count | 80+ | 0 | FR-22 |
| Public method count | TBD | Expected lower | FR-21 |
| Extensibility | High (OCP-compliant) | Low (if/else chain) | Qualitative (trade-off doc) |
| Unit testability of sub-operations | Yes (each class testable in isolation) | No (single static method) | Qualitative (trade-off doc) |
| External runtime dependencies | Spring 3.2.13 (5 JARs) | None | FR-20 |

---

## Section 6: Verification Approach

To prove that a given post-refactor implementation satisfies all requirements in this document:

### V-01 — Automated Test Verification
Run `mvn test`. All tests must pass (0 failures, 0 errors). This verifies FR-01 through FR-12, SR-03, SR-04, SR-06, NFR-04, NFR-05, NFR-06.

### V-02 — Compile and Build Verification
Run `mvn clean package` and `./gradlew clean build`. Both must succeed. This verifies NFR-01, NFR-02, NFR-08.

### V-03 — Runtime JAR Verification
Run `java -jar target/FizzBuzzEnterpriseEdition-1.0-SNAPSHOT.jar` and verify: (a) the process exits without error, (b) the first 16 lines of output match the oracle for N=16, (c) both pre- and post-refactor produce the same number of output lines. This verifies FR-03 through FR-08, FR-11.

### V-04 — Spot-Check FR-09 (N=0)
Run a test or manual check that `fizzBuzz(0)` produces no output. Verify zero bytes are written to the captured stream.

### V-05 — IOException Suppression Verification
Confirm that `fizzBuzz` declares no checked exceptions in its signature, and that no `IOException` is declared as thrown or allowed to propagate. This verifies SR-01, SR-07.

### V-06 — Metric Collection
Collect all measurements defined in FR-14 through FR-22 for both pre- and post-refactor systems and record results in the experimental data file. Run JaCoCo and confirm FR-13 (post-refactor coverage ≥ pre-refactor coverage). This verifies the non-functional comparison is grounded in measured data rather than assertion.

---

## Section 7: Requirements Change Log

This table documents all requirements that were revised, added, or removed during the transition from REQUIREMENTS_INITIAL to REQUIREMENTS_REFINED_1. Changes were driven by annotations provided by DY (Derek).

| Req. ID | Original Requirement (Summary) | Annotated Feedback | Rewritten / New Requirement (Summary) |
|---------|-------------------------------|-------------------|---------------------------------------|
| FR-11 | `main()` must produce output equivalent to `fizzBuzz(100)`. | N=100 is not testable by the existing test suite (max testable N=16). Requirement must reflect this limitation. | `main()` must call `fizzBuzz(N)` for some positive N; both systems shall use the same N. Oracle conformance is validated only for N=1–16. Output beyond N=16 is verified by manual inspection only. |
| SR-02 | System must maintain no state between calls. Two consecutive calls to `fizzBuzz(5)` must produce identical output. Result of any call must be independent of any prior call. | (1) Prohibiting state between calls is unnecessary — memoization could yield performance gains and is not observable to end users. Remove the prohibition. (2) The "consecutive calls to fizzBuzz(5)" determinism language duplicates SR-06 and is too narrow (only N=5). Remove it, leaving only the final sentence. | The result of any call to `fizzBuzz(N)` must be independent of any prior call. Implementations may maintain internal state (e.g., memoization) provided it does not alter observable output. |
| NFR-03 | No language features above Java 7 **may** be required for compilation. | The word "may" makes the cutoff appear optional. Replace with "shall" to express a firm constraint. | No language features above Java 7 **shall** be required for compilation. |
| FR-13 | *(Not present in REQUIREMENTS_INITIAL)* | NFR-07 (Coverage Instrumentation) requires JaCoCo reporting but has no corresponding functional requirement for coverage equivalence. A functional requirement must be added. | **NEW — FR-13:** Post-refactor JaCoCo instruction coverage of production code must be equal to or greater than pre-refactor coverage. |
| FR-14 | *(Not present in REQUIREMENTS_INITIAL)* | NFR-10 annotation: Functional requirements must include explicit definitions of all metrics to be gathered. Lines of code is explicitly called out. | **NEW — FR-14:** Lines of code (total, blank, comment, source) must be measured with `cloc` for both systems and reported side by side. |
| FR-15 | *(Not present in REQUIREMENTS_INITIAL)* | NFR-10 annotation: Class and object counts must be measured. | **NEW — FR-15:** Total `.java` files, interface count, and concrete class count must be recorded for production code in both systems. |
| FR-16 | *(Not present in REQUIREMENTS_INITIAL)* | NFR-10 annotation: Additional measurable metrics suggested. Cyclomatic complexity was already in the plan (PLAN_INITIAL.md) and should be formalized as a requirement. | **NEW — FR-16:** Aggregate and per-method cyclomatic complexity must be measured via PMD and recorded for both systems. |
| FR-17 | *(Not present in REQUIREMENTS_INITIAL)* | NFR-10 annotation: Build time must be measured. | **NEW — FR-17:** Median wall-clock time for `mvn clean package -DskipTests` (3 runs) must be recorded for both systems. |
| FR-18 | *(Not present in REQUIREMENTS_INITIAL)* | NFR-10 annotation: Execution (test) time must be measured. | **NEW — FR-18:** Median wall-clock time for `mvn test` (3 runs) must be recorded for both systems. |
| FR-19 | *(Not present in REQUIREMENTS_INITIAL)* | NFR-10 annotation: Additional metrics suggested. Package depth is a direct consequence of the refactor goal and should be measured. | **NEW — FR-19:** Maximum package nesting depth of production source must be recorded for both systems. |
| FR-20 | *(Not present in REQUIREMENTS_INITIAL)* | NFR-10 annotation: Additional metrics suggested. Runtime dependency count quantifies the Spring removal directly. | **NEW — FR-20:** Number of external runtime JAR dependencies declared in `pom.xml` must be recorded for both systems. |
| FR-21 | *(Not present in REQUIREMENTS_INITIAL)* | NFR-10 annotation: Additional metrics suggested. Public method count reflects API surface area reduction. | **NEW — FR-21:** Total public methods in production source code must be recorded for both systems. |
| FR-22 | *(Not present in REQUIREMENTS_INITIAL)* | NFR-10 annotation: Additional metrics suggested. DI annotation count directly quantifies the Spring coupling that is being eliminated. | **NEW — FR-22:** Count of Spring DI annotations (`@Service`, `@Autowired`, etc.) in production source must be recorded for both systems. |
| AS-06 | JVM has sufficient memory for N in range tested (1–100 in production; 1–16 in tests). | *Consequential update for consistency with FR-11 revision.* FR-11 no longer specifies N=100; the production range is implementation-defined. | JVM has sufficient memory for any N exercised by the test suite (1–16) and by `main()`. |
| V-03 | Verify JAR output matches known-good FizzBuzz for N=100. | *Consequential update for consistency with FR-11 revision.* N=100 is no longer a specified requirement. | Verify JAR output exits without error; first 16 lines match oracle for N=16; both systems produce the same line count. |
