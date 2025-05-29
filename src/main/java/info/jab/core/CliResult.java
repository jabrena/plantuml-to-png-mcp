package info.jab.core;

/**
 * Represents the result of CLI operations.
 *
 * @since 1.0
 */
public enum CliResult {
    /**
     * Successful operation.
     */
    OK(0),

    /**
     * Failed operation.
     */
    KO(1);

    private final int exitCode;

    CliResult(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Gets the exit code value.
     *
     * @return the exit code as integer
     */
    public int getExitCode() {
        return exitCode;
    }
}
