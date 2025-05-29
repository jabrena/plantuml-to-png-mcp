# ADR-001: PlantUML to PNG CLI Tool - Technology Stack and Architecture

**Date:** 2024-12-19
**Status:** Proposed
**Deciders:** Juan Antonio Breña Moral, Development Team
**Technical Area:** CLI Development

---

## Context

### Business Context
GitHub doesn't automatically render PlantUML (.puml) files in Markdown documentation, requiring developers to manually convert diagrams to PNG format for proper display. This manual process is time-consuming, error-prone, and disrupts the development workflow when updating documentation.

### Technical Context
Current project setup includes:
- **Java 24** with Maven build system
- **Package:** `info.jab.cli.plantuml-to-png` (version 0.1.0)
- **Quality Tools:** Error Prone, NullAway, JUnit 5, JaCoCo coverage (80% minimum)
- **Build Standards:** Strict compiler warnings, dependency convergence enforcement
- **Target Users:** Developers working with PlantUML diagrams in GitHub repositories

### Functional Requirements Summary
- **Primary Goal:** Convert single .puml files to .png format via CLI
- **File Management:** Generate PNG with same filename in same directory as source
- **User Interface:** Simple command-line tool with `--file` argument
- **Error Handling:** Clear error messages for syntax errors, file issues, and Graphviz problems
- **Distribution:** Single JAR executable with JBang support
- **Performance:** Optimized for small diagram files with fast execution

### Problem Statement
Need to implement a simple, reliable CLI tool that converts PlantUML files to PNG format while leveraging existing Java/Maven infrastructure and providing excellent developer experience through clear error handling and easy distribution.

---

## Decision

### Chosen Technology Stack
- **Programming Language:** Java 24 - Leverages existing project setup and team expertise
- **CLI Framework:** PicoCLI - Team's preferred framework for annotation-driven CLI development
- **PlantUML Integration:** net.sourceforge.plantuml:plantuml:1.2023.13 - Stable version with proven reliability
- **Build System:** Maven - Already configured with quality tools and dependency management
- **Testing Framework:** JUnit 5 + JaCoCo - Integrated with existing test infrastructure
- **Distribution:** Single JAR + JBang - Easy execution without complex installation

### Architecture Approach
- **Command Structure:** Simple single-purpose tool (`jbang plantuml-cli.jar --file diagram.puml`)
- **Data Processing:** File-based I/O with direct PlantUML library integration
- **Error Handling:** Comprehensive validation with clear, actionable error messages
- **Dependency Management:** Graphviz availability checking with helpful guidance
- **Output Strategy:** PNG generation in same directory with automatic naming

### Implementation Strategy
1. **Core CLI Setup:** PicoCLI framework with `--file` argument validation
2. **Dependency Validation:** Check Graphviz availability before processing
3. **File Processing:** Input validation, PlantUML syntax checking, and conversion
4. **Output Management:** PNG generation with same base filename in same directory
5. **Error Handling:** Clear messages for all failure scenarios (file not found, syntax errors, Graphviz issues)

---

## Rationale

### Why This Technology Stack
- **Java Choice:** Seamless integration with PlantUML library and existing project infrastructure
- **PicoCLI Selection:** Team familiarity and excellent annotation-driven development experience
- **PlantUML Version 1.2023.13:** Stable, well-tested version with reliable Graphviz integration
- **Single JAR + JBang:** Simplifies distribution and execution for developer users
- **Maven Integration:** Leverages existing build infrastructure and quality tools

### Key Trade-offs Accepted
- **Simplicity vs Flexibility:** Chose single-file processing over batch operations for initial implementation
- **Dependencies vs Control:** Accepted external Graphviz dependency for proven rendering capabilities
- **Distribution vs Performance:** JBang execution overhead acceptable for ease of use
- **Feature Scope vs Delivery:** Focus on core functionality with future CI/CD integration planned

---

## Alternatives Considered

