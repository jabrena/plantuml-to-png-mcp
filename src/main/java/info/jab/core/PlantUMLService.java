package info.jab.core;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class that handles PlantUML to PNG conversion operations.
 *
 * This class encapsulates the core business logic for:
 * - Validating PlantUML file syntax
 * - Converting PlantUML content to PNG format
 * - Managing file I/O operations
 */
public class PlantUMLService {

    /**
     * Converts a PlantUML file to PNG format.
     *
     * @param inputPath the path to the input .puml file
     * @return Optional containing the path to the generated PNG file, or empty if conversion fails
     */
    public Optional<Path> convertToPng(Path inputPath) {
        try {
            // Read the PlantUML file content
            String plantUMLContent = Files.readString(inputPath, StandardCharsets.UTF_8);

            // Validate PlantUML syntax
            if (!isValidPlantUMLSyntax(plantUMLContent)) {
                return Optional.empty();
            }

            // Generate PNG content
            Optional<byte[]> pngDataOpt = generatePngData(plantUMLContent);
            if (pngDataOpt.isEmpty()) {
                return Optional.empty();
            }

            // Determine output path (same directory, same name, .png extension)
            Path outputPath = createOutputPath(inputPath);

            // Write PNG file
            Files.write(outputPath, pngDataOpt.get());

            return Optional.of(outputPath);

        } catch (IOException | SecurityException e) {
            // Handle file I/O errors gracefully
            return Optional.empty();
        }
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
     * Generates PNG data from PlantUML content using the PlantUML library.
     *
     * @param plantUMLContent the PlantUML content to convert
     * @return Optional containing the PNG data as a byte array, or empty if conversion fails
     */
    Optional<byte[]> generatePngData(String plantUMLContent) {
        try {
            SourceStringReader reader = new SourceStringReader(plantUMLContent);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Convert to PNG format
            @SuppressWarnings("UnusedVariable")
            DiagramDescription description = reader.outputImage(outputStream, new FileFormatOption(FileFormat.PNG));

            byte[] pngData = outputStream.toByteArray();

            if (pngData.length == 0) {
                return Optional.empty();
            }

            return Optional.of(pngData);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Creates the output path for the PNG file based on the input path.
     * The PNG file will have the same name as the input file but with .png extension,
     * and will be placed in the same directory.
     *
     * @param inputPath the input .puml file path
     * @return the output .png file path
     */
    private Path createOutputPath(Path inputPath) {
        String fileName = inputPath.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String outputFileName = baseName + ".png";

        Path parent = inputPath.getParent();
        if (Objects.isNull(parent)) {
            // If parent is null, use current directory
            parent = Path.of(".");
        }
        return parent.resolve(outputFileName);
    }
}
