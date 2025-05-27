# Epic: EPIC-001: PlantUML to PNG CLI Tool

**Epic Owner:** Juan Antonio Breña Moral
**Priority:** Medium
**Target Release:** ASAP
**Estimated Scope:** Medium - 1-3 months

---

## Business Value & Strategic Goals

Facilitate the generation of the diagram in PNG format automatically because Github doesn't render in Markdown .plantuml files.

### Target Users
Developers

### Success Criteria
Generate a .png with the same filename given in the .puml and generate the image in the same path of the .puml.

---

## Problem Statement

Generate .png files on demand using a CLI tool passing as input the path of a .puml

---

## Solution Overview

Develop a CLI which pass a .puml and the tool then generate with the same filename a .png

---

## Key Features & Components

- **PlantUML File Processing**: Convert from .puml to .png format
- **Command Line Interface**: Accept .puml file path as input parameter
- **File Output Management**: Generate PNG with same filename in same directory as source
- **PlantUML Integration**: Utilize net.sourceforge.plantuml:plantuml:1.2023.13 dependency
- **Graphviz Integration**: Leverage internal Graphviz rendering capabilities

---

## User Stories

_User stories for this epic will be defined and linked here as they are created._

---

## Dependencies

Use net.sourceforge.plantuml:plantuml:1.2023.13 as Dependency. Internally, it uses Graphviz

---

## Risks & Assumptions

### Risks
- Graphviz installation may not be available on target systems
- PlantUML dependency compatibility issues
- File permission issues when writing PNG files

### Assumptions
- Verify that Graphviz is installed on target systems
- Users have appropriate file system permissions
- PlantUML files are syntactically valid

---

## Acceptance Criteria

This epic will be considered complete when:
- [ ] All identified user stories are completed and accepted
- [ ] Success criteria metrics are achieved
- [ ] All dependencies are resolved
- [ ] Solution is deployed to production
- [ ] User acceptance testing is completed successfully
- [ ] CLI tool successfully converts .puml files to .png format
- [ ] Generated PNG files have the same filename as source .puml files
- [ ] PNG files are created in the same directory as source files

---

## Related Documentation

_Related documentation will be linked here as it becomes available._

---

## Epic Progress

**Status:** Not Started
**Completion:** 0%

### Milestones
- [ ] Epic planning and breakdown complete
- [ ] User stories defined and estimated
- [ ] Development started
- [ ] First increment delivered
- [ ] User testing completed
- [ ] Epic completed and deployed

---

**Created:** December 19, 2024
**Last Updated:** December 19, 2024
