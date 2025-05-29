# PlantUML to PNG CLI Tool - Class Diagram Documentation

## Overview

This document provides a comprehensive analysis of the PlantUML to PNG CLI Tool's class structure, based on the UML class diagram generated from the Java source code in `src/main/java/info/jab/core`.

**Diagram File:** `PlantUMLTool_Class_Diagram.puml`

## Architecture Overview

The PlantUML to PNG CLI Tool follows a **layered architecture** with clear separation of concerns:

### **Command Layer**
- **PlantUMLToPngCli**: Main entry point implementing the Command pattern via PicoCLI framework
- Handles user interaction, command-line parsing, and orchestrates the conversion workflow

### **Service Layer**
- **PlantUMLService**: Core business logic for PlantUML to PNG conversion
- Encapsulates file processing, syntax validation, and coordinates with HTTP client for PNG generation

### **HTTP Communication Layer**
- **PlantUMLHttpClient**: Handles HTTP communication with PlantUML servers
- Implements custom PlantUML encoding and manages HTTP requests/responses

### **Validation Layer**
- **PlantUMLFileValidator**: Validates input files (existence, readability, extension)

### **External Dependencies**
- PicoCLI framework for CLI functionality
- Java HTTP Client for server communication
- Java NIO for file operations
- Optional pattern for null safety

## Key Design Patterns

### 1. **Command Pattern**
- **Implementation**: `PlantUMLToPngCli implements Callable<Integer>`
- **Purpose**: Integrates with PicoCLI framework for command execution
- **Benefits**: Clean separation between command parsing and execution logic

### 2. **Service Layer Pattern**
- **Implementation**: `PlantUMLService` as core business logic container
- **Purpose**: Encapsulates conversion operations and file management
- **Benefits**: Testable, reusable business logic independent of CLI concerns

### 3. **HTTP Client Pattern**
- **Implementation**: `PlantUMLHttpClient` as dedicated HTTP communication layer
- **Purpose**: Handles all server communication and PlantUML-specific encoding
- **Benefits**: Separation of network concerns, reusable HTTP logic

### 4. **Validator Pattern**
- **Implementation**: `PlantUMLFileValidator` for input validation
- **Purpose**: Input validation and file system checks
- **Benefits**: Single responsibility, reusable validation logic

### 5. **Dependency Injection**
- **Implementation**: Constructor injection in `PlantUMLToPngCli` and `PlantUMLService`
- **Purpose**: Enables testing with mock dependencies
- **Benefits**: Loose coupling, improved testability

## Class Responsibilities

### PlantUMLToPngCli
**Role**: Command-line interface orchestrator
**Key Responsibilities:**
- Parse command-line arguments using PicoCLI annotations
- Coordinate validation workflow (File → Conversion)
- Handle user feedback and error reporting
- Manage application exit codes

**Key Methods:**
- `call()`: Main execution method implementing Callable interface
- `validateInputFile()`: Validates input file using file validator
- `convertToPng()`: Orchestrates conversion using service layer

### PlantUMLService
**Role**: Core conversion engine
**Key Responsibilities:**
- Read and validate PlantUML file content
- Perform syntax validation (check for @startuml/@enduml tags)
- Coordinate with HTTP client for PNG generation
- Create output files with proper naming convention

**Key Methods:**
- `convertToPng()`: Main conversion workflow
- `isValidPlantUMLSyntax()`: Basic PlantUML syntax validation
- `generatePngData()`: PNG generation via HTTP client delegation
- `createOutputPath()`: Output file path generation logic

### PlantUMLHttpClient
**Role**: HTTP communication specialist
**Key Responsibilities:**
- Manage HTTP communication with PlantUML servers
- Implement custom PlantUML content encoding (deflate + custom base64)
- Handle HTTP request/response lifecycle
- Provide clean API for PNG data generation

**Key Methods:**
- `generatePngData()`: Main HTTP workflow for PNG generation
- `encodePlantUMLContent()`: PlantUML-specific content encoding
- `encodePlantUMLBase64()`: Custom base64 encoding for PlantUML
- `buildPngUrl()`: URL construction for PNG requests
- `createHttpRequest()`: HTTP request configuration

### PlantUMLFileValidator
**Role**: Input file validation specialist
**Key Responsibilities:**
- Validate file path format and existence
- Check file permissions and readability
- Verify correct PlantUML file extension (.puml)
- Return validated Path objects wrapped in Optional

**Key Methods:**
- `validatePlantUMLFile()`: Main validation entry point
- `isFileExists()`, `isRegularFile()`, `isReadable()`: File system checks
- `hasPlantUMLExtension()`: Extension validation

## Relationship Analysis

### **Composition Relationships**
- `PlantUMLToPngCli` **uses** `PlantUMLFileValidator`, `PlantUMLService`
- `PlantUMLService` **uses** `PlantUMLHttpClient`
- Strong ownership - classes manage lifecycle of their dependencies

### **Dependency Relationships**
- `PlantUMLToPngCli` **depends on** PicoCLI framework for annotations and execution
- `PlantUMLHttpClient` **depends on** Java HTTP Client for network communication
- All classes **use** Java NIO Path and Optional for type safety

### **Implementation Relationships**
- `PlantUMLToPngCli` **implements** `Callable<Integer>` for PicoCLI integration

## Key Architectural Decisions

