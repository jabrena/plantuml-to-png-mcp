package info.jab.core;

import java.io.IOException;

/**
 * Validator class for checking Graphviz availability.
 *
 * This class provides methods to check if Graphviz is properly installed
 * and available on the system for PlantUML diagram generation.
 *
 * @since 1.0
 */
public class GraphvizValidator {

    private static final String DOT_COMMAND = "dot";
    private static final String VERSION_FLAG = "-V";

    /**
     * Checks if Graphviz is available on the system by attempting to execute the 'dot' command.
     *
     * @return true if Graphviz is available and accessible, false otherwise
     */
    public boolean isGraphvizAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(DOT_COMMAND, VERSION_FLAG);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
