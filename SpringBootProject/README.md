# Warehouse Employee Management System

This is a Spring Boot application for managing warehouse employees, schedules, attendance, safety, and more. It is modular, secure, and ready for production deployment.

## Features
- Employee master data management (CRUD)
- Role-based access control (RBAC)
- Time & attendance tracking
- Shift & schedule management
- Training & certification tracking
- Safety incident & OSHA reporting
- Payroll export integration
- Integration APIs (HRIS/WMS)
- Audit trail & compliance
- Reporting & analytics
- Spring Boot Actuator health checks
- OpenAPI/Swagger documentation

## Build & Run

1. **Build:**
   ```bash
   mvn clean install
   ```
2. **Run:**
   ```bash
   mvn spring-boot:run
   ```
3. **Test:**
   ```bash
   mvn test
   ```

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Health Check
- Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Database
- Uses PostgreSQL by default. Configure in `src/main/resources/application.yml`.
- Flyway is used for database migrations.

## Security
- Spring Security with RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- JWT/OAuth2 support

## Directory Structure
- `src/main/java/com/warehouse/employee_mgmt/` - Source code
- `src/main/resources/` - Configuration and migration scripts

## Contribution
PRs welcome. Please follow code style and add tests for new features.
