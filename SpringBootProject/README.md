# Warehouse Employee Management System (EMS)

## Overview
Warehouse EMS is a comprehensive Spring Boot application for managing warehouse employee data, time & attendance, scheduling, leave, training/certifications, safety, equipment, performance reviews, payroll, notifications, integrations, audit trails, reporting, mobile access (PWA), and onboarding/offboarding workflows.

## Features
- Employee CRUD with soft-delete, pagination, filtering
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- Time & attendance tracking
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incidents & OSHA reporting
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications & announcements
- Integration layer (HRIS/WMS APIs)
- Audit trail & compliance
- Reporting & analytics
- Mobile access (PWA)
- Onboarding & offboarding workflows

## Build & Run
1. Ensure Java 17+ and Maven are installed.
2. Configure PostgreSQL database (see `src/main/resources/application.yml`).
3. Run database migrations with Flyway.
4. Build the project:
   ```bash
   mvn clean install
   ```
5. Run the application:
   ```bash
   mvn spring-boot:run
   ```
6. Access API docs at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
7. Actuator health endpoint: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Testing
- Unit and integration tests can be run with:
  ```bash
  mvn test
  ```

## Security
- Default users (in-memory):
  - admin/adminpass (ADMIN)
  - hr/hrpass (HR)
  - supervisor/supervisorpass (SUPERVISOR)
  - worker/workerpass (WORKER)
- Change to real user store for production.

## Project Structure
- `com.warehouse.ems.employee` - Employee domain
- `com.warehouse.ems.security` - Security config
- `com.warehouse.ems.common` - Exception handling
- More modules to be added for each epic

## Contributing
- Follow standard Java/Spring Boot best practices
- Add inline comments and documentation
- Ensure code is ready for unit testing

## License
MIT
