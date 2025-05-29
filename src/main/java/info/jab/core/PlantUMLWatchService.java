package info.jab.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Service responsible for watching directory changes and automatically converting PlantUML files.
 *
 * This service implements a polling-based file watcher that scans for .puml files
 * and converts them to PNG format when they don't have corresponding PNG files.
 */
public class PlantUMLWatchService {

    private static final long DEFAULT_POLLING_INTERVAL_MS = 5000L;
    private static final String PUML_EXTENSION = ".puml";
    private static final String PNG_EXTENSION = ".png";

    private final PlantUMLFileService plantUMLService;
    private final long pollingIntervalMs;
    private volatile boolean stopRequested = false;

    /**
     * Constructor with default polling interval.
     *
     * @param plantUMLService The service to use for file conversion
     */
    public PlantUMLWatchService(PlantUMLFileService plantUMLService) {
        this(plantUMLService, DEFAULT_POLLING_INTERVAL_MS);
    }

    /**
     * Constructor with custom polling interval.
     *
     * @param plantUMLService The service to use for file conversion
     * @param pollingIntervalMs The polling interval in milliseconds
     */
    public PlantUMLWatchService(PlantUMLFileService plantUMLService, long pollingIntervalMs) {
        this.plantUMLService = Objects.requireNonNull(plantUMLService, "plantUMLService cannot be null");
        this.pollingIntervalMs = pollingIntervalMs;
    }

    /**
     * Starts watching the specified directory for PlantUML files.
     *
     * This method runs indefinitely, checking for new .puml files every polling interval.
     * It converts files that don't have corresponding PNG files.
     *
     * @param watchDirectory The directory to watch for PlantUML files
     * @return Exit code (0 for success, 1 for error)
     */
    public Integer startWatching(Path watchDirectory) {
        return startWatching(watchDirectory, () -> !stopRequested);
    }

    /**
     * Starts watching the current directory for PlantUML files.
     *
     * This method runs indefinitely, checking for new .puml files every polling interval.
     * It converts files that don't have corresponding PNG files.
     *
     * @return Exit code (0 for success, 1 for error)
     */
    public Integer startWatching() {
        return startWatching(Path.of(System.getProperty("user.dir")));
    }

    /**
     * Requests the watch service to stop after the current iteration completes.
     */
    public void stopWatching() {
        this.stopRequested = true;
    }

    /**
     * Package-private method for testing that allows control over the loop condition.
     *
     * @param watchDirectory The directory to watch for PlantUML files
     * @param shouldContinue Function that returns true if the loop should continue
     * @return Exit code (0 for success, 1 for error)
     */
    Integer startWatching(Path watchDirectory, BooleanSupplier shouldContinue) {
        System.out.println("Starting watch mode in directory: " + watchDirectory);

        try {
            while (shouldContinue.getAsBoolean()) {
                processPlantUMLFiles(watchDirectory);
                if (shouldContinue.getAsBoolean()) { // Check again before sleeping
                    Thread.sleep(pollingIntervalMs);
                }
            }
            return 0;

        } catch (InterruptedException e) {
            System.out.println("Watch mode interrupted. Exiting...");
            Thread.currentThread().interrupt();
            return 1;
        } catch (IOException e) {
            System.err.println("Error scanning directory for .puml files: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Processes all PlantUML files in the given directory once.
     * This method is package-private to facilitate testing.
     *
     * @param directory The directory to scan for .puml files
     * @throws IOException If there's an error reading the directory
     */
    void processPlantUMLFiles(Path directory) throws IOException {
        List<Path> pumlFiles = listPlantUMLFiles(directory);

        if (!pumlFiles.isEmpty()) {
            pumlFiles.forEach(file -> {
                Path relativePath = directory.relativize(file);
                Path pngFile = getPngPath(file);
                boolean pngExists = Files.exists(pngFile);

                // Only print and convert if the PNG file does not exist
                if (!pngExists) {
                    System.out.println("Found: " + relativePath);
                    convertToPng(file);
                }
            });
        }
    }

    /**
     * Lists all PlantUML files in the specified directory.
     *
     * @param directory The directory to scan
     * @return List of .puml file paths
     * @throws IOException If there's an error reading the directory
     */
    private List<Path> listPlantUMLFiles(Path directory) throws IOException {
        try (var stream = Files.walk(directory)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase(Locale.ENGLISH).endsWith(PUML_EXTENSION))
                .sorted()
                .toList();
        }
    }

    /**
     * Converts a .puml file path to the corresponding .png file path.
     *
     * @param pumlFile The .puml file path
     * @return The corresponding .png file path
     */
    private Path getPngPath(Path pumlFile) {
        String pumlFileName = pumlFile.toString();
        String pngFileName = pumlFileName.substring(0, pumlFileName.length() - PUML_EXTENSION.length()) + PNG_EXTENSION;
        return Path.of(pngFileName);
    }

    /**
     * Converts a PlantUML file to PNG format.
     *
     * @param inputPath The path to the .puml file
     */
    private void convertToPng(Path inputPath) {
        boolean success = plantUMLService.processFile(inputPath);
        if (!success) {
            System.err.println("Error: Failed to convert PlantUML file: " + inputPath);
        }
    }
}
