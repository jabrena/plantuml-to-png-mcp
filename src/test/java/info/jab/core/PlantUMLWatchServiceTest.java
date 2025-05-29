package info.jab.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    void processPlantUMLFiles_WithExistingPngFile_ShouldNotConvert() throws IOException {
        // Given
        Path pumlFile = tempDir.resolve("existing.puml");
        Path pngFile = tempDir.resolve("existing.png");
        Files.createFile(pumlFile);
        Files.createFile(pngFile); // PNG already exists

        // When
        watchService.processPlantUMLFiles(tempDir);

        // Then
        verify(mockPlantUMLService, never()).processFile(pumlFile);
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
        AtomicInteger iterations = new AtomicInteger(0);

        // When - Use custom condition to run only one iteration
        Integer result = watchService.startWatching(tempDir, () -> iterations.incrementAndGet() <= 1);

        // Then
        assertEquals(0, result);
        verify(mockPlantUMLService).processFile(pumlFile);
    }

    @Test
    void startWatching_WithMultipleIterations_ShouldProcessOnlyNewFiles() throws IOException {
        // Given
        Path pumlFile = tempDir.resolve("test.puml");
        Files.createFile(pumlFile);
        when(mockPlantUMLService.processFile(any(Path.class))).thenReturn(true);
        AtomicInteger iterations = new AtomicInteger(0);

        // When - Run first iteration, then create PNG file, then run second iteration
        Integer result = watchService.startWatching(tempDir, () -> {
            int currentIteration = iterations.incrementAndGet();
            if (currentIteration == 2) {
                try {
                    // Create PNG file after first iteration to simulate successful conversion
                    Files.createFile(tempDir.resolve("test.png"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return currentIteration <= 2;
        });

        // Then
        assertEquals(0, result);
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
    void startWatching_WithIOException_ShouldReturnErrorCode() throws Exception {
        // Given
        Path nonExistentDir = tempDir.resolve("non-existent");
        Files.delete(tempDir); // Delete the temp directory to cause IOException

        // When
        Integer result = watchService.startWatching(nonExistentDir, () -> true);

        // Then
        assertEquals(1, result);
    }

    @Test
    void startWatching_WithStopRequest_ShouldStopGracefully() throws Exception {
        // Given
        Path pumlFile = tempDir.resolve("test.puml");
        Files.createFile(pumlFile);
        when(mockPlantUMLService.processFile(any(Path.class))).thenReturn(true);

        // When
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() ->
            watchService.startWatching(tempDir)
        );

        Thread.sleep(150); // Let it run one iteration
        watchService.stopWatching(); // Request stop
        Integer result = future.get(1, TimeUnit.SECONDS);

        // Then
        assertEquals(0, result);
        verify(mockPlantUMLService, atLeastOnce()).processFile(pumlFile);
    }

    @Test
    void startWatching_WithCurrentDirectory_ShouldProcessFiles() throws Exception {
        // Given
        String originalUserDir = System.getProperty("user.dir");
        Path pumlFile = tempDir.resolve("test.puml");
        Files.createFile(pumlFile);
        when(mockPlantUMLService.processFile(any(Path.class))).thenReturn(true);

        try {
            // When
            System.setProperty("user.dir", tempDir.toString());
            AtomicInteger iterations = new AtomicInteger(0);

            PlantUMLWatchService tempWatchService = new PlantUMLWatchService(mockPlantUMLService, 100);
            Integer result = tempWatchService.startWatching(tempDir, () -> iterations.incrementAndGet() <= 1);

            // Then
            assertEquals(0, result);
            verify(mockPlantUMLService).processFile(pumlFile);
        } finally {
            // Restore original directory
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void startWatching_ShouldRespectPollingInterval() throws Exception {
        // Given
        PlantUMLWatchService slowWatchService = new PlantUMLWatchService(mockPlantUMLService, 200);
        Path pumlFile = tempDir.resolve("test.puml");
        Files.createFile(pumlFile);

        // When - Measure time for exactly 2 polling cycles
        long startTime = System.currentTimeMillis();
        AtomicInteger iterationCount = new AtomicInteger(0);

        slowWatchService.startWatching(tempDir, () -> {
            int currentCount = iterationCount.get();
            if (currentCount < 2) {
                iterationCount.incrementAndGet();
                return true;
            }
            return false;
        });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then - Should take at least 200ms (one polling interval between iterations)
        assertTrue(duration >= 200, "Should respect polling interval, duration was: " + duration);
        assertEquals(2, iterationCount.get(), "Should complete exactly 2 iterations");
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
