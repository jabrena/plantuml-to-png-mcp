package info.jab.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility class for loading test resources in unit tests.
 *
 * This class provides helper methods to load PlantUML test files
 * from the test resources directory.
 */
public final class TestResourceLoader {

    private TestResourceLoader() {
        // Utility class - prevent instantiation
    }

    /**
     * Loads a PlantUML resource from the test resources directory and copies it to the target path.
     *
     * @param resourceName The name of the resource file (relative to plantuml/ directory)
     * @param targetPath The target path where the resource should be copied
     * @throws IOException if the resource cannot be loaded or copied
     */
    public static void loadPlantUMLResource(String resourceName, Path targetPath) throws IOException {
        String resourcePath = "plantuml/" + resourceName;

        try (InputStream resourceStream = TestResourceLoader.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (Objects.isNull(resourceStream)) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            Files.copy(resourceStream, targetPath);
        }
    }

    /**
     * Loads a PlantUML test resource content as a string.
     *
     * @param resourceName the name of the resource file (without path)
     * @return the content of the resource file
     * @throws IOException if the resource cannot be loaded
     */
    public static String loadPlantUMLResourceAsString(String resourceName) throws IOException {
        String resourcePath = "/plantuml/" + resourceName;

        try (InputStream inputStream = TestResourceLoader.class.getResourceAsStream(resourcePath)) {
            if (Objects.isNull(inputStream)) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
