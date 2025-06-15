package info.jab.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
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
 * - Using CommandLine API for proper CLI argument simulation
 */
@NullMarked
@ExtendWith(MockitoExtension.class)
class MainApplicatonTest {

    @TempDir
    private Path tempDir;

    @Mock
    private PlantUMLFileService mockService;

    @Mock
    private PlantUMLWatchService mockWatchService;

    @Mock
    private PlantUMLFileValidator mockValidator;

    private MainApplication cli;
    private CommandLine commandLine;

    @BeforeEach
    @SuppressWarnings("NullAway.Init")
    void setUp() {
        cli = new MainApplication(mockValidator, mockService, mockWatchService);
        commandLine = new CommandLine(cli);
    }

    @Test
    @DisplayName("Should return CliResult.OK when converting valid PlantUML file")
    void should_returnCliResultOK_when_convertingValidPlantUMLFile() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.puml");
        TestResourceLoader.loadPlantUMLResource("hello-world.puml", testFile);

        when(mockValidator.validatePlantUMLFile(testFile.toString())).thenReturn(Optional.of(testFile));
        when(mockService.processFile(testFile)).thenReturn(true);

        // Parse CLI arguments to set the inputFile
        commandLine.parseArgs("--file", testFile.toString());

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

        // Parse CLI arguments to set the inputFile
        commandLine.parseArgs("--file", nonExistentFile.toString());

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

        // Parse CLI arguments to set the inputFile
        commandLine.parseArgs("--file", invalidFile);

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

        // Parse CLI arguments to set the inputFile
        commandLine.parseArgs("--file", testFile.toString());

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
        when(mockWatchService.startWatching(any(Path.class))).thenReturn(0);

        // Parse CLI arguments to set the watch directory
        commandLine.parseArgs("--watch", tempDir.toString());

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.OK from successful watch service")
            .isEqualTo(CliResult.OK);

        // Verify watch service was called
        verify(mockWatchService).startWatching(any(Path.class));
        verifyNoInteractions(mockService, mockValidator);
    }

    @Test
    @DisplayName("Should return CliResult.KO when watch service fails")
    void should_returnCliResultKO_when_watchServiceFails() {
        // Given
        when(mockWatchService.startWatching(any(Path.class))).thenReturn(1);

        // Parse CLI arguments to set the watch directory
        commandLine.parseArgs("--watch", tempDir.toString());

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.KO when watch service fails")
            .isEqualTo(CliResult.KO);

        // Verify watch service was called
        verify(mockWatchService).startWatching(any(Path.class));
        verifyNoInteractions(mockService, mockValidator);
    }

    @Test
    @DisplayName("Should return CliResult.KO when no arguments provided")
    void should_returnCliResultKO_when_noArgumentsProvided() {
        // Given - no arguments parsed (no inputFile and no watchDirectory set)

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.KO when no arguments provided")
            .isEqualTo(CliResult.KO);

        // Verify no service interactions
        verifyNoInteractions(mockService, mockWatchService, mockValidator);
    }

    @Test
    @DisplayName("Should return CliResult.KO when watch directory does not exist")
    void should_returnCliResultKO_when_watchDirectoryDoesNotExist() {
        // Given
        String nonExistentDir = "/non/existent/directory";

        // Parse CLI arguments to set the watch directory
        commandLine.parseArgs("--watch", nonExistentDir);

        // When
        CliResult result = cli.execute();

        // Then
        assertThat(result)
            .as("CLI should return CliResult.KO when watch directory does not exist")
            .isEqualTo(CliResult.KO);

        // Verify no service interactions (because directory validation failed)
        verifyNoInteractions(mockService, mockWatchService, mockValidator);
    }
}
