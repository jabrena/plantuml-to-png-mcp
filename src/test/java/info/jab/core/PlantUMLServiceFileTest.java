package info.jab.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PlantUMLService class using HTTP-based conversion.
 *
 * This test class demonstrates best practices by:
 * - Testing HTTP-based PlantUML conversion
 * - Following the Given-When-Then structure
 * - Testing both positive and negative scenarios
 * - Using descriptive test method names
 * - Ensuring test independence with @TempDir
 * - Using AssertJ for fluent assertions
 * - Mocking HTTP client for better unit testing
 */
class PlantUMLServiceFileTest {

    @SuppressWarnings("NullAway")  // Mock fields are initialized by MockitoAnnotations.openMocks
    private PlantUMLFileService plantUMLService;

    @Mock
    @SuppressWarnings("NullAway")  // Mock fields are initialized by MockitoAnnotations.openMocks
    private PlantUMLHttpClient mockHttpClient;

    @TempDir
    @SuppressWarnings("NullAway")  // @TempDir fields are automatically initialized by JUnit
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        plantUMLService = new PlantUMLFileService(mockHttpClient);
    }

    @Test
    void shouldCreateServiceWithDefaultUrl() {
        PlantUMLFileService service = new PlantUMLFileService();
        assertThat(service).isNotNull();
    }

    @Test
    void shouldCreateServiceWithCustomUrl() {
        String customUrl = "http://localhost:8080/plantuml";
        PlantUMLFileService service = new PlantUMLFileService(customUrl);
        assertThat(service).isNotNull();
    }

    @Test
    @SuppressWarnings("NullAway")  // Intentionally testing null parameter
    void shouldThrowExceptionWhenServerUrlIsNull() {
        String nullUrl = null;
        assertThatThrownBy(() -> new PlantUMLFileService(nullUrl))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("PlantUML server URL cannot be null");
    }

    @Test
    @SuppressWarnings("NullAway")  // Intentionally testing null parameter
    void shouldThrowExceptionWhenHttpClientIsNull() {
        PlantUMLHttpClient nullClient = null;
        assertThatThrownBy(() -> new PlantUMLFileService(nullClient))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("HTTP client cannot be null");
    }

    @Test
    void shouldConvertValidPlantUMLFile() throws Exception {
        // Given
        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        Path inputFile = tempDir.resolve("test.puml");
        Files.writeString(inputFile, validPlantUML, StandardCharsets.UTF_8);

        byte[] mockPngData = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG header
        when(mockHttpClient.generatePngData(validPlantUML)).thenReturn(Optional.of(mockPngData));

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then
        assertThat(result).isPresent();
        Path outputFile = result.get();
        assertThat(outputFile).exists();
        assertThat(outputFile.getFileName().toString()).isEqualTo("test.png");
        assertThat(Files.size(outputFile)).isGreaterThan(0);
    }

    @Test
    void shouldReturnEmptyForInvalidPlantUMLFile() throws Exception {
        // Given
        String invalidPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("invalid-content.puml");
        Path inputFile = tempDir.resolve("invalid.puml");
        Files.writeString(inputFile, invalidPlantUML, StandardCharsets.UTF_8);

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForEmptyFile() throws Exception {
        // Given
        Path inputFile = tempDir.resolve("empty.puml");
        Files.writeString(inputFile, "", StandardCharsets.UTF_8);

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNonExistentFile() {
        // Given
        Path nonExistentFile = tempDir.resolve("nonexistent.puml");

        // When
        Optional<Path> result = plantUMLService.convertToPng(nonExistentFile);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenHttpClientFails() throws Exception {
        // Given
        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        Path inputFile = tempDir.resolve("diagram.puml");
        Files.writeString(inputFile, validPlantUML, StandardCharsets.UTF_8);

        when(mockHttpClient.generatePngData(validPlantUML)).thenReturn(Optional.empty());

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGenerateCorrectOutputPath() throws Exception {
        // Given
        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        Path inputFile = tempDir.resolve("diagram.puml");
        Files.writeString(inputFile, validPlantUML, StandardCharsets.UTF_8);

        byte[] mockPngData = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG header
        when(mockHttpClient.generatePngData(validPlantUML)).thenReturn(Optional.of(mockPngData));

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then
        assertThat(result).isPresent();
        Path outputFile = result.get();
        assertThat(outputFile.getParent()).isEqualTo(tempDir);
        assertThat(outputFile.getFileName().toString()).isEqualTo("diagram.png");
    }

    @Test
    void shouldValidatePlantUMLSyntax() throws IOException {
        // Test invalid PlantUML syntax validation
        String invalidContent = TestResourceLoader.loadPlantUMLResourceAsString("invalid-content.puml");
        Optional<byte[]> invalidResult = plantUMLService.generatePngData(invalidContent);
        assertThat(invalidResult).isEmpty();
    }

    @Test
    void shouldHandlePlantUMLWithComplexDiagram() throws Exception {
        // Given
        String complexPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("complex-class-diagram.puml");

        Path inputFile = tempDir.resolve("complex.puml");
        Files.writeString(inputFile, complexPlantUML, StandardCharsets.UTF_8);

        byte[] mockPngData = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}; // PNG header
        when(mockHttpClient.generatePngData(complexPlantUML)).thenReturn(Optional.of(mockPngData));

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then
        assertThat(result).isPresent();
        assertThat(Files.size(result.get())).isGreaterThan(0);
    }

    @Test
    void shouldTestEncodingFunctionality() throws IOException {
        // Given
        String validPlantUML = TestResourceLoader.loadPlantUMLResourceAsString("hello-world.puml");

        when(mockHttpClient.generatePngData(validPlantUML)).thenReturn(Optional.empty());

        // When
        Optional<byte[]> result = plantUMLService.generatePngData(validPlantUML);

        // Then
        assertThat(result).isEmpty(); // Should be empty due to mock returning empty
    }
}
