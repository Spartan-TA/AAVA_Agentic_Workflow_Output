# Warehouse Employee Management System

This is a comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, safety, and more. It is designed for extensibility, security, and integration with enterprise systems.

## Features
- Employee CRUD with soft delete, pagination, filtering
- Role-Based Access Control (RBAC) with Spring Security
- Attendance tracking, shift management, leave workflows
- Training, certification, safety incident tracking
- Asset assignment, performance reviews, payroll export
- Notifications, integration APIs, audit trail, reporting
- Mobile access (PWA), onboarding/offboarding workflows
- Multi-tenant, localization, observability, CI/CD

## Project Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/
â   âââ main/
â   â   âââ java/com/warehouse/employee/...
â   â   âââ resources/
â   â       âââ application.properties
â   â       âââ db/migration/V1__Initial_Schema.sql
â   âââ test/java/com/warehouse/employee/...
```

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or update datasource in `application.properties`)

## Build & Run
1. Clone the repository
2. Configure your database in `src/main/resources/application.properties`
3. Build the project:
   ```sh
   mvn clean install
   ```
4. Run the application:
   ```sh
   mvn spring-boot:run
   ```
5. Access API docs at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Testing
Run all tests:
```sh
mvn test
```

## Database Migration
Flyway will auto-apply migrations on startup. See `src/main/resources/db/migration/`.

## Security
- Default users: `admin`/`admin123`, `hr`/`hr123`, `supervisor`/`supervisor123`, `worker`/`worker123`
- Change credentials in `SecurityConfig.java` and `application.properties` for production

## Extending
- Add new entities, repositories, services, and controllers under `com.warehouse.employee`
- Use Spring Data JPA for persistence
- Use @PreAuthorize for method-level security

## License
MIT
