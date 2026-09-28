/**
 * Entry point of the project.
 *   java Main              -> opens the GUI (String Matching using Z-Algorithm)
 *   java Main benchmark    -> runs the 1000-record benchmark in the console
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("benchmark")) {
            Benchmark.main(new String[0]);
        } else {
            StringMatcherApp.main(args);
        }
    }
}
