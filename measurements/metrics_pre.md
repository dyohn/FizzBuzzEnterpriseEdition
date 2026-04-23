# Metrics Collection: pre
Date: Wed Apr 22 20:49:20 EDT 2026
Git commit: 6b7f98bb65590a246ac92f80726b23c760dcd4ff
Git branch: ai-refactor-experiment

## FR-14: Lines of Code (cloc)
github.com/AlDanial/cloc v 2.08  T=0.04 s (2508.9 files/s, 82822.0 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            89            518            814           1540
Maven                            1              6              0            113
Gradle                           1              6              0             27
XML                              1              2              0             11
-------------------------------------------------------------------------------
SUM:                            92            532            814           1691
-------------------------------------------------------------------------------

## FR-15: Class and Interface Counts
Total Java files (src/main): 87
Interfaces:       26
Abstract classes: 0
Enums:            1

## FR-16 + M-D: PMD Analysis
(PMD XML parse failed)

## FR-17: Build Time (mvn clean package -DskipTests, 5 runs)
Run 1: real	0m1.614s
Run 2: real	0m1.512s
Run 3: real	0m1.541s
Run 4: real	0m1.531s
Run 5: real	0m1.524s

## FR-18: Test Execution Time (mvn test, 5 runs)
Run 1: real	0m3.078s
Run 2: real	0m3.198s
Run 3: real	0m3.213s
Run 4: real	0m3.239s
Run 5: real	0m3.220s

## FR-13: JaCoCo Instruction Coverage
covered=3 missed=0 total=3 pct=100.00%
covered=24 missed=2 total=26 pct=92.31%
covered=27 missed=2 total=29 pct=93.10%
covered=49 missed=0 total=49 pct=100.00%
covered=24 missed=0 total=24 pct=100.00%
covered=73 missed=0 total=73 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=10 missed=0 total=10 pct=100.00%
covered=16 missed=0 total=16 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=8 missed=0 total=8 pct=100.00%
covered=11 missed=0 total=11 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=16 missed=0 total=16 pct=100.00%
covered=22 missed=0 total=22 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=8 missed=0 total=8 pct=100.00%
covered=11 missed=0 total=11 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=7 missed=0 total=7 pct=100.00%
covered=10 missed=0 total=10 pct=100.00%
covered=73 missed=0 total=73 pct=100.00%
covered=10 missed=0 total=10 pct=100.00%
covered=11 missed=0 total=11 pct=100.00%
covered=11 missed=0 total=11 pct=100.00%
covered=27 missed=2 total=29 pct=93.10%
covered=22 missed=0 total=22 pct=100.00%
covered=16 missed=0 total=16 pct=100.00%
covered=170 missed=2 total=172 pct=98.84%
covered=5 missed=0 total=5 pct=100.00%
covered=5 missed=0 total=5 pct=100.00%
covered=5 missed=0 total=5 pct=100.00%
covered=5 missed=0 total=5 pct=100.00%
covered=5 missed=0 total=5 pct=100.00%
covered=5 missed=0 total=5 pct=100.00%
covered=10 missed=0 total=10 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=16 missed=0 total=16 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=28 missed=0 total=28 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=18 missed=0 total=18 pct=100.00%
covered=0 missed=3 total=3 pct=0.00%
covered=27 missed=3 total=30 pct=90.00%
covered=9 missed=0 total=9 pct=100.00%
covered=16 missed=0 total=16 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=28 missed=0 total=28 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=19 missed=0 total=19 pct=100.00%
covered=0 missed=5 total=5 pct=0.00%
covered=6 missed=0 total=6 pct=100.00%
covered=34 missed=5 total=39 pct=87.18%
covered=6 missed=0 total=6 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=27 missed=3 total=30 pct=90.00%
covered=6 missed=0 total=6 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=28 missed=0 total=28 pct=100.00%
covered=34 missed=5 total=39 pct=87.18%
covered=28 missed=0 total=28 pct=100.00%
covered=141 missed=8 total=149 pct=94.63%
covered=9 missed=0 total=9 pct=100.00%
covered=38 missed=9 total=47 pct=80.85%
covered=47 missed=9 total=56 pct=83.93%
covered=3 missed=0 total=3 pct=100.00%
covered=13 missed=0 total=13 pct=100.00%
covered=17 missed=3 total=20 pct=85.00%
covered=33 missed=3 total=36 pct=91.67%
covered=33 missed=3 total=36 pct=91.67%
covered=47 missed=9 total=56 pct=83.93%
covered=80 missed=12 total=92 pct=86.96%
covered=34 missed=0 total=34 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=8 missed=0 total=8 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=60 missed=0 total=60 pct=100.00%
covered=12 missed=0 total=12 pct=100.00%
covered=17 missed=0 total=17 pct=100.00%
covered=29 missed=0 total=29 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=2 missed=0 total=2 pct=100.00%
covered=5 missed=0 total=5 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=16 missed=0 total=16 pct=100.00%
covered=19 missed=0 total=19 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=4 missed=0 total=4 pct=100.00%
covered=7 missed=0 total=7 pct=100.00%
covered=7 missed=0 total=7 pct=100.00%
covered=19 missed=0 total=19 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=60 missed=0 total=60 pct=100.00%
covered=29 missed=0 total=29 pct=100.00%
covered=5 missed=0 total=5 pct=100.00%
covered=129 missed=0 total=129 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=15 missed=0 total=15 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=17 missed=0 total=17 pct=100.00%
covered=20 missed=0 total=20 pct=100.00%
covered=15 missed=0 total=15 pct=100.00%
covered=20 missed=0 total=20 pct=100.00%
covered=35 missed=0 total=35 pct=100.00%
covered=15 missed=5 total=20 pct=75.00%
covered=15 missed=5 total=20 pct=75.00%
covered=11 missed=0 total=11 pct=100.00%
covered=11 missed=0 total=11 pct=100.00%
covered=34 missed=0 total=34 pct=100.00%
covered=34 missed=0 total=34 pct=100.00%
covered=15 missed=5 total=20 pct=75.00%
covered=34 missed=0 total=34 pct=100.00%
covered=11 missed=0 total=11 pct=100.00%
covered=60 missed=5 total=65 pct=92.31%
covered=6 missed=0 total=6 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=0 missed=3 total=3 pct=0.00%
covered=0 missed=3 total=3 pct=0.00%
covered=0 missed=3 total=3 pct=0.00%
covered=0 missed=22 total=22 pct=0.00%
covered=0 missed=25 total=25 pct=0.00%
covered=4 missed=0 total=4 pct=100.00%
covered=4 missed=0 total=4 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=8 missed=0 total=8 pct=100.00%
covered=14 missed=0 total=14 pct=100.00%
covered=4 missed=0 total=4 pct=100.00%
covered=2 missed=0 total=2 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=0 missed=25 total=25 pct=0.00%
covered=16 missed=0 total=16 pct=100.00%
covered=0 missed=3 total=3 pct=0.00%
covered=14 missed=0 total=14 pct=100.00%
covered=30 missed=28 total=58 pct=51.72%
covered=3 missed=0 total=3 pct=100.00%
covered=19 missed=0 total=19 pct=100.00%
covered=22 missed=0 total=22 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=16 missed=0 total=16 pct=100.00%
covered=19 missed=0 total=19 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=19 missed=0 total=19 pct=100.00%
covered=22 missed=0 total=22 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=13 missed=0 total=13 pct=100.00%
covered=16 missed=0 total=16 pct=100.00%
covered=16 missed=0 total=16 pct=100.00%
covered=22 missed=0 total=22 pct=100.00%
covered=19 missed=0 total=19 pct=100.00%
covered=22 missed=0 total=22 pct=100.00%
covered=79 missed=0 total=79 pct=100.00%
covered=0 missed=6 total=6 pct=0.00%
covered=0 missed=6 total=6 pct=0.00%
covered=0 missed=3 total=3 pct=0.00%
covered=0 missed=15 total=15 pct=0.00%
covered=0 missed=15 total=15 pct=0.00%
covered=0 missed=15 total=15 pct=0.00%
covered=6 missed=0 total=6 pct=100.00%
covered=4 missed=0 total=4 pct=100.00%
covered=10 missed=0 total=10 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=6 missed=3 total=9 pct=66.67%
covered=12 missed=3 total=15 pct=80.00%
covered=12 missed=3 total=15 pct=80.00%
covered=10 missed=0 total=10 pct=100.00%
covered=22 missed=3 total=25 pct=88.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=12 missed=0 total=12 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=7 missed=0 total=7 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=28 missed=0 total=28 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=6 missed=0 total=6 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=28 missed=0 total=28 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=9 missed=0 total=9 pct=100.00%
covered=154 missed=0 total=154 pct=100.00%
covered=3 missed=0 total=3 pct=100.00%
covered=6 missed=2 total=8 pct=75.00%
covered=9 missed=2 total=11 pct=81.82%
covered=3 missed=0 total=3 pct=100.00%
covered=6 missed=2 total=8 pct=75.00%
covered=9 missed=2 total=11 pct=81.82%
covered=9 missed=2 total=11 pct=81.82%
covered=9 missed=2 total=11 pct=81.82%
covered=18 missed=4 total=22 pct=81.82%
covered=928 missed=77 total=1005 pct=92.34%

## FR-19: Package Depth
Maximum package depth: 10

## FR-20: Runtime Dependency Count
Compile-scope dependencies: 0

## FR-21: Public Method Count
Approximate public method count: 129

## FR-22: DI Annotation Count
@Service: 51
@Autowired: 29
@Component: 0
@Repository: 0
@Controller: 0
@PostConstruct: 1
Total: 81

## M-A: JAR Artifact Size
(No JAR found — run mvn package first)

## M-B: Application Execution Time (java -jar, 6 runs; run 1 discarded)
(No JAR available)

## M-E: Spring ApplicationContext Instantiation Sites
Count: 5
src/main/java/com/seriouscompany/business/java/fizzbuzz/packagenamingpackage/impl/Main.java:5:import org.springframework.context.support.ClassPathXmlApplicationContext;
src/main/java/com/seriouscompany/business/java/fizzbuzz/packagenamingpackage/impl/Main.java:20:		final ApplicationContext context = new ClassPathXmlApplicationContext(Constants.SPRING_XML);
src/main/java/com/seriouscompany/business/java/fizzbuzz/packagenamingpackage/impl/ApplicationContextHolder.java:29:		static ApplicationContextReferenceUpdater INSTANCE = new ApplicationContextReferenceUpdater();
src/main/java/com/seriouscompany/business/java/fizzbuzz/packagenamingpackage/impl/loop/LoopContext.java:5:import org.springframework.context.support.ClassPathXmlApplicationContext;
src/main/java/com/seriouscompany/business/java/fizzbuzz/packagenamingpackage/impl/loop/LoopContext.java:28:		final ApplicationContext context = new ClassPathXmlApplicationContext(Constants.SPRING_XML);

## M-F: Test Metrics
@Test methods: 12
github.com/AlDanial/cloc v 2.08  T=0.01 s (241.2 files/s, 46182.7 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                             2             37             96            250
-------------------------------------------------------------------------------
SUM:                             2             37             96            250
-------------------------------------------------------------------------------

---
Collection complete: Wed Apr 22 20:49:55 EDT 2026
