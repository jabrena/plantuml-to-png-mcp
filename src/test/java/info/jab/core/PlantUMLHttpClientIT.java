package info.jab.core;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Optional;

/**
 * Integration tests for PlantUMLHttpClient focusing on HTTP boundary with PlantUML server.
 *
 * These tests use WireMock to simulate PlantUML server responses,
 * eliminating the need for internet connectivity while testing
 * the HTTP client's behavior at the boundary with the PlantUML server.
 */
class PlantUMLHttpClientIT {

    private WireMockServer wireMockServer;
    private PlantUMLHttpClient httpClient;

    @BeforeEach
    void setUp() {
        // Start WireMock server on a dynamic port
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        // Create HTTP client pointing to WireMock server
        String mockServerUrl = "http://localhost:" + wireMockServer.port();
        httpClient = new PlantUMLHttpClient(mockServerUrl);
    }

    @AfterEach
    void tearDown() {
        if (Objects.nonNull(wireMockServer)) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldGeneratePngDataFromValidPlantUMLContent() throws Exception {
        // Given - Set up successful response stub with real PNG data
        wireMockServer.stubFor(get(urlMatching("/png/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "image/png")
                .withBodyFile("hello-world.png")));

        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        // When
        Optional<byte[]> result = httpClient.generatePngData(validPlantUML);

        // Then
        assertThat(result).isPresent();
        byte[] pngData = result.get();
        assertThat(pngData).isNotEmpty();

        // Verify it's a PNG file by checking PNG header (magic bytes)
        assertThat(pngData).hasSizeGreaterThan(100); // Real PNG file should be substantial in size
        assertValidPngHeader(pngData);

        // Verify WireMock received the expected call
        wireMockServer.verify(getRequestedFor(urlMatching("/png/.*")));
    }

    @Test
    void shouldReturnEmptyOptionalWhenServerReturnsErrorStatus() throws Exception {
        // Given - Set up error response stub
        wireMockServer.stubFor(get(urlMatching("/png/.*"))
            .willReturn(aResponse()
                .withStatus(500)
                .withBody("Internal server error")));

        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        // When
        Optional<byte[]> result = httpClient.generatePngData(validPlantUML);

        // Then
        assertThat(result).isEmpty();

        // Verify WireMock received the expected call
        wireMockServer.verify(getRequestedFor(urlMatching("/png/.*")));
    }

    @Test
    void shouldReturnEmptyOptionalWhenServerReturnsEmptyResponse() throws Exception {
        // Given - Set up response with empty body
        wireMockServer.stubFor(get(urlMatching("/png/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "image/png")
                .withBody(new byte[0])));

        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        // When
        Optional<byte[]> result = httpClient.generatePngData(validPlantUML);

        // Then
        assertThat(result).isEmpty();

        // Verify WireMock received the expected call
        wireMockServer.verify(getRequestedFor(urlMatching("/png/.*")));
    }

    @Test
    void shouldReturnEmptyOptionalWhenServerReturns404() throws Exception {
        // Given - Set up 404 response stub
        wireMockServer.stubFor(get(urlMatching("/png/.*"))
            .willReturn(aResponse()
                .withStatus(404)
                .withBody("Not found")));

        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        // When
        Optional<byte[]> result = httpClient.generatePngData(validPlantUML);

        // Then
        assertThat(result).isEmpty();

        // Verify WireMock received the expected call
        wireMockServer.verify(getRequestedFor(urlMatching("/png/.*")));
    }

    @Test
    void shouldMakeCorrectHttpRequestToPlantUMLServer() throws Exception {
        // Given - Set up successful response
        wireMockServer.stubFor(get(urlMatching("/png/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "image/png")
                .withBodyFile("hello-world.png")));

        String plantUMLContent = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        // When
        httpClient.generatePngData(plantUMLContent);

        // Then - Verify the request was made correctly
        wireMockServer.verify(getRequestedFor(urlPathMatching("/png/.*"))
            .withHeader("User-Agent", matching(".*Java.*"))); // HTTP client should include User-Agent

        // Verify URL structure - should be /png/{encoded-content}
        wireMockServer.verify(getRequestedFor(urlMatching("/png/.+")));
    }

    @Test
    void shouldHandleNetworkTimeout() throws Exception {
        // Given - Set up a response with a long delay
        wireMockServer.stubFor(get(urlMatching("/png/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(15000) // Slightly longer than the 10-second timeout
                .withBodyFile("hello-world.png")));

        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        // When
        Optional<byte[]> result = httpClient.generatePngData(validPlantUML);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnCorrectServerUrl() {
        // When
        String serverUrl = httpClient.getServerUrl();

        // Then
        assertThat(serverUrl).isEqualTo("http://localhost:" + wireMockServer.port());
    }

    @Test
    void shouldEncodeComplexPlantUMLContentCorrectly() throws Exception {
        // Given - Complex PlantUML content from resource file
        wireMockServer.stubFor(get(urlMatching("/png/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "image/png")
                .withBodyFile("hello-world.png")));

        String complexPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("complex-class-diagram.puml");

        // When
        Optional<byte[]> result = httpClient.generatePngData(complexPlantUML);

        // Then
        assertThat(result).isPresent();

        // Verify the request was made with encoded content
        wireMockServer.verify(getRequestedFor(urlMatching("/png/.+")));
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
