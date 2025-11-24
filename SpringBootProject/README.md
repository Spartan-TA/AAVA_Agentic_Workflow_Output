# Warehouse Employee Management System (EMS)

## Overview
This is a comprehensive Spring Boot application for managing warehouse employees, including modules for employee CRUD, RBAC, time & attendance, shift management, leave, training, safety, equipment, performance reviews, payroll, notifications, integrations, audit trail, reporting, mobile PWA, and onboarding/offboarding.

## Technology Stack
- Spring Boot 2.7+
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway
- Maven

## Build & Run
1. Ensure Java 11+ and Maven are installed.
2. Configure PostgreSQL database (see `src/main/resources/application.yml`).
3. Run database migrations:
   ```bash
   mvn flyway:migrate
   ```
4. Build and run the application:
   ```bash
   mvn clean spring-boot:run
   ```
5. Access Actuator health endpoint at `http://localhost:8080/actuator/health`.

## Testing
- Unit and integration tests can be run with:
  ```bash
  mvn test
  ```

## API Documentation
- OpenAPI/Swagger UI available at `/swagger-ui.html` after startup.

## Modules
- Employee CRUD
- RBAC (Role Based Access Control)
- Time & Attendance
- Shift Management
- Leave Management
- Training & Certification
- Safety Incidents
- Equipment Assignment
- Performance Reviews
- Payroll Export
- Notifications
- Integration Layer
- Audit Trail
- Reporting
- Mobile PWA
- Onboarding/Offboarding

## Contact
For questions, contact the EMS engineering team.
