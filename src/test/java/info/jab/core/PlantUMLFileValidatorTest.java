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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Path validFile = getTestResourcePath("plantuml/valid-simple.puml");

        // When
        Path result = validator.validatePlantUMLFile(validFile.toString());

        // Then
        assertThat(result).isEqualTo(validFile);
    }

    private Path getTestResourcePath(String resourcePath) {
        return Paths.get(Objects.requireNonNull(
            getClass().getClassLoader().getResource(resourcePath)
        ).getPath());
    }

    @Test
    void validatePlantUMLFile_withNullPath_shouldThrowException() {
        // Given
        @Nullable String nullPath = null;

        // When & Then
        assertThatThrownBy(() -> validator.validatePlantUMLFile(nullPath))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File path cannot be empty");
    }

    @Test
    void validatePlantUMLFile_withEmptyPath_shouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> validator.validatePlantUMLFile(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File path cannot be empty");
    }

    @Test
    void validatePlantUMLFile_withWhitespaceOnlyPath_shouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> validator.validatePlantUMLFile("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File path cannot be empty");
    }

    @Test
    void validatePlantUMLFile_withNonExistentFile_shouldThrowException() {
        // Given
        String nonExistentFile = tempDir.resolve("nonexistent.puml").toString();

        // When & Then
        assertThatThrownBy(() -> validator.validatePlantUMLFile(nonExistentFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File does not exist: " + nonExistentFile);
    }

    @Test
    void validatePlantUMLFile_withDirectory_shouldThrowException() throws IOException {
        // Given
        Path directory = tempDir.resolve("testdir");
        Files.createDirectory(directory);

        // When & Then
        assertThatThrownBy(() -> validator.validatePlantUMLFile(directory.toString()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Path is not a regular file: " + directory.toString());
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void validatePlantUMLFile_withUnreadableFile_shouldThrowException() throws IOException {
        // Given
        Path unreadableFile = tempDir.resolve("unreadable.puml");
        Files.createFile(unreadableFile);

        // Make file unreadable (POSIX systems only)
        Files.setPosixFilePermissions(unreadableFile, Set.of(PosixFilePermission.OWNER_WRITE));

        // When & Then
        assertThatThrownBy(() -> validator.validatePlantUMLFile(unreadableFile.toString()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File is not readable: " + unreadableFile.toString());
    }

    @Test
    void validatePlantUMLFile_withWrongExtension_shouldThrowException() throws IOException {
        // Given
        Path wrongExtensionFile = tempDir.resolve("test.txt");
        Files.createFile(wrongExtensionFile);

        // When & Then
        assertThatThrownBy(() -> validator.validatePlantUMLFile(wrongExtensionFile.toString()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File must have .puml extension: " + wrongExtensionFile.toString());
    }

    @Test
    void validatePlantUMLFile_withNoExtension_shouldThrowException() throws IOException {
        // Given
        Path noExtensionFile = tempDir.resolve("test");
        Files.createFile(noExtensionFile);

        // When & Then
        assertThatThrownBy(() -> validator.validatePlantUMLFile(noExtensionFile.toString()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File must have .puml extension: " + noExtensionFile.toString());
    }

    @Test
    void validatePlantUMLFile_withUppercaseExtension_shouldReturnPath() throws IOException {
        // Given
        Path uppercaseFile = tempDir.resolve("test.PUML");
        Files.createFile(uppercaseFile);

        // When
        Path result = validator.validatePlantUMLFile(uppercaseFile.toString());

        // Then
        assertThat(result).isEqualTo(uppercaseFile);
    }

    @Test
    void validatePlantUMLFile_withMixedCaseExtension_shouldReturnPath() throws IOException {
        // Given
        Path mixedCaseFile = tempDir.resolve("test.Puml");
        Files.createFile(mixedCaseFile);

        // When
        Path result = validator.validatePlantUMLFile(mixedCaseFile.toString());

        // Then
        assertThat(result).isEqualTo(mixedCaseFile);
    }


}
