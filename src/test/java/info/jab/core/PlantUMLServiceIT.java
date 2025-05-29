package info.jab.core;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.io.IOException;

/**
 * Integration tests for PlantUMLService using WireMock for HTTP mocking.
 *
 * These tests use WireMock to simulate PlantUML server responses,
 * eliminating the need for internet connectivity while still testing
 * the full HTTP integration flow.
 */
class PlantUMLServiceIT {

    private WireMockServer wireMockServer;
    private PlantUMLService plantUMLService;
    private PlantUMLHttpClient httpClient;

    @TempDir
    @SuppressWarnings("NullAway")  // @TempDir fields are automatically initialized by JUnit
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // Start WireMock server on a dynamic port
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        // Create HTTP client pointing to WireMock server
        String mockServerUrl = "http://localhost:" + wireMockServer.port();
        httpClient = new PlantUMLHttpClient(mockServerUrl);

        // Create PlantUMLService with the mocked HTTP client
        plantUMLService = new PlantUMLService(httpClient);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldConvertValidPlantUMLFileWithMockedHttpCall() throws Exception {
        // Given - Set up successful response stub with real PNG data using native WireMock file serving
        wireMockServer.stubFor(get(urlMatching("/png/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "image/png")
                .withBodyFile("hello-world.png")));

        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        Path inputFile = tempDir.resolve("test.puml");
        Files.writeString(inputFile, validPlantUML, StandardCharsets.UTF_8);

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then
        assertThat(result).isPresent();
        Path outputPath = result.get();
        assertThat(outputPath).exists();

        // Additional assertions - verify the output file contains PNG data
        byte[] fileBytes = Files.readAllBytes(outputPath);
        assertThat(fileBytes).isNotEmpty();

        // Verify it's a PNG file by checking PNG header (magic bytes)
        assertThat(fileBytes).hasSizeGreaterThan(100); // Real PNG file should be substantial in size
        assertThat(fileBytes[0]).isEqualTo((byte) 0x89); // PNG signature
        assertThat(fileBytes[1]).isEqualTo((byte) 0x50); // P
        assertThat(fileBytes[2]).isEqualTo((byte) 0x4E); // N
        assertThat(fileBytes[3]).isEqualTo((byte) 0x47); // G

        // Verify WireMock received the expected call
        wireMockServer.verify(getRequestedFor(urlMatching("/png/.*")));
    }

    @Test
    void shouldReturnEmptyOptionalWhenHttpCallFails() throws Exception {
        // Given - Set up error response stub
        wireMockServer.stubFor(get(urlMatching("/png/.*"))
            .willReturn(aResponse()
                .withStatus(500)
                .withBody("Internal server error")));

        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        Path inputFile = tempDir.resolve("test.puml");
        Files.writeString(inputFile, validPlantUML, StandardCharsets.UTF_8);

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then
        assertThat(result).isEmpty();

        // Verify WireMock received the expected call
        wireMockServer.verify(getRequestedFor(urlMatching("/png/.*")));
    }
}
