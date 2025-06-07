# Core Package Class Diagram

## Overview

This document provides a detailed analysis of the `info.jab.core` package structure through a comprehensive UML class diagram. The core package implements a PlantUML to PNG conversion tool with both single file conversion and directory watching capabilities.

## Architecture Overview

The core package follows several key architectural patterns:

### 1. **Command Pattern with PicoCLI Integration**
- `MainApplication` serves as the command handler implementing `Callable<Integer>`
- Uses PicoCLI annotations for command-line argument processing
- Separates CLI concerns from business logic

### 2. **Service Layer Pattern**
- `PlantUMLFileService` - Core business logic for file conversion
- `PlantUMLFileValidator` - Input validation and file system checks
- `PlantUMLWatchService` - Directory monitoring and automated processing
- `PlantUMLHttpClient` - HTTP communication with PlantUML servers

### 3. **Dependency Injection**
- Constructor-based dependency injection throughout
- Facilitates testing and loose coupling
- Default constructors for CLI usage, parameterized for testing

### 4. **Value Objects and Enums**
- `CliResult` enum for standardized exit codes
- `ConversionDecision` record for conversion logic decisions
- `ConversionReason` enum for decision justification

## Key Components

### MainApplication
**Role**: Application entry point and CLI orchestrator
- **Responsibilities**:
  - CLI argument parsing and validation
  - Coordinating between validator, file service, and watch service
  - Banner display and application lifecycle management
- **Pattern**: Command pattern implementation
- **Dependencies**: All core services (composition relationships)

### PlantUMLFileService
**Role**: Core business logic for PlantUML conversion
- **Responsibilities**:
  - File content validation (PlantUML syntax)
  - PNG generation coordination
  - Output path management
- **Pattern**: Service layer with dependency injection
- **Key Methods**: `processFile()`, `convertToPng()`, `generatePngData()`

### PlantUMLHttpClient
**Role**: HTTP communication with PlantUML servers
- **Responsibilities**:
  - Custom PlantUML encoding (deflate + custom base64)
  - HTTP request/response handling
  - Error handling and timeout management
- **Notable Features**:
  - Custom base64 encoding for PlantUML compatibility
  - Functional approach with switch expressions
  - Comprehensive error handling

### PlantUMLFileValidator
**Role**: Input validation and file system verification
- **Responsibilities**:
  - File path validation
  - File existence and permissions checking
  - PlantUML extension validation
- **Return Pattern**: Uses `Optional<Path>` for safe validation results

### PlantUMLWatchService
**Role**: Directory monitoring and automated conversion
- **Responsibilities**:
  - Polling-based file watching
  - Intelligent conversion decisions
  - File modification tracking
- **Key Features**:
  - Configurable polling intervals
  - Smart conversion logic (avoids unnecessary conversions)
  - Inner classes for decision modeling

## Class Relationships

### Composition Relationships
- `MainApplication` → `PlantUMLFileValidator`, `PlantUMLFileService`, `PlantUMLWatchService`
- `PlantUMLFileService` → `PlantUMLHttpClient`
- `PlantUMLWatchService` → `PlantUMLFileService`
- `ConversionDecision` → `ConversionReason`

### Dependency Relationships
- `MainApplication` → `CliResult`, `CommandLine`, `GitInfo`
- `PlantUMLFileService` → `CliResult`

### Inner Class Relationships
- `PlantUMLWatchService` contains `ConversionDecision` (record)
- `PlantUMLWatchService` contains `ConversionReason` (enum)

## Design Patterns Identified

### 1. **Null Object Pattern**
- Extensive use of `Optional<T>` to avoid null pointer exceptions
- Null-safe operations throughout the codebase

### 2. **Strategy Pattern (Implicit)**
- Different conversion strategies based on file states
- `ConversionDecision` encapsulates the decision logic

### 3. **Template Method Pattern**
- `PlantUMLWatchService.processPlantUMLFiles()` defines the algorithm structure
- Specific decision logic in `shouldConvertFile()`

### 4. **Builder Pattern (External)**
- PicoCLI uses builder pattern for command configuration
- HttpClient uses builder pattern for client configuration

## Key Design Decisions

### 1. **Immutable Records for Decisions**
- `ConversionDecision` record provides immutable decision representation
- Factory methods for common decision types

### 2. **Functional Programming Elements**
- Switch expressions in HTTP response processing
- Stream API usage in file listing
- Optional chaining for null safety

### 3. **Separation of Concerns**
- Clear boundaries between validation, processing, and communication
- Each service has a single, well-defined responsibility

### 4. **Error Handling Strategy**
- Boolean returns for success/failure operations
- Optional returns for values that might not exist
- Centralized logging throughout the application

## Testing Considerations

The architecture supports testing through:
- **Constructor injection**: All dependencies can be mocked
- **Package-private methods**: Internal logic can be tested in isolation
- **Pure functions**: Many methods have no side effects
- **Clear return types**: Easy to verify expected outcomes

## Extension Points

The current design allows for easy extension:
- **New conversion formats**: Extend `PlantUMLFileService`
- **Different validation rules**: Extend `PlantUMLFileValidator`
- **Alternative watch strategies**: Implement new watch service
- **Multiple server support**: Extend `PlantUMLHttpClient`

## File Location

The PlantUML source for this diagram is located at:
- `docs/requirements/design/uml/core-package-class-diagram.puml`

To generate the PNG diagram, use the PlantUML CLI or any PlantUML-compatible tool.
