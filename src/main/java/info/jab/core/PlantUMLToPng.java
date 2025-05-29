package info.jab.core;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import com.diogonunes.jcolor.Attribute;
import static com.diogonunes.jcolor.Ansi.colorize;
import com.github.lalyos.jfiglet.FigletFont;

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
    private final PlantUMLWatchService watchService;

    /**
     * Default constructor for CLI usage.
     */
    public PlantUMLToPng() {
        this.fileValidator = new PlantUMLFileValidator();
        this.plantUMLService = new PlantUMLFileService();
        this.watchService = new PlantUMLWatchService(plantUMLService);
    }

    /**
     * Constructor for dependency injection (primarily for testing).
     *
     * @param plantUMLService The PlantUML service to use
     * @param watchService The watch service to use
     * @param fileValidator The file validator to use
     */
    public PlantUMLToPng(PlantUMLFileValidator fileValidator, PlantUMLFileService plantUMLService, PlantUMLWatchService watchService) {
        this.fileValidator = fileValidator;
        this.plantUMLService = plantUMLService;
        this.watchService = watchService;
    }

    @Override
    public Integer call() {
        CliResult result = execute();
        return result.getExitCode();
    }

    /**
     * Executes the CLI logic and returns the result.
     *
     * @return CliResult indicating success or failure
     */
    public CliResult execute() {
        if (Objects.nonNull(inputFile)) {
            return handleSingleFileConversion(inputFile);
        }

        if (watchOption) {
            return handleWatchMode();
        }

        System.out.println("Use --help to see available options.");
        return CliResult.KO;
    }

    private CliResult handleSingleFileConversion(String inputFile) {
        if (Objects.isNull(inputFile)) {
            System.err.println("Error: No input file specified. Use -f or --file option.");
            return CliResult.KO;
        }

        // Validate input file using PlantUMLFileValidator
        Optional<Path> validatedPath = fileValidator.validatePlantUMLFile(inputFile);
        if (validatedPath.isEmpty()) {
            System.err.println("Invalid PlantUML file: " + inputFile);
            return CliResult.KO;
        }

        // Use the validated Path for processing
        if (plantUMLService.processFile(validatedPath.get())) {
            return CliResult.OK;
        } else {
            return CliResult.KO;
        }
    }

    private CliResult handleWatchMode() {
        int watchResult = watchService.startWatching();
        return (watchResult == 0) ? CliResult.OK : CliResult.KO;
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
