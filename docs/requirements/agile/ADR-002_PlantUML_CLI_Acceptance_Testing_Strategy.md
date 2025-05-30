# ADR-002: PlantUML to PNG CLI Tool - Acceptance Testing Strategy

**Date:** 2025-05-30
**Status:** Proposed
**Technical Area:** Acceptance Testing Strategy
**Software Type:** Command-Line Interface (CLI) Tool

## Context

### Software Overview
**Type:** Command-Line Interface (CLI) Tool
**Architecture:** Monolithic Java Application
**Technology Stack:** Java 24, Maven, PicoCLI Framework, HTTP PlantUML Integration, JUnit 5, JaCoCo, JBang Distribution

### Business Context
The PlantUML to PNG CLI tool addresses a critical developer workflow issue where GitHub doesn't automatically render PlantUML (.puml) files in Markdown documentation. The tool converts single .puml files to .png format via command line, generating PNG files with the same filename in the same directory as the source file. This automation eliminates manual conversion processes and ensures documentation diagrams are always accessible in GitHub repositories.

### Current State
The project has established comprehensive functional requirements including:
- Epic (EPIC-001), Feature (FEAT-001), and User Story (US-001) documentation
- Existing Gherkin acceptance tests in `plantuml_png_conversion.feature`
- Technology stack decision (ADR-001) with quality tools (Error Prone, NullAway, JaCoCo 80% coverage)
- C4 model architecture documentation
- Developer-owned testing approach with team responsibility for test maintenance

### Problem Statement
Need to establish a comprehensive acceptance testing strategy that achieves **zero production issues related to file conversion** while addressing the three primary risk areas: Network connectivity management, invalid PlantUML file handling, and file system path issues. The strategy must integrate seamlessly with developer workflows and provide confidence for continuous delivery.

## Functional Requirements Impact

### Critical User Journeys
1. **Single File Conversion:** Developer executes CLI tool with valid .puml file path and receives correct PNG output
2. **Network Connectivity Validation:** Tool detects PlantUML server availability and provides clear guidance when unavailable
3. **Error Handling:** Tool provides actionable error messages for invalid files, syntax errors, and connectivity issues
4. **Cross-Platform Execution:** Consistent behavior across environments where JBang is installed

### User Types and Personas
- **Primary Users:** Software developers working with PlantUML diagrams in GitHub repositories
- **Secondary Users:** CI/CD systems executing automated documentation generation
- **Environment Users:** Developers on different platforms relying on JBang for execution

### Integration Points
- **File System:** Critical integration for reading .puml files and writing .png files with path validation
- **PlantUML Server:** HTTP-based dependency requiring connectivity detection and error guidance
- **HTTP Client:** Core integration for communicating with PlantUML server endpoints
- **JBang Runtime:** Simplified distribution eliminating complex cross-platform installation requirements

### Data and Compliance Requirements
- **File Data:** Text-based PlantUML files with syntax validation requirements
- **Output Quality:** PNG files must accurately represent PlantUML diagrams without corruption
- **Error Handling:** Clear, actionable error messages for all failure scenarios

## Decision

We have decided to implement **Comprehensive Multi-Layered Testing Strategy** combining BDD automation, risk-based testing focus, and continuous CI/CD integration.

### Testing Strategy Overview
Implement a three-pronged approach that leverages existing Gherkin scenarios while expanding coverage to address all identified risk areas. The strategy emphasizes automated execution through Maven/JUnit integration with comprehensive error scenario testing and environment validation, all designed to achieve zero production conversion issues.

### Testing Scope and Coverage
- **End-to-End CLI Testing:** Complete command execution from file input to PNG output validation
- **Risk-Focused Testing:** Comprehensive coverage of network connectivity, bad PlantUML files, and path issues
- **Environment Validation:** Network connectivity detection and JBang execution across different platforms
- **Error Scenario Testing:** All failure modes with validation of error messages and exit codes
- **Regression Testing:** Ensure existing functionality remains intact with new changes
- **Integration Testing:** HTTP PlantUML server integration and file system operations

### Automation vs Manual Balance
- **Automated Testing (95%):** All core functionality, error scenarios, environment validation, and regression tests
- **Manual Testing (5%):** Complex diagram visual verification and exploratory testing of edge cases
- **Continuous Validation:** Automated execution on every commit with quality gates

