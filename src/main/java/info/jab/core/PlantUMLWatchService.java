package info.jab.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible for watching directory changes and automatically converting PlantUML files.
 *
 * This service implements a polling-based file watcher that scans for .puml files
 * and converts them to PNG format when they don't have corresponding PNG files.
 */
public class PlantUMLWatchService {

    private static final Logger logger = LoggerFactory.getLogger(PlantUMLWatchService.class);

    private static final long DEFAULT_POLLING_INTERVAL_MS = 5000L;

    private static final String PUML_EXTENSION = ".puml";
    private static final String PNG_EXTENSION = ".png";

    private final PlantUMLFileService plantUMLService;
    private final long pollingIntervalMs;

    /**
     * Represents the decision whether to convert a file and the reason for that decision.
     */
    public record ConversionDecision(boolean shouldConvert, ConversionReason reason) {

        public static ConversionDecision convert(ConversionReason reason) {
            return new ConversionDecision(true, reason);
        }

        public static ConversionDecision skip() {
            return new ConversionDecision(false, ConversionReason.UP_TO_DATE);
        }

        public String getReason() {
            return reason.getDescription();
        }
    }

    /**
     * Enumeration of reasons why a file might need conversion.
     */
    public enum ConversionReason {
        NO_PNG_EXISTS("no .png exists"),
        PUML_RECENTLY_MODIFIED("recently modified .puml file"),
        SYNC_REQUIRED("both files recently modified"),
        UP_TO_DATE("up to date");

        private final String description;

        ConversionReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

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
     * Use Ctrl+C to stop the process.
     *
     * @param watchDirectory The directory to watch for PlantUML files
     * @return Exit code (0 for success, 1 for error)
     */
    public Integer startWatching(Path watchDirectory) {
        logger.info("Starting watch mode in directory: {}", watchDirectory);

        try {
            while (true) {
                processPlantUMLFiles(watchDirectory);
                Thread.sleep(pollingIntervalMs);
            }

        } catch (InterruptedException e) {
            logger.error("Watch mode interrupted. Exiting...");
            Thread.currentThread().interrupt();
            return 1;
        } catch (IOException e) {
            logger.error("Error scanning directory for .puml files: {}", e.getMessage());
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
                ConversionDecision decision = shouldConvertFile(file);

                if (decision.shouldConvert()) {
                    logger.info("Found: {} ({})", relativePath, decision.getReason());
                    convertToPng(file);
                }
            });
        }
    }

    /**
     * Determines whether a PUML file should be converted to PNG.
     * Package-private to facilitate testing.
     *
     * @param pumlFile The PUML file to evaluate
     * @return ConversionDecision indicating whether to convert and why
     */
    ConversionDecision shouldConvertFile(Path pumlFile) {
        Path pngFile = getPngPath(pumlFile);
        boolean pngExists = Files.exists(pngFile);
        boolean pumlRecentlyModified = isFileModifiedInLastSeconds(pumlFile);

        // Primary decision: convert if PNG doesn't exist
        if (!pngExists) {
            return ConversionDecision.convert(ConversionReason.NO_PNG_EXISTS);
        }

        // Secondary decision: convert if PUML was recently modified
        if (pumlRecentlyModified) {
            return ConversionDecision.convert(ConversionReason.PUML_RECENTLY_MODIFIED);
        }

        // Tertiary decision: convert if both files need synchronization
        if (requiresSynchronization(pumlFile, pngFile)) {
            return ConversionDecision.convert(ConversionReason.SYNC_REQUIRED);
        }

        return ConversionDecision.skip();
    }

    /**
     * Checks if the PUML and PNG files require synchronization.
     * This happens when both files were modified recently but not at the same time.
     *
     * @param pumlFile The PUML file
     * @param pngFile The PNG file
     * @return true if synchronization is required
     */
    private boolean requiresSynchronization(Path pumlFile, Path pngFile) {
        boolean pumlRecentlyModified = isFileModifiedInLastSeconds(pumlFile);
        boolean pngRecentlyModified = isFileModifiedInLastSeconds(pngFile);

        // Synchronization needed if PUML was modified but PNG wasn't
        return pumlRecentlyModified && !pngRecentlyModified;
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
     * Checks if a file was modified in the last minute.
     * Package-private to allow overriding in tests.
     *
     * @param filePath The path to the file to check
     * @return true if the file was modified in the last minute, false otherwise
     */
    boolean isFileModifiedInLastSeconds(Path filePath) {
        try {
            FileTime lastModified = Files.getLastModifiedTime(filePath);
            Instant tenSecondsAgo = Instant.now().minus(10, ChronoUnit.SECONDS);
            return lastModified.toInstant().isAfter(tenSecondsAgo);
        } catch (IOException e) {
            logger.warn("Could not check modification time for file: {}", filePath, e);
            return false;
        }
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
