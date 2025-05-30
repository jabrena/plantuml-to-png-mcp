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
- **PlantUML Integration**: HTTP-based integration with PlantUML server for rendering
- **Network Connectivity**: Leverages HTTP requests to PlantUML server for diagram generation

---

## User Stories

_User stories for this epic will be defined and linked here as they are created._

---

## Dependencies

Uses HTTP integration with PlantUML server (e.g., plantuml.com) for diagram rendering, eliminating local installation requirements

---

## Risks & Assumptions

### Risks
- Network connectivity issues may affect diagram generation
- PlantUML server availability and performance
- File permission issues when writing PNG files

### Assumptions
- Reliable internet connection available for HTTP requests to PlantUML server
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
- [ ] CLI tool successfully converts .puml files to .png format using HTTP integration
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

**Created:** May 30, 2025
**Last Updated:** May 30, 2025
