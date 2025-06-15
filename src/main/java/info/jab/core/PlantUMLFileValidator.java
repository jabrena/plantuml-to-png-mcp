package info.jab.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Validator class for validating PlantUML files.
 *
 * This class provides methods to validate PlantUML file paths and ensure
 * they meet the requirements for processing (existence, readability, correct extension).
 *
 * @since 1.0
 */
public class PlantUMLFileValidator {

    private static final String PLANTUML_EXTENSION = ".puml";

    /**
     * Validates a PlantUML file path and returns the corresponding Path object wrapped in Optional.
     *
     * @param filePath The file path to validate
     * @return An Optional containing a valid Path object for the PlantUML file, or empty if validation fails
     */
    public Optional<Path> validatePlantUMLFile(@Nullable String filePath) {
        if (!isFilePathValid(filePath)) {
            return Optional.empty();
        }

        // After validation, we know filePath is not null
        String validatedFilePath = Objects.requireNonNull(filePath, "File path should not be null after validation");
        Path path = Paths.get(validatedFilePath);

        if (!isFileExists(path) ||
            !isRegularFile(path) ||
            !isReadable(path) ||
            !hasPlantUMLExtension(path)) {
            return Optional.empty();
        }

        return Optional.of(path);
    }

    /**
     * Validates that the file path is not null or empty.
     *
     * @param filePath The file path to check
     * @return true if the file path is valid, false otherwise
     */
    private boolean isFilePathValid(@Nullable String filePath) {
        return !Objects.isNull(filePath) && !filePath.trim().isEmpty();
    }

    /**
     * Validates that the file exists.
     *
     * @param path The path to check
     * @return true if the file exists, false otherwise
     */
    private boolean isFileExists(Path path) {
        return Files.exists(path);
    }

    /**
     * Validates that the path points to a regular file (not a directory or special file).
     *
     * @param path The path to check
     * @return true if the path is a regular file, false otherwise
     */
    private boolean isRegularFile(Path path) {
        return Files.isRegularFile(path);
    }

    /**
     * Validates that the file is readable.
     *
     * @param path The path to check
     * @return true if the file is readable, false otherwise
     */
    private boolean isReadable(Path path) {
        return Files.isReadable(path);
    }

    /**
     * Validates that the file has the correct PlantUML extension (.puml).
     *
     * @param path The path to check
     * @return true if the file has the .puml extension, false otherwise
     */
    private boolean hasPlantUMLExtension(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(PLANTUML_EXTENSION);
    }
}
