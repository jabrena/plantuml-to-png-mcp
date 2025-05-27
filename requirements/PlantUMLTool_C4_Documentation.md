# C4 Model Documentation: PlantUML to PNG CLI Tool

## Overview
This documentation describes the C4 model diagrams for the PlantUML to PNG CLI Tool as derived from the following agile artifacts:

- **Epic:** EPIC-001_PlantUML_to_PNG_CLI.md
- **Feature:** FEAT-001_PlantUML_to_PNG_CLI.md
- **Acceptance Tests:** plantuml_png_conversion.feature
- **Overview Documentation:** plantuml_to_png_conversion_overview_README.md
- **Sequence Diagram:** plantuml_to_png_conversion_overview.puml

## C4 Model Levels

### C4 Model Structure for PlantUML to PNG CLI Tool
- **System**: PlantUML to PNG CLI Tool (the entire application)
- **Containers**: PicoCLI Interface + PlantUML Service (2 containers within the system)
- **External Systems**: File System, Graphviz, GitHub Repository (systems outside our application)

**Note**: For this simple system with only 2 containers, the Container level provides sufficient architectural detail. A Component level diagram would add unnecessary complexity without significant value.

### Level 1: System Context
**File:** `PlantUMLTool_Context.puml`

**Purpose:** Shows the PlantUML to PNG CLI Tool in its environment, focusing on developers and external systems.

**Key Elements:**
- **Developer:** Software developer who creates PlantUML diagrams and needs PNG output for GitHub documentation
- **PlantUML to PNG CLI Tool:** Command-line tool that converts PlantUML files to PNG format with automatic file naming and placement
- **File System:** Local file system where PlantUML source files are stored and PNG outputs are generated
- **Graphviz:** External graph visualization software used by PlantUML for rendering diagrams
- **GitHub Repository:** Version control system where generated PNG diagrams are used in Markdown documentation

**Main Interactions:**
- **Developer to CLI Tool:** Developer executes the CLI tool with .puml file path via command line
- **CLI Tool to File System:** Tool reads .puml files and writes .png files using file I/O operations
- **CLI Tool to Graphviz:** Tool uses Graphviz for diagram rendering through internal PlantUML library
- **Developer to GitHub:** Developer commits generated PNG diagrams using Git

### Level 2: Containers
**File:** `PlantUMLTool_Container.puml`

**Purpose:** Shows the high-level technology choices and how responsibilities are distributed across containers within the CLI tool.

**Key Containers:**
- **PicoCLI Interface** (PicoCLI Framework): Command-line interface using PicoCLI framework that accepts .puml file paths and handles user interaction
- **PlantUML Service** (Java Service): Core service that handles file processing, PlantUML conversion, error handling, and coordinates with Graphviz for PNG generation

**Key Data Flows:**
- **Command Processing Flow:** Developer input flows through PicoCLI Interface to PlantUML Service
- **Unified Processing Flow:** PlantUML Service handles all file operations, conversion, and error management in a single service
- **Rendering Flow:** PlantUML Service coordinates with Graphviz for diagram rendering and file system for I/O operations

### Implementation Guidance

For this simple 2-container system, the Container level provides sufficient architectural guidance for implementation:

**PicoCLI Interface Implementation:**
- Use PicoCLI framework for command-line argument parsing and validation
- Handle user interaction, help text, and command-line options
- Delegate core processing to PlantUML Service
- Manage application exit codes and user feedback

**PlantUML Service Implementation:**
- Implement file reading and validation logic
- Integrate PlantUML Maven dependency (net.sourceforge.plantuml:plantuml:1.2023.13)
- Handle PNG generation and file writing with proper naming
- Provide comprehensive error handling and meaningful error messages
- Coordinate with Graphviz through PlantUML library for diagram rendering

## Architecture Decision Records (ADRs) Derived from Artifacts

### Technology Choices
- **Java Platform:** Chosen for cross-platform compatibility and integration with PlantUML library
- **Maven Build System:** Selected for dependency management and build automation
- **PicoCLI Framework:** Modern CLI framework for robust command-line argument parsing and validation
- **PlantUML Library 1.2023.13:** Specific version chosen for stability and Graphviz integration
- **Simplified Architecture:** Single service approach for easier maintenance and reduced complexity

### Integration Patterns
- **PicoCLI Integration:** Modern CLI framework integration for robust command-line interface
- **File System Integration:** Direct file I/O for reading source .puml files and writing output .png files
- **Maven Dependency Integration:** Internal use of PlantUML library (net.sourceforge.plantuml:plantuml:1.2023.13) as Maven dependency
- **Graphviz Integration:** External dependency on Graphviz for diagram rendering through PlantUML library
- **Unified Service Pattern:** Single PlantUML Service handles all core functionality (file processing, conversion, error handling)

