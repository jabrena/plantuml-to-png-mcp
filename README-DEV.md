# Essential Maven Goals:

```bash
# Analyze dependencies
./mvnw dependency:tree
./mvnw dependency:analyze
./mvnw dependency:resolve

./mvnw clean validate -U
./mvnw buildplan:list-phase
./mvnw license:third-party-report
jwebserver -p 8000 -d "$(pwd)/target/reports/"

# Clean the project
./mvnw clean

# Run integration tests
./mvnw clean verify
./mvnw clean verify -DENABLE_INTEGRATION_TESTS=true
./mvnw clean verify surefire-report:report
./mvnw clean verify jacoco:report -Pjacoco
jwebserver -p 8001 -d "$(pwd)/target/site/jacoco"

# Check for dependency updates
./mvnw versions:display-property-updates
./mvnw versions:display-dependency-updates
./mvnw versions:display-plugin-updates

# Generate project reports
./mvnw site
jwebserver -p 8005 -d "$(pwd)/target/site/"
```
