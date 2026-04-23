# Experiment Reflections
**Question:** So the experiment is complete? Were there any problems or unexpected developments?
**Date:** 2026-04-22

---

Yes, the experiment is complete. Here is an honest summary of problems and unexpected findings.

---

## Problems Encountered

**1. `collect_metrics.sh` strict-mode bug**
The script was written with `set -euo pipefail`. The `-e` flag caused the script to abort whenever `grep` returned exit code 1 (no matches), which is standard behavior for "nothing found." Fixed by removing `-e`. This was caught during Phase 0 dry-run verification, before any real data collection.

**2. `collect_metrics.sh` FR-20 bug (persists in the script)**
`mvn dependency:list -q` suppresses the dependency list output entirely, so the script always reported 0 compile-scope dependencies — even pre-refactor when there were 8. We worked around this for both Phase 1 and Phase 3 by running the command manually without `-q`. The script itself still has this bug; it just wasn't worth fixing mid-experiment.

**3. `collect_metrics.sh` M-A/M-B structural gap**
The script's FR-18 section runs `mvn clean` before each test run, which deletes the JAR built during FR-17. So by the time M-A/M-B execute, no JAR exists. Both required manual collection in both phases. This is a design flaw in the script, not a measurement error.

**4. PMD XML not present when the script ran**
The script runs `mvn pmd:pmd` internally, but this requires compilation to have happened first. In the collection flow, PMD ran but the XML wasn't parseable at the right moment. FR-16/M-D were collected manually in both phases. Pre-refactor result: 0 violations. Post-refactor: 0 violations (same).

**5. `java -jar` didn't work pre-refactor**
The plan's M-B step assumed `java -jar` would work in both phases. Pre-refactor, the thin JAR doesn't bundle Spring, so it fails with `NoClassDefFoundError`. We used `java -cp` with the Maven-generated classpath instead. This creates a slight methodological inconsistency in the M-B comparison — noted and documented in the Phase 4 report.

---

## Unexpected Findings

**1. PMD found zero violations in the pre-refactor codebase**
The plan expected CC and coupling violations to be measurable and decrease post-refactor. Instead, the pre-refactor codebase had CC ≤ 10 in every method (PMD threshold) despite being 87 files. The complexity was entirely structural — achieved by class proliferation, not branching depth. PMD's metrics were blind to this. The keyword-count approximation gave 100 occurrences across 87 files (~1.1 per file), confirming each method was trivially simple. This is a genuine finding about the limits of standard code complexity metrics for satirically over-engineered code.

**2. M-C: no GC triggered post-refactor**
The post-refactor application ran to completion inside the JVM's initial 4 MB heap allocation without a single garbage collection event. Java heap used at exit: 0K. Total JVM memory footprint was ~4.4 MB of Metaspace (class loading only). We hadn't predicted the heap usage would be this low.

**3. Test count didn't change (12 → 12)**
The plan's M-F section anticipated the post-refactor test count would be higher than the pre-refactor (which was expected to have only 1 test, `testFizzBuzz`). In the pre-execution audit we had already expanded the test suite to 12 tests and carried those forward — so the pre/post counts were identical. Not a problem, but it made M-F's "↑ expected" direction a non-event.

**4. Build time reduction was modest (−17.5%)**
We expected a larger build time improvement from compiling 1 file vs. 87. In practice, Maven's lifecycle overhead (dependency resolution, plugin initialization, JaCoCo agent setup) dominates compilation time. The compiler itself is not the bottleneck at this scale.

**5. The first deletion attempt silently failed**
When deleting the old production directories in Phase 2, the first `rm -rf` invocation using comma-separated arguments in a single command didn't work as expected — the files remained. The second attempt, using separate `rm -rf` calls, succeeded. Nothing was lost; it was caught immediately by the `find` verification.

---

## Scientific Validity Note

The biggest honest caveat for the experiment's findings is the one documented in Phase 4: this was a **rewrite to a minimal implementation**, not an incremental refactoring. The measured improvements reflect the gap between a deliberately absurd baseline and a correct-but-minimal target. The numbers would look very different if the comparison were pre/post of a normal production codebase undergoing disciplined refactoring.
