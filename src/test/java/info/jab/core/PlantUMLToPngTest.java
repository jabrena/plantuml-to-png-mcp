package info.jab.core;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PlantUMLToPng class.
 *
 * This test class demonstrates best practices by:
 * - Using Mockito for dependency mocking (following Rule 8)
 * - Following the Given-When-Then structure
 * - Testing both positive and negative scenarios
 * - Using descriptive test method names and @DisplayName annotations
 * - Ensuring test independence with @TempDir
 * - Using AssertJ for fluent assertions
 * - Leveraging JSpecify for null safety
 * - Proper verification of mock interactions
 * - Testing the execute() method that returns CliResult for type-safe assertions
 */
@NullMarked
@ExtendWith(MockitoExtension.class)
class PlantUMLToPngTest {

    @TempDir
    private Path tempDir;

    @Mock
    private PlantUMLFileService mockService;

    @Mock
    private PlantUMLWatchService mockWatchService;

    @Mock
    private PlantUMLFileValidator mockValidator;

    private PlantUMLToPng cli;

    @BeforeEach
    @SuppressWarnings("NullAway.Init")
    void setUp() {
        cli = new PlantUMLToPng(mockValidator, mockService, mockWatchService);
    }

    @Test
    @DisplayName("Should return CliResult.OK when converting valid PlantUML file")
    void should_returnCliResultOK_when_convertingValidPlantUMLFile() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.puml");
        TestResourceLoader.loadPlantUMLResource("hello-world.puml", testFile);

        when(mockValidator.validatePlantUMLFile(testFile.toString())).thenReturn(Optional.of(testFile));
        when(mockService.processFile(testFile)).thenReturn(true);

        // Set the inputFile for the CLI
        cli.inputFile = testFile.toString();

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.OK for valid conversion")
            .isEqualTo(CliResult.OK);

        // Verify interactions
        verify(mockValidator).validatePlantUMLFile(testFile.toString());
        verify(mockService).processFile(testFile);
        verifyNoMoreInteractions(mockService);
        verifyNoInteractions(mockWatchService);
    }

    @Test
    @DisplayName("Should return CliResult.KO when file does not exist")
    void should_returnCliResultKO_when_fileDoesNotExist() {
        // Given
        Path nonExistentFile = tempDir.resolve("nonexistent.puml");
        when(mockValidator.validatePlantUMLFile(nonExistentFile.toString()))
            .thenReturn(Optional.empty());

        // Set the inputFile for the CLI
        cli.inputFile = nonExistentFile.toString();

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.KO for non-existent file")
            .isEqualTo(CliResult.KO);

        // Verify only file validation was called
        verify(mockValidator).validatePlantUMLFile(nonExistentFile.toString());
        verifyNoInteractions(mockService, mockWatchService);
    }

    @Test
    @DisplayName("Should return CliResult.KO when file has invalid extension")
    void should_returnCliResultKO_when_fileHasInvalidExtension() {
        // Given
        String invalidFile = "test.txt";
        when(mockValidator.validatePlantUMLFile(invalidFile))
            .thenReturn(Optional.empty());

        // Set the inputFile for the CLI
        cli.inputFile = invalidFile;

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.KO for invalid file extension")
            .isEqualTo(CliResult.KO);

        // Verify only file validation was called
        verify(mockValidator).validatePlantUMLFile(invalidFile);
        verifyNoInteractions(mockService, mockWatchService);
    }

    @Test
    @DisplayName("Should return CliResult.KO when conversion fails")
    void should_returnCliResultKO_when_conversionFails() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.puml");
        TestResourceLoader.loadPlantUMLResource("hello-world.puml", testFile);

        when(mockValidator.validatePlantUMLFile(testFile.toString())).thenReturn(Optional.of(testFile));
        when(mockService.processFile(testFile)).thenReturn(false);

        // Set the inputFile for the CLI
        cli.inputFile = testFile.toString();

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.KO for conversion failure")
            .isEqualTo(CliResult.KO);

        // Verify service interactions
        verify(mockValidator).validatePlantUMLFile(testFile.toString());
        verify(mockService).processFile(testFile);
        verifyNoInteractions(mockWatchService);
    }

    @Test
    @DisplayName("Should return CliResult.OK when watch option is used successfully")
    void should_returnCliResultOK_when_watchOptionUsedSuccessfully() {
        // Given
        when(mockWatchService.startWatching()).thenReturn(0);
        cli.watchOption = true;

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.OK from successful watch service")
            .isEqualTo(CliResult.OK);

        // Verify watch service was called
        verify(mockWatchService).startWatching();
        verifyNoInteractions(mockService, mockValidator);
    }

    @Test
    @DisplayName("Should return CliResult.KO when watch service fails")
    void should_returnCliResultKO_when_watchServiceFails() {
        // Given
        when(mockWatchService.startWatching()).thenReturn(1);
        cli.watchOption = true;

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.KO when watch service fails")
            .isEqualTo(CliResult.KO);

        // Verify watch service was called
        verify(mockWatchService).startWatching();
        verifyNoInteractions(mockService, mockValidator);
    }

    @Test
    @DisplayName("Should return CliResult.KO when no arguments provided")
    void should_returnCliResultKO_when_noArgumentsProvided() {
        // Given - no inputFile and no watchOption set

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.KO when no arguments provided")
            .isEqualTo(CliResult.KO);

        // Verify no service interactions
        verifyNoInteractions(mockService, mockWatchService, mockValidator);
    }
}