### Alternative 1: Python + Click Framework
**Stack:** Python + Click + plantuml-python wrapper
**Pros:** Rapid development, excellent CLI frameworks, simple scripting
**Cons:** Additional runtime dependency, slower execution, packaging complexity
**Rejection Reason:** Doesn't leverage existing Java infrastructure and team expertise

### Alternative 2: Go + Cobra CLI
**Stack:** Go + Cobra + PlantUML server integration
**Pros:** Fast startup, small binary size, excellent CLI libraries
**Cons:** Requires PlantUML server setup, network dependency, learning curve
**Rejection Reason:** Adds infrastructure complexity and external service dependency

### Alternative 3: Node.js + Commander.js
**Stack:** Node.js + Commander.js + node-plantuml
**Pros:** Fast development, good CLI ecosystem, JSON configuration support
**Cons:** Runtime dependency on Node.js, less mature PlantUML integration
**Rejection Reason:** Additional runtime requirements and team unfamiliarity

---

## Consequences

### Positive Consequences
- **Development Efficiency:** Leverages existing Java/Maven infrastructure and team expertise with PicoCLI
- **User Experience:** Simple, intuitive CLI with clear error messages and easy JBang execution
- **Maintainability:** Clean architecture with single responsibility and well-defined error handling
- **Quality Assurance:** Built-in quality tools (Error Prone, NullAway, JaCoCo) ensure robust implementation
- **Distribution Simplicity:** JBang eliminates installation complexity for end users

### Negative Consequences & Mitigations
- **Java Startup Time:** ~200-500ms startup overhead → **Mitigation:** Acceptable for typical single-file usage patterns
- **Graphviz Dependency:** External dependency required → **Mitigation:** Clear error messages and installation guidance when missing
- **Single File Limitation:** No batch processing initially → **Mitigation:** Future enhancement planned for CI/CD integration

### Technical Debt
- Initial implementation focuses on single-file processing; batch processing will require architecture extension
- Error handling strategy may need refinement based on real-world usage patterns
- Future CI/CD integration may require additional command-line options and configuration

---

## Implementation Guidelines

### Core Functional Requirements Implementation

**Command Structure & Parsing:**
- Framework: PicoCLI with `@Option(names = "--file")` annotation
- Pattern: `jbang plantuml-cli.jar --file <input.puml>`
- Validation: File existence, .puml extension, readability checks

**Dependency Validation:**
- Graphviz Availability: Check system PATH for `dot` command
- Error Guidance: Provide installation instructions when Graphviz missing
- Graceful Failure: Clear error messages with actionable next steps

**Input/Output Processing:**
- File Validation: Verify .puml file exists and is readable
- Syntax Validation: PlantUML syntax checking before conversion attempt
- Output Generation: PNG creation with same base filename in same directory
- Error Reporting: Specific error messages for each failure type

**Error Handling Categories:**
1. **File Errors:** File not found, permission issues, invalid extension
2. **Syntax Errors:** Invalid PlantUML syntax with line number information
3. **Dependency Errors:** Graphviz not available or not working
4. **Conversion Errors:** PlantUML processing failures

### Development Setup
```bash
# Add required dependencies to pom.xml
# - info.picocli:picocli:4.7.5
# - net.sourceforge.plantuml:plantuml:1.2023.13

# Development workflow
mvn clean compile                 # Compile sources
mvn clean test                    # Run unit tests
mvn clean verify -Pjacoco        # Run with coverage
mvn clean package               # Build executable JAR

# JBang execution
jbang target/plantuml-to-png-0.1.0.jar --file example.puml
```

### Testing Strategy
- **Unit Testing:** PicoCLI command parsing, file validation, PlantUML integration components
- **End-to-End Testing:** Complete CLI execution with sample .puml files and error scenarios
- **Error Scenario Testing:** Invalid files, missing Graphviz, syntax errors, permission issues
- **Integration Testing:** Verify PNG output quality and file naming conventions

