# Sequence Diagram: PlantUML to PNG CLI Tool - Conversion Workflow

## Overview
This sequence diagram illustrates the complete workflow for converting PlantUML (.puml) files to PNG format using a command-line interface tool as described in the following agile artifacts:

- **Epic:** EPIC-001_PlantUML_to_PNG_CLI.md
- **Feature:** FEAT-001_PlantUML_to_PNG_CLI.md
- **User Story:** US-001_JBang_PlantUML_CLI.md
- **Test Scenarios:** plantuml_png_conversion.feature

## Diagram Elements

### Actors/Participants
- **Developer:** The end user who executes the CLI tool to convert PlantUML files
- **JBang CLI Tool:** The main application that orchestrates the conversion process
- **File System:** Handles file I/O operations for reading .puml files and writing .png files
- **PlantUML Library:** The core library (net.sourceforge.plantuml:plantuml:1.2023.13) that processes PlantUML syntax and generates diagrams
- **Graphviz Engine:** The rendering engine (internal to PlantUML) that creates the actual diagram images

### Key Interactions
1. **CLI Execution:** Developer invokes the tool with a .puml file path as parameter
2. **Input Validation:** Tool validates command line arguments and checks file existence
3. **File Reading:** Tool reads the PlantUML source file content
4. **PlantUML Processing:** Library parses and validates the PlantUML syntax
5. **Diagram Rendering:** Graphviz engine generates the visual diagram
6. **PNG Generation:** Tool creates PNG file with same name in same directory
7. **User Feedback:** Tool provides success confirmation or error messages

## Process Flow Description

The sequence diagram shows the complete workflow starting when a developer executes the CLI tool with a .puml file path. The tool first validates the input parameters and checks if the specified file exists. Upon successful validation, it reads the file content and initializes the PlantUML processor.

The PlantUML library then validates the syntax of the provided content. If the syntax is valid, it requests diagram rendering from the internal Graphviz engine, which returns the rendered diagram data. The CLI tool then generates a PNG file with the same base filename in the same directory as the source file.

The diagram includes comprehensive error handling for various failure scenarios including invalid PlantUML syntax and file system errors.

## Alternative Scenarios

### Error Handling
The diagram includes two main error paths:

1. **Invalid PlantUML Syntax:** When the PlantUML library detects syntax errors, it returns error details to the CLI tool, which then displays appropriate error messages to the developer.

2. **File System Errors:** When file permission issues occur (either reading the source file or writing the output PNG), the file system returns error information that is communicated back to the user.

### Asynchronous Operations
The diagram represents synchronous operations as the CLI tool waits for each step to complete before proceeding to the next step in the conversion workflow.

## Technical Notes

### Technology Stack
- **JBang:** CLI framework for Java-based command line tools
- **PlantUML Library:** net.sourceforge.plantuml:plantuml:1.2023.13
- **Graphviz:** Integrated rendering engine within PlantUML
- **Java:** Runtime environment for the CLI tool

### Data Flow
- **Input:** .puml file path as command line argument
- **Processing:** PlantUML syntax parsing and validation
- **Output:** PNG file with same filename in same directory
- **Feedback:** Success/error messages to user console

### Key Requirements Addressed
- Command-line interface accepting .puml file paths
- Automatic PNG generation with consistent naming
- Same-directory output placement
- Comprehensive error handling and user feedback
- Integration with PlantUML library and Graphviz rendering

## Generating the Visual Diagram

### Using PlantUML Online
1. Copy the contents of `plantuml_cli_conversion_sequence.puml`
2. Go to http://www.plantuml.com/plantuml/uml/
3. Paste the code and generate the diagram

### Using PlantUML CLI
```bash
# Install PlantUML (requires Java)
java -jar plantuml.jar plantuml_cli_conversion_sequence.puml
```

### Using VS Code Extension
1. Install the "PlantUML" extension
2. Open the `.puml` file
3. Use Ctrl+Shift+P and select "PlantUML: Preview Current Diagram"

## Related Documentation
- [Epic: EPIC-001_PlantUML_to_PNG_CLI.md](requirements/EPIC-001_PlantUML_to_PNG_CLI.md)
- [Feature: FEAT-001_PlantUML_to_PNG_CLI.md](requirements/FEAT-001_PlantUML_to_PNG_CLI.md)
- [User Story: US-001_JBang_PlantUML_CLI.md](requirements/US-001_JBang_PlantUML_CLI.md)
- [Test Scenarios: plantuml_png_conversion.feature](requirements/plantuml_png_conversion.feature)

## Maintenance Notes
- Update this diagram when user stories or technical specifications change
- Consider creating separate diagrams for batch processing scenarios (future feature)
- Review and validate the diagram with stakeholders and technical teams
- Keep the diagram synchronized with actual CLI tool implementation
- Update PlantUML library version references as dependencies evolve

## Usage in Development Process
- Use this diagram for technical discussions and architecture reviews
- Reference during code implementation to ensure all interaction points are covered
- Include in technical documentation and developer onboarding materials
- Validate against acceptance criteria defined in the Gherkin scenarios
- Share with QA team for test case development and validation
