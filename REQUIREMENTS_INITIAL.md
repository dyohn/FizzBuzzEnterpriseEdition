# FizzBuzzEnterpriseEdition — Equivalence Requirements
**Document ID:** REQUIREMENTS_INITIAL
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
Invoking the system via its `main(String[] args)` method (with no arguments) must produce output equivalent to `fizzBuzz(100)` — i.e., FizzBuzz for N = 100.

### FR-12 — Oracle Conformance
For each N in {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, the output of `fizzBuzz(N)` must exactly match the corresponding expected string in `TestConstants`, after substituting all `"\n"` literals in the expected string with `System.getProperty("line.separator")`.

---

## Section 2: Semantic Requirements

These requirements define the behavioral contracts and invariants that both systems must uphold, beyond the literal output bytes. They cover error handling, state, and execution semantics.

### SR-01 — IOException Suppression
Any `IOException` arising from the act of writing output must be silently swallowed. It must not propagate to the caller of `fizzBuzz(N)` and must not terminate the loop prematurely.
> *Rationale: The original `FizzBuzzOutputStrategyToFizzBuzzExceptionSafeOutputStrategyAdapter` wraps all output calls and discards `IOException`. This is an observable semantic: `fizzBuzz` has a void return type and declares no checked exceptions.*

### SR-02 — No Cross-Call State
The system must maintain no state between successive calls to `fizzBuzz(N)`. Two consecutive calls to `fizzBuzz(5)` must produce identical output. The result of any call must be independent of any prior call.

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
The divisibility tests applied to each iteration index i must be arithmetically equivalent to the modulo operations `i % 3 == 0` and `i % 5 == 0`. No floating-point arithmetic, string-based comparison, or lookup-table shortcut may produce a result that differs from these modulo operations for any integer i in [1, N].
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
Both systems must compile targeting Java 1.7 (`<source>1.7</source><target>1.7</target>` in the Maven compiler plugin, or equivalent Gradle configuration). No language features above Java 7 may be required for compilation.

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
The JVM has sufficient memory to execute `fizzBuzz(N)` for N in the range tested (1–100 in production; 1–16 in tests). Out-of-memory conditions are out of scope.

---

## Section 5: Explicitly Out-of-Scope Equivalence

The following properties are **not** required to be equivalent between the pre- and post-refactor systems. Differences in these areas are expected, documented, and are in fact the subject of the experimental measurement.

| Property | Pre-Refactor | Post-Refactor | Notes |
|----------|-------------|---------------|-------|
| Class count | 87 Java files | 3 Java files | Reduction is the experiment goal |
| Interface count | 26 | 0 | Intentional elimination |
| Design patterns | 8+ | 0 explicitly named | Intentional simplification |
| Spring dependency | Required | Absent | Explicitly removed |
| Cyclomatic complexity | ~50–80 | ~7 | Reduction is the experiment goal |
| Build time | TBD | Expected lower | To be measured |
| Test runtime | TBD | Expected lower | To be measured |
| Extensibility | High (OCP-compliant) | Low (if/else chain) | Documented trade-off |
| Unit testability of sub-operations | Yes (each class testable in isolation) | No (single static method) | Documented trade-off |
| Package depth | 7 levels | 1 level (impl only) | Intentional simplification |
| External runtime dependencies | Spring 3.2.13 (5 JARs) | None | Intentional removal |

---

## Section 6: Verification Approach

To prove that a given post-refactor implementation satisfies all requirements in this document:

### V-01 — Automated Test Verification
Run `mvn test`. All tests must pass (0 failures, 0 errors). This verifies FR-01 through FR-12, SR-03, SR-04, SR-06, NFR-04, NFR-05, NFR-06.

### V-02 — Compile and Build Verification
Run `mvn clean package` and `./gradlew clean build`. Both must succeed. This verifies NFR-01, NFR-02, NFR-08.

### V-03 — Runtime JAR Verification
Run `java -jar target/FizzBuzzEnterpriseEdition-1.0-SNAPSHOT.jar | head -20` and compare to known-good FizzBuzz output for N=100. This verifies FR-03 through FR-08, FR-11.

### V-04 — Spot-Check FR-09 (N=0)
Run a test or manual check that `fizzBuzz(0)` produces no output. Verify zero bytes are written to the captured stream.

### V-05 — IOException Suppression Verification
Confirm that `fizzBuzz` declares no checked exceptions in its signature, and that no `IOException` is declared as thrown or allowed to propagate. This verifies SR-01, SR-07.

### V-06 — Metric Collection
Collect all metrics defined in PLAN_INITIAL.md Phase 0 (baseline) and Phase 4 (post-refactor) and record results in the experimental data file. This verifies the non-functional comparison is grounded in measured data rather than assertion.
