# Warehouse Employee Management System

This is a modular Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Build & Run

1. Ensure you have Java 17+ and Maven installed.
2. Clone the repository.
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   The application will start on port 8080.

## Test

Run all unit and integration tests:
```bash
mvn test
```

## Health Check

Spring Boot Actuator is enabled. Check health endpoint:
```bash
curl http://localhost:8080/actuator/health
```

## Modules
- Employee Master Data (CRUD)
- Role-Based Access Control (RBAC)
- Time & Attendance
- Shift & Schedule Management
- Leave & Absence Management
- Training & Certification Tracking
- Safety Incidents & OSHA Reporting
- Equipment & Asset Assignment
- Performance Reviews & Goals
- Payroll Export Integration
- Notifications & Announcements
- Integration Layer
- Audit Trail & Compliance
- Reporting & Analytics
- Mobile Access (PWA)
- Onboarding & Offboarding Workflow

## Database
Flyway/Liquibase is used for DB migrations. See `src/main/resources/db/migration` for scripts.

## API Documentation
OpenAPI/Swagger is available at `/swagger-ui.html` after startup.

## Security
Spring Security is enabled with RBAC. See configuration in `src/main/java/com/example/warehouse/config/SecurityConfig.java`.

## Contact
For issues or contributions, please open a GitHub issue or pull request.
