package info.jab.core;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PlantUMLToPngCli class.
 *
 * This test class demonstrates best practices by:
 * - Using Mockito for dependency mocking (following Rule 8)
 * - Using nested test classes to group related functionality
 * - Following the Given-When-Then structure
 * - Testing both positive and negative scenarios
 * - Using descriptive test method names and @DisplayName annotations
 * - Ensuring test independence with @TempDir
 * - Using AssertJ for fluent assertions
 * - Leveraging JSpecify for null safety
 * - Proper verification of mock interactions
 */
@NullMarked
@ExtendWith(MockitoExtension.class)
@DisplayName("PlantUML to PNG CLI")
class PlantUMLToPngCliTest {

    @TempDir
    private Path tempDir;

    @Mock
    private PlantUMLService mockService;

    @Mock
    private PlantUMLFileValidator mockFileValidator;

    private PlantUMLToPngCli cli;

    @BeforeEach
    @SuppressWarnings("NullAway.Init")
    void setUp() {
        cli = new PlantUMLToPngCli(mockFileValidator, mockService);
    }

    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("Should return exit code 0 when converting valid PlantUML file")
        void should_returnExitCode0_when_convertingValidPlantUMLFile() throws IOException {
            // Given
            Path testFile = tempDir.resolve("test.puml");
            TestResourceLoader.loadPlantUMLResource("hello-world.puml", testFile);

            Path expectedOutputPath = tempDir.resolve("test.png");
            when(mockFileValidator.validatePlantUMLFile(testFile.toString())).thenReturn(Optional.of(testFile));
            when(mockService.convertToPng(testFile)).thenReturn(Optional.of(expectedOutputPath));

            // When
            CommandLine cmd = new CommandLine(cli);
            int exitCode = cmd.execute("--file", testFile.toString());

            // Then
            assertThat(exitCode)
                .as("CLI should return success exit code for valid conversion")
                .isZero();

            // Verify interactions
            verify(mockFileValidator).validatePlantUMLFile(testFile.toString());
            verify(mockService).convertToPng(testFile);
            verifyNoMoreInteractions(mockService, mockFileValidator);
        }

        @Test
        @DisplayName("Should return exit code 0 when help option is requested")
        void should_returnExitCode0_when_helpOptionRequested() {
            // Given & When
            CommandLine cmd = new CommandLine(cli);
            int exitCode = cmd.execute("--help");

            // Then
            assertThat(exitCode)
                .as("CLI should return success exit code for help option")
                .isZero();

            // Verify no service interactions for help
            verifyNoInteractions(mockService, mockFileValidator);
        }

        @Test
        @DisplayName("Should return exit code 0 when version option is requested")
        void should_returnExitCode0_when_versionOptionRequested() {
            // Given & When
            CommandLine cmd = new CommandLine(cli);
            int exitCode = cmd.execute("--version");

            // Then
            assertThat(exitCode)
                .as("CLI should return success exit code for version option")
                .isZero();

            // Verify no service interactions for version
            verifyNoInteractions(mockService, mockFileValidator);
        }
    }

    @Nested
    @DisplayName("Error Scenarios")
    class ErrorScenarios {

        @Test
        @DisplayName("Should return exit code 1 when file does not exist")
        void should_returnExitCode1_when_fileDoesNotExist() {
            // Given
            Path nonExistentFile = tempDir.resolve("nonexistent.puml");
            when(mockFileValidator.validatePlantUMLFile(nonExistentFile.toString()))
                .thenReturn(Optional.empty());

            // When
            CommandLine cmd = new CommandLine(cli);
            int exitCode = cmd.execute("--file", nonExistentFile.toString());

            // Then
            assertThat(exitCode)
                .as("CLI should return error exit code for non-existent file")
                .isOne();

            // Verify only file validation was called
            verify(mockFileValidator).validatePlantUMLFile(nonExistentFile.toString());
            verifyNoInteractions(mockService);
        }

        @Test
        @DisplayName("Should return exit code 1 when file has invalid extension")
        void should_returnExitCode1_when_fileHasInvalidExtension() throws IOException {
            // Given
            Path testFile = tempDir.resolve("test.txt");
            Files.writeString(testFile, "some content");
            when(mockFileValidator.validatePlantUMLFile(testFile.toString()))
                .thenReturn(Optional.empty());

            // When
            CommandLine cmd = new CommandLine(cli);
            int exitCode = cmd.execute("--file", testFile.toString());

            // Then
            assertThat(exitCode)
                .as("CLI should return error exit code for invalid file extension")
                .isOne();

            // Verify only file validation was called
            verify(mockFileValidator).validatePlantUMLFile(testFile.toString());
            verifyNoInteractions(mockService);
        }

        @Test
        @DisplayName("Should return exit code 1 when conversion fails")
        void should_returnExitCode1_when_conversionFails() throws IOException {
            // Given
            Path testFile = tempDir.resolve("test.puml");
            TestResourceLoader.loadPlantUMLResource("hello-world.puml", testFile);

            when(mockFileValidator.validatePlantUMLFile(testFile.toString())).thenReturn(Optional.of(testFile));
            when(mockService.convertToPng(testFile)).thenReturn(Optional.empty());

            // When
            CommandLine cmd = new CommandLine(cli);
            int exitCode = cmd.execute("--file", testFile.toString());

            // Then
            assertThat(exitCode)
                .as("CLI should return error exit code when conversion fails")
                .isOne();

            // Verify all methods were called
            verify(mockFileValidator).validatePlantUMLFile(testFile.toString());
            verify(mockService).convertToPng(testFile);
            verifyNoMoreInteractions(mockService, mockFileValidator);
        }
    }
}
