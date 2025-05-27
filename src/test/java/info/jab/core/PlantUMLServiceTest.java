package info.jab.core;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PlantUMLService class.
 *
 * This test class demonstrates best practices by:
 * - Using externalized test resources for PlantUML samples
 * - Following the Given-When-Then structure
 * - Testing both positive and negative scenarios
 * - Using descriptive test method names and @DisplayName annotations
 * - Ensuring test independence with @TempDir
 * - Using parameterized tests for data variations
 * - Grouping related tests with @Nested classes
 * - Using AssertJ for fluent assertions
 */
@NullMarked
@DisplayName("PlantUML Service")
class PlantUMLServiceTest {

    @TempDir
    @SuppressWarnings("NullAway.Init")
    Path tempDir;

    private PlantUMLService service;

    @BeforeEach
    void setUp() {
        service = new PlantUMLService();
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("Should return empty Optional when PlantUML file is empty")
        void should_returnEmptyOptional_when_emptyPlantUMLFile() throws IOException {
            // Given
            Path inputFile = tempDir.resolve("empty.puml");
            TestResourceLoader.loadPlantUMLResource("empty.puml", inputFile);

            // When
            Optional<Path> result = service.convertToPng(inputFile);

            // Then
            assertThat(result).isEmpty();
        }

        @ParameterizedTest(name = "Should return empty Optional for {0}")
        @ValueSource(strings = {"invalid-missing-startuml.puml", "invalid-missing-enduml.puml"})
        @DisplayName("Should return empty Optional when required tags are missing")
        void should_returnEmptyOptional_when_requiredTagsMissing(String invalidResource) throws IOException {
            // Given
            Path inputFile = tempDir.resolve("invalid.puml");
            TestResourceLoader.loadPlantUMLResource(invalidResource, inputFile);

            // When
            Optional<Path> result = service.convertToPng(inputFile);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when file contains only whitespace")
        void should_returnEmptyOptional_when_whitespaceOnlyFile() throws IOException {
            // Given
            Path inputFile = tempDir.resolve("whitespace.puml");
            TestResourceLoader.loadPlantUMLResource("whitespace-only.puml", inputFile);

            // When
            Optional<Path> result = service.convertToPng(inputFile);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when input file does not exist")
        void should_returnEmptyOptional_when_inputFileDoesNotExist() {
            // Given
            Path nonExistentFile = tempDir.resolve("non-existent.puml");

            // When
            Optional<Path> result = service.convertToPng(nonExistentFile);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("PNG Conversion")
    class PngConversion {

        @Test
        @DisplayName("Should convert valid PlantUML file to PNG")
        void should_convertToPng_when_validPlantUMLProvided() throws IOException {
            // Given
            Path inputFile = tempDir.resolve("test.puml");
            TestResourceLoader.loadPlantUMLResource("valid-simple.puml", inputFile);

            // When
            Optional<Path> result = service.convertToPng(inputFile);

            // Then
            assertThat(result).isPresent();
            Path outputPath = result.get();
            assertThat(outputPath)
                .exists()
                .hasFileName("test.png");
            assertThat(outputPath.getParent())
                .isEqualTo(inputFile.getParent());
            assertThat(Files.size(outputPath))
                .isPositive();
        }

        @ParameterizedTest(name = "Should convert {0} diagram to PNG")
        @ValueSource(strings = {"sequence-diagram.puml", "class-diagram.puml"})
        @DisplayName("Should convert different diagram types to PNG")
        void should_convertDiagramToPng_when_validDiagramTypeProvided(String diagramResource) throws IOException {
            // Given
            String baseName = diagramResource.replace(".puml", "");
            Path inputFile = tempDir.resolve(baseName + ".puml");
            TestResourceLoader.loadPlantUMLResource(diagramResource, inputFile);

            // When
            Optional<Path> result = service.convertToPng(inputFile);

            // Then
            assertThat(result).isPresent();
            Path outputPath = result.get();
            assertThat(outputPath)
                .exists()
                .hasFileName(baseName + ".png");
            assertThat(Files.size(outputPath))
                .isPositive();
        }

        @Test
        @DisplayName("Should create output in same directory as input when input is in subdirectory")
        void should_createCorrectOutputPath_when_inputInSubdirectory() throws IOException {
            // Given
            Path subdirectory = tempDir.resolve("subdirectory");
            Files.createDirectories(subdirectory);
            Path inputFile = subdirectory.resolve("my-diagram.puml");
            TestResourceLoader.loadPlantUMLResource("valid-simple.puml", inputFile);

            // When
            Optional<Path> result = service.convertToPng(inputFile);

            // Then
            assertThat(result).isPresent();
            Path outputPath = result.get();
            assertThat(outputPath)
                .exists()
                .hasFileName("my-diagram.png");
            assertThat(outputPath.getParent())
                .isEqualTo(inputFile.getParent());
        }
    }

}
