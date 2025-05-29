package info.jab.core;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

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
    description = "Convert PlantUML files to PNG format for GitHub documentation",
    mixinStandardHelpOptions = true,
    usageHelpAutoWidth = true
)
public class PlantUMLToPngCli implements Callable<Integer> {

    @Option(
        names = {"-f", "--file"},
        description = "PlantUML file to convert (.puml extension required)",
        required = true
    )
    @SuppressWarnings("NullAway.Init") // TODO: Fix this
    private String inputFile;

    private final PlantUMLFileValidator fileValidator;
    private final PlantUMLService plantUMLService;

    /**
     * Default constructor for CLI usage.
     */
    public PlantUMLToPngCli() {
        this.plantUMLService = new PlantUMLService();
        this.fileValidator = new PlantUMLFileValidator();
    }

    /**
     * Constructor for dependency injection (primarily for testing).
     *
     * @param fileValidator The file validator to use
     * @param plantUMLService The PlantUML service to use
     */
    public PlantUMLToPngCli(PlantUMLFileValidator fileValidator, PlantUMLService plantUMLService) {
        this.fileValidator = fileValidator;
        this.plantUMLService = plantUMLService;
    }

    @Override
    public Integer call() {
        return validateInputFile()
            .flatMap(this::convertToPng)
            .map(success -> success ? 0 : 1)
            .orElse(1);
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
        PlantUMLToPngCli cli = new PlantUMLToPngCli();
        int exitCode = new CommandLine(cli).execute(args);
        System.exit(exitCode);
    }
}
