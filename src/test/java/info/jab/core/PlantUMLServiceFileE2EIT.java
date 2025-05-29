package info.jab.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PlantUMLService that test actual HTTP functionality.
 *
 * These tests require internet connectivity and are disabled by default.
 * Set the environment variable ENABLE_INTEGRATION_TESTS=true to run them.
 */
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
class PlantUMLServiceFileE2EIT {

    private PlantUMLFileService plantUMLService;

    @TempDir
    @SuppressWarnings("NullAway")  // @TempDir fields are automatically initialized by JUnit
    Path tempDir;

    @BeforeEach
    void setUp() {
        plantUMLService = new PlantUMLFileService();
    }

    @Test
    void shouldConvertValidPlantUMLFileWithRealHttpCall() throws Exception {
        // Given
        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        Path inputFile = tempDir.resolve("test.puml");
        Files.writeString(inputFile, validPlantUML, StandardCharsets.UTF_8);

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then - Note: This test requires internet connectivity
        assertThat(result).isPresent();
        Path outputFile = result.get();
        assertThat(outputFile).exists();
        assertThat(outputFile.getFileName().toString()).isEqualTo("test.png");
        assertThat(Files.size(outputFile)).isGreaterThan(0);

        // Verify it's actually a PNG file by checking the header
        byte[] fileContent = Files.readAllBytes(outputFile);
        assertThat(fileContent).hasSizeGreaterThan(8);
        assertThat(fileContent[0]).isEqualTo((byte) 0x89); // PNG signature
        assertThat(fileContent[1]).isEqualTo((byte) 0x50); // 'P'
        assertThat(fileContent[2]).isEqualTo((byte) 0x4E); // 'N'
        assertThat(fileContent[3]).isEqualTo((byte) 0x47); // 'G'
    }

    @Test
    void shouldHandleComplexDiagramWithRealHttpCall() throws Exception {
        // Given
        String complexPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("complex-class-diagram.puml");

        Path inputFile = tempDir.resolve("complex.puml");
        Files.writeString(inputFile, complexPlantUML, StandardCharsets.UTF_8);

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then - Note: This test requires internet connectivity
        assertThat(result).isPresent();
        assertThat(Files.size(result.get())).isGreaterThan(0);
    }

    @Test
    void shouldTestHttpClientDirectly() throws IOException {
        // Given
        PlantUMLHttpClient httpClient = new PlantUMLHttpClient("http://www.plantuml.com/plantuml");
        String rawPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        // When
        Optional<byte[]> result = httpClient.generatePngData(rawPlantUML);

        // Then - Note: This test requires internet connectivity
        assertThat(result).isPresent();
        byte[] pngData = result.get();
        assertThat(pngData).hasSizeGreaterThan(0);

        // Verify it's actually PNG data
        assertThat(pngData[0]).isEqualTo((byte) 0x89); // PNG signature
        assertThat(pngData[1]).isEqualTo((byte) 0x50); // 'P'
        assertThat(pngData[2]).isEqualTo((byte) 0x4E); // 'N'
        assertThat(pngData[3]).isEqualTo((byte) 0x47); // 'G'
    }
}