### 1. **HTTP-Based Architecture**
- **Decision**: Use HTTP client to communicate with remote PlantUML servers instead of local PlantUML library
- **Rationale**: Reduces application size, eliminates local dependencies, leverages PlantUML.com infrastructure
- **Impact**: Requires network connectivity, but provides better maintainability and reduced complexity

### 2. **Custom PlantUML Encoding**
- **Decision**: Implement custom base64 encoding specific to PlantUML protocol
- **Rationale**: PlantUML servers require specific encoding format different from standard base64
- **Impact**: Ensures compatibility with PlantUML server infrastructure

### 3. **Separation of HTTP Concerns**
- **Decision**: Create dedicated `PlantUMLHttpClient` class for all HTTP operations
- **Rationale**: Single responsibility principle, testability, reusability
- **Impact**: Clean separation allows independent testing and future enhancements

### 4. **Elimination of Local Dependencies**
- **Decision**: Remove Graphviz validation and local PlantUML library dependencies
- **Rationale**: Simplifies deployment, reduces application size, eliminates system dependency checks
- **Impact**: Cleaner architecture but requires network connectivity

### 5. **Optional Pattern Usage**
- **Decision**: Use Optional<T> for method return types that may fail
- **Rationale**: Explicit null safety, functional programming style
- **Impact**: Reduces null pointer exceptions, improves error handling

### 6. **Constructor Injection**
- **Decision**: Provide constructor for dependency injection alongside default constructor
- **Rationale**: Enables testing with mock objects while maintaining simple CLI usage
- **Impact**: Improved testability without compromising usability

## External Dependencies Integration

### PicoCLI Framework
- **Integration Point**: `@Command` and `@Option` annotations on PlantUMLToPngCli
- **Purpose**: Command-line argument parsing and help generation
- **Benefits**: Robust CLI framework with minimal boilerplate

### Java HTTP Client (java.net.http)
- **Integration Point**: PlantUMLHttpClient for server communication
- **Purpose**: HTTP communication with PlantUML servers
- **Benefits**: Built-in Java 11+ feature, no external dependencies

### Java NIO (java.nio.file.Path)
- **Integration Point**: File operations across all classes
- **Purpose**: Modern file system operations
- **Benefits**: Better error handling, cross-platform compatibility

## HTTP Communication Details

### PlantUML Encoding Process
1. **Content Preparation**: Raw PlantUML text content
2. **UTF-8 Encoding**: Convert to byte array
3. **Deflate Compression**: Apply compression for size reduction
4. **Custom Base64**: Apply PlantUML-specific base64 encoding
5. **URL Construction**: Build request URL with encoded content

### HTTP Request Flow
1. **Encoding**: PlantUML content → Custom encoded string
2. **URL Building**: Server URL + "/png/" + encoded content
3. **Request Creation**: GET request with timeout configuration
4. **Response Processing**: Extract PNG data from response body
5. **Validation**: Check status code and response size

## Testing Considerations

### Testable Design Elements
1. **Constructor Injection**: Enables mock injection for unit testing
2. **Package-Private Methods**: Allows testing of internal logic
3. **Optional Return Types**: Clear success/failure semantics for testing
4. **Separated HTTP Layer**: HTTP communication can be mocked independently
5. **Pure Validation Logic**: File validation can be tested in isolation

### Testing Strategy Alignment
- **Unit Testing**: Each class can be tested independently with mocks
- **Integration Testing**: HTTP client can be tested with test servers
- **End-to-End Testing**: Complete workflow testing via main() method

## Future Extension Points

### Potential Enhancements
1. **Batch Processing**: Add support for multiple file conversion
2. **Configuration**: Add configuration file support for custom server URLs
3. **Output Formats**: Extend beyond PNG to other formats (SVG, PDF)
4. **Caching**: Add local caching of converted diagrams
5. **Offline Mode**: Support for local PlantUML processing when available

### Architecture Flexibility
- **Service Layer**: Can be extended with additional conversion methods
- **HTTP Client**: Can support additional PlantUML server endpoints
- **Validator Pattern**: New validators can be added for different validation concerns
- **Dependency Injection**: New dependencies can be easily integrated

## Error Handling Strategy

### Network Resilience
- **Timeouts**: Configurable HTTP timeouts for server communication
- **Graceful Degradation**: Clear error messages for network failures
- **Optional Pattern**: Non-throwing methods with Optional returns

### File System Robustness
- **Validation**: Comprehensive file validation before processing
- **Error Propagation**: Clear error messages for file system issues
- **Path Safety**: Modern NIO path handling for cross-platform compatibility

## Maintenance Guidelines

### Code Quality
- All classes follow single responsibility principle
- Clear method naming and comprehensive documentation
- Consistent error handling patterns using Optional
- Proper encapsulation with appropriate access modifiers

### Evolution Strategy
- Add new features through service layer extension
- Maintain backward compatibility in CLI interface
- Use dependency injection for new external integrations
- Follow established validation patterns for new input types

## Related Documentation

- **C4 Model**: `PlantUMLTool_C4_Documentation.md` - System and container views
- **Sequence Diagrams**: `plantuml_to_png_conversion_overview.puml` - Process flow
- **ADRs**: Architecture decision records in requirements directory
- **Source Code**: `src/main/java/info/jab/core/` - Implementation details

---

*This class diagram represents the current state of the PlantUML to PNG CLI Tool architecture as of December 2024. The design emphasizes HTTP-based communication, clear separation of concerns, and maintainability while eliminating local system dependencies.*
