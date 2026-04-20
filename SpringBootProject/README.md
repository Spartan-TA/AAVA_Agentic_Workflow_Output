# Warehouse Employee Management System

This is a Spring Boot application for managing warehouse employees, shifts, attendance, safety, and more.

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or update datasource in `application.yml`)

### Database Setup
1. Create a database named `warehouse` and a user with credentials matching `application.yml`.
2. Flyway will run migrations automatically on startup.

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The application will start on [http://localhost:8080](http://localhost:8080)

### API Docs
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Health Check
- Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Project Structure
- `src/main/java/com/warehouse/management/` - Main source code
- `src/main/resources/` - Configuration and migration scripts

## Modules
- Employee, Scheduling, Attendance, Safety, Integration, Audit, Reporting, etc.

## Security
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- API Key/OAuth2 toggle via config

## Contribution
PRs welcome! Please follow code style and add tests.
