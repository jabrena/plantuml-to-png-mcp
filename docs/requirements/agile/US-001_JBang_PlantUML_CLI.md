# User Story: US-001 - Implement a JBang CLI to convert a .puml into .png

**As a** Business/Analyst, Product Owner & Software engineer
**I want to** pass the .puml path to generate a .png in the same path with the same file name
**So that** I can automate the process without plugins

---

## Acceptance Criteria

The detailed acceptance criteria for this user story, illustrated with concrete examples, are defined in Gherkin format in the following file:
- [`plantuml_png_conversion.feature`](plantuml_png_conversion.feature)

---

## Additional Notes

- Uses JBang for CLI implementation
- Requires Graphviz installation on the host system
- Output PNG file maintains same filename as input .puml file
- PNG file is generated in the same directory as the source .puml file
