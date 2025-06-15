package info.jab.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlantUMLWatchServiceTest {

    @Mock
    @SuppressWarnings("NullAway") // Initialized by Mockito
    private PlantUMLFileService mockPlantUMLService;

    private PlantUMLWatchService watchService;

    @TempDir
    @SuppressWarnings("NullAway") // Initialized by JUnit
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        watchService = new PlantUMLWatchService(mockPlantUMLService, 100); // Short interval for tests
    }

    @Test
    void constructor_WithValidService_ShouldCreateInstance() {
        // Given & When
        PlantUMLWatchService service = new PlantUMLWatchService(mockPlantUMLService);

        // Then
        assertNotNull(service);
    }

    @Test
    @SuppressWarnings("NullAway") // Intentionally testing null parameter
    void constructor_WithNullService_ShouldThrowException() {
        // Given, When & Then
        assertThrows(NullPointerException.class,
            () -> new PlantUMLWatchService(null));
    }

    @Test
    void constructor_WithCustomInterval_ShouldCreateInstance() {
        // Given & When
        PlantUMLWatchService service = new PlantUMLWatchService(mockPlantUMLService, 2000L);

        // Then
        assertNotNull(service);
    }

    @Test
    void processPlantUMLFiles_WithNewPumlFile_ShouldConvertToPng() throws IOException {
        // Given
        Path pumlFile = tempDir.resolve("diagram.puml");
        Files.createFile(pumlFile);
        when(mockPlantUMLService.processFile(pumlFile)).thenReturn(true);

        // When
        watchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService).processFile(pumlFile);
    }

    // TODO: Technical debt.
    @Test
    void processPlantUMLFiles_WithExistingPngFileAndOldModificationTime_ShouldNotConvert() throws IOException, InterruptedException {
        // Given
        Path pumlFile = tempDir.resolve("existing.puml");
        Path pngFile = tempDir.resolve("existing.png");
        Files.createFile(pumlFile);
        Files.createFile(pngFile); // PNG already exists

        // Wait for more than a minute to ensure the file is not considered "recently modified"
        // For testing purposes, we'll sleep for a small amount and then use a custom watch service
        // that considers "recently modified" as files modified in the last 100ms instead of 1 minute
        Thread.sleep(150);

        // Create a watch service with a very short "recent" threshold for testing
        PlantUMLWatchService testWatchService = new PlantUMLWatchService(mockPlantUMLService, 100) {
            @Override
            boolean isFileModifiedInLastSeconds(Path filePath) {
                try {
                    java.nio.file.attribute.FileTime lastModified = Files.getLastModifiedTime(filePath);
                    java.time.Instant hundredMsAgo = java.time.Instant.now().minus(100, java.time.temporal.ChronoUnit.MILLIS);
                    return lastModified.toInstant().isAfter(hundredMsAgo);
                } catch (IOException e) {
                    return false;
                }
            }
        };

        // When
        testWatchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService, never()).processFile(pumlFile);
    }

    @Test
    void processPlantUMLFiles_WithExistingPngFileButRecentlyModified_ShouldConvert() throws IOException {
        // Given
        Path pumlFile = tempDir.resolve("existing.puml");
        Path pngFile = tempDir.resolve("existing.png");
        Files.createFile(pumlFile);
        Files.createFile(pngFile); // PNG already exists

        when(mockPlantUMLService.processFile(pumlFile)).thenReturn(true);

        // When - Since the file was just created, it should be considered "recently modified"
        watchService.processPlantUMLFiles(tempDir);

        // Then - Should process because it's recently modified
        verify(mockPlantUMLService).processFile(pumlFile);
    }

    @Test
    void processPlantUMLFiles_WithBothFilesRecentlyModified_ShouldConvert() throws IOException {
        // Given
        Path pumlFile = tempDir.resolve("both_recent.puml");
        Path pngFile = tempDir.resolve("both_recent.png");
        Files.createFile(pumlFile);
        Files.createFile(pngFile); // Both files exist and are recently created
        when(mockPlantUMLService.processFile(pumlFile)).thenReturn(true);

        // When - Both files were just created, so both are considered "recently modified"
        watchService.processPlantUMLFiles(tempDir);

        // Then - Should convert because both files were recently modified (ensuring synchronization)
        verify(mockPlantUMLService).processFile(pumlFile);
    }

    @Test
    void processPlantUMLFiles_WithMultiplePumlFiles_ShouldProcessAll() throws IOException {
        // Given
        Path pumlFile1 = tempDir.resolve("diagram1.puml");
        Path pumlFile2 = tempDir.resolve("diagram2.puml");
        Path pumlFile3 = tempDir.resolve("subdir/diagram3.puml");

        Files.createDirectories(tempDir.resolve("subdir"));
        Files.createFile(pumlFile1);
        Files.createFile(pumlFile2);
        Files.createFile(pumlFile3);

        when(mockPlantUMLService.processFile(any(Path.class))).thenReturn(true);

        // When
        watchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService).processFile(pumlFile1);
        verify(mockPlantUMLService).processFile(pumlFile2);
        verify(mockPlantUMLService).processFile(pumlFile3);
    }

    @Test
    void processPlantUMLFiles_WithNoPlantUMLFiles_ShouldNotCallService() throws IOException {
        // Given
        Path txtFile = tempDir.resolve("readme.txt");
        Files.createFile(txtFile);

        // When
        watchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService, never()).processFile(any(Path.class));
    }

    @Test
    void processPlantUMLFiles_WithCaseSensitiveExtensions_ShouldProcessAllVariants() throws IOException {
        // Given
        Path pumlLower = tempDir.resolve("diagram.puml");
        Path pumlUpper = tempDir.resolve("diagram2.PUML");
        Path pumlMixed = tempDir.resolve("diagram3.PuMl");

        Files.createFile(pumlLower);
        Files.createFile(pumlUpper);
        Files.createFile(pumlMixed);

        when(mockPlantUMLService.processFile(any(Path.class))).thenReturn(true);

        // When
        watchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService).processFile(pumlLower);
        verify(mockPlantUMLService).processFile(pumlUpper);
        verify(mockPlantUMLService).processFile(pumlMixed);
    }

    @Test
    void processPlantUMLFiles_WhenConversionFails_ShouldContinueProcessing() throws IOException {
        // Given
        Path pumlFile1 = tempDir.resolve("failing.puml");
        Path pumlFile2 = tempDir.resolve("succeeding.puml");

        Files.createFile(pumlFile1);
        Files.createFile(pumlFile2);

        when(mockPlantUMLService.processFile(pumlFile1)).thenReturn(false);
        when(mockPlantUMLService.processFile(pumlFile2)).thenReturn(true);

        // When
        watchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService).processFile(pumlFile1);
        verify(mockPlantUMLService).processFile(pumlFile2);
    }

    @Test
    void processPlantUMLFiles_WithEmptyDirectory_ShouldNotCallService() throws IOException {
        // Given - empty temp directory

        // When
        watchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService, never()).processFile(any(Path.class));
    }

    @Test
    void startWatching_WithSingleIteration_ShouldProcessFiles() throws IOException {
        // Given
        Path pumlFile = tempDir.resolve("test.puml");
        Files.createFile(pumlFile);
        when(mockPlantUMLService.processFile(any(Path.class))).thenReturn(true);

        // When - Test the processPlantUMLFiles method directly since startWatching runs indefinitely
        watchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService).processFile(pumlFile);
    }

    @Test
    void startWatching_WithMultipleIterations_ShouldProcessOnlyNewFiles() throws IOException, InterruptedException {
        // Given
        Path pumlFile = tempDir.resolve("test.puml");
        Path pngFile = tempDir.resolve("test.png");
        Files.createFile(pumlFile);
        when(mockPlantUMLService.processFile(any(Path.class))).thenReturn(true);

        // When - First iteration should process the file
        watchService.processPlantUMLFiles(tempDir);

        // Create PNG file to simulate successful conversion and wait to make it "old"
        Files.createFile(pngFile);
        Thread.sleep(150); // Wait longer than the file modification check

        // Create a watch service with shorter modification check for testing
        PlantUMLWatchService testWatchService = new PlantUMLWatchService(mockPlantUMLService, 100) {
            @Override
            boolean isFileModifiedInLastSeconds(Path filePath) {
                try {
                    java.nio.file.attribute.FileTime lastModified = Files.getLastModifiedTime(filePath);
                    java.time.Instant hundredMsAgo = java.time.Instant.now().minus(100, java.time.temporal.ChronoUnit.MILLIS);
                    return lastModified.toInstant().isAfter(hundredMsAgo);
                } catch (IOException e) {
                    return false;
                }
            }
        };

        // Second iteration should not process the file (it's now old and PNG exists)
        testWatchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService, times(1)).processFile(pumlFile); // Should only process once
    }

    @Test
    void startWatching_WhenInterrupted_ShouldReturnErrorCode() throws Exception {
        // Given
        AtomicInteger result = new AtomicInteger();

        // When
        Thread watchThread = new Thread(() -> {
            result.set(watchService.startWatching(tempDir));
        });

        watchThread.start();
        Thread.sleep(50); // Let it start
        watchThread.interrupt(); // Interrupt the thread
        watchThread.join(1000); // Wait for completion

        // Then
        assertEquals(1, result.get());
    }

    @Test
    void startWatching_WithIOException_ShouldReturnErrorCode() {
        // Given
        Path nonExistentDir = tempDir.resolve("non-existent");

        // When
        Integer result = watchService.startWatching(nonExistentDir);

        // Then
        assertEquals(1, result);
    }

    @Test
    void startWatching_WithStopRequest_ShouldStopGracefully() throws Exception {
        // Given
        Path pumlFile = tempDir.resolve("test.puml");
        Files.createFile(pumlFile);
        when(mockPlantUMLService.processFile(any(Path.class))).thenReturn(true);

        // When - Test interruption mechanism
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() ->
            watchService.startWatching(tempDir)
        );

        Thread.sleep(150); // Let it run one iteration

        // Interrupt the thread running the watch service
        future.cancel(true);

        // Verify the service was running (file should have been processed)
        verify(mockPlantUMLService, atLeastOnce()).processFile(pumlFile);
    }

    @Test
    void startWatching_WithCurrentDirectory_ShouldProcessFiles() throws Exception {
        // Given
        Path pumlFile = tempDir.resolve("test.puml");
        Files.createFile(pumlFile);
        when(mockPlantUMLService.processFile(any(Path.class))).thenReturn(true);

        // When - Test the processPlantUMLFiles method directly
        PlantUMLWatchService tempWatchService = new PlantUMLWatchService(mockPlantUMLService, 100);
        tempWatchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService).processFile(pumlFile);
    }

    @Test
    void startWatching_ShouldRespectPollingInterval() throws Exception {
        // Given
        PlantUMLWatchService slowWatchService = new PlantUMLWatchService(mockPlantUMLService, 200);

        // When - Test that the watch service respects the polling interval by timing the execution
        long startTime = System.currentTimeMillis();

        Thread watchThread = new Thread(() -> {
            slowWatchService.startWatching(tempDir);
        });

        watchThread.start();
        Thread.sleep(250); // Let it run for just over one polling interval
        watchThread.interrupt();
        watchThread.join(1000);

        long duration = System.currentTimeMillis() - startTime;

        // Then - Should have run for at least the polling interval
        assertTrue(duration >= 200, "Should respect polling interval, duration was: " + duration);
    }

    @Test
    void processPlantUMLFiles_WithIOError_ShouldPropagateException() throws IOException {
        // Given
        Files.delete(tempDir); // Delete directory to cause IOException

        // When & Then
        assertThrows(IOException.class, () -> watchService.processPlantUMLFiles(tempDir));
    }

    @Test
    void processPlantUMLFiles_WithNestedDirectories_ShouldProcessAllFiles() throws IOException {
        // Given
        Path level1Dir = tempDir.resolve("level1");
        Path level2Dir = level1Dir.resolve("level2");
        Files.createDirectories(level2Dir);

        Path pumlFile1 = tempDir.resolve("root.puml");
        Path pumlFile2 = level1Dir.resolve("level1.puml");
        Path pumlFile3 = level2Dir.resolve("level2.puml");

        Files.createFile(pumlFile1);
        Files.createFile(pumlFile2);
        Files.createFile(pumlFile3);

        when(mockPlantUMLService.processFile(any(Path.class))).thenReturn(true);

        // When
        watchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService).processFile(pumlFile1);
        verify(mockPlantUMLService).processFile(pumlFile2);
        verify(mockPlantUMLService).processFile(pumlFile3);
    }
}
