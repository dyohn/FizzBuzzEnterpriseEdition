import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.interfaces.FizzBuzz;

/**
 * Tests for FizzBuzz — pre-refactor (Spring-based) implementation.
 *
 * Test inventory:
 *   T-00  testFizzBuzz                    — Oracle conformance N=1..16 (FR-03–FR-06, FR-12, NFR-05)
 *   T-01  testFizzBuzzZeroProducesNoOutput — N=0 produces zero bytes (FR-09)
 *   T-02  testOutputCardinality           — Exactly N entries for fizzBuzz(N) (FR-01)
 *   T-03  testNoPreambleAndNoTrailingContent — No preamble; output ends after Nth separator (FR-07, FR-10)
 *   T-04  testPlatformLineSeparator       — Uses System.getProperty("line.separator"), not hardcoded (FR-08)
 *   T-05  testCallIndependence            — Repeated calls produce identical output (SR-02, SR-06)
 *   T-06  testNoExceptionPropagation      — fizzBuzz(N) never throws for N >= 0 (SR-07)
 *   T-07  testIOExceptionSuppression      — IOException from output stream does not escape (SR-01, SR-07)
 *   T-08  testPerEntryFlush               — flush() called at least once per entry (SR-04)
 *   T-09  testArithmeticCorrectnessExhaustive — Token matches modulo rules for i=1..100 (FR-03–FR-06, SR-08)
 *   T-10  testNoSystemErrOutput           — Nothing written to System.err (SR-05)
 *   T-11  testOutputOrdering              — Entries appear in strictly ascending index order (FR-02)
 */
public class FizzBuzzTest {

    private PrintStream originalOut;
    private PrintStream originalErr;
    private FizzBuzz fb;

    @Before
    public void setUp() {
        final ApplicationContext context = new ClassPathXmlApplicationContext(TestConstants.SPRING_XML);
        this.fb = (FizzBuzz) context.getBean(TestConstants.STANDARD_FIZZ_BUZZ);
        this.originalOut = System.out;
        this.originalErr = System.err;
        ((ConfigurableApplicationContext) context).close();
    }

    @After
    public void tearDown() {
        System.setOut(this.originalOut);
        System.setErr(this.originalErr);
    }