### Data Architecture
- **File-Based Processing:** Input and output are file-based with automatic naming convention (diagram.puml → diagram.png)
- **In-Memory Processing:** PlantUML content is processed in memory during conversion using internal Maven dependency
- **External Rendering:** Graphviz handles the actual diagram rendering as an external system dependency
- **Error State Management:** Comprehensive error handling with appropriate exit codes for automation integration

## Generating Visual Diagrams

### Using PlantUML Online
1. Copy the contents of each `.puml` file
2. Go to http://www.plantuml.com/plantuml/uml/
3. Paste the code and generate the diagram
4. Repeat for both levels (Context and Container)

### Using PlantUML CLI
```bash
# Install PlantUML (requires Java)
# Generate all diagrams
java -jar plantuml.jar PlantUMLTool_Context.puml
java -jar plantuml.jar PlantUMLTool_Container.puml
```

### Using VS Code Extension
1. Install the "PlantUML" extension
2. Open each `.puml` file
3. Use Ctrl+Shift+P and select "PlantUML: Preview Current Diagram"

### Using C4-PlantUML with Docker
```bash
# Using the official PlantUML Docker image with C4 support
docker run --rm -v $(pwd):/data plantuml/plantuml:latest -tpng /data/PlantUMLTool_*.puml
```

## Related Agile Artifacts
- **EPIC-001_PlantUML_to_PNG_CLI.md:** Defines the business value and strategic goals for automatic PNG generation from PlantUML files
- **FEAT-001_PlantUML_to_PNG_CLI.md:** Describes the detailed feature requirements including CLI interface, file processing, and error handling
- **plantuml_png_conversion.feature:** Provides acceptance criteria and test scenarios for the conversion functionality
- **plantuml_to_png_conversion_overview_README.md:** Documents the high-level sequence flow and system overview
- **plantuml_to_png_conversion_overview.puml:** Illustrates the main conversion process flow

## Architecture Evolution Notes
- **Current State:** This C4 model represents the architecture as described in the current agile artifacts for a single-file conversion CLI tool
- **Future Considerations:** Epic mentions potential for batch processing and CI/CD integration, which could expand the Container level architecture
- **Validation:** Review these diagrams with technical stakeholders and validate against actual implementation during development

## Maintenance Guidelines
1. **Update Triggers:** Update diagrams when new epics, features, or technical decisions are made
2. **Review Cycle:** Review diagrams quarterly or when major features are delivered
3. **Stakeholder Validation:** Share Context diagrams with business stakeholders, Container diagrams with architects and developers
4. **Tool Integration:** Consider integrating these diagrams into your documentation pipeline or architecture decision records

## C4 Model Best Practices Applied
- **Abstraction Levels:** Each level focuses on a different audience and level of detail
- **Technology Focus:** Container level highlights key technology choices (Java, Maven, PicoCLI, PlantUML library)
- **Business Context:** Context level emphasizes business value (GitHub documentation workflow) and user interactions
- **Consistency:** Consistent naming and styling across both levels
- **Documentation:** Each diagram is accompanied by explanatory documentation

## Next Steps
1. **Validate Architecture:** Review diagrams with development teams and stakeholders
2. **Implementation Focus:** Use Container diagram to guide implementation of PicoCLI Interface and PlantUML Service
3. **Add Deployment View:** Consider creating deployment diagrams showing infrastructure and runtime environment
4. **Link to Code:** Connect container responsibilities to actual code structure and packages during implementation
5. **Continuous Updates:** Establish a process for keeping diagrams synchronized with evolving requirements

## Usage Guidelines

### **Context Diagrams**
Share with business stakeholders, product managers, and non-technical team members to communicate:
- Overall system purpose and business value
- Key user interactions and external system dependencies
- High-level integration points

### **Container Diagrams**
Use for architecture reviews, technical planning, and technology decisions to show:
- Application structure and technology choices
- Container responsibilities and interactions
- Integration patterns with external systems

### **Implementation Guidance**
For this simple system, the Container diagram provides sufficient detail for:
- Understanding the separation of concerns between CLI and business logic
- Technology choices and integration patterns
- Implementation guidance for developers
- Code organization and package structure

## Integration with Agile Process
- Update diagrams when new epics or features significantly change the architecture
- Use diagrams to validate that user stories align with the intended architecture
- Reference diagrams during sprint planning to identify technical dependencies
- Include architecture validation as acceptance criteria for major features
