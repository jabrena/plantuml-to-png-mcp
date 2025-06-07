package info.jab.core;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.Deflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP client for PlantUML server operations.
 *
 * This class handles all HTTP communication with PlantUML servers,
 * including request construction, sending, response processing, and
 * PlantUML-specific content encoding.
 */
public class PlantUMLHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(PlantUMLHttpClient.class);

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(10);

    private final String serverUrl;
    private final HttpClient httpClient;

    /**
     * Creates a new PlantUMLHttpClient with the specified server URL.
     *
     * @param serverUrl the PlantUML server URL
     * @throws NullPointerException if serverUrl is null
     */
    public PlantUMLHttpClient(String serverUrl) {
        this.serverUrl = Objects.requireNonNull(serverUrl, "Server URL cannot be null");
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .build();
    }

    /**
     * Generates PNG data from raw PlantUML content by encoding it and making an HTTP request.
     *
     * @param plantUMLContent the raw PlantUML content to convert
     * @return Optional containing the PNG data as a byte array, or empty if request fails
     */
    public Optional<byte[]> generatePngData(String plantUMLContent) {
        Objects.requireNonNull(plantUMLContent, "PlantUML content cannot be null");

        try {
            // Encode PlantUML content for HTTP transmission
            String encodedContent = encodePlantUMLContent(plantUMLContent);

            String requestUrl = buildPngUrl(encodedContent);
            HttpRequest request = createHttpRequest(requestUrl);

            HttpResponse<byte[]> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofByteArray());

            return processHttpResponse(response);
        } catch (Exception e) {
            logger.error("Failed to generate PNG data. {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Processes HTTP response using functional approach with switch expressions.
     *
     * @param response the HTTP response to process
     * @return Optional containing PNG data if successful, empty otherwise
     */
    private Optional<byte[]> processHttpResponse(HttpResponse<byte[]> response) {
        return switch (response.statusCode()) {
            case 200 -> processPngData(response.body());
            case 400 -> {
                // Useful for debug, server returns a black image with useful information to debug.
                logger.error("Status code: {}", response.statusCode());
                yield processPngData(response.body());
            }
            default -> {
                logger.error("Status code: {}", response.statusCode());
                yield Optional.empty();
            }
        };
    }

    /**
     * Pure function to process PNG data from response body.
     *
     * @param pngData the raw PNG data from the response
     * @return Optional containing the PNG data if valid, empty if null or empty
     */
    private Optional<byte[]> processPngData(byte[] pngData) {
        return Optional.ofNullable(pngData)
            .filter(data -> data.length > 0);
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
     * Builds the complete URL for PNG generation.
     *
     * @param encodedContent the encoded PlantUML content
     * @return the complete URL for the PNG request
     */
    private String buildPngUrl(String encodedContent) {
        return serverUrl + "/png/" + encodedContent;
    }

    /**
     * Creates an HTTP request for the given URL.
     *
     * @param requestUrl the URL to request
     * @return the configured HttpRequest
     */
    private HttpRequest createHttpRequest(String requestUrl) {
        return HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .timeout(HTTP_TIMEOUT)
            .GET()
            .build();
    }

    /**
     * Gets the configured server URL.
     *
     * @return the server URL
     */
    public String getServerUrl() {
        return serverUrl;
    }
}
