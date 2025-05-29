package info.jab.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.Deflater;

/**
 * Service class that handles PlantUML to PNG conversion operations using HTTP calls.
 *
 * This class encapsulates the core business logic for:
 * - Validating PlantUML file syntax
 * - Converting PlantUML content to PNG format via HTTP service
 * - Managing file I/O operations
 */
public class PlantUMLService {

    private static final String DEFAULT_PLANTUML_SERVER = "http://www.plantuml.com/plantuml";
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);

    private final String plantUmlServerUrl;
    private final HttpClient httpClient;

    /**
     * Creates a new PlantUMLService with default server URL.
     */
    public PlantUMLService() {
        this(DEFAULT_PLANTUML_SERVER);
    }

    /**
     * Creates a new PlantUMLService with custom server URL.
     *
     * @param plantUmlServerUrl the PlantUML server URL
     */
    public PlantUMLService(String plantUmlServerUrl) {
        this.plantUmlServerUrl = Objects.requireNonNull(plantUmlServerUrl, "PlantUML server URL cannot be null");
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .build();
    }

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

            // Generate PNG content via HTTP
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
     * Generates PNG data from PlantUML content using HTTP calls to PlantUML server.
     *
     * @param plantUMLContent the PlantUML content to convert
     * @return Optional containing the PNG data as a byte array, or empty if conversion fails
     */
    Optional<byte[]> generatePngData(String plantUMLContent) {
        try {
            // Encode PlantUML content for HTTP transmission
            String encodedContent = encodePlantUMLContent(plantUMLContent);

            // Build the URL for PNG generation
            String requestUrl = plantUmlServerUrl + "/png/" + encodedContent;

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();

            // Send request and get response
            HttpResponse<byte[]> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofByteArray());

            // Check if request was successful
            if (response.statusCode() == 200) {
                byte[] pngData = response.body();
                return pngData.length > 0 ? Optional.of(pngData) : Optional.empty();
            }

            return Optional.empty();

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Encodes PlantUML content using the PlantUML encoding scheme.
     * This uses a combination of deflate compression and base64 encoding
     * with a custom character set.
     *
     * @param plantUMLContent the PlantUML content to encode
     * @return the encoded content suitable for HTTP URLs
     */
    private String encodePlantUMLContent(String plantUMLContent) {
        try {
            // Convert to bytes
            byte[] data = plantUMLContent.getBytes(StandardCharsets.UTF_8);

            // Compress using deflate
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
            deflater.setInput(data);
            deflater.finish();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            deflater.end();

            byte[] compressedData = outputStream.toByteArray();

            // Encode using PlantUML's custom base64 encoding
            return encodePlantUMLBase64(compressedData);

        } catch (Exception e) {
            throw new RuntimeException("Failed to encode PlantUML content", e);
        }
    }

    /**
     * Encodes bytes using PlantUML's custom base64 encoding scheme.
     *
     * <p><strong>Technical reasons for maintaining this custom implementation:</strong></p>
     * <ul>
     *   <li><strong>Different Character Set:</strong> PlantUML uses "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"
     *       while standard Base64 uses "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"</li>
     *   <li><strong>No Padding:</strong> PlantUML encoding does not use padding characters ('=') that standard Base64 requires</li>
     *   <li><strong>URL Safety:</strong> PlantUML's character set is inherently URL-safe (uses '-_' instead of '+/'),
     *       eliminating the need for URL encoding when used in HTTP requests</li>
     *   <li><strong>Protocol Requirement:</strong> PlantUML servers specifically expect this custom encoding format.
     *       Using standard Base64 would result in server-side decoding failures</li>
     *   <li><strong>Compatibility:</strong> This encoding is part of the PlantUML specification and cannot be changed
     *       without breaking compatibility with existing PlantUML infrastructure</li>
     * </ul>
     *
     * <p>Example comparison:</p>
     * <pre>
     * Standard Base64: "cyguSSwqKc3N4XLMyUxOVdC1U3DKT7JS8EjNycnnckjNSwHKAQA="
     * PlantUML encoding: "SoWkIImgAStDuNBCoKnELT2rKt3AJx9Iy4ZDoSddSaZDIm7A0G0"
     * </pre>
     *
     * @param data the data to encode
     * @return the PlantUML base64 encoded string
     */
    private String encodePlantUMLBase64(byte[] data) {
        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_";
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < data.length; i += 3) {
            int b1 = data[i] & 0xFF;
            int b2 = (i + 1 < data.length) ? data[i + 1] & 0xFF : 0;
            int b3 = (i + 2 < data.length) ? data[i + 2] & 0xFF : 0;

            int c1 = b1 >> 2;
            int c2 = ((b1 & 0x3) << 4) | (b2 >> 4);
            int c3 = ((b2 & 0xF) << 2) | (b3 >> 6);
            int c4 = b3 & 0x3F;

            result.append(chars.charAt(c1));
            result.append(chars.charAt(c2));
            if (i + 1 < data.length) {
                result.append(chars.charAt(c3));
            }
            if (i + 2 < data.length) {
                result.append(chars.charAt(c4));
            }
        }

        return result.toString();
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
