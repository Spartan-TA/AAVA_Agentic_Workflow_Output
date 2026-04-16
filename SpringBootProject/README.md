# Warehouse Employee Management System (EMS)

This is a modular Spring Boot application for managing warehouse employees, roles, attendance, and compliance.

## Features
- Project scaffolding with Maven, Java 17+
- Employee CRUD with badgeId uniqueness and soft-delete
- Role-based access control (RBAC) with Spring Security
- Flyway database migrations
- Actuator health endpoint
- OpenAPI/Swagger documentation

## Build & Run

```shell
./mvnw clean install
./mvnw spring-boot:run
```

The application will start on [http://localhost:8080](http://localhost:8080).

- Health check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Test

```shell
./mvnw test
```

## API Endpoints

- `GET /employees` - List employees (paginated)
- `GET /employees/{id}` - Get employee by ID
- `POST /employees` - Create employee
- `PUT /employees/{id}` - Update employee
- `DELETE /employees/{id}` - Soft-delete employee

## Security
- RBAC enforced via roles: ADMIN, HR, SUPERVISOR, WORKER
- API key or OAuth2 mode (see `application.yml`)

## Database
- PostgreSQL required (see `application.yml` for connection details)
- Flyway migrations auto-run on startup

## Extending
- Add new modules under `com.warehouse.ems.*` packages
- See technical design document for epic/user story mapping
