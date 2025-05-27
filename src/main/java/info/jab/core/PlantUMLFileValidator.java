package info.jab.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

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
     * Validates a PlantUML file path and returns the corresponding Path object.
     *
     * @param filePath The file path to validate
     * @return A valid Path object for the PlantUML file
     * @throws IllegalArgumentException if the file path is invalid, file doesn't exist,
     *                                  is not readable, or doesn't have the correct extension
     */
    public Path validatePlantUMLFile(@Nullable String filePath) {
        validateFilePathNotEmpty(filePath);

        // After validation, we know filePath is not null
        String validatedFilePath = Objects.requireNonNull(filePath, "File path should not be null after validation");
        Path path = Paths.get(validatedFilePath);

        validateFileExists(path, validatedFilePath);
        validateIsRegularFile(path, validatedFilePath);
        validateIsReadable(path, validatedFilePath);
        validatePlantUMLExtension(path, validatedFilePath);

        return path;
    }

    /**
     * Validates that the file path is not null or empty.
     *
     * @param filePath The file path to check
     * @throws IllegalArgumentException if the file path is null or empty
     */
    private void validateFilePathNotEmpty(@Nullable String filePath) {
        if (Objects.isNull(filePath) || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
    }

    /**
     * Validates that the file exists.
     *
     * @param path The path to check
     * @param originalFilePath The original file path string for error messages
     * @throws IllegalArgumentException if the file does not exist
     */
    private void validateFileExists(Path path, String originalFilePath) {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File does not exist: " + originalFilePath);
        }
    }

    /**
     * Validates that the path points to a regular file (not a directory or special file).
     *
     * @param path The path to check
     * @param originalFilePath The original file path string for error messages
     * @throws IllegalArgumentException if the path is not a regular file
     */
    private void validateIsRegularFile(Path path, String originalFilePath) {
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Path is not a regular file: " + originalFilePath);
        }
    }

    /**
     * Validates that the file is readable.
     *
     * @param path The path to check
     * @param originalFilePath The original file path string for error messages
     * @throws IllegalArgumentException if the file is not readable
     */
    private void validateIsReadable(Path path, String originalFilePath) {
        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("File is not readable: " + originalFilePath);
        }
    }

    /**
     * Validates that the file has the correct PlantUML extension (.puml).
     *
     * @param path The path to check
     * @param originalFilePath The original file path string for error messages
     * @throws IllegalArgumentException if the file doesn't have the .puml extension
     */
    private void validatePlantUMLExtension(Path path, String originalFilePath) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(PLANTUML_EXTENSION)) {
            throw new IllegalArgumentException("File must have " + PLANTUML_EXTENSION + " extension: " + originalFilePath);
        }
    }
}
