package info.jab.core;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;

import com.diogonunes.jcolor.Attribute;
import static com.diogonunes.jcolor.Ansi.colorize;
import com.github.lalyos.jfiglet.FigletFont;

import java.util.Optional;

/**
 * PlantUML to PNG CLI Tool
 *
 * A command-line interface tool that converts PlantUML (.puml) files to PNG format,
 * automatically generating PNG images with the same filename in the same directory as the source file.
 */
@Command(
    name = "plantuml-to-png",
    version = "0.1.0",
    description = "Plantuml-to-png is a CLI utility designed to convert PlantUML files (.puml) to PNG format",
    mixinStandardHelpOptions = true,
    usageHelpAutoWidth = true
)
public class PlantUMLToPng implements Callable<Integer> {

    @Option(
        names = {"-f", "--file"},
        description = "PlantUML file to convert (.puml extension required)"
    )
    @Nullable
    String inputFile;

    @Option(
        names = {"-w", "--watch"},
        description = "Watch for changes in the current directory to convert .puml files automatically"
    )
    boolean watchOption;

    private final PlantUMLFileValidator fileValidator;
    private final PlantUMLFileService plantUMLService;

    /**
     * Default constructor for CLI usage.
     */
    public PlantUMLToPng() {
        this.plantUMLService = new PlantUMLFileService();
        this.fileValidator = new PlantUMLFileValidator();
    }

    /**
     * Constructor for dependency injection (primarily for testing).
     *
     * @param fileValidator The file validator to use
     * @param plantUMLService The PlantUML service to use
     */
    public PlantUMLToPng(PlantUMLFileValidator fileValidator, PlantUMLFileService plantUMLService) {
        this.fileValidator = fileValidator;
        this.plantUMLService = plantUMLService;
    }

    @Override
    public Integer call() {
        if (Objects.nonNull(inputFile)) {
            return handleSingleFileConversion();
        }

        if(watchOption) {
            return handleWatchMode();
        }

        System.out.println("Use --help to see available options.");
        return 1;
    }

    private Integer handleSingleFileConversion() {
        return validateInputFile()
            .flatMap(this::convertToPng)
            .map(success -> success ? 0 : 1)
            .orElse(1);
    }

    private Integer handleWatchMode() {
        System.out.println("Starting watch mode in current directory...");

        try {
            var currentDirectory = Path.of(System.getProperty("user.dir"));

            while (true) {
                var pumlFiles = listPlantUMLFiles(currentDirectory);

                if (!pumlFiles.isEmpty()) {
                    pumlFiles.forEach(file -> {
                        var relativePath = currentDirectory.relativize(file);
                        var pngFile = getPngPath(file);
                        var pngExists = Files.exists(pngFile);

                        //Only print and convert if the PNG file does not exist
                        if (!pngExists) {
                            System.out.println("Found: " + relativePath);
                            convertToPng(file);
                        }
                    });
                }

                // Wait 5 seconds before next iteration
                Thread.sleep(5000);
            }

        } catch (InterruptedException e) {
            System.out.println("Watch mode interrupted. Exiting...");
            Thread.currentThread().interrupt();
            return 1;
        } catch (IOException e) {
            System.err.println("Error scanning directory for .puml files: " + e.getMessage());
            return 1;
        }
    }

    private java.util.List<Path> listPlantUMLFiles(Path directory) throws IOException {
        try (var stream = Files.walk(directory)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase(Locale.ENGLISH).endsWith(".puml"))
                .sorted()
                .toList();
        }
    }

    private Path getPngPath(Path pumlFile) {
        var pumlFileName = pumlFile.toString();
        var pngFileName = pumlFileName.substring(0, pumlFileName.length() - 5) + ".png"; // Replace .puml with .png
        return Path.of(pngFileName);
    }

    private Optional<Path> validateInputFile() {
        return fileValidator.validatePlantUMLFile(inputFile)
            .or(() -> {
                System.err.println("Error: Invalid PlantUML file. Please check the file path, extension (.puml), and permissions.");
                return Optional.empty();
            });
    }

    private Optional<Boolean> convertToPng(Path inputPath) {
        return plantUMLService.convertToPng(inputPath)
            .map(outputPath -> {
                System.out.println("Successfully converted: " + inputPath + " -> " + outputPath);
                return true;
            })
            .or(() -> {
                System.err.println("Error: Failed to convert PlantUML file. Please check the file format and content.");
                return Optional.of(false);
            });
    }

    private static void printBanner() {
        try {
            System.out.println();
            String asciiArt = FigletFont.convertOneLine("PlantUML to PNG CLI");
            System.out.println(colorize(asciiArt, Attribute.GREEN_TEXT()));
        } catch (IOException e) {
            System.out.println("Error printing banner: " + e.getMessage());
        }
    }

    /**
     * Main method for CLI execution.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        printBanner();
        PlantUMLToPng cli = new PlantUMLToPng();
        int exitCode = new CommandLine(cli).execute(args);
        System.exit(exitCode);
    }
}