### Tool Selection
**Primary Testing Tools:**
- **BDD Framework:** Cucumber-JVM with enhanced Gherkin scenarios - Builds on existing test definitions while providing business-readable specifications
- **CLI Testing:** ProcessBuilder with JUnit 5 - Direct CLI execution testing for realistic user scenarios and error condition validation
- **Test Management:** Maven Surefire/Failsafe - Integrated with existing build system and quality infrastructure
- **Coverage Analysis:** JaCoCo - Already configured with 80% minimum coverage requirement
- **CI/CD Integration:** Maven lifecycle integration - Seamless integration with existing build pipeline and quality gates

## Alternatives Considered

### Option 1: Basic Unit Testing Only
**Description:** Focus primarily on unit tests with minimal CLI integration testing
**Pros:** Fast execution, easy to maintain, good code coverage metrics
**Cons:** Doesn't validate actual CLI behavior, misses integration issues, insufficient for zero production issues goal
**Why Not Chosen:** Cannot achieve zero production conversion issues without end-to-end validation

### Option 2: Manual Testing Procedures
**Description:** Rely on manual testing checklists and procedures for validation
**Pros:** Flexible, can catch visual issues, low initial setup investment
**Cons:** Time-consuming, error-prone, not suitable for continuous delivery, doesn't scale with feature additions
**Why Not Chosen:** Incompatible with zero production issues goal and developer-owned testing approach

### Option 3: External Testing Service
**Description:** Use cloud-based testing services for cross-platform validation
**Pros:** Comprehensive platform coverage, managed infrastructure
**Cons:** Additional cost, external dependency, slower feedback loops, complexity for simple CLI tool
**Why Not Chosen:** JBang distribution simplifies cross-platform concerns, making external services unnecessary

## Implementation Strategy

### Phase 1: Enhanced BDD Foundation (Week 1-2)
- Expand existing Gherkin scenarios in `plantuml_png_conversion.feature` to cover all three risk areas
- Implement Cucumber-JVM test runner with Maven integration
- Create CLI test utilities using ProcessBuilder for actual command execution
- Develop comprehensive test data set including valid/invalid .puml files and path scenarios
- Add network connectivity detection tests

### Phase 2: Risk-Focused Test Development (Week 2-3)
- Implement automated tests for all network connectivity scenarios (available/unavailable/timeout)
- Create comprehensive bad PlantUML file test suite (syntax errors, malformed files, edge cases)
- Develop bad path testing (non-existent files, permission issues, invalid characters)
- Add error message validation and exit code verification
- Integrate with existing JaCoCo coverage reporting

### Phase 3: Continuous Integration and Quality Gates (Week 3-4)
- Configure Maven build lifecycle integration (test and verify phases)
- Implement quality gates requiring all acceptance tests to pass before merge
- Set up test reporting and failure notification mechanisms
- Add performance validation for conversion time and memory usage
- Create regression test suite for existing functionality protection

### Team Structure and Responsibilities
- **Development Team:** Write and maintain acceptance tests, ensure test coverage, fix failing tests, own test strategy evolution
- **CI/CD System:** Execute automated test suite on every build and pull request with quality gate enforcement
- **Manual Validation:** Minimal manual testing for complex diagram visual verification and exploratory edge case testing

### CI/CD Integration
- **Build Integration:** Tests execute during Maven `verify` phase with fail-fast on acceptance test failures
- **Quality Gates:** All acceptance tests must pass before merge/deployment with zero tolerance for conversion failures
- **Reporting:** Test results integrated with existing build reporting and team notifications
- **Failure Handling:** Clear error messages, logs, and debugging guidance for failed test scenarios

## Success Metrics and Quality Gates

### Testing KPIs
- **Zero Production Issues:** Primary success metric - no conversion-related bugs reaching production
- **Test Coverage:** 100% of critical user journeys and all three risk areas covered by automated tests
- **Test Execution Time:** Complete test suite execution under 3 minutes for fast feedback
- **Test Reliability:** Less than 1% flaky test rate (false positives/negatives)
- **Defect Detection Rate:** 100% of conversion issues caught by acceptance tests before manual testing

### Quality Gates
- All Gherkin scenarios pass with valid .puml files and proper network connectivity
- All three risk areas (network connectivity, bad files, bad paths) have comprehensive test coverage
- Error scenarios produce appropriate error messages and exit codes
- PNG output files are generated correctly with proper naming and directory placement
- Performance criteria met for typical diagram conversion scenarios

