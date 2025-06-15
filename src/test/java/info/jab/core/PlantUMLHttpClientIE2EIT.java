package info.jab.core;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End integration tests for PlantUMLHttpClient that test actual HTTP boundary with PlantUML server.
 *
 * These tests require internet connectivity and are disabled by default.
 * Set the environment variable ENABLE_INTEGRATION_TESTS=true to run them.
 *
 * These tests verify the HTTP client's behavior when communicating with the real PlantUML server,
 * ensuring proper encoding, network handling, and response parsing.
 */
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
class PlantUMLHttpClientIE2EIT {

    private PlantUMLHttpClient httpClient;

    @BeforeEach
    void setUp() {
        // Use the official PlantUML server for E2E testing
        httpClient = new PlantUMLHttpClient("http://www.plantuml.com/plantuml");
    }

    @Test
    void shouldGeneratePngDataFromValidDiagramWithRealServer() throws IOException {
        // Given - Load valid PlantUML content from resources
        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        // When - Make actual HTTP call to PlantUML server
        Optional<byte[]> result = httpClient.generatePngData(validPlantUML);

        // Then - Verify we get valid PNG data
        assertThat(result).isPresent();
        byte[] pngData = result.get();
        assertThat(pngData).hasSizeGreaterThan(0);

        // Verify it's actually a valid PNG file by checking the header
        assertValidPngHeader(pngData);
    }

    @Test
    void shouldHandleInvalidContentGracefullyWithRealServer() throws IOException {
        // Given - Load invalid PlantUML content from resources
        String invalidPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("invalid-content.puml");

        // When - Make actual HTTP call to PlantUML server with invalid content
        Optional<byte[]> result = httpClient.generatePngData(invalidPlantUML);

        // Then - Should handle server errors gracefully
        // Note: PlantUML server might still generate a PNG with error message,
        // so we just verify the call doesn't throw an exception
        // The result might be present (with error diagram) or empty depending on server behavior
        assertThat(result).isNotNull();
    }

    /**
     * Verifies that the provided byte array contains a valid PNG file header.
     * PNG files start with the signature: 0x89 0x50 0x4E 0x47 (89 PNG)
     *
     * @param pngData the byte array to verify
     */
    private void assertValidPngHeader(byte[] pngData) {
        assertThat(pngData).hasSizeGreaterThanOrEqualTo(4);
        assertThat(pngData[0]).isEqualTo((byte) 0x89); // PNG signature
        assertThat(pngData[1]).isEqualTo((byte) 0x50); // 'P'
        assertThat(pngData[2]).isEqualTo((byte) 0x4E); // 'N'
        assertThat(pngData[3]).isEqualTo((byte) 0x47); // 'G'
    }
}
