# String Matching Application using the Z-Algorithm (Java)
DSA-3 (25CS2103E) | Team 03 | KL University

## Files
| File | Purpose |
|---|---|
| ZAlgorithm.java | Z-array construction + pattern search, O(n + m) |
| NaiveMatcher.java | Naive search, O(n x m) worst case (for comparison) |
| Main.java | **Entry point** - run this file |
| StringMatcherApp.java | Swing GUI (replaces Streamlit) |
| Benchmark.java | 1,000-record test set + worst-case test, writes benchmark_results.csv |

## Requirements
JDK 11 or newer (tested on JDK 21). No external libraries.

## Run
    javac *.java
    java Main                 # opens the GUI
    java Main benchmark       # console benchmark + correctness check

In VS Code: open Main.java and click Run above main (or use Run and Debug -> "Run GUI (Main)").

## GUI features
- Enter text (or load a .txt file) and a pattern
- All match positions with line/column, highlighted in the text
- Z-Algorithm vs Naive time, with a bar chart
- Ignore-case option
- "Worst-case demo" button (text = 200000 x 'a', pattern = 2000 x 'a' + 'b')
- For short texts (<= 60 chars) shows the combined string and Z-array
