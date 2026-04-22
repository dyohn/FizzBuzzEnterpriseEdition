# Build Modernization Notes

**Date:** 2026-04-21
**Branch:** `ai-refactor-experiment`
**Java version:** OpenJDK 11.0.25 (Eclipse Temurin)

This document records the build infrastructure changes made to bring the project's
Maven and Gradle configurations into compatibility with Java 11. No application
source code was modified.

---

## Problems

The project was originally written and configured for Java 7 (2015). Three
incompatibilities prevented it from building or running tests under Java 11.

### 1. Removed JEE API: `javax.annotation.PostConstruct`

`NumberIsMultipleOfAnotherNumberVerifier.java` imports
`javax.annotation.PostConstruct`. This annotation was part of the JEE API bundle
shipped inside the JDK through Java 8. Java 9 began the process of removing
bundled JEE modules, and Java 11 completed that removal. The class was simply
absent at compile time, producing a hard compilation error.

### 2. Outdated JaCoCo plugin (Maven)

`pom.xml` declared `jacoco-maven-plugin` version `0.5.8.201207111220` (released
2012). JaCoCo 0.5.x uses an old version of the ASM bytecode library that cannot
instrument class files produced by Java 9 or later JVMs, and the JaCoCo agent
itself relies on internal JVM APIs that were changed in Java 9. This would have
caused `mvn test` to fail.

### 3. Missing source/target compatibility in Gradle (Gradle)

`build.gradle` had no `sourceCompatibility` or `targetCompatibility` settings.
Without them, Gradle compiles to the JVM's native class file version — Java 11
(class file version 55) in this case. Spring Framework 3.2.13 bundles ASM 3.x,
which only supports class files up to Java 7 (class file version 51). At test
time, Spring's component-scan tried to read the compiled class files and crashed
with `ASM ClassReader failed to parse class file`. Maven was unaffected because
its compiler plugin already had `<source>1.7</source><target>1.7</target>`
configured.

---

## Changes

### `pom.xml`

| Item | Before | After |
|------|--------|-------|
| `maven-compiler-plugin` version | `2.3` | `3.11.0` |
| `jacoco-maven-plugin` version | `0.5.8.201207111220` | `0.8.11` |
| `javax.annotation-api` dependency | absent | `1.3.2` |

### `build.gradle`

| Item | Before | After |
|------|--------|-------|
| Repository | `jcenter()` | `mavenCentral()` |
| `javax.annotation-api` dependency | absent | `1.3.2` (compile scope) |
| `sourceCompatibility` | not set | `'1.7'` |
| `targetCompatibility` | not set | `'1.7'` |

> `jcenter()` was also replaced with `mavenCentral()` because JFrog shut down
> the JCenter repository's write access in 2021 and its read access in 2022.
> All required artifacts (Spring 3.2.13, JUnit 4.8.2) are available on Maven
> Central.

### `gradle/wrapper/gradle-wrapper.properties`

| Item | Before | After |
|------|--------|-------|
| Gradle version | `2.8` | `6.9.4` |

Gradle 2.8 (2015) requires Java 6–8 and cannot run under Java 11. Gradle 6.9.4
is the last release in the 6.x line and fully supports Java 11. Gradle 7.x was
not used because it removes the `compile` and `testCompile` dependency
configurations that `build.gradle` relies on; upgrading to 7.x would require
rewriting the dependency declarations.

---

## Verified results

After the changes above, both build systems complete all stages successfully
with zero test failures.

| Stage | Maven command | Result | Gradle command | Result |
|-------|--------------|--------|----------------|--------|
| Compile | `mvn compile` | PASS | `./gradlew classes` | PASS |
| Test | `mvn test` | PASS (1/1) | `./gradlew test` | PASS (1/1) |
| Clean | `mvn clean` | PASS | `./gradlew clean` | PASS |

---

## Known warnings

Gradle 6.9.4 emits a deprecation warning on every build:

```
Deprecated Gradle features were used in this build, making it incompatible with Gradle 7.0.
```

This is expected. The `compile` and `testCompile` configurations used in
`build.gradle` were deprecated in Gradle 3.4 and removed in Gradle 7.0. The
warning is harmless at Gradle 6.9.4 and will be resolved when the dependency
declarations are updated to `implementation` / `testImplementation` as part of
the planned refactor work.
