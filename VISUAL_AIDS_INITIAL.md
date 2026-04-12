# Visual Aids — FizzBuzzEnterpriseEdition Refactor Experiment
**Document ID:** VISUAL_AIDS_INITIAL
**Branch:** `ai-refactor-experiment`
**Sources:** REQUIREMENTS_REFINED_1.md, PLAN_REVISED_1.md, source code (pre-refactor)
**Rendering note:** All Mermaid blocks render as diagrams in GitHub, GitLab, VS Code with the Mermaid Preview extension, and any Markdown viewer with Mermaid support. The raw Mermaid source code is the block itself.

---

## Table of Contents

1. [Pre-Refactor System](#1-pre-refactor-system)
   - 1.1 UML Class Diagrams
   - 1.2 Use Cases
   - 1.3 Sequence Diagrams
2. [Post-Refactor System](#2-post-refactor-system)
   - 2.1 UML Class Diagram
   - 2.2 Use Cases
   - 2.3 Sequence Diagram
3. [Architectural Comparison](#3-architectural-comparison)

---

## 1. Pre-Refactor System

The pre-refactor system contains **87 Java source files** (26 interfaces + 61 concrete classes), organized across a 7-level-deep package hierarchy under `com.seriouscompany.business.java.fizzbuzz.packagenamingpackage`. It uses Spring Framework 3.2.13 for dependency injection and implements 8+ GoF design patterns. All diagrams in this section reflect the code on the `uinverse` branch.

---

### 1.1 UML Class Diagrams

Two diagrams are provided: a high-level architecture overview covering the full execution spine, and a detail diagram focused on the printer and output sub-system.

#### 1.1.1 Architecture Overview

This diagram shows all major interfaces and classes involved in the execution path, grouped by architectural layer. Factory classes (15 total) are represented by a single representative example; the remaining 14 are noted. The full package prefix `com.seriouscompany.business.java.fizzbuzz.packagenamingpackage` is omitted for readability.

```mermaid
classDiagram
    direction TB

    %% ══════════════════════════════
    %% LAYER 1 — ENTRY POINT
    %% ══════════════════════════════
    class Main {
        +main(String[] args)$
    }
    class FizzBuzz {
        <<interface>>
        +fizzBuzz(int n)
    }
    class StandardFizzBuzz {
        -_fizzBuzzSolutionStrategyFactory
        +fizzBuzz(int n)
    }

    %% ══════════════════════════════
    %% LAYER 2 — SOLUTION STRATEGY
    %% ══════════════════════════════
    class FizzBuzzSolutionStrategyFactory {
        <<interface>>
        +createFizzBuzzSolutionStrategy()
    }
    class FizzBuzzSolutionStrategy {
        <<interface>>
        +runSolution(int n)
    }
    class EnterpriseGradeFizzBuzzSolutionStrategy {
        -_loopPayloadExecution
        +runSolution(int n)
    }

    %% ══════════════════════════════
    %% LAYER 3 — LOOP
    %% ══════════════════════════════
    class LoopContextStateManipulation {
        <<interface>>
        +start()
        +shouldProceed() bool
        +proceed()
    }
    class LoopContextStateRetrieval {
        <<interface>>
        +getControlParameter() int
    }
    class LoopContext {
        -myCurrentControlParameterValue int
        +start()
        +shouldProceed() bool
        +proceed()
        +getControlParameter() int
    }
    class LoopRunner {
        -myStateManipulation
        -myStateRetrieval
        -myPayload
        +runLoop()
    }
    class LoopPayloadExecution {
        <<interface>>
        +runLoopPayload(LoopContextStateRetrieval)
    }

    %% ══════════════════════════════
    %% LAYER 4 — PAYLOAD / ADAPTER
    %% ══════════════════════════════
    class SingleStepPayload {
        -_outputGenerationStrategy
        +runLoopPayload(LoopContextStateRetrieval)
    }
    class SingleStepOutputGenerationParameter {
        <<interface>>
        +retrieveIntegerValue() int
    }
    class LoopContextStateRetrievalAdapter {
        +retrieveIntegerValue() int
    }

    %% ══════════════════════════════
    %% LAYER 5 — OUTPUT GENERATION
    %% ══════════════════════════════
    class OutputGenerationStrategy {
        <<interface>>
        +performGenerationForCurrentStep(param)
    }
    class SingleStepOutputGenerationStrategy {
        -contexts List~OutputGenerationContext~
        -contextVisitor
        -myNewLinePrinter
        +performGenerationForCurrentStep(param)
    }

    %% ══════════════════════════════
    %% LAYER 6 — VISITOR PATTERN
    %% ══════════════════════════════
    class OutputGenerationContextVisitor {
        <<interface>>
        +visit(OutputGenerationContext, int)
    }
    class FizzBuzzOutputGenerationContextVisitor {
        +visit(OutputGenerationContext, int)
    }
    class OutputGenerationContext {
        <<interface>>
        +getStrategy() IsEvenlyDivisibleStrategy
        +getPrinter() DataPrinter
    }
    class FizzBuzzOutputGenerationContext {
        -strategy IsEvenlyDivisibleStrategy
        -printer DataPrinter
        +getStrategy()
        +getPrinter()
    }

    %% ══════════════════════════════
    %% LAYER 7 — DIVISIBILITY STRATEGIES
    %% ══════════════════════════════
    class IsEvenlyDivisibleStrategy {
        <<interface>>
        +isEvenlyDivisible(int) bool
    }
    class FizzStrategy {
        +isEvenlyDivisible(int) bool
    }
    class BuzzStrategy {
        +isEvenlyDivisible(int) bool
    }
    class NoFizzNoBuzzStrategy {
        +isEvenlyDivisible(int) bool
    }

    %% ══════════════════════════════
    %% LAYER 8 — PRINTERS
    %% ══════════════════════════════
    class DataPrinter {
        <<interface>>
        +print()
        +printValue(Object)
    }
    class StringPrinter {
        <<interface>>
        +print()
    }
    class IntegerPrinter {
        <<interface>>
        +printInteger(int)
    }
    class FizzStringPrinter {
        +print()
        +printValue(Object)
    }
    class BuzzStringPrinter {
        +print()
        +printValue(Object)
    }
    class NewLineStringPrinter {
        +print()
        +printValue(Object)
    }
    class IntegerIntegerPrinter {
        +printInteger(int)
        +printValue(Object)
        +print() throws UnsupportedOp
    }

    %% ══════════════════════════════
    %% LAYER 9 — MATH
    %% ══════════════════════════════
    class NumberIsMultipleOfAnotherNumberVerifier {
        -integerDivider$ IntegerDivider
        +numberIsMultipleOfAnotherNumber(int,int)$ bool
        +init()
    }
    class IntegerDivider {
        +divide(int, int) int
    }
    class ApplicationContextHolder {
        -applicationContext ApplicationContext
        +getApplicationContext() ApplicationContext
    }

    %% ══════════════════════════════
    %% LAYER 10 — OUTPUT CHANNEL
    %% ══════════════════════════════
    class FizzBuzzOutputStrategy {
        <<interface>>
        +output(String)
    }
    class FizzBuzzExceptionSafeOutputStrategy {
        <<interface>>
        +output(String)
    }
    class SystemOutFizzBuzzOutputStrategy {
        +output(String)
    }
    class FizzBuzzOutputStrategyAdapter {
        +output(String)
    }

    %% ══════════════════════════════════════════
    %% RELATIONSHIPS
    %% ══════════════════════════════════════════

    %% Entry → Strategy
    Main --> FizzBuzz : gets bean via Spring
    StandardFizzBuzz ..|> FizzBuzz
    StandardFizzBuzz --> FizzBuzzSolutionStrategyFactory : uses
    StandardFizzBuzz --> FizzBuzzSolutionStrategy : creates via factory

    %% Solution Strategy → Loop
    EnterpriseGradeFizzBuzzSolutionStrategy ..|> FizzBuzzSolutionStrategy
    EnterpriseGradeFizzBuzzSolutionStrategy --> LoopContext : creates
    EnterpriseGradeFizzBuzzSolutionStrategy --> LoopRunner : creates

    %% Loop internal
    LoopContext ..|> LoopContextStateManipulation
    LoopContext ..|> LoopContextStateRetrieval
    LoopContext ..> ApplicationContextHolder : 2nd Spring ctx ⚠
    LoopRunner --> LoopContextStateManipulation : controls via
    LoopRunner --> LoopContextStateRetrieval : reads via
    LoopRunner --> LoopPayloadExecution : executes each step

    %% Payload
    SingleStepPayload ..|> LoopPayloadExecution
    SingleStepPayload --> OutputGenerationStrategy : delegates to
    SingleStepPayload --> LoopContextStateRetrievalAdapter : creates

    %% Adapter
    LoopContextStateRetrievalAdapter ..|> SingleStepOutputGenerationParameter
    LoopContextStateRetrievalAdapter --> LoopContextStateRetrieval : adapts

    %% Output generation
    SingleStepOutputGenerationStrategy ..|> OutputGenerationStrategy
    SingleStepOutputGenerationStrategy "1" --> "3" FizzBuzzOutputGenerationContext : holds
    SingleStepOutputGenerationStrategy --> FizzBuzzOutputGenerationContextVisitor : uses
    SingleStepOutputGenerationStrategy --> NewLineStringPrinter : newline after each step

    %% Visitor
    FizzBuzzOutputGenerationContextVisitor ..|> OutputGenerationContextVisitor
    FizzBuzzOutputGenerationContext ..|> OutputGenerationContext
    FizzBuzzOutputGenerationContext --> IsEvenlyDivisibleStrategy : holds
    FizzBuzzOutputGenerationContext --> DataPrinter : holds

    %% Divisibility
    FizzStrategy ..|> IsEvenlyDivisibleStrategy
    BuzzStrategy ..|> IsEvenlyDivisibleStrategy
    NoFizzNoBuzzStrategy ..|> IsEvenlyDivisibleStrategy
    FizzStrategy --> NumberIsMultipleOfAnotherNumberVerifier : uses static
    BuzzStrategy --> NumberIsMultipleOfAnotherNumberVerifier : uses static
    NoFizzNoBuzzStrategy --> NumberIsMultipleOfAnotherNumberVerifier : uses static
    NumberIsMultipleOfAnotherNumberVerifier --> IntegerDivider : uses
    NumberIsMultipleOfAnotherNumberVerifier --> ApplicationContextHolder : service locator

    %% Printer hierarchy
    StringPrinter --|> DataPrinter
    IntegerPrinter --|> DataPrinter
    FizzStringPrinter ..|> StringPrinter
    BuzzStringPrinter ..|> StringPrinter
    NewLineStringPrinter ..|> StringPrinter
    IntegerIntegerPrinter ..|> IntegerPrinter

    %% Output channel
    SystemOutFizzBuzzOutputStrategy ..|> FizzBuzzOutputStrategy
    FizzBuzzOutputStrategyAdapter ..|> FizzBuzzExceptionSafeOutputStrategy
    FizzBuzzOutputStrategyAdapter --> FizzBuzzOutputStrategy : wraps
    FizzStringPrinter --> FizzBuzzOutputStrategyAdapter : creates & uses
    BuzzStringPrinter --> FizzBuzzOutputStrategyAdapter : creates & uses
    IntegerIntegerPrinter --> FizzBuzzOutputStrategyAdapter : creates & uses
```
(DY): The above class diagram is very dense and difficult to follow. However, it probably needs to be retained to get the full picture of the system. It would be good to generate additional diagrams, one for each of the identified layers of the architecture.

> **Diagram notes:**
> - ⚠ `LoopContext` bootstraps a **second independent Spring `ApplicationContext`** at construction time to retrieve its loop components, then immediately closes it. This is the Service Locator anti-pattern applied within an already-DI-managed object.
> - 15 factory classes are not shown. All follow the same pattern: `@Service`, constructor-inject their dependencies, implement a factory interface, and `new` their product. `FizzBuzzSolutionStrategyFactory` (shown) is a representative example.
> - `FizzBuzzOutputStrategyAdapter` is the shortened name for `FizzBuzzOutputStrategyToFizzBuzzExceptionSafeOutputStrategyAdapter`.
> - `LoopContextStateRetrievalAdapter` is the shortened name for `LoopContextStateRetrievalToSingleStepOutputGenerationAdapter`.

---

#### 1.1.2 Printer & Output Chain Detail

This diagram focuses on how a token string travels from a divisibility decision to bytes on `System.out` — the most deeply nested sub-system.

```mermaid
classDiagram
    direction LR

    class IsEvenlyDivisibleStrategy {
        <<interface>>
        +isEvenlyDivisible(int) bool
    }

    class DataPrinter {
        <<interface>>
        +print()
        +printValue(Object)
    }

    class StringPrinter {
        <<interface>>
        +print()
    }

    class IntegerPrinter {
        <<interface>>
        +printInteger(int)
        +print() throws UnsupportedOp
    }

    class FizzStringPrinter {
        +print()
        +printValue(Object) delegates to print()
    }

    class BuzzStringPrinter {
        +print()
        +printValue(Object) delegates to print()
    }

    class NewLineStringPrinter {
        +print()
        +printValue(Object) delegates to print()
    }

    class IntegerIntegerPrinter {
        +printInteger(int)
        +printValue(Object) casts to Integer then printInteger()
        +print() throws UnsupportedOperationException
    }

    class StringStringReturner {
        <<interface>>
        +getReturnString() String
    }

    class IntegerStringReturner {
        <<interface>>
        +getIntegerReturnString(int) String
    }

    class FizzStringReturner {
        +getReturnString() → "Fizz"
    }

    class BuzzStringReturner {
        +getReturnString() → "Buzz"
    }

    class NewLineStringReturner {
        +getReturnString() → line.separator
    }

    class IntegerIntegerStringReturner {
        +getIntegerReturnString(int) → Integer.toString(i)
    }

    class FizzBuzzOutputStrategy {
        <<interface>>
        +output(String) throws IOException
    }

    class FizzBuzzExceptionSafeOutputStrategy {
        <<interface>>
        +output(String)
    }

    class SystemOutFizzBuzzOutputStrategy {
        +output(String)
    }

    class FizzBuzzOutputStrategyToExceptionSafeAdapter {
        -wrappedStrategy FizzBuzzOutputStrategy
        +output(String) swallows IOException
    }

    %% Hierarchy
    StringPrinter --|> DataPrinter
    IntegerPrinter --|> DataPrinter
    FizzStringPrinter ..|> StringPrinter
    BuzzStringPrinter ..|> StringPrinter
    NewLineStringPrinter ..|> StringPrinter
    IntegerIntegerPrinter ..|> IntegerPrinter

    FizzStringReturner ..|> StringStringReturner
    BuzzStringReturner ..|> StringStringReturner
    NewLineStringReturner ..|> StringStringReturner
    IntegerIntegerStringReturner ..|> IntegerStringReturner

    SystemOutFizzBuzzOutputStrategy ..|> FizzBuzzOutputStrategy
    FizzBuzzOutputStrategyToExceptionSafeAdapter ..|> FizzBuzzExceptionSafeOutputStrategy

    %% Print chain
    FizzStringPrinter --> FizzStringReturner : gets "Fizz"
    BuzzStringPrinter --> BuzzStringReturner : gets "Buzz"
    NewLineStringPrinter --> NewLineStringReturner : gets newline
    IntegerIntegerPrinter --> IntegerIntegerStringReturner : gets Integer.toString(i)

    FizzStringPrinter --> FizzBuzzOutputStrategyToExceptionSafeAdapter : creates
    BuzzStringPrinter --> FizzBuzzOutputStrategyToExceptionSafeAdapter : creates
    NewLineStringPrinter --> FizzBuzzOutputStrategyToExceptionSafeAdapter : creates
    IntegerIntegerPrinter --> FizzBuzzOutputStrategyToExceptionSafeAdapter : creates

    FizzBuzzOutputStrategyToExceptionSafeAdapter --> FizzBuzzOutputStrategy : wraps
    FizzBuzzOutputStrategyToExceptionSafeAdapter --> SystemOutFizzBuzzOutputStrategy : delegates to
    SystemOutFizzBuzzOutputStrategy --> SystemOutFizzBuzzOutputStrategy : System.out.write(bytes) + flush()
```

---

### 1.2 Use Cases

#### 1.2.1 Use Case Inventory

| ID | Use Case | Primary Actor | Preconditions | Postconditions | Notes |
|----|----------|--------------|---------------|----------------|-------|
| UC-01 | Run FizzBuzz (1..100) | User/Operator | JRE available; JAR on filesystem | FizzBuzz output for 1–100 written to stdout | Invoked via `java -jar` |
| UC-02 | Run FizzBuzz for arbitrary N | Developer / Test caller | Spring context active | FizzBuzz output for 1–N written to stdout | Invoked via `StandardFizzBuzz.fizzBuzz(N)` |
| UC-03 | Validate FizzBuzz correctness | Automated Test Harness | Spring context available; System.out redirected | All 16 assertions pass; System.out restored | `FizzBuzzTest.testFizzBuzz()` |
| UC-04 | Add a new FizzBuzz rule (e.g., "Bazz" for multiples of 7) | Developer | Existing codebase | New rule produces correct output without modifying existing classes | Enabled by Strategy + Factory + Visitor patterns; complies with Open/Closed Principle |

#### 1.2.2 Use Case Diagram

```mermaid
graph TB
    User(["👤 User / Operator"])
    Dev(["👤 Developer"])
    QA(["🤖 Test Harness"])

    subgraph sys ["FizzBuzzEnterpriseEdition — System Boundary"]
        UC01(["UC-01\nRun FizzBuzz 1..100\nvia main()"])
        UC02(["UC-02\nRun FizzBuzz for N\nvia fizzBuzz(N)"])
        UC03(["UC-03\nValidate output\nfor N = 1..16"])
        UC04(["UC-04\nAdd new FizzBuzz rule\nwithout modifying existing code"])

        UC01 -. "extends" .-> UC02
    end

    User -->|"executes JAR"| UC01
    Dev -->|"calls directly"| UC02
    QA -->|"runs test suite"| UC03
    Dev -->|"implements Strategy +\nregisters via Spring"| UC04
```
(DY): In the rendering of the graph defined immediately above, the `Developer` and `Test Harness` entries display on the far right side of the `System Boundary`, but this makes the graph excessively wide. Let's move those to be rendered above the `System Boundary` box, to the right of where `User/Operator` is located.

---

### 1.3 Sequence Diagrams

#### 1.3.1 Full Execution Flow

This diagram traces the complete call chain from `main()` through Spring context setup, solution strategy delegation, loop execution, and final output to `System.out`.

```mermaid
sequenceDiagram
    actor User
    participant Main
    participant Spring1 as Spring Context #1
    participant SFB as StandardFizzBuzz
    participant EGFBSS as EnterpriseGradeFizzBuzzSolutionStrategy
    participant LoopCtx as LoopContext
    participant Spring2 as Spring Context #2 ⚠
    participant LCF as LoopComponentFactory
    participant LR as LoopRunner
    participant SSP as SingleStepPayload
    participant SSOS as SingleStepOutputGenerationStrategy
    participant Visitor as FizzBuzzOutputGenerationContextVisitor

    User->>Main: main(args)

    Note over Main,Spring1: Bootstrap Phase
    Main->>Spring1: new ClassPathXmlApplicationContext("spring.xml")
    Spring1-->>Main: ApplicationContext
    Main->>Spring1: getBean("standardFizzBuzz")
    Spring1-->>Main: StandardFizzBuzz instance
    Main->>SFB: fizzBuzz(100)

    Note over SFB,EGFBSS: Strategy Resolution
    SFB->>SFB: factory.createFizzBuzzSolutionStrategy()
    SFB->>EGFBSS: runSolution(100)

    Note over EGFBSS,Spring2: ⚠ LoopContext creates a SECOND Spring context
    EGFBSS->>LoopCtx: new LoopContext(100)
    LoopCtx->>Spring2: new ClassPathXmlApplicationContext("spring.xml")
    Spring2-->>LoopCtx: ApplicationContext
    LoopCtx->>Spring2: getBean(LoopComponentFactory)
    Spring2-->>LoopCtx: LoopComponentFactory
    LoopCtx->>LCF: createLoopInitializer()
    LoopCtx->>LCF: createLoopFinalizer(100)
    LoopCtx->>LCF: createLoopCondition()
    LoopCtx->>LCF: createLoopStep()
    LoopCtx->>Spring2: close()

    EGFBSS->>LR: new LoopRunner(LoopCtx, LoopCtx, SingleStepPayload)
    EGFBSS->>LR: runLoop()

    Note over LR,Visitor: Main Loop (i = 1 to 100)
    loop for each iteration i
        LR->>LoopCtx: start() / shouldProceed() / proceed()
        LR->>SSP: runLoopPayload(LoopCtx as stateRetrieval)
        SSP->>SSP: new LoopContextStateRetrievalToSingleStepOutputGenerationAdapter(LoopCtx)
        SSP->>SSOS: performGenerationForCurrentStep(adapter)

        loop for each of 3 OutputGenerationContexts [Fizz, Buzz, NoFizzNoBuzz]
            SSOS->>Visitor: visit(context, i)
            Visitor->>Visitor: context.getStrategy().isEvenlyDivisible(i)
            alt isEvenlyDivisible == true
                Visitor->>Visitor: context.getPrinter().printValue(i)
                Note right of Visitor: Printer → StringReturner → ExceptionSafeAdapter → SystemOutStrategy → System.out.write() + flush()
            end
        end

        SSOS->>SSOS: newLinePrinter.print()
        Note right of SSOS: Writes platform line separator to System.out
    end

    Main->>Spring1: close()
```

---

#### 1.3.2 Per-Iteration Step Detail

This diagram zooms into a single iteration (e.g., i = 15, which produces "FizzBuzz") and shows the full call chain through divisibility checking, string retrieval, and output writing.

```mermaid
sequenceDiagram
    participant SSOS as SingleStepOutputGenerationStrategy
    participant Visitor as FizzBuzzOutputGenerationContextVisitor
    participant FizzCtx as FizzBuzzOutputGenerationContext [Fizz]
    participant BuzzCtx as FizzBuzzOutputGenerationContext [Buzz]
    participant NoBCtx as FizzBuzzOutputGenerationContext [NoFizzNoBuzz]
    participant FizzStrat as FizzStrategy
    participant BuzzStrat as BuzzStrategy
    participant NoStrat as NoFizzNoBuzzStrategy
    participant NMAV as NumberIsMultipleOfAnotherNumberVerifier
    participant ID as IntegerDivider
    participant FizzP as FizzStringPrinter
    participant BuzzP as BuzzStringPrinter
    participant Adapter as FizzBuzzOutputStrategyToExceptionSafeAdapter
    participant SysOut as SystemOutFizzBuzzOutputStrategy

    Note over SSOS,SysOut: Iteration i = 15

    %% Context 1: Fizz
    SSOS->>Visitor: visit(FizzCtx, 15)
    Visitor->>FizzCtx: getStrategy()
    FizzCtx-->>Visitor: FizzStrategy
    Visitor->>FizzStrat: isEvenlyDivisible(15)
    FizzStrat->>NMAV: numberIsMultipleOfAnotherNumber(15, 3)
    NMAV->>ID: divide(15, 3) → 5
    ID-->>NMAV: 5
    NMAV->>NMAV: 5 * 3 == 15 → true
    NMAV-->>FizzStrat: true
    FizzStrat-->>Visitor: true
    Visitor->>FizzCtx: getPrinter()
    FizzCtx-->>Visitor: FizzStringPrinter
    Visitor->>FizzP: printValue(15)
    FizzP->>FizzP: print() → FizzStringReturner.getReturnString() → "Fizz"
    FizzP->>Adapter: new Adapter(SystemOutStrategy)
    FizzP->>Adapter: output("Fizz")
    Adapter->>SysOut: output("Fizz")
    SysOut->>SysOut: System.out.write("Fizz".getBytes())
    SysOut->>SysOut: System.out.flush()

    %% Context 2: Buzz
    SSOS->>Visitor: visit(BuzzCtx, 15)
    Visitor->>BuzzCtx: getStrategy()
    BuzzCtx-->>Visitor: BuzzStrategy
    Visitor->>BuzzStrat: isEvenlyDivisible(15)
    BuzzStrat->>NMAV: numberIsMultipleOfAnotherNumber(15, 5)
    NMAV->>ID: divide(15, 5) → 3
    ID-->>NMAV: 3
    NMAV->>NMAV: 3 * 5 == 15 → true
    NMAV-->>BuzzStrat: true
    BuzzStrat-->>Visitor: true
    Visitor->>BuzzCtx: getPrinter()
    BuzzCtx-->>Visitor: BuzzStringPrinter
    Visitor->>BuzzP: printValue(15)
    BuzzP->>BuzzP: print() → BuzzStringReturner.getReturnString() → "Buzz"
    BuzzP->>Adapter: new Adapter(SystemOutStrategy)
    BuzzP->>Adapter: output("Buzz")
    Adapter->>SysOut: output("Buzz")
    SysOut->>SysOut: System.out.write("Buzz".getBytes())
    SysOut->>SysOut: System.out.flush()

    %% Context 3: NoFizzNoBuzz
    SSOS->>Visitor: visit(NoBCtx, 15)
    Visitor->>NoBCtx: getStrategy()
    NoBCtx-->>Visitor: NoFizzNoBuzzStrategy
    Visitor->>NoStrat: isEvenlyDivisible(15)
    NoStrat->>NMAV: numberIsMultipleOfAnotherNumber(15, 3) → true
    NoStrat->>NMAV: numberIsMultipleOfAnotherNumber(15, 5) → true
    NoStrat->>NoStrat: !divisible3 && !divisible5 → false
    NoStrat-->>Visitor: false
    Note over Visitor,NoBCtx: Condition false — no output for NoFizzNoBuzz context

    %% Newline
    SSOS->>SSOS: newLinePrinter.print()
    Note right of SSOS: Writes System.getProperty("line.separator") to System.out
    Note over SSOS,SysOut: Net output for i=15: "Fizz" + "Buzz" + newline = "FizzBuzz\n"
```

---

## 2. Post-Refactor System

The post-refactor system contains **1 Java source file** (`FizzBuzz.java`) with no external framework dependencies. The entire logic fits in a single `static` method. All diagrams in this section reflect the planned implementation described in PLAN_REVISED_1.md.

---

### 2.1 UML Class Diagram

```mermaid
classDiagram
    direction TB

    class FizzBuzz {
        +main(String[] args)$
        +fizzBuzz(int n)$
        -FizzBuzz() private constructor
    }

    note for FizzBuzz "Package: com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl\nNo external dependencies. No Spring. No interfaces.\nAll logic in fizzBuzz(int n): for-loop + if/else if + System.out.write()."
```

---

### 2.2 Use Cases

#### 2.2.1 Use Case Inventory

| ID | Use Case | Primary Actor | Preconditions | Postconditions | Change from Pre-Refactor |
|----|----------|--------------|---------------|----------------|--------------------------|
| UC-01 | Run FizzBuzz (1..100) | User/Operator | JRE available; JAR on filesystem | FizzBuzz output for 1–100 written to stdout | None — same behavior |
| UC-02 | Run FizzBuzz for arbitrary N | Developer / Test caller | None (no framework required) | FizzBuzz output for 1–N written to stdout | Simpler: direct `FizzBuzz.fizzBuzz(N)` static call |
| UC-03 | Validate FizzBuzz correctness | Automated Test Harness | System.out redirected | All 16 assertions pass; System.out restored | Spring context startup eliminated; test is faster |
| ~~UC-04~~ | ~~Add a new FizzBuzz rule without modifying existing code~~ | ~~Developer~~ | — | — | **Removed.** No longer supported. Adding a rule now requires modifying the `if/else if` chain in `fizzBuzz()`. |

#### 2.2.2 Use Case Diagram

```mermaid
graph TB
    User(["👤 User / Operator"])
    Dev(["👤 Developer"])
    QA(["🤖 Test Harness"])

    subgraph sys ["FizzBuzz (Refactored) — System Boundary"]
        UC01(["UC-01\nRun FizzBuzz 1..100\nvia main()"])
        UC02(["UC-02\nRun FizzBuzz for N\nvia FizzBuzz.fizzBuzz(N)"])
        UC03(["UC-03\nValidate output\nfor N = 1..16"])

        UC01 -. "extends" .-> UC02
    end

    User -->|"executes JAR"| UC01
    Dev -->|"calls static method"| UC02
    QA -->|"runs test suite"| UC03
```

> **UC-04 removed.** The Open/Closed extensibility afforded by the Strategy pattern (pre-refactor) does not exist in the post-refactor. Adding a new rule (e.g., "Bazz" for multiples of 7) requires editing `FizzBuzz.java`. This is the primary extensibility trade-off documented in REQUIREMENTS_REFINED_1.md Section 5.

---

### 2.3 Sequence Diagram

#### 2.3.1 Full Execution Flow

```mermaid
sequenceDiagram
    actor User
    participant FB as FizzBuzz

    User->>FB: main(args)
    FB->>FB: fizzBuzz(100)

    Note over FB: newLine = System.getProperty("line.separator")

    loop for i = 1 to 100
        alt i % 3 == 0 && i % 5 == 0
            FB->>FB: output = "FizzBuzz"
        else i % 3 == 0
            FB->>FB: output = "Fizz"
        else i % 5 == 0
            FB->>FB: output = "Buzz"
        else
            FB->>FB: output = Integer.toString(i)
        end

        FB->>FB: System.out.write((output + newLine).getBytes())
        FB->>FB: System.out.flush()
    end
```

---

#### 2.3.2 Per-Iteration Step Detail (i = 15)

```mermaid
sequenceDiagram
    participant FB as FizzBuzz.fizzBuzz()
    participant SysOut as System.out

    Note over FB,SysOut: Iteration i = 15

    FB->>FB: 15 % 3 == 0 → true
    FB->>FB: 15 % 5 == 0 → true
    FB->>FB: output = "FizzBuzz"
    FB->>SysOut: write(("FizzBuzz" + newLine).getBytes())
    SysOut-->>FB: (written)
    FB->>SysOut: flush()

    Note over FB,SysOut: Net output: "FizzBuzz\n" (4 steps vs. ~30 in pre-refactor)
```
(DY): The `Note over` statement clips outside the rectangle it is placed inside. Can this be fixed?

---

## 3. Architectural Comparison

### 3.1 Structural Contrast

```mermaid
graph LR
    subgraph pre ["Pre-Refactor (87 files)"]
        direction TB
        L1["Entry: Main + StandardFizzBuzz"]
        L2["Strategy Layer: EnterpriseGradeFizzBuzzSolutionStrategy\n+ Factory"]
        L3["Loop Layer: LoopContext + LoopRunner\n+ LoopInitializer + LoopCondition\n+ LoopStep + LoopFinalizer\n+ LoopComponentFactory"]
        L4["Payload Layer: SingleStepPayload\n+ Adapter"]
        L5["Output Gen: SingleStepOutputGenerationStrategy\n+ 3x FizzBuzzOutputGenerationContext\n+ FizzBuzzOutputGenerationContextVisitor"]
        L6["Divisibility: FizzStrategy + BuzzStrategy\n+ NoFizzNoBuzzStrategy"]
        L7["Math: NumberIsMultipleOfAnotherNumberVerifier\n+ IntegerDivider + comparators + converters"]
        L8["Printing: 4 printers + 4 string returners\n+ 14 more factories"]
        L9["Output: SystemOutFizzBuzzOutputStrategy\n+ ExceptionSafeAdapter"]
        L10["DI: ApplicationContextHolder\n+ spring.xml + Spring 3.2.13"]
        L1 --> L2 --> L3 --> L4 --> L5 --> L6 --> L7 --> L8 --> L9
        L10 -.->|"wires"| L1
        L10 -.->|"wires"| L2
        L10 -.->|"wires"| L3
    end

    subgraph post ["Post-Refactor (1 file)"]
        direction TB
        P1["FizzBuzz.main()"]
        P2["FizzBuzz.fizzBuzz(n)"]
        P3["for loop + if/else if"]
        P4["System.out.write() + flush()"]
        P1 --> P2 --> P3 --> P4
    end
```

### 3.2 Pattern Inventory Comparison

| Pattern | Pre-Refactor | Post-Refactor |
|---------|-------------|---------------|
| Strategy | 7 implementations (FizzBuzzSolutionStrategy, IsEvenlyDivisibleStrategy, OutputGenerationStrategy, FizzBuzzOutputStrategy, FizzBuzzExceptionSafeOutputStrategy, LoopPayloadExecution, SingleStepOutputGenerationParameter) | 0 |
| Factory | 15 factory classes | 0 |
| Visitor | FizzBuzzOutputGenerationContextVisitor visits 3 OutputGenerationContexts | 0 |
| Adapter | 2 adapters (ExceptionSafe output, LoopContextRetrieval→SingleStepParam) | 0 |
| Dependency Injection | Spring 3.2.13 (@Service, @Autowired, spring.xml) | 0 |
| Service Locator (anti-pattern) | ApplicationContextHolder + 2nd Spring context in LoopContext | 0 |
| Template Method | LoopRunner's for-loop template (init/condition/step/finalize) | 0 |
| Comparator | ThreeWayIntegerComparator, IntegerForEqualityComparator, double comparators | 0 |
| **Total named patterns** | **8+** | **0** |

### 3.3 Call Depth Comparison (for a single output token, e.g., "Fizz")

```mermaid
graph TD
    subgraph pre2 ["Pre-Refactor call depth: ~14 frames"]
        A1["main()"] --> A2["fizzBuzz(n)"] --> A3["runSolution(n)"] --> A4["runLoop()"]
        A4 --> A5["runLoopPayload(stateRetrieval)"]
        A5 --> A6["performGenerationForCurrentStep(adapter)"]
        A6 --> A7["visit(context, i)"]
        A7 --> A8["isEvenlyDivisible(i)"]
        A8 --> A9["numberIsMultipleOfAnotherNumber(i, 3)"]
        A9 --> A10["divide(i, 3)"]
        A10 --> A11["printValue(i)"] --> A12["print()"]
        A12 --> A13["output(string)"] --> A14["System.out.write(bytes)"]
    end

    subgraph post2 ["Post-Refactor call depth: 3 frames"]
        B1["main()"] --> B2["fizzBuzz(n)"] --> B3["System.out.write(bytes)"]
    end
```

(DY): I do not see anything in the above diagrams that links the requirements (from REQUIREMENTS_REFINED_1.md) to any generated graphs. This means that I have no way of tracing whether requirements are satisfied as part of the visual design artifacts. This is traceability needs to be added.
