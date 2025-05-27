package info.jab.core;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;

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
    private final GraphvizValidator graphvizValidator;
    private final PlantUMLService plantUMLService;

    /**
     * Default constructor for CLI usage.
     */
    public PlantUMLToPngCli() {
        this.plantUMLService = new PlantUMLService();
        this.graphvizValidator = new GraphvizValidator();
        this.fileValidator = new PlantUMLFileValidator();
    }

    /**
     * Constructor for dependency injection (primarily for testing).
     *
     * @param fileValidator The file validator to use
     * @param graphvizValidator The Graphviz validator to use
     * @param plantUMLService The PlantUML service to use
     */
    public PlantUMLToPngCli(PlantUMLFileValidator fileValidator, GraphvizValidator graphvizValidator, PlantUMLService plantUMLService) {
        this.fileValidator = fileValidator;
        this.graphvizValidator = graphvizValidator;
        this.plantUMLService = plantUMLService;
    }

    @Override
    public Integer call() {
        // TODO Refactor to be more functional the validation process.

        // Validate input file
        Path inputPath = fileValidator.validatePlantUMLFile(inputFile);

        // Validate Graphviz availability
        if (!graphvizValidator.isGraphvizAvailable()) {
            System.err.println("Error: Graphviz is not available. Please install Graphviz to use this tool.");
            return 1;
        }

        // Convert PlantUML to PNG
        return plantUMLService.convertToPng(inputPath)
            .map(outputPath -> {
                System.out.println("Successfully converted: " + inputPath + " -> " + outputPath);
                return 0;
            })
            .orElseGet(() -> {
                System.err.println("Error: Failed to convert PlantUML file. Please check the file format and content.");
                return 1;
            });
    }

    /**
     * Main method for CLI execution.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        PlantUMLToPngCli cli = new PlantUMLToPngCli();
        int exitCode = new CommandLine(cli).execute(args);
        System.exit(exitCode);
    }
}