### Build & Distribution
- **Build System:** Maven with standard packaging (no assembly plugin needed for JBang)
- **JBang Integration:** Standard JAR works directly with JBang execution
- **Installation:** No installation required - direct JAR execution via JBang
- **Updates:** Version management through Maven, semantic versioning

---

## Success Criteria & Monitoring

### Technical Success Metrics
- CLI startup time: < 2 seconds including JBang overhead
- Memory usage: < 256MB for typical .puml files
- Conversion time: < 5 seconds for standard diagrams
- Error rate: < 5% for valid PlantUML files with Graphviz available

### Functional Success Metrics
- User onboarding: Complete first conversion within 2 minutes
- Error clarity: 90% of users can resolve issues from error messages alone
- File handling: 100% success rate for valid .puml files
- Graphviz detection: Clear guidance when dependency missing

### User Acceptance Criteria
- **Single File Conversion:** Successfully converts .puml to .png with same name/directory
- **Error Handling:** Clear, actionable error messages for all failure scenarios
- **Graphviz Integration:** Detects availability and provides guidance when missing
- **JBang Execution:** Works seamlessly with JBang for easy distribution
- **Quality Standards:** Meets existing code quality requirements (80% coverage, Error Prone, NullAway)

### Review Schedule
- **Next Review:** 2024-03-19 (3 months post-implementation)
- **Review Triggers:** User feedback, error patterns, CI/CD integration requirements
- **Success Validation:** Developer adoption, error rates, conversion success metrics

---

## References

### Functional Requirements Documents
- [EPIC-001: PlantUML to PNG CLI Tool](./EPIC-001_PlantUML_to_PNG_CLI.md)
- [FEAT-001: PlantUML to PNG CLI Tool](./FEAT-001_PlantUML_to_PNG_CLI.md)
- [US-001: JBang PlantUML CLI](./US-001_JBang_PlantUML_CLI.md)
- [Acceptance Tests: plantuml_png_conversion.feature](./plantuml_png_conversion.feature)

### Technical Documentation
- [C4 Model Documentation](./PlantUMLTool_C4_Documentation.md)
- [Sequence Diagram Overview](./plantuml_to_png_conversion_overview_README.md)
- [Project POM Configuration](../pom.xml)

### External References
- [PicoCLI Documentation](https://picocli.info/)
- [PlantUML Library Documentation](https://plantuml.com/)
- [JBang Documentation](https://www.jbang.dev/)
- [Graphviz Installation Guide](https://graphviz.org/download/)

---

## Implementation Roadmap

### Phase 1: Core Implementation (Week 1)
- [ ] Add PicoCLI and PlantUML dependencies to pom.xml
- [ ] Implement basic CLI structure with `--file` argument parsing
- [ ] Create Graphviz availability checking
- [ ] Implement file validation and PlantUML conversion
- [ ] Add comprehensive error handling with clear messages

### Phase 2: Testing & Quality (Week 2)
- [ ] Implement unit tests for all components
- [ ] Add end-to-end tests with sample .puml files
- [ ] Test all error scenarios (missing files, syntax errors, no Graphviz)
- [ ] Ensure code coverage meets 80% threshold
- [ ] Validate JBang execution and distribution

### Phase 3: Documentation & Deployment (Week 3)
- [ ] Complete user documentation and error message guidance
- [ ] Create installation and usage guides for JBang
- [ ] Validate cross-platform compatibility (Windows, macOS, Linux)
- [ ] Prepare for user acceptance testing
- [ ] Document Graphviz installation requirements

### Future Enhancements (Post-MVP)
- [ ] Batch processing capability for multiple files (CI/CD integration)
- [ ] Additional command-line options (output directory, format options)
- [ ] Integration with popular build tools and IDEs
- [ ] Performance optimization for larger diagram files
- [ ] Configuration file support for advanced use cases
