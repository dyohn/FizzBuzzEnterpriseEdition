# Phase 00: Environment Baseline
Date: 2026-04-22
Git commit: 53c97af7e584998eb753ae8a643d2412d52e85b6
Git branch: ai-refactor-experiment

## A.1 Tool Versions

| Tool    | Version                        |
|---------|--------------------------------|
| Java    | OpenJDK 11.0.25 Temurin (aarch64) |
| Maven   | 3.9.12                         |
| Gradle  | 6.9.4                          |
| python3 | 3.13.9                         |
| cloc    | 2.08                           |

## A.2 Hardware / OS

| Property | Value                      |
|----------|----------------------------|
| OS       | Darwin 24.6.0 (macOS)      |
| Arch     | arm64 (Apple Silicon)      |
| CPU      | Apple M3 Pro               |
| RAM      | 36 GB                      |
| Shell    | /bin/zsh                   |

## A.3 Collection Script Verification

Script: `scripts/collect_metrics.sh`
Dry-run command: `./scripts/collect_metrics.sh pre-verify`
Exit code: 0
Output file: `measurements/metrics_pre-verify.md`

### Known pre-verify gaps (expected, not errors)

- **FR-16/M-D (PMD)**: `(PMD XML parse failed)` — `mvn pmd:pmd` runs but produces no violations XML without prior compile; Phase 1 script will work correctly after `mvn package`.
- **M-A/M-B (JAR size / exec time)**: No JAR found — FR-18 test runs invoke `mvn clean` which removes the FR-17 build artifact. Phase 1 will run `mvn package -DskipTests` explicitly before M-A collection.
- **FR-20 (dependency count)**: Reports 0 — `mvn dependency:list -q` suppresses the dependency list. Phase 1 collection will use a corrected invocation without `-q`.

## A.4 Pre-Refactor Baseline Snapshot (from pre-verify)

| Metric | Value |
|--------|-------|
| Java source files (src/main) | 87 |
| Interfaces | 26 |
| Abstract classes | 0 |
| Enums | 1 |
| Total source lines (cloc: code) | 1540 Java + 113 Maven + 27 Gradle + 11 XML = **1691** |
| Test methods (@Test) | 12 |
| Maximum package depth | 10 |
| Public method count (approx) | 129 |
| @Service annotations | 51 |
| @Autowired annotations | 29 |
| @PostConstruct annotations | 1 |
| DI annotation total | 81 |
| Build time (5 runs, mvn package -DskipTests) | 1.564 / 1.543 / 1.498 / 1.500 / 1.466 s |
| Test time (5 runs, mvn test) | 3.185 / 3.361 / 3.303 / 3.163 / 3.160 s |
| JaCoCo instruction coverage (overall) | 928/1005 = **92.34%** |
| Spring ApplicationContext instantiation sites | 5 |
