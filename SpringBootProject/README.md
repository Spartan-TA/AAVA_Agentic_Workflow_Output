# Warehouse Employee Management System

This is a production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, training, equipment, payroll, notifications, integrations, audit trails, reporting, mobile access, and onboarding/offboarding workflows.

## Features
- Employee master data management
- Attendance and clock-in/out
- Shift scheduling and conflict detection
- Leave management and accruals
- Training and certification tracking
- Safety incident reporting
- Equipment and asset assignment
- Performance reviews
- Payroll export integration
- Notifications (in-app, email, SMS)
- Integration APIs (HRIS, WMS, SSO)
- Audit trail and compliance
- Reporting and analytics
- Mobile/PWA support
- Automated onboarding/offboarding

## Build & Run

1. Ensure you have Java 17+ and Maven installed.
2. Configure PostgreSQL and update `src/main/resources/application.yml`.
3. Run database migrations with Flyway.
4. Build and start the application:
   ```
   mvn clean install
   mvn spring-boot:run
   ```
5. Access Actuator health endpoint at `/actuator/health`.
6. API documentation available at `/swagger-ui.html`.

## Directory Structure
- `src/main/java/com/company/wms/` - Main Java source code
- `src/main/resources/` - Configuration and migration scripts
- `pom.xml` - Maven dependencies

## Security
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- JWT/OAuth2 authentication

## License
Proprietary - Company Internal Use Only
