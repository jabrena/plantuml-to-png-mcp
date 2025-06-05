package info.jab.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for handling PlantUML file processing operations.
 *
 * This service provides functionality to convert PlantUML files to PNG format,
 * including content processing and output generation.
 *
 * @since 1.0
 */
public class PlantUMLFileService {

    private static final Logger logger = LoggerFactory.getLogger(PlantUMLFileService.class);

    private static final String DEFAULT_PLANTUML_SERVER = "http://www.plantuml.com/plantuml";

    private final PlantUMLHttpClient httpClient;

    /**
     * Default constructor.
     */
    public PlantUMLFileService() {
        this(DEFAULT_PLANTUML_SERVER);
    }

    /**
     * Constructs a PlantUMLFileService with the specified PlantUML server URL.
     *
     * @param plantUmlServerUrl the URL of the PlantUML server
     */
    public PlantUMLFileService(String plantUmlServerUrl) {
        Objects.requireNonNull(plantUmlServerUrl, "PlantUML server URL cannot be null");
        this.httpClient = new PlantUMLHttpClient(plantUmlServerUrl);
    }

    /**
     * Constructs a PlantUMLFileService with the provided HTTP client for testing.
     *
     * @param httpClient the HTTP client to use
     */
    PlantUMLFileService(PlantUMLHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "HTTP client cannot be null");
    }

    /**
     * Processes a PlantUML file and converts it to PNG format.
     *
     * @param inputPath The validated Path of the PlantUML file to process
     * @return true if conversion was successful, false otherwise
     */
    public boolean processFile(Path inputPath) {
        Optional<Path> result = convertToPng(inputPath);
        if (result.isPresent()) {
            logger.info("Successfully converted: {} -> {}", inputPath, result.get());
            return true;
        } else {
            logger.error("Failed to convert file: {}", inputPath);
            return false;
        }
    }

    /**
     * Converts a PlantUML file to PNG format.
     *
     * @param inputPath The path to the input PlantUML file
     * @return Optional containing the PNG output file path, or empty if conversion fails
     */
    public Optional<Path> convertToPng(Path inputPath) {
        logger.info("Converting PlantUML file to PNG: {}", inputPath);
        try {
            // Read file content
            String content = Files.readString(inputPath, StandardCharsets.UTF_8);

            // Validate PlantUML syntax
            if (!isValidPlantUMLSyntax(content)) {
                return Optional.empty();
            }

            // Generate PNG data
            Optional<byte[]> pngData = generatePngData(content);
            if (pngData.isEmpty()) {
                return Optional.empty();
            }

            // Generate output path
            Path outputPath = generateOutputPath(inputPath);

            // Write PNG file
            Files.write(outputPath, pngData.get());

            return Optional.of(outputPath);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Generates the output PNG file path based on the input PlantUML file path.
     *
     * @param inputPath The input PlantUML file path
     * @return The corresponding PNG output file path
     */
    private Path generateOutputPath(Path inputPath) {
        String inputFileName = inputPath.getFileName().toString();
        String outputFileName = inputFileName.substring(0, inputFileName.lastIndexOf('.')) + ".png";

        Path parent = inputPath.getParent();
        if (Objects.isNull(parent)) {
            // If parent is null, use current directory
            parent = Path.of(".");
        }
        return parent.resolve(outputFileName);
    }

    /**
     * Validates PlantUML syntax by checking the content.
     *
     * @param plantUMLContent the PlantUML content to validate
     * @return true if the syntax is valid, false otherwise
     */
    private boolean isValidPlantUMLSyntax(String plantUMLContent) {
        if (Objects.isNull(plantUMLContent) || plantUMLContent.trim().isEmpty()) {
            return false;
        }

        // Basic syntax validation - check for @startuml and @enduml
        String trimmedContent = plantUMLContent.trim();
        return trimmedContent.contains("@startuml") && trimmedContent.contains("@enduml");
    }

    /**
     * Generates PNG data from PlantUML content using HTTP calls to PlantUML server.
     *
     * @param plantUMLContent the PlantUML content to convert
     * @return Optional containing the PNG data as a byte array, or empty if conversion fails
     */
    Optional<byte[]> generatePngData(String plantUMLContent) {
        try {
            // Delegate to HTTP client which handles encoding internally
            return httpClient.generatePngData(plantUMLContent);

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