### Reporting and Documentation
- **Test Results:** Cucumber HTML reports with scenario details and failure analysis
- **Coverage Reports:** JaCoCo integration showing acceptance test coverage metrics
- **CI/CD Dashboard:** Build status with test execution summary and trend analysis
- **Documentation:** Test execution guide, troubleshooting procedures, and maintenance guidelines

## Consequences

### Positive Outcomes
- **Zero Production Issues:** Comprehensive testing strategy designed to catch all conversion-related problems before production
- **Developer Confidence:** Automated regression testing enables safe refactoring and feature additions
- **Risk Mitigation:** Focused coverage of the three primary risk areas (network connectivity, bad files, bad paths)
- **Continuous Quality:** Automated quality gates prevent broken functionality from reaching users
- **Maintainable Tests:** Developer-owned approach ensures tests evolve with the codebase
- **Fast Feedback:** Quick test execution provides immediate validation of changes

### Negative Outcomes and Mitigation
- **Test Maintenance Overhead:** Additional code to maintain → **Mitigation:** Keep tests focused on critical paths, leverage existing Maven infrastructure, developer ownership ensures relevance
- **Initial Setup Time:** Comprehensive strategy requires upfront investment → **Mitigation:** Phased implementation spreads effort, immediate value from enhanced existing tests
- **Build Time Impact:** More comprehensive testing increases build duration → **Mitigation:** Optimize test execution, parallel execution where possible, target under 3 minutes total

### Risks and Contingencies
- **Environment Dependency Risk:** Tests may fail if network connectivity unavailable in CI/CD → **Mitigation:** Include test environment setup with network validation, clear documentation for local development
- **Test Data Management:** Sample .puml files may become outdated → **Mitigation:** Version control test data, regular review process, automated test data validation
- **False Positive Risk:** Overly strict tests may block valid changes → **Mitigation:** Focus on functional correctness, avoid brittle assertions, regular test review

## Compliance and Standards

### Testing Standards
- **BDD Standards:** Gherkin scenarios follow Given-When-Then format with clear, business-readable language focused on user outcomes
- **Java Testing Standards:** JUnit 5 best practices, proper test isolation, cleanup, and resource management
- **Maven Standards:** Integration with standard Maven lifecycle, consistent reporting, and build integration
- **Code Quality:** Tests subject to same quality standards as production code (Error Prone, NullAway, coverage requirements)

## Maintenance and Evolution

### Test Maintenance Strategy
- **Test Data:** Version-controlled sample .puml files covering various complexity levels and error scenarios
- **Environment Management:** Documented setup procedures for development and CI/CD environments with network connectivity requirements
- **Test Updates:** Tests updated alongside feature development as first-class citizens, not afterthoughts
- **Regular Review:** Monthly assessment of test effectiveness and coverage gaps

### Review and Update Schedule
- **Regular Reviews:** Monthly review of test effectiveness, failure patterns, and coverage adequacy
- **Strategy Evolution:** Quarterly assessment of testing approach effectiveness toward zero production issues goal
- **Tool Evaluation:** Annual review of testing tools and frameworks for better alternatives or optimizations

## References

### Related Documents
- [ADR-001: PlantUML CLI Functional Requirements](./ADR-001_PlantUML_CLI_Functional_Requirements.md)
- [EPIC-001: PlantUML to PNG CLI Tool](./EPIC-001_PlantUML_to_PNG_CLI.md)
- [FEAT-001: PlantUML to PNG CLI Tool](./FEAT-001_PlantUML_to_PNG_CLI.md)
- [US-001: JBang PlantUML CLI](./US-001_JBang_PlantUML_CLI.md)
- [Existing Acceptance Tests](./plantuml_png_conversion.feature)
- [C4 Model Documentation](./PlantUMLTool_C4_Documentation.md)

### External Standards and Frameworks
- [Cucumber-JVM Documentation](https://cucumber.io/docs/cucumber/api/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [Behavior-Driven Development Best Practices](https://cucumber.io/docs/bdd/)

### Tool Documentation
- [PicoCLI Testing Guide](https://picocli.info/#_testing)
- [PlantUML Server Documentation](https://plantuml.com/server)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [JBang Documentation](https://www.jbang.dev/)

---
*This ADR defines a comprehensive acceptance testing strategy focused on achieving zero production issues for file conversion functionality. Last updated: 2025-05-30*
