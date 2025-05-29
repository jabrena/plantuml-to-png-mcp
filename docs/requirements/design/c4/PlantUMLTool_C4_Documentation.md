# PlantUML to PNG CLI Tool - C4 Architecture Documentation

This document describes the architecture of the PlantUML to PNG CLI Tool using the C4 model. The diagrams have been updated to accurately reflect the final implementation.

## System Overview

The PlantUML to PNG CLI Tool is a command-line application that converts PlantUML diagram files (.puml) to PNG format using HTTP requests to a PlantUML server. The tool supports both single file conversion and watch mode for automatic processing.

## Architecture Diagrams

### 1. System Context Diagram

**File:** `PlantUMLTool_Context.puml`

The System Context diagram shows the high-level interactions between the main system and external entities:

- **Developer**: Creates PlantUML diagrams and executes the CLI tool
- **PlantUML to PNG CLI Tool**: The main system that processes conversions
- **File System**: Local storage for .puml source files and .png output files
- **PlantUML Server**: HTTP-based rendering service (default: plantuml.com)
- **GitHub Repository**: Version control system where generated PNGs are used

**Key Implementation Details Reflected:**
- HTTP-based communication with PlantUML server (not local Graphviz)
- Support for both single file and watch mode operations
- Direct file I/O operations for reading .puml and writing .png files

### 2. Container Diagram

**File:** `PlantUMLTool_Container.puml`

The Container diagram breaks down the main system into logical containers:

- **CLI Interface**: PicoCLI-based command-line interface
- **File Validator**: Validates PlantUML file paths and properties
- **PlantUML File Service**: Core business logic for file processing
- **PlantUML HTTP Client**: Specialized HTTP client for server communication
- **Watch Service**: Directory monitoring for automatic conversion

**Key Implementation Details Reflected:**
- Separation of concerns with dedicated validator and HTTP client components
- PicoCLI framework for command-line argument handling
- HTTP-based architecture instead of local processing
- Watch service for directory monitoring functionality

### 3. Component Diagram

**File:** `PlantUMLTool_Component.puml`

The Component diagram shows the detailed internal structure of the system:

**Components by Layer:**

**CLI Layer:**
- `PlantUMLToPng`: Main PicoCLI command class with @Command annotations
- `CliResult`: Enumeration for exit codes (OK=0, KO=1)

**Validation Layer:**
- `PlantUMLFileValidator`: File path and property validation

**Core Processing Layer:**
- `PlantUMLFileService`: Core business logic and workflow orchestration

**HTTP Communication Layer:**
- `PlantUMLHttpClient`: HTTP client with custom PlantUML encoding

**Watch Mode Layer:**
- `PlantUMLWatchService`: Polling-based directory monitoring

**Key Implementation Details Reflected:**
- Actual class names and responsibilities from the codebase
- Method-level interactions between components
- Custom PlantUML encoding implementation in HTTP client
- Polling-based watch service architecture

## Architecture Decisions Reflected

### 1. HTTP-Based Processing
The final implementation uses HTTP requests to a PlantUML server rather than local Graphviz processing. This is reflected in:
- Removal of Graphviz dependency from Context diagram
- Addition of PlantUML Server as external system
- PlantUMLHttpClient component for server communication

### 2. Custom Encoding Implementation
The HTTP client implements PlantUML's custom encoding scheme:
- Deflate compression
- Custom base64 encoding with character set: "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"
- URL-safe encoding without padding

### 3. Separation of Concerns
The architecture clearly separates different responsibilities:
- **Validation**: File path and property checking
- **Processing**: Core business logic and workflow
- **Communication**: HTTP client handling
- **Monitoring**: Watch service for directory scanning

### 4. Watch Mode Architecture
The watch service uses a polling-based approach:
- Configurable polling intervals (default: 5 seconds)
- Scans for .puml files without corresponding .png files
- Automatic conversion triggering

## File Relationships

```
PlantUMLToPng (CLI)
├── validates files using → PlantUMLFileValidator
├── processes files using → PlantUMLFileService
└── starts watching using → PlantUMLWatchService

PlantUMLFileService
├── generates PNG using → PlantUMLHttpClient
└── returns results as → CliResult

PlantUMLWatchService
└── processes files using → PlantUMLFileService

PlantUMLHttpClient
└── communicates with → PlantUML Server (External)
```

## Technology Stack

- **CLI Framework**: PicoCLI for command-line interface
- **HTTP Client**: Java 11+ HttpClient for server communication
- **File I/O**: Java NIO for file operations
- **Compression**: Java Deflater for PlantUML encoding
- **Null Safety**: JSpecify annotations for null safety

## Usage Patterns

### Single File Conversion
```bash
plantuml-to-png -f diagram.puml
```

### Watch Mode
```bash
plantuml-to-png -w /path/to/directory
```

## Error Handling

The architecture includes comprehensive error handling:
- File validation at entry point
- HTTP timeout handling in client
- Graceful failure with meaningful exit codes
- Error message propagation to CLI interface

This architecture provides a robust, maintainable, and extensible solution for PlantUML to PNG conversion with both interactive and automated usage patterns.
