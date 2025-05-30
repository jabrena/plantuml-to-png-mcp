Feature: PNG generation from puml files

Background:
  Given HTTP connectivity to PlantUML server is available

Scenario: Successfully convert valid PlantUML file to PNG
  Given a valid .puml file exists at '/path/to/diagram.puml'
  When I execute the JBang CLI tool with the .puml file path as parameter
  Then a PNG file named 'diagram.png' is created in the same directory '/path/to/'
  And the PNG file contains the rendered diagram from the PlantUML source

  Examples:
    | Input File Path                        | Expected Output File Path              |
    | /home/user/docs/sequence-diagram.puml  | /home/user/docs/sequence-diagram.png  |
    | /project/diagrams/class-diagram.puml   | /project/diagrams/class-diagram.png   |

Scenario: Handle invalid PlantUML file syntax
  Given an invalid .puml file exists at '/path/to/invalid-diagram.puml'
  When I execute the JBang CLI tool with the invalid .puml file path as parameter
  Then the tool displays an error message indicating syntax issues
  And no PNG file is created
  And the exit code is non-zero

  Examples:
    | Input File Path                     | Expected Error Message                    |
    | /docs/broken-syntax.puml            | "PlantUML syntax error detected"          |
    | /diagrams/incomplete-diagram.puml   | "Invalid PlantUML file format"            |

Scenario: Handle network connectivity issues
  Given a valid .puml file exists at '/path/to/diagram.puml'
  And the PlantUML server is not accessible
  When I execute the JBang CLI tool with the .puml file path as parameter
  Then the tool displays an error message indicating network connectivity issues
  And no PNG file is created
  And the exit code is non-zero
