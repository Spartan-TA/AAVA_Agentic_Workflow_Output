# Warehouse Employee Management System (WEM)

This is a comprehensive Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features
- Modular architecture: employee, attendance, schedule, safety, common
- Employee master data CRUD
- Role-Based Access Control (RBAC)
- Time & Attendance (clock in/out)
- Shift & Schedule management
- Leave & Absence management
- Training & Certification tracking
- Safety incidents & OSHA reporting
- Equipment & Asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications & Announcements
- Integration layer (HRIS/WMS APIs)
- Audit trail & compliance
- Reporting & analytics
- Mobile access (PWA)
- Onboarding & Offboarding workflow

## Tech Stack
- Spring Boot 3.x
- Maven
- JPA/Hibernate
- Flyway
- Spring Security
- Spring Actuator
- OpenAPI/Swagger
- PostgreSQL (default, configurable)

## Build & Run
1. Ensure Java 17+ and Maven are installed.
2. Configure database in `src/main/resources/application.yml`.
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```
5. Access API docs at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
6. Health endpoint: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Database Migration
- Flyway runs automatically on startup.
- Migration scripts are in `src/main/resources/db/migration`.

## Security
- RBAC roles: ADMIN, HR, SUPERVISOR, WORKER
- API key/OAuth2 toggle via config

## Contribution
- Standard Maven structure
- Inline comments and documentation
- Unit tests recommended for all modules

## License
MIT