    /**
     * Captures the output of fizzBuzz(n) as a String.
     * Redirects System.out to a ByteArrayOutputStream, calls fizzBuzz(n),
     * flushes, restores System.out to originalOut, and returns the captured string.
     */
    private String captureOutput(final int n) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        this.fb.fizzBuzz(n);
        System.out.flush();
        System.setOut(this.originalOut);
        return baos.toString();
    }

    /**
     * Helper used by T-00 oracle test: captures output and asserts against expected string.
     * Uses BufferedOutputStream to match original test behavior.
     */
    private void doFizzBuzz(final int n, final String s) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final BufferedOutputStream bos = new BufferedOutputStream(baos);
        System.setOut(new PrintStream(bos));
        this.fb.fizzBuzz(n);
        System.out.flush();
        final String platformDependentExpectedResult =
                s.replaceAll("\\n", System.getProperty("line.separator"));
        assertEquals(platformDependentExpectedResult, baos.toString());
    }

    // -------------------------------------------------------------------------
    // T-00: Oracle conformance N=1..16
    // Verifies: FR-03, FR-04, FR-05, FR-06, FR-12, NFR-05
    // -------------------------------------------------------------------------

    @Test
    public void testFizzBuzz() throws IOException {
        this.doFizzBuzz(TestConstants.INT_1,  TestConstants._1_);
        this.doFizzBuzz(TestConstants.INT_2,  TestConstants._1_2_);
        this.doFizzBuzz(TestConstants.INT_3,  TestConstants._1_2_FIZZ);
        this.doFizzBuzz(TestConstants.INT_4,  TestConstants._1_2_FIZZ_4);
        this.doFizzBuzz(TestConstants.INT_5,  TestConstants._1_2_FIZZ_4_BUZZ);
        this.doFizzBuzz(TestConstants.INT_6,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ);
        this.doFizzBuzz(TestConstants.INT_7,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7);
        this.doFizzBuzz(TestConstants.INT_8,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8);
        this.doFizzBuzz(TestConstants.INT_9,  TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ);
        this.doFizzBuzz(TestConstants.INT_10, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ);
        this.doFizzBuzz(TestConstants.INT_11, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11);
        this.doFizzBuzz(TestConstants.INT_12, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ);
        this.doFizzBuzz(TestConstants.INT_13, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13);
        this.doFizzBuzz(TestConstants.INT_14, TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14);
        this.doFizzBuzz(TestConstants.INT_15,
                TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14_FIZZ_BUZZ);
        this.doFizzBuzz(TestConstants.INT_16,
                TestConstants._1_2_FIZZ_4_BUZZ_FIZZ_7_8_FIZZ_BUZZ_11_FIZZ_13_14_FIZZ_BUZZ_16);
    }

    // -------------------------------------------------------------------------
    // T-01: N=0 produces zero bytes
    // Verifies: FR-09
    // -------------------------------------------------------------------------

    @Test
    public void testFizzBuzzZeroProducesNoOutput() {
        assertEquals("fizzBuzz(0) must produce no output", 0, captureOutput(0).length());
    }

    // -------------------------------------------------------------------------
    // T-02: Exactly N entries for fizzBuzz(N)
    // Verifies: FR-01
    // -------------------------------------------------------------------------

    @Test
    public void testOutputCardinality() {
        final String sep = System.getProperty("line.separator");
        for (int n = 1; n <= 20; n++) {
            final String output = captureOutput(n);
            // Split on sep with limit -1 to preserve trailing empty string after final sep.
            // Entry count = parts.length - 1 (trailing "" after the final separator).
            final String[] parts = output.split(sep, -1);
            final int entryCount = parts.length - 1;
            assertEquals("fizzBuzz(" + n + ") must produce exactly " + n + " entries",
                    n, entryCount);
        }
    }

    // -------------------------------------------------------------------------
    // T-03: No preamble before first entry; no trailing content after last separator
    // Verifies: FR-07, FR-10
    // -------------------------------------------------------------------------

    @Test
    public void testNoPreambleAndNoTrailingContent() {
        final String sep = System.getProperty("line.separator");
        final String output = captureOutput(5); // "1<sep>2<sep>Fizz<sep>4<sep>Buzz<sep>"
        assertFalse("Output must not begin with the line separator", output.startsWith(sep));
        assertTrue("Output must end with the line separator", output.endsWith(sep));
        final String afterLastSep = output.substring(output.lastIndexOf(sep) + sep.length());
        assertEquals("There must be no content after the final line separator", "", afterLastSep);
    }

    // -------------------------------------------------------------------------
    // T-04: Line separator is read from System property, not hardcoded
    // Verifies: FR-08
    //
    // Note: NewLineStringReturner.getReturnString() reads System.getProperty("line.separator")
    // at call time (not cached at Spring bean construction time), so this test is valid
    // for the pre-refactor system.
    // -------------------------------------------------------------------------

    @Test
    public void testPlatformLineSeparator() {
        final String original = System.getProperty("line.separator");
        System.setProperty("line.separator", "|||");
        try {
            final String output = captureOutput(3); // "1|||2|||Fizz|||"
            assertTrue("Output must contain the overridden line separator '|||'",
                    output.contains("|||"));
            // Verify it is not using the original separator (only meaningful if original != "|||")
            if (!original.equals("|||")) {
                assertFalse("Output must not contain the original line separator when overridden",
                        output.contains(original));
            }
        } finally {
            System.setProperty("line.separator", original);
        }
    }

    // -------------------------------------------------------------------------
    // T-05: Repeated calls produce byte-identical output
    // Verifies: SR-02 (call independence), SR-06 (determinism)
    // -------------------------------------------------------------------------

    @Test
    public void testCallIndependence() {
        final String first  = captureOutput(10);
        final String second = captureOutput(10);
        final String third  = captureOutput(10);
        assertEquals("1st and 2nd calls to fizzBuzz(10) must produce identical output",
                first, second);
        assertEquals("2nd and 3rd calls to fizzBuzz(10) must produce identical output",
                second, third);
    }

    // -------------------------------------------------------------------------
    // T-06: fizzBuzz(N) does not throw for any N >= 0
    // Verifies: SR-07
    // -------------------------------------------------------------------------

    @Test
    public void testNoExceptionPropagation() {
        for (final int n : new int[]{0, 1, 3, 5, 15, 16, 100}) {
            try {
                captureOutput(n);
            } catch (final Throwable t) {
                fail("fizzBuzz(" + n + ") must not throw any exception; got: " + t);
            }
        }
    }

    // -------------------------------------------------------------------------
    // T-07: IOException from the output stream does not propagate out of fizzBuzz
    // Verifies: SR-01, SR-07
    //
    // Note: In the pre-refactor system, exception suppression is provided by
    // FizzBuzzOutputStrategyToFizzBuzzExceptionSafeOutputStrategyAdapter.
    // PrintStream also silently catches IOExceptions from its underlying stream
    // and sets an internal error flag instead of propagating them, so this test
    // may pass trivially in both systems. It remains a valid regression guard
    // that no unchecked exception escapes fizzBuzz().
    // -------------------------------------------------------------------------

    @Test
    public void testIOExceptionSuppression() {
        final PrintStream throwingStream = new PrintStream(new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
                throw new IOException("forced write failure");
            }
            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                throw new IOException("forced write failure");
            }
        });
        System.setOut(throwingStream);
        try {
            this.fb.fizzBuzz(5);
        } catch (final Throwable t) {
            fail("fizzBuzz() must suppress IOException and not propagate it; instead threw: " + t);
        } finally {
            System.setOut(this.originalOut);
        }
    }

    // -------------------------------------------------------------------------
    // T-08: At least one flush per entry
    // Verifies: SR-04
    // -------------------------------------------------------------------------

    @Test
    public void testPerEntryFlush() {
        final int[] flushCount = {0};
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream countingStream = new PrintStream(baos) {
            @Override
            public void flush() {
                flushCount[0]++;
                super.flush();
            }
        };
        System.setOut(countingStream);
        this.fb.fizzBuzz(10);
        System.setOut(this.originalOut);
        assertTrue("Expected at least 10 flush() calls for fizzBuzz(10), got: " + flushCount[0],
                flushCount[0] >= 10);
    }

    // -------------------------------------------------------------------------
    // T-09: Exhaustive arithmetic correctness for i=1..100
    // Verifies: FR-03, FR-04, FR-05, FR-06, SR-08
    // -------------------------------------------------------------------------

    @Test
    public void testArithmeticCorrectnessExhaustive() {
        final String sep = System.getProperty("line.separator");
        final String[] lines = captureOutput(100).split(sep, -1);
        for (int i = 1; i <= 100; i++) {
            final String token = lines[i - 1];
            final boolean div3 = (i % 3 == 0);
            final boolean div5 = (i % 5 == 0);
            if (div3 && div5) {
                assertEquals("i=" + i + " (div3 && div5) must be FizzBuzz", "FizzBuzz", token);
            } else if (div3) {
                assertEquals("i=" + i + " (div3) must be Fizz", "Fizz", token);
            } else if (div5) {
                assertEquals("i=" + i + " (div5) must be Buzz", "Buzz", token);
            } else {
                assertEquals("i=" + i + " must be integer string", Integer.toString(i), token);
            }
        }
    }

    // -------------------------------------------------------------------------
    // T-10: Nothing written to System.err during fizzBuzz execution
    // Verifies: SR-05 (partial — covers System.err; filesystem/network/thread
    //           side effects require manual inspection per the validation matrix)
    // -------------------------------------------------------------------------

    @Test
    public void testNoSystemErrOutput() {
        final ByteArrayOutputStream errCapture = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errCapture));
        captureOutput(16);
        System.setErr(this.originalErr);
        assertEquals("fizzBuzz() must produce no output on System.err", 0, errCapture.size());
    }

    // -------------------------------------------------------------------------
    // T-11: Entries appear in strictly ascending iteration order i=1..N
    // Verifies: FR-02
    //
    // The oracle test (T-00) implicitly validates ordering via exact string
    // matching. This test makes the ordering assertion explicit and independent
    // of the oracle string constants.
    // -------------------------------------------------------------------------

    @Test
    public void testOutputOrdering() {
        final String sep = System.getProperty("line.separator");
        final String[] lines = captureOutput(16).split(sep, -1);
        final String[] expected = {
            "1", "2", "Fizz", "4", "Buzz", "Fizz", "7", "8",
            "Fizz", "Buzz", "11", "Fizz", "13", "14", "FizzBuzz", "16"
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Entry at position " + (i + 1) + " must match ordered sequence",
                    expected[i], lines[i]);
        }
    }

}
