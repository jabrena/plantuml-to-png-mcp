package info.jab.core;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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

    /**
     * Mutually exclusive operation modes for the CLI tool.
     * Users must specify exactly one of these options.
     */
    @ArgGroup(exclusive = true, multiplicity = "1")
    @SuppressWarnings("NullAway.Init")
    private OperationMode operationMode;

    /**
     * Defines the mutually exclusive operation modes for the CLI.
     * Either convert a single file or watch for changes, but not both.
     */
    static class OperationMode {
        @Option(
            names = {"-f", "--file"},
            description = "PlantUML file to convert (.puml extension required)"
        )
        @SuppressWarnings("NullAway.Init")
        String inputFile;

        @Option(
            names = {"-w", "--watch"},
            description = "Watch for changes in the current directory to convert .puml files automatically"
        )
        boolean watchOption;
    }

    private final PlantUMLFileValidator fileValidator;
    private final PlantUMLService plantUMLService;

    /**
     * Default constructor for CLI usage.
     */
    public PlantUMLToPng() {
        this.plantUMLService = new PlantUMLService();
        this.fileValidator = new PlantUMLFileValidator();
    }

    /**
     * Constructor for dependency injection (primarily for testing).
     *
     * @param fileValidator The file validator to use
     * @param plantUMLService The PlantUML service to use
     */
    public PlantUMLToPng(PlantUMLFileValidator fileValidator, PlantUMLService plantUMLService) {
        this.fileValidator = fileValidator;
        this.plantUMLService = plantUMLService;
    }

    @Override
    public Integer call() {
        if (operationMode.watchOption) {
            return handleWatchMode();
        } else {
            // File option is guaranteed to be present due to ArgGroup multiplicity = "1"
            return handleSingleFileConversion();
        }
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
            System.out.println("Recursively scanning for .puml files in: " + currentDirectory);
            var pumlFiles = listPlantUMLFiles(currentDirectory);

            if (pumlFiles.isEmpty()) {
                System.out.println("No .puml files found in current directory or subdirectories: " + currentDirectory);
            } else {
                System.out.println("Found " + pumlFiles.size() + " .puml file(s):");
                pumlFiles.forEach(file -> {
                    var relativePath = currentDirectory.relativize(file);
                    System.out.println("  - " + relativePath);
                });
            }

            // TODO: Implement file watching functionality
            System.out.println("Directory watch mode not yet implemented");
            return 0;

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
                .collect(Collectors.toList());
        }
    }

    private Optional<Path> validateInputFile() {
        return fileValidator.validatePlantUMLFile(operationMode.inputFile)
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
