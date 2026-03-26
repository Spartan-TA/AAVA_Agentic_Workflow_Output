# Warehouse Employee Management System (EMS)

A production-ready Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, payroll, and more.

## Features
- Modular architecture (employee, attendance, scheduling, safety, payroll, etc.)
- Spring Boot 3.x, Spring Data JPA, PostgreSQL
- Spring Security with JWT/OAuth2 and RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- Flyway for database migrations
- OpenAPI/Swagger UI for API documentation
- MapStruct for DTO mapping
- Exception handling, async processing, scheduled jobs
- Integration with HRIS, WMS, Payroll, Email/SMS

## Project Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/
â   âââ main/
â   â   âââ java/com/warehouse/ems/
â   â   â   âââ WarehouseEmsApplication.java
â   â   â   âââ config/
â   â   â   âââ employee/
â   â   â   âââ ...
â   â   âââ resources/
â   â   â   âââ application.yml
â   â   â   âââ application-dev.yml
â   â   â   âââ application-prod.yml
â   â   â   âââ db/migration/
â   â   â   â   âââ V1__create_employees.sql
â   â   â   â   âââ ...
â   âââ test/
â   â   âââ java/com/warehouse/ems/
```

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL

### Setup Database
1. Create databases for dev/prod:
   - `warehouse_ems`, `warehouse_ems_dev`, `warehouse_ems_prod`
2. Create users and grant privileges as per `application.yml`.
3. Flyway will auto-run migrations on startup.

### Build
```
mvn clean install
```

### Run (Dev)
```
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run (Prod)
```
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### API Docs
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Security
- JWT/OAuth2 authentication
- RBAC enforced via `@PreAuthorize` annotations
- Method-level and row-level security

## Testing
- Unit and integration tests under `src/test/java`

## Contribution
- Fork, branch, and submit PRs

## License
MIT
