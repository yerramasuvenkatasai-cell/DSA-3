import java.util.ArrayList;
import java.util.List;

/**
 * Z-Algorithm string matching  -  O(n + m) time.
 * Combined string = pattern + delimiter + text
 */
public class ZAlgorithm {

    private static final char DELIMITER = '\u0001';

    /** Builds the Z-array: z[i] = length of longest substring starting at i that is also a prefix of s. */
    public static int[] buildZArray(String s) {
        int n = s.length();
        int[] z = new int[n];
        int l = 0, r = 0;                       // [l, r) = rightmost Z-box
        for (int i = 1; i < n; i++) {
            if (i < r) {
                z[i] = Math.min(r - i, z[i - l]); // reuse earlier work
            }
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
                z[i]++;
            }
            if (i + z[i] > r) {
                l = i;
                r = i + z[i];
            }
        }
        return z;
    }

    /** Returns the combined string (pattern + delimiter + text). */
    public static String combine(String text, String pattern) {
        return pattern + DELIMITER + text;
    }

    /** Returns all 0-based starting positions of pattern in text. */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> positions = new ArrayList<>();
        int m = pattern.length();
        if (m == 0 || m > text.length()) return positions;

        int[] z = buildZArray(combine(text, pattern));
        for (int i = m + 1; i < z.length; i++) {
            if (z[i] >= m) {
                positions.add(i - (m + 1));
            }
        }
        return positions;
    }
}
