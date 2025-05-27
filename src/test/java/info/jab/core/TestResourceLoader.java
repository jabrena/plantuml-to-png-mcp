package info.jab.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility class for loading test resources.
 */
public final class TestResourceLoader {

    private TestResourceLoader() {
        // Utility class
    }

    /**
     * Loads a PlantUML test resource and writes it to a temporary file.
     *
     * @param resourceName the name of the resource file (without path)
     * @param targetPath   the target path where to write the content
     * @throws IOException if the resource cannot be loaded or written
     */
    public static void loadPlantUMLResource(String resourceName, Path targetPath) throws IOException {
        String resourcePath = "/plantuml/" + resourceName;

        try (InputStream inputStream = TestResourceLoader.class.getResourceAsStream(resourcePath)) {
            if (Objects.isNull(inputStream)) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Files.writeString(targetPath, content, StandardCharsets.UTF_8);
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
