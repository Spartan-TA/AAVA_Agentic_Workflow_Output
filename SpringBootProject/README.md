# Warehouse Employee Management System

This is a production-ready, multi-module Spring Boot application for comprehensive warehouse employee management, including:
- Employee master data
- Scheduling and shift management
- Time and attendance
- Safety incidents and OSHA reporting

## Project Structure

- `core`: Shared domain classes and configuration
- `employee`: Employee master data and RBAC
- `scheduling`: Shift and schedule management
- `attendance`: Time and attendance tracking
- `safety`: Safety incidents and OSHA reporting

## Prerequisites
- Java 17+
- Maven 3.8+
- Docker (optional, for DB)

## Build

```bash
mvn clean install
```

## Run (example for employee module)

```bash
cd employee
mvn spring-boot:run
```

## Test

```bash
mvn test
```

## Database Migrations

Flyway/Liquibase migrations run automatically on startup. Configure DB connection in `src/main/resources/application.properties`.

## Health Check

Actuator health endpoint: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## API Documentation

OpenAPI/Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Notes
- Each module can be run independently for development.
- See individual module READMEs for more details.
