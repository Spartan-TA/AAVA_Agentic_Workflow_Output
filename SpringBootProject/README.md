# Warehouse Employee Management System (EMS)

This is a Spring Boot application for managing warehouse employees, attendance, shifts, leave requests, certifications, safety incidents, assets, performance reviews, and audit entries.

## Prerequisites
- Java 17 or higher
- Maven 3.8+
- PostgreSQL (or update application.properties for your DB)

## Build Instructions

1. Clone the repository:
   ```bash
   git clone <REPO_URL>
   cd <REPO_DIR>
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

## Run Instructions

1. Ensure your database is running and credentials are set in `src/main/resources/application.properties`.

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   or
   ```bash
   java -jar target/warehouse-ems-0.0.1-SNAPSHOT.jar
   ```

## API Documentation

- Swagger UI is available at: `http://localhost:8080/swagger-ui.html`

## Database Migration

- Flyway migration scripts are located in `src/main/resources/db/migration/` and will run automatically on startup.

## Testing

- To run unit tests:
   ```bash
   mvn test
   ```

## Project Structure

- `src/main/java/com/warehouse/ems/` - Source code
- `src/main/resources/` - Configuration and migration scripts
- `src/test/java/com/warehouse/ems/` - Unit tests

## Contact

For issues or contributions, please open a GitHub issue or pull request.
