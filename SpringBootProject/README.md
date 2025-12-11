# Warehouse Employee Management System

This is a comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, safety, and more.

## Features
- Employee CRUD with RBAC and soft-delete
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- Attendance, scheduling, leave, and more (see project epics)
- OpenAPI documentation (Swagger)
- Flyway DB migrations
- Actuator health and metrics endpoints
- API key/OAuth2 security toggle
- Multi-language and multi-tenant support

## Requirements
- Java 17+
- Maven 3.8+
- PostgreSQL (default config, see `application.yml`)

## Build
```bash
mvn clean install
```

## Run
```bash
mvn spring-boot:run
```

The application will start on [http://localhost:8080](http://localhost:8080)

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Health Check
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Test
```bash
mvn test
```

## Database Migration
- Flyway runs automatically on startup.

## Security
- Default: OAuth2 JWT (see `application.yml`)
- To enable API key, set `security.api-key.enabled=true` in `application.yml`.

## Localization
- English and Spanish supported (see `src/main/resources/i18n/messages_en.properties`, etc.)

## Multi-Tenancy
- Tenant context enabled by default.

## Contribution
- Fork, branch, and submit PRs.

## License
MIT
