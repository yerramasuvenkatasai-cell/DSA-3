import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

/**
 * String Matching Application using the Z-Algorithm (Swing GUI).
 * Enter text + pattern -> shows all match positions, highlights them,
 * and compares Z-Algorithm vs Naive execution time.
 */
public class StringMatcherApp extends JFrame {

    private static final Color KLU_RED = new Color(0xC00000);

    private final JTextArea textArea = new JTextArea(12, 60);
    private final JTextField patternField = new JTextField(28);
    private final JCheckBox ignoreCase = new JCheckBox("Ignore case");
    private final JLabel statusLabel = new JLabel("Enter text and a pattern, then click Search.");
    private final JTextArea resultArea = new JTextArea(9, 60);
    private final BarPanel barPanel = new BarPanel();

    public StringMatcherApp() {
        super("String Matching using Z-Algorithm  |  DSA-3 (25CS2103E)  |  Team 03");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(8, 8, 8, 8));

        // ---- header ----
        JLabel title = new JLabel("  String Matching Application - Z-Algorithm  ", SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(KLU_RED);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setBorder(new EmptyBorder(8, 8, 8, 8));
        add(title, BorderLayout.NORTH);

        // ---- centre: text + results ----
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setBorder(BorderFactory.createTitledBorder("Text"));

        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane resScroll = new JScrollPane(resultArea);
        resScroll.setBorder(BorderFactory.createTitledBorder("Matching positions / Z-array"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScroll, resScroll);
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);

        // ---- bottom: controls, status, chart ----
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controls.add(new JLabel("Pattern:"));
        controls.add(patternField);
        controls.add(ignoreCase);
        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Clear");
        JButton loadBtn = new JButton("Load .txt file");
        JButton worstBtn = new JButton("Worst-case demo");
        controls.add(searchBtn);
        controls.add(clearBtn);
        controls.add(loadBtn);
        controls.add(worstBtn);

        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.add(controls, BorderLayout.NORTH);
        bottom.add(statusLabel, BorderLayout.CENTER);
        barPanel.setPreferredSize(new Dimension(100, 90));
        bottom.add(barPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        // ---- actions ----
        searchBtn.addActionListener(e -> doSearch());
        patternField.addActionListener(e -> doSearch());
        clearBtn.addActionListener(e -> clearAll());
        loadBtn.addActionListener(e -> loadFile());
        worstBtn.addActionListener(e -> worstCaseDemo());

        // sample data
        textArea.setText("abababcabababcabab. Z-Algorithm finds every occurrence of ababc or abab quickly.");
        patternField.setText("abab");

        pack();
        setLocationRelativeTo(null);
    }

    // -------------------------------------------------------------------
    private void doSearch() {
        String text = textArea.getText();
        String pattern = patternField.getText();
        if (text.isEmpty() || pattern.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both text and a pattern.",
                    "Missing input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String t = ignoreCase.isSelected() ? text.toLowerCase(Locale.ROOT) : text;
        String p = ignoreCase.isSelected() ? pattern.toLowerCase(Locale.ROOT) : pattern;
        if (t.length() != text.length()) { t = text; p = pattern; } // safety for odd Unicode

        // warm-up so timings are fair
        ZAlgorithm.search(t, p);
        NaiveMatcher.search(t, p);

        long a = System.nanoTime();
        List<Integer> zRes = ZAlgorithm.search(t, p);
        long b = System.nanoTime();
        List<Integer> nRes = NaiveMatcher.search(t, p);
        long c = System.nanoTime();

        double zMs = (b - a) / 1e6, nMs = (c - b) / 1e6;
        boolean same = zRes.equals(nRes);

        showResults(t, p, zRes);
        highlight(zRes, p.length());

        statusLabel.setText(String.format(
                "Matches: %d  |  Z-Algorithm: %.4f ms  |  Naive: %.4f ms  |  n=%d, m=%d  |  Both methods agree: %s",
                zRes.size(), zMs, nMs, t.length(), p.length(), same ? "YES" : "NO (bug!)"));
        barPanel.setTimes(zMs, nMs);
    }

    private void showResults(String text, String pattern, List<Integer> pos) {
        StringBuilder sb = new StringBuilder();
        if (pos.isEmpty()) {
            sb.append("Pattern not found in the text.\n");
        } else {
            sb.append("Pattern found ").append(pos.size()).append(" time(s). Start positions (0-based):\n");
            int line = 1, lineStart = 0, idx = 0, shown = 0;
            for (int p : pos) {
                if (shown++ >= 200) { sb.append("... (showing first 200 only)\n"); break; }
                while (idx < p) {                       // track line/column incrementally
                    if (text.charAt(idx) == '\n') { line++; lineStart = idx + 1; }
                    idx++;
                }
                sb.append(String.format("  index %-8d (line %d, col %d)%n", p, line, p - lineStart + 1));
            }
        }
        if (text.length() <= 60) {                      // teaching aid: show Z-array for small input
            String combined = ZAlgorithm.combine(text, pattern);
            int[] z = ZAlgorithm.buildZArray(combined);
            sb.append("\nCombined string (pattern + '#' + text):\n  ");
            for (char ch : combined.toCharArray()) sb.append(ch == '\u0001' ? '#' : ch).append(' ');
            sb.append("\nZ-array:\n  ");
            for (int v : z) sb.append(v).append(' ');
            sb.append('\n');
        }
        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);
    }

    private void highlight(List<Integer> pos, int m) {
        Highlighter h = textArea.getHighlighter();
        h.removeAllHighlights();
        Highlighter.HighlightPainter painter =
                new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 215, 0));
        int limit = 5000;
        try {
            for (int p : pos) {
                if (limit-- <= 0) break;
                h.addHighlight(p, p + m, painter);
            }
        } catch (BadLocationException ignored) { }
    }

    private void clearAll() {
        textArea.setText("");
        patternField.setText("");
        resultArea.setText("");
        textArea.getHighlighter().removeAllHighlights();
        statusLabel.setText("Cleared.");
        barPanel.setTimes(0, 0);
    }

    private void loadFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();
                textArea.setText(new String(Files.readAllBytes(f.toPath())));
                textArea.setCaretPosition(0);
                statusLabel.setText("Loaded " + f.getName() + " (" + textArea.getText().length() + " chars)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not read file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** text = 200000 x 'a', pattern = 2000 x 'a' + 'b'  ->  Naive hits O(n x m). */
    private void worstCaseDemo() {
        textArea.setText("a".repeat(200_000));
        patternField.setText("a".repeat(2_000) + "b");
        ignoreCase.setSelected(false);
        statusLabel.setText("Running worst-case demo (Naive may take a moment)...");
        SwingUtilities.invokeLater(this::doSearch);
    }

    // -------------------------------------------------------------------
    /** Small bar chart comparing the two execution times. */
    static class BarPanel extends JPanel {
        private double z, n;
        void setTimes(double z, double n) { this.z = z; this.n = n; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double max = Math.max(Math.max(z, n), 1e-9);
            int w = getWidth() - 220, h = 22;
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
            drawBar(g2, "Z-Algorithm", z, max, w, 12, h, new Color(0x2E7D32));
            drawBar(g2, "Naive",       n, max, w, 46, h, KLU_RED);
        }
        private void drawBar(Graphics2D g2, String label, double v, double max, int w, int y, int h, Color c) {
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(label, 8, y + 16);
            int len = (int) (w * (v / max));
            g2.setColor(c);
            g2.fillRoundRect(110, y, Math.max(len, 2), h, 8, 8);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.format("%.4f ms", v), 116 + len, y + 16);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StringMatcherApp().setVisible(true));
    }
}
