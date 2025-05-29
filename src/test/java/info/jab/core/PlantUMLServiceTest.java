package info.jab.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
 */
class PlantUMLServiceTest {

    private PlantUMLService plantUMLService;

    @TempDir
    @SuppressWarnings("NullAway")  // @TempDir fields are automatically initialized by JUnit
    Path tempDir;

    @BeforeEach
    void setUp() {
        plantUMLService = new PlantUMLService();
    }

    @Test
    void shouldCreateServiceWithDefaultUrl() {
        PlantUMLService service = new PlantUMLService();
        assertThat(service).isNotNull();
    }

    @Test
    void shouldCreateServiceWithCustomUrl() {
        String customUrl = "http://localhost:8080/plantuml";
        PlantUMLService service = new PlantUMLService(customUrl);
        assertThat(service).isNotNull();
    }

    @Test
    @SuppressWarnings("NullAway")  // Intentionally testing null parameter
    void shouldThrowExceptionWhenServerUrlIsNull() {
        String nullUrl = null;
        assertThatThrownBy(() -> new PlantUMLService(nullUrl))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("PlantUML server URL cannot be null");
    }

    @Test
    void shouldConvertValidPlantUMLFile() throws Exception {
        // Given
        String validPlantUML = """
            @startuml
            Alice -> Bob: Hello
            Bob -> Alice: Hi!
            @enduml
            """;

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
    }

    @Test
    void shouldReturnEmptyForInvalidPlantUMLFile() throws Exception {
        // Given
        String invalidPlantUML = "This is not valid PlantUML content";
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
    void shouldGenerateCorrectOutputPath() throws Exception {
        // Given
        String validPlantUML = """
            @startuml
            A -> B
            @enduml
            """;

        Path inputFile = tempDir.resolve("diagram.puml");
        Files.writeString(inputFile, validPlantUML, StandardCharsets.UTF_8);

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then - Note: This test requires internet connectivity
        assertThat(result).isPresent();
        Path outputFile = result.get();
        assertThat(outputFile.getParent()).isEqualTo(tempDir);
        assertThat(outputFile.getFileName().toString()).isEqualTo("diagram.png");
    }

    @Test
    void shouldValidatePlantUMLSyntax() {
        // Test invalid PlantUML syntax validation
        String invalidContent = "not valid plantuml";
        Optional<byte[]> invalidResult = plantUMLService.generatePngData(invalidContent);
        assertThat(invalidResult).isEmpty();
    }

    @Test
    void shouldHandlePlantUMLWithComplexDiagram() throws Exception {
        // Given
        String complexPlantUML = """
            @startuml
            !define RECTANGLE class

            RECTANGLE User {
                +String name
                +String email
                +login()
                +logout()
            }

            RECTANGLE Order {
                +String id
                +Date date
                +calculateTotal()
            }

            User ||--o{ Order : places
            @enduml
            """;

        Path inputFile = tempDir.resolve("complex.puml");
        Files.writeString(inputFile, complexPlantUML, StandardCharsets.UTF_8);

        // When
        Optional<Path> result = plantUMLService.convertToPng(inputFile);

        // Then - Note: This test requires internet connectivity
        assertThat(result).isPresent();
        assertThat(Files.size(result.get())).isGreaterThan(0);
    }
}
