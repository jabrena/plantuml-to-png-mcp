# Core Class Diagram - Mermaid Version

This document contains the Mermaid version of the PlantUML to PNG CLI tool core class diagram.

## Class Diagram

```mermaid
classDiagram
    namespace info_jab_core {
        class MainApplication {
            -Logger logger
            -String inputFile
            -String watchDirectory
            -PlantUMLFileValidator fileValidator
            -PlantUMLFileService fileService
            -PlantUMLWatchService watchService
            +MainApplication()
            +MainApplication(fileValidator, fileService, watchService)
            +call() Integer
            +execute() CliResult
            -handleSingleFileConversion(inputFile) CliResult
            -handleWatchMode(parameter) CliResult
            -printBanner() void
            +main(args) void
        }

        class CliResult {
            <<enumeration>>
            OK(0)
            KO(1)
            -int exitCode
            +getExitCode() int
        }

        class PlantUMLFileValidator {
            -String PLANTUML_EXTENSION
            +validatePlantUMLFile(filePath) Optional~Path~
            -isFilePathValid(filePath) boolean
            -isFileExists(path) boolean
            -isRegularFile(path) boolean
            -isReadable(path) boolean
            -hasPlantUMLExtension(path) boolean
        }

        class PlantUMLFileService {
            -Logger logger
            -String DEFAULT_PLANTUML_SERVER
            -PlantUMLHttpClient httpClient
            +PlantUMLFileService()
            +PlantUMLFileService(plantUmlServerUrl)
            +PlantUMLFileService(httpClient)
            +processFile(inputPath) boolean
            +convertToPng(inputPath) Optional~Path~
            -generateOutputPath(inputPath) Path
            -isValidPlantUMLSyntax(plantUMLContent) boolean
            -generatePngData(plantUMLContent) Optional~byte[]~
        }

        class PlantUMLHttpClient {
            -Logger logger
            -Duration HTTP_TIMEOUT
            -String serverUrl
            -HttpClient httpClient
            +PlantUMLHttpClient(serverUrl)
            +generatePngData(plantUMLContent) Optional~byte[]~
            -processHttpResponse(response) Optional~byte[]~
            -processPngData(pngData) Optional~byte[]~
            -encodePlantUMLContent(plantUMLContent) String
            -encodePlantUMLBase64(data) String
            -buildPngUrl(encodedContent) String
            -createHttpRequest(requestUrl) HttpRequest
            +getServerUrl() String
        }

        class PlantUMLWatchService {
            -Logger logger
            -long DEFAULT_POLLING_INTERVAL_MS
            -String PUML_EXTENSION
            -String PNG_EXTENSION
            -PlantUMLFileService plantUMLService
            -long pollingIntervalMs
            +PlantUMLWatchService(plantUMLService)
            +PlantUMLWatchService(plantUMLService, pollingIntervalMs)
            +startWatching(watchDirectory) Integer
            -processPlantUMLFiles(directory) void
            -shouldConvertFile(pumlFile) ConversionDecision
            -requiresSynchronization(pumlFile, pngFile) boolean
            -listPlantUMLFiles(directory) List~Path~
            -getPngPath(pumlFile) Path
            -isFileModifiedInLastSeconds(filePath) boolean
            -convertToPng(inputPath) void
        }

        class ConversionDecision {
            +boolean shouldConvert
            +ConversionReason reason
            +convert(reason) ConversionDecision
            +skip() ConversionDecision
            +getReason() String
        }

        class ConversionReason {
            <<enumeration>>
            NO_PNG_EXISTS("no .png exists")
            PUML_RECENTLY_MODIFIED("recently modified .puml file")
            SYNC_REQUIRED("both files recently modified")
            UP_TO_DATE("up to date")
            -String description
            +getDescription() String
        }
    }

    class CommandLine {
        <<external>>
    }

    class Callable {
        <<interface>>
    }

    class HttpClient {
        <<external>>
    }

    class Path {
        <<external>>
    }

    class Logger {
        <<external>>
    }

    %% Relationships
    MainApplication *-- PlantUMLFileValidator : composition
    MainApplication *-- PlantUMLFileService : composition
    MainApplication *-- PlantUMLWatchService : composition
    MainApplication --> CliResult : returns
    MainApplication ..|> Callable : implements

    PlantUMLFileService *-- PlantUMLHttpClient : composition
    PlantUMLFileService ..> Path : uses

    PlantUMLWatchService *-- PlantUMLFileService : composition
    PlantUMLWatchService --> ConversionDecision : creates
    ConversionDecision *-- ConversionReason : composition

    PlantUMLHttpClient *-- HttpClient : composition

    %% External dependencies
    MainApplication ..> CommandLine : uses
    PlantUMLFileValidator ..> Path : uses
    PlantUMLWatchService ..> Path : uses
```

## Architecture Overview

### Core Components

**MainApplication**
- Main entry point for CLI operations
- Implements `Callable` interface for PicoCLI integration
- Coordinates file processing operations
- Handles both single file conversion and watch mode

**PlantUMLFileValidator**
- Validates input files (existence, permissions, .puml extension)
- Returns `Optional<Path>` for validated files

**PlantUMLFileService**
- Core business logic for PlantUML to PNG conversion
- Delegates HTTP operations to `PlantUMLHttpClient`
- Handles file reading, syntax validation, and output generation

**PlantUMLHttpClient**
- Manages HTTP communication with PlantUML server
- Implements custom PlantUML encoding (deflate compression + custom base64)
- Handles HTTP timeout and error scenarios

**PlantUMLWatchService**
- Polling-based file watcher for automatic conversion
- Uses `ConversionDecision` pattern to determine when conversion is needed
- Supports configurable polling intervals

### Supporting Types

**CliResult**
- Enumeration for command execution results (OK=0, KO=1)

**ConversionDecision & ConversionReason**
- Encapsulates logic for determining when files should be converted
- Reasons include: no PNG exists, recently modified PUML, synchronization required, etc.

### External Dependencies

- **PicoCLI**: Command line parsing and execution framework
- **Java HTTP Client**: For HTTP communication with PlantUML server
- **SLF4J**: Logging framework
- **Java NIO**: File system operations

## Usage

This diagram represents the architecture of a CLI tool that:
1. Accepts `.puml` files as input
2. Validates and processes them
3. Sends content to a PlantUML server for PNG generation
4. Saves the resulting PNG files locally
5. Optionally watches directories for automatic conversion
