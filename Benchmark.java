import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

/**
 * Console benchmark:
 *  1) 1,000 text-pattern records (Small / Medium / Large) - validates correctness + measures time
 *  2) Worst-case test for Naive method (text = aaaa..., pattern = aaa..b) to show O(n x m) vs O(n + m)
 * Results are also saved to benchmark_results.csv
 */
public class Benchmark {

    static final String DNA = "ACGT";

    static String randomText(Random rnd, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(DNA.charAt(rnd.nextInt(DNA.length())));
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        Random rnd = new Random(42);
        String[] names = {"Small", "Medium", "Large"};
        int[] counts = {400, 400, 200};                    // total = 1000 records
        int[] minN   = {100, 10_000, 200_000};
        int[] maxN   = {1_000, 100_000, 500_000};
        int[] minM   = {3, 5, 8};
        int[] maxM   = {8, 20, 30};

        PrintWriter csv = new PrintWriter("benchmark_results.csv");
        csv.println("category,records,avg_text_len,avg_z_ms,avg_naive_ms,all_correct");

        System.out.println("=== TEST 1: 1000 random records (DNA alphabet) ===");
        System.out.printf("%-8s %8s %12s %12s %14s %10s%n",
                "Size", "Records", "AvgLen", "Z (ms)", "Naive (ms)", "Correct");

        int totalRecords = 0;
        boolean everythingCorrect = true;

        for (int c = 0; c < 3; c++) {
            long zNanos = 0, nNanos = 0, lenSum = 0;
            boolean ok = true;
            for (int k = 0; k < counts[c]; k++) {
                int n = minN[c] + rnd.nextInt(maxN[c] - minN[c] + 1);
                int m = minM[c] + rnd.nextInt(maxM[c] - minM[c] + 1);
                String text = randomText(rnd, n);
                String pattern;
                if (k % 2 == 0) {                          // guaranteed-match pattern
                    int start = rnd.nextInt(n - m);
                    pattern = text.substring(start, start + m);
                } else {                                    // random pattern
                    pattern = randomText(rnd, m);
                }

                long t0 = System.nanoTime();
                List<Integer> z = ZAlgorithm.search(text, pattern);
                long t1 = System.nanoTime();
                List<Integer> nv = NaiveMatcher.search(text, pattern);
                long t2 = System.nanoTime();

                zNanos += (t1 - t0);
                nNanos += (t2 - t1);
                lenSum += n;
                if (!z.equals(nv)) ok = false;
            }
            totalRecords += counts[c];
            everythingCorrect &= ok;
            double zMs = zNanos / 1e6 / counts[c];
            double nMs = nNanos / 1e6 / counts[c];
            long avgLen = lenSum / counts[c];
            System.out.printf("%-8s %8d %12d %12.4f %14.4f %10s%n",
                    names[c], counts[c], avgLen, zMs, nMs, ok ? "YES" : "NO");
            csv.printf("%s,%d,%d,%.4f,%.4f,%s%n", names[c], counts[c], avgLen, zMs, nMs, ok);
        }
        System.out.println("Total records tested: " + totalRecords
                + " | Z-Algorithm output identical to Naive: " + (everythingCorrect ? "YES" : "NO"));

        // ---------- worst case ----------
        System.out.println("\n=== TEST 2: Worst case for Naive (text=aaa..a, pattern=aaa..ab, m=n/20) ===");
        System.out.printf("%10s %8s %12s %14s %10s%n", "n", "m", "Z (ms)", "Naive (ms)", "Speedup");
        csv.println();
        csv.println("worst_case_n,m,z_ms,naive_ms");

        int[] sizes = {20_000, 40_000, 80_000, 160_000};
        for (int n : sizes) {
            int m = n / 20;
            String text = "a".repeat(n);
            String pattern = "a".repeat(m - 1) + "b";

            ZAlgorithm.search(text, pattern);              // warm-up
            long t0 = System.nanoTime();
            List<Integer> z = ZAlgorithm.search(text, pattern);
            long t1 = System.nanoTime();
            List<Integer> nv = NaiveMatcher.search(text, pattern);
            long t2 = System.nanoTime();

            double zMs = (t1 - t0) / 1e6, nMs = (t2 - t1) / 1e6;
            System.out.printf("%10d %8d %12.3f %14.3f %9.1fx%s%n",
                    n, m, zMs, nMs, nMs / Math.max(zMs, 0.001), z.equals(nv) ? "" : "  MISMATCH!");
            csv.printf("%d,%d,%.3f,%.3f%n", n, m, zMs, nMs);
        }
        csv.close();
        System.out.println("\nSaved: benchmark_results.csv");
    }
}
