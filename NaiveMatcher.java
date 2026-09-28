import java.util.ArrayList;
import java.util.List;

/** Naive string matching  -  O(n x m) worst case. Used for comparison. */
public class NaiveMatcher {

    public static List<Integer> search(String text, String pattern) {
        List<Integer> positions = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        if (m == 0 || m > n) return positions;

        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            while (j < m && text.charAt(i + j) == pattern.charAt(j)) {
                j++;
            }
            if (j == m) positions.add(i);
        }
        return positions;
    }
}
