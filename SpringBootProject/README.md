# SpringBootProject

## Overview
This project is a modular Spring Boot application generated from a comprehensive low-level technical design document. It follows industry best practices and includes layered architecture, DTOs, mappers, exception handling, and is ready for unit testing.

## Requirements
- Java 17+
- Maven 3.8+

## Build & Run

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. **Build the project:**
   ```bash
   mvn clean install
   ```
3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   Or:
   ```bash
   java -jar target/SpringBootProject-0.0.1-SNAPSHOT.jar
   ```

## API Documentation
- Swagger UI available at `/swagger-ui.html` after running the application.

## Testing
- Unit and integration tests can be added in `src/test/java/com/application/`
- Run tests:
   ```bash
   mvn test
   ```

## Project Structure
- `src/main/java/com/application/` contains all source code organized by layer and feature.
- `src/main/resources/application.properties` for configuration.

## Contact
For questions, contact the project maintainer.
