# Warehouse Employee Management Spring Boot Project

## Overview
This project is a Spring Boot application for managing warehouse employees, attendance, and shifts. It includes RESTful APIs, DTOs, mappers, security configuration, exception handling, Flyway migrations, and unit tests.

## Project Structure
- `src/main/java/com/warehouse/management/` - Java source code
- `src/main/resources/` - Resources (application.properties, Flyway migrations)
- `src/test/java/com/warehouse/management/` - Unit tests

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.6+

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## API Endpoints
- `/api/employees` - CRUD for employees
- `/api/attendances` - CRUD for attendance
- `/api/shifts` - CRUD for shifts

## Database Migration
Flyway is used for database migrations. On startup, the migration scripts in `src/main/resources/db/migration/` will be applied automatically.

## Testing
Run all unit tests with:
```bash
mvn test
```

## Security
Basic security is configured. All `/api/**` endpoints are permitted by default. Adjust `SecurityConfig.java` for custom rules.

## Exception Handling
Global exception handling is provided by `GlobalExceptionHandler.java`.

## Contribution
Feel free to fork and submit pull requests.
