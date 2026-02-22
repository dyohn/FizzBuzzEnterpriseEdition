# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

Both Maven and Gradle are supported:

```bash
# Maven
mvn compile          # Compile
mvn test             # Run all tests
mvn package          # Build jar
mvn exec:java -Dexec.mainClass="com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl.Main"  # Run

# Gradle
./gradlew build      # Build
./gradlew test       # Run all tests
./gradlew run        # Run the application
```

There is only one test class (`FizzBuzzTest`), so running all tests is the same as running a single test.

## Architecture

This is a satirical over-engineered Java implementation of FizzBuzz using Spring IoC and classic GoF design patterns.

**Execution flow:**
1. `Main` bootstraps a Spring `ApplicationContext` from `resources/assets/configuration/spring/dependencyinjection/configuration/spring.xml` (component-scan only, no explicit bean definitions)
2. `StandardFizzBuzz` (implements `FizzBuzz`) delegates to `EnterpriseGradeFizzBuzzSolutionStrategy` via a factory
3. The strategy creates a `LoopContext` + `LoopRunner` that iterates 1..N
4. Each iteration runs `SingleStepOutputGenerationStrategy`, which visits three sub-strategies: `FizzStrategy`, `BuzzStrategy`, and `NoFizzNoBuzzStrategy`
5. Each sub-strategy checks divisibility (`NumberIsMultipleOfAnotherNumberVerifier` → `IntegerDivider`), then uses a `StringReturner` → `StringPrinter` chain to emit output

**Package layout** (all under `com.seriouscompany.business.java.fizzbuzz.packagenamingpackage`):
- `interfaces/` — all interfaces (strategies, factories, printers, visitors, loop, etc.)
- `impl/` — concrete implementations mirroring the interface hierarchy
  - `strategies/` — `FizzStrategy`, `BuzzStrategy`, `NoFizzNoBuzzStrategy`, `EnterpriseGradeFizzBuzzSolutionStrategy`, plus `adapters/`, `comparators/`, `converters/`, `constants/`
  - `factories/` — one `@Service` factory per strategy/printer/returner type
  - `printers/` — `FizzStringPrinter`, `BuzzStringPrinter`, `NewLineStringPrinter`, `IntegerIntegerPrinter`
  - `stringreturners/` — return "Fizz", "Buzz", "\n", or the integer as string
  - `loop/` — `LoopContext`, `LoopRunner`, `LoopCondition`, `LoopInitializer`, `LoopFinalizer`, `LoopStep`
  - `visitors/` — `FizzBuzzOutputGenerationContext` / `FizzBuzzOutputGenerationContextVisitor`
  - `math/arithmetics/` — `IntegerDivider`, `NumberIsMultipleOfAnotherNumberVerifier`

**Key design patterns used:** Strategy, Factory, Visitor, Adapter, Template Method, Dependency Injection (Spring).

**Spring wiring:** All beans are discovered via `@Service` component-scan. There are no XML bean definitions. Constants for Spring bean names are in `impl/Constants.java`.

**Tests:** `src/test/java/FizzBuzzTest.java` bootstraps Spring and tests output of `fizzBuzz(n)` for n=1 through 16 by capturing `System.out`. Expected strings are in `TestConstants.java`.
