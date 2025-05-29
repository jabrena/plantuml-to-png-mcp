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
- Encapsulates file processing, syntax validation, and PNG generation

### **Validation Layer**
- **PlantUMLFileValidator**: Validates input files (existence, readability, extension)
- **GraphvizValidator**: Checks system dependencies (Graphviz availability)

### **External Dependencies**
- PicoCLI framework for CLI functionality
- PlantUML library for diagram conversion
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

### 3. **Validator Pattern**
- **Implementation**: Separate validator classes for different concerns
- **Purpose**: Input validation and system dependency checking
- **Benefits**: Single responsibility, reusable validation logic

### 4. **Dependency Injection**
- **Implementation**: Constructor injection in `PlantUMLToPngCli`
- **Purpose**: Enables testing with mock dependencies
- **Benefits**: Loose coupling, improved testability

## Class Responsibilities

### PlantUMLToPngCli
**Role**: Command-line interface orchestrator
**Key Responsibilities:**
- Parse command-line arguments using PicoCLI annotations
- Coordinate validation workflow (Graphviz → File → Conversion)
- Handle user feedback and error reporting
- Manage application exit codes

**Key Methods:**
- `call()`: Main execution method implementing Callable interface
- `validateGraphviz()`: Ensures Graphviz dependency is available
- `validateInputFile()`: Validates input file using file validator
- `convertToPng()`: Orchestrates conversion using service layer

### PlantUMLService
**Role**: Core conversion engine
**Key Responsibilities:**
- Read and validate PlantUML file content
- Perform syntax validation (check for @startuml/@enduml tags)
- Generate PNG data using PlantUML library
- Create output files with proper naming convention

**Key Methods:**
- `convertToPng()`: Main conversion workflow
- `isValidPlantUMLSyntax()`: Basic PlantUML syntax validation
- `generatePngData()`: PNG generation using PlantUML library
- `createOutputPath()`: Output file path generation logic

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

### GraphvizValidator
**Role**: System dependency checker
**Key Responsibilities:**
- Check Graphviz installation and availability
- Validate that 'dot' command is accessible
- Provide system environment validation

**Key Methods:**
- `isGraphvizAvailable()`: Execute 'dot -V' to check Graphviz

## Relationship Analysis

### **Composition Relationships**
- `PlantUMLToPngCli` **uses** `PlantUMLFileValidator`, `GraphvizValidator`, `PlantUMLService`
- Strong ownership - CLI class manages lifecycle of validator and service instances

### **Dependency Relationships**
- `PlantUMLToPngCli` **depends on** PicoCLI framework for annotations and execution
- `PlantUMLService` **depends on** PlantUML library for conversion functionality
- All classes **use** Java NIO Path and Optional for type safety

### **Implementation Relationships**
- `PlantUMLToPngCli` **implements** `Callable<Integer>` for PicoCLI integration

## Key Architectural Decisions

### 1. **Separation of Concerns**
- **Decision**: Separate validation, service, and CLI concerns into distinct classes
- **Rationale**: Improves testability, maintainability, and follows single responsibility principle
- **Impact**: Clean, focused classes with clear responsibilities

### 2. **Optional Pattern Usage**
- **Decision**: Use Optional<T> for method return types that may fail
- **Rationale**: Explicit null safety, functional programming style
- **Impact**: Reduces null pointer exceptions, improves error handling

### 3. **Constructor Injection**
- **Decision**: Provide constructor for dependency injection alongside default constructor
- **Rationale**: Enables testing with mock objects while maintaining simple CLI usage
- **Impact**: Improved testability without compromising usability

### 4. **Package-Private Methods**
- **Decision**: `generatePngData()` method is package-private in PlantUMLService
- **Rationale**: Allows testing while not exposing internal implementation details
- **Impact**: Better encapsulation with testing accessibility

## External Dependencies Integration

### PicoCLI Framework
- **Integration Point**: `@Command` and `@Option` annotations on PlantUMLToPngCli
- **Purpose**: Command-line argument parsing and help generation
- **Benefits**: Robust CLI framework with minimal boilerplate

### PlantUML Library (net.sourceforge.plantuml:plantuml:1.2023.13)
- **Integration Point**: PlantUMLService.generatePngData() method
- **Purpose**: Core diagram conversion functionality
- **Benefits**: Mature, stable library with Graphviz integration

### Java NIO (java.nio.file.Path)
- **Integration Point**: File operations across all classes
- **Purpose**: Modern file system operations
- **Benefits**: Better error handling, cross-platform compatibility

## Testing Considerations

### Testable Design Elements
1. **Constructor Injection**: Enables mock injection for unit testing
2. **Package-Private Methods**: Allows testing of internal logic
3. **Optional Return Types**: Clear success/failure semantics for testing
4. **Separated Concerns**: Each class can be tested independently

### Testing Strategy Alignment
- **Unit Testing**: Each class can be tested in isolation
- **Integration Testing**: CLI class can be tested with real dependencies
- **End-to-End Testing**: Complete workflow testing via main() method

## Future Extension Points

### Potential Enhancements
1. **Batch Processing**: Add support for multiple file conversion
2. **Configuration**: Add configuration file support for advanced options
3. **Output Formats**: Extend beyond PNG to other formats
4. **Validation**: Enhanced PlantUML syntax validation

### Architecture Flexibility
- **Service Layer**: Can be extended with additional conversion methods
- **Validator Pattern**: New validators can be added for different validation concerns
- **Dependency Injection**: New dependencies can be easily integrated

## Maintenance Guidelines

### Code Quality
- All classes follow single responsibility principle
- Clear method naming and documentation
- Consistent error handling patterns
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

*This class diagram represents the current state of the PlantUML to PNG CLI Tool architecture as of December 2024. The design emphasizes simplicity, testability, and clear separation of concerns while maintaining the flexibility for future enhancements.*
