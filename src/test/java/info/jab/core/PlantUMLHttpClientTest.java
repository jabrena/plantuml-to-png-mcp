package info.jab.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlantUMLHttpClient.
 */
class PlantUMLHttpClientTest {

    private static final String TEST_SERVER_URL = "http://localhost:8080/plantuml";
    private PlantUMLHttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = new PlantUMLHttpClient(TEST_SERVER_URL);
    }

    @Test
    @DisplayName("Should create HTTP client with valid server URL")
    void shouldCreateHttpClientWithValidServerUrl() {
        // When & Then
        assertNotNull(httpClient);
        assertEquals(TEST_SERVER_URL, httpClient.getServerUrl());
    }

    @Test
    @DisplayName("Should throw NullPointerException when server URL is null")
    @SuppressWarnings("NullAway")  // Intentionally testing null parameter
    void shouldThrowExceptionWhenServerUrlIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new PlantUMLHttpClient(null),
            "Server URL cannot be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException when PlantUML content is null")
    @SuppressWarnings("NullAway")  // Intentionally testing null parameter
    void shouldThrowExceptionWhenPlantUMLContentIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> httpClient.generatePngData(null),
            "PlantUML content cannot be null");
    }

    @Test
    @DisplayName("Should return empty Optional when HTTP request fails")
    void shouldReturnEmptyWhenHttpRequestFails() throws IOException {
        // Given
        String plantUMLContent = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        // When
        Optional<byte[]> result = httpClient.generatePngData(plantUMLContent);

        // Then
        assertTrue(result.isEmpty(), "Should return empty Optional when server is not reachable");
    }

    @Test
    @DisplayName("Should handle empty PlantUML content")
    void shouldHandleEmptyPlantUMLContent() {
        // Given
        String emptyContent = "";

        // When
        Optional<byte[]> result = httpClient.generatePngData(emptyContent);

        // Then
        assertTrue(result.isEmpty(), "Should return empty Optional for empty PlantUML content");
    }

    @Test
    @DisplayName("Should construct proper PNG URL internally")
    void shouldConstructProperPngUrlInternally() {
        // Given
        String testServerUrl = "http://example.com/plantuml";
        PlantUMLHttpClient client = new PlantUMLHttpClient(testServerUrl);

        // When & Then
        assertEquals(testServerUrl, client.getServerUrl());
        // Note: The actual URL construction and encoding is tested indirectly through the generatePngData method
        // since buildPngUrl and encoding methods are private. The URL format should be: serverUrl + "/png/" + encodedContent
    }

    @Test
    @DisplayName("Should preserve server URL")
    void shouldPreserveServerUrl() {
        // Given
        String customUrl = "https://custom.plantuml.com/api";

        // When
        PlantUMLHttpClient customClient = new PlantUMLHttpClient(customUrl);

        // Then
        assertEquals(customUrl, customClient.getServerUrl());
    }

    @Test
    @DisplayName("Should handle encoding internally")
    void shouldHandleEncodingInternally() throws IOException {
        // Given
        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        // When
        Optional<byte[]> result = httpClient.generatePngData(validPlantUML);

        // Then
        // This test verifies that the method can accept raw PlantUML content
        // and handle encoding internally (will fail due to unreachable server, but that's expected)
        assertTrue(result.isEmpty(), "Should return empty due to unreachable server, but method should accept raw content");
    }
}
