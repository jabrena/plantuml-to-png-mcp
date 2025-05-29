package info.jab.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import org.jspecify.annotations.Nullable;

class PlantUMLFileValidatorTest {

    @TempDir
    @SuppressWarnings("NullAway.Init")
    Path tempDir;

    private PlantUMLFileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PlantUMLFileValidator();
    }

    @Test
    void validatePlantUMLFile_withValidFile_shouldReturnPath() {
        // Given
        Path validFile = getTestResourcePath("plantuml/hello-world.puml");

        // When
        Optional<Path> result = validator.validatePlantUMLFile(validFile.toString());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(validFile);
    }

    private Path getTestResourcePath(String resourcePath) {
        return Paths.get(Objects.requireNonNull(
            getClass().getClassLoader().getResource(resourcePath)
        ).getPath());
    }

    @Test
    void validatePlantUMLFile_withNullPath_shouldReturnEmpty() {
        // Given
        @Nullable String nullPath = null;

        // When
        Optional<Path> result = validator.validatePlantUMLFile(nullPath);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void validatePlantUMLFile_withEmptyPath_shouldReturnEmpty() {
        // When
        Optional<Path> result = validator.validatePlantUMLFile("");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void validatePlantUMLFile_withWhitespaceOnlyPath_shouldReturnEmpty() {
        // When
        Optional<Path> result = validator.validatePlantUMLFile("   ");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void validatePlantUMLFile_withNonExistentFile_shouldReturnEmpty() {
        // Given
        String nonExistentFile = tempDir.resolve("nonexistent.puml").toString();

        // When
        Optional<Path> result = validator.validatePlantUMLFile(nonExistentFile);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void validatePlantUMLFile_withDirectory_shouldReturnEmpty() throws IOException {
        // Given
        Path directory = tempDir.resolve("testdir");
        Files.createDirectory(directory);

        // When
        Optional<Path> result = validator.validatePlantUMLFile(directory.toString());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void validatePlantUMLFile_withUnreadableFile_shouldReturnEmpty() throws IOException {
        // Given
        Path unreadableFile = tempDir.resolve("unreadable.puml");
        Files.createFile(unreadableFile);

        // Make file unreadable (POSIX systems only)
        Files.setPosixFilePermissions(unreadableFile, Set.of(PosixFilePermission.OWNER_WRITE));

        // When
        Optional<Path> result = validator.validatePlantUMLFile(unreadableFile.toString());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void validatePlantUMLFile_withWrongExtension_shouldReturnEmpty() throws IOException {
        // Given
        Path wrongExtensionFile = tempDir.resolve("test.txt");
        Files.createFile(wrongExtensionFile);

        // When
        Optional<Path> result = validator.validatePlantUMLFile(wrongExtensionFile.toString());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void validatePlantUMLFile_withNoExtension_shouldReturnEmpty() throws IOException {
        // Given
        Path noExtensionFile = tempDir.resolve("test");
        Files.createFile(noExtensionFile);

        // When
        Optional<Path> result = validator.validatePlantUMLFile(noExtensionFile.toString());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void validatePlantUMLFile_withUppercaseExtension_shouldReturnPath() throws IOException {
        // Given
        Path uppercaseFile = tempDir.resolve("test.PUML");
        Files.createFile(uppercaseFile);

        // When
        Optional<Path> result = validator.validatePlantUMLFile(uppercaseFile.toString());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(uppercaseFile);
    }

    @Test
    void validatePlantUMLFile_withMixedCaseExtension_shouldReturnPath() throws IOException {
        // Given
        Path mixedCaseFile = tempDir.resolve("test.Puml");
        Files.createFile(mixedCaseFile);

        // When
        Optional<Path> result = validator.validatePlantUMLFile(mixedCaseFile.toString());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mixedCaseFile);
    }
}
