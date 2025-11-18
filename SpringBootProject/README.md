# Warehouse Employee Management System

This project is a Spring Boot application for managing warehouse employees, including scheduling, attendance, safety, and more.

## Prerequisites
- Java 17+
- Maven 3.8+
- Docker (optional, for DB)

## Build
```bash
mvn clean install
```

## Run
```bash
mvn spring-boot:run
```
Application runs on port 8080 by default.

## Test
```bash
mvn test
```

## API Documentation
- OpenAPI/Swagger available at `/swagger-ui.html` after startup.

## Health Check
- Actuator endpoint: `GET /actuator/health` should return `UP`.

## Database Migrations
- Flyway/Liquibase runs automatically on startup.

## Project Structure
- `src/main/java/com/warehouse/employee` - Source code
- `src/main/resources` - Configurations and migrations

## Modules
- Employee CRUD
- Scheduling & Attendance
- Safety & Certifications
- RBAC & Security
- Reporting & Analytics

## Contribution
See `CONTRIBUTING.md` for guidelines.
