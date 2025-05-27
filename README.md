# Plantuml to PNG

## References

```bash
./mvnw buildplan:list-phase
./mvnw license:third-party-report
jwebserver -p 8000 -d "$(pwd)/target/reports/"
./mvnw clean verify
./mvnw clean verify surefire-report:report
./mvnw clean verify jacoco:report -Pjacoco
jwebserver -p 8001 -d "$(pwd)/target/site/jacoco"

./mvnw versions:display-dependency-updates
./mvnw versions:display-plugin-updates
./mvnw versions:display-property-updates
```