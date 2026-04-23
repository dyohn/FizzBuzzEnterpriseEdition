package com.seriouscompany.business.java.fizzbuzz.packagenamingpackage.impl;

/**
 * FizzBuzz: minimal single-class replacement for the original 87-file enterprise edition.
 *
 * Behavioral contract (preserved from original):
 *   fizzBuzz(n) loops from 1 to n inclusive and writes to System.out, one entry
 *   per iteration followed by the platform line separator:
 *     "Fizz"     for multiples of 3
 *     "Buzz"     for multiples of 5
 *     "FizzBuzz" for multiples of both 3 and 5
 *     the integer itself otherwise
 *
 * Output mechanism mirrors SystemOutFizzBuzzOutputStrategy (SR-03, SR-04):
 *   System.out.write(bytes) + System.out.flush() per entry, so the test harness's
 *   System.setOut() redirect captures output correctly.
 * IOException is swallowed to match the original adapter behavior (SR-01).
 * Line separator uses System.getProperty("line.separator") to match
 *   NewLineStringReturner and the test's platform-normalization logic (FR-08).
 */
public final class FizzBuzz {

    private FizzBuzz() {}

    /**
     * Runs FizzBuzz from 1 to n inclusive, writing each result to System.out.
     * @param n upper limit (inclusive); must be >= 0 (FR-09: n=0 produces no output)
     */
    public static void fizzBuzz(final int n) {
        final String newLine = System.getProperty("line.separator");
        for (int i = 1; i <= n; i++) {
            final String output;
            if      (i % 3 == 0 && i % 5 == 0) { output = "FizzBuzz"; }  // FR-05
            else if (i % 3 == 0)                { output = "Fizz"; }      // FR-03
            else if (i % 5 == 0)                { output = "Buzz"; }      // FR-04
            else                                { output = Integer.toString(i); } // FR-06
            try {
                System.out.write((output + newLine).getBytes()); // SR-03, SR-09
                System.out.flush();                               // SR-04
            } catch (java.io.IOException e) {
                // swallowed intentionally — matches original adapter behavior (SR-01)
            }
        }
    }

    /**
     * Entry point. Runs FizzBuzz 1 through 100.
     * The value 100 matches DEFAULT_FIZZ_BUZZ_UPPER_LIMIT_PARAMETER_VALUE
     * from the original implementation (FR-11).
     * @param args command-line arguments (ignored)
     */
    public static void main(final String[] args) {
        fizzBuzz(100);
    }
}
