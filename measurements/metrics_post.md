# Metrics Collection: post
Date: Wed Apr 22 21:02:34 EDT 2026
Git commit: 58dcc6f69f023c17be9662906a2c61b15e1f4618
Git branch: ai-refactor-experiment

## FR-14: Lines of Code (cloc)
github.com/AlDanial/cloc v 2.08  T=0.01 s (526.5 files/s, 54233.5 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                             3             41            121            255
Maven                            1              6              0             77
Gradle                           1              4              0             11
-------------------------------------------------------------------------------
SUM:                             5             51            121            343
-------------------------------------------------------------------------------

## FR-15: Class and Interface Counts
Total Java files (src/main): 1
Interfaces:       0
Abstract classes: 0
Enums:            0

## FR-16 + M-D: PMD Analysis
(PMD XML parse failed)

## FR-17: Build Time (mvn clean package -DskipTests, 5 runs)
Run 1: real	0m1.253s
Run 2: real	0m1.263s
Run 3: real	0m1.266s
Run 4: real	0m1.251s
Run 5: real	0m1.276s

## FR-18: Test Execution Time (mvn test, 5 runs)
Run 1: real	0m1.505s
Run 2: real	0m1.527s
Run 3: real	0m1.509s
Run 4: real	0m1.500s
Run 5: real	0m1.510s

## FR-13: JaCoCo Instruction Coverage
covered=53 missed=1 total=54 pct=98.15%
covered=0 missed=3 total=3 pct=0.00%
covered=53 missed=4 total=57 pct=92.98%
covered=53 missed=4 total=57 pct=92.98%
covered=53 missed=4 total=57 pct=92.98%
covered=53 missed=4 total=57 pct=92.98%

## FR-19: Package Depth
Maximum package depth: 7

## FR-20: Runtime Dependency Count
Compile-scope dependencies: 0

## FR-21: Public Method Count
Approximate public method count: 2

## FR-22: DI Annotation Count
@Service: 0
@Autowired: 0
@Component: 0
@Repository: 0
@Controller: 0
@PostConstruct: 0
Total: 0

## M-A: JAR Artifact Size
(No JAR found — run mvn package first)

## M-B: Application Execution Time (java -jar, 6 runs; run 1 discarded)
(No JAR available)

## M-E: Spring ApplicationContext Instantiation Sites
Count: 0

## M-F: Test Metrics
@Test methods: 12
github.com/AlDanial/cloc v 2.08  T=0.01 s (250.0 files/s, 45250.6 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                             2             37             92            233
-------------------------------------------------------------------------------
SUM:                             2             37             92            233
-------------------------------------------------------------------------------

---
Collection complete: Wed Apr 22 21:02:58 EDT 2026
