# Warehouse Employee Management System (EMS)

A production-ready, multi-module Spring Boot application for managing warehouse employees, attendance, scheduling, safety, and more.

## Features
- Employee master data management
- Role-based access control (RBAC)
- Time & attendance tracking
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incident reporting
- Asset assignment
- Performance reviews
- Payroll export
- Notifications & announcements
- Integration layer (HRIS/WMS)
- Audit trail
- Reporting & analytics
- Mobile access (PWA)
- Onboarding/offboarding workflows
- Overtime & break compliance
- Multi-warehouse support
- Advanced analytics (ML)

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

### Steps
```bash
# Clone repository
$ git clone <repo-url>
$ cd SpringBootProject

# Build project
$ mvn clean install

# Run application
$ mvn spring-boot:run
```

- Application runs on [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Actuator health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Database
- Default: PostgreSQL (see `src/main/resources/application.yml`)
- Flyway auto-applies migrations on startup

## Module Structure
- `employee/` - Employee CRUD, DTOs, validation
- `attendance/` - Clock-in/out, shift association
- `scheduling/` - Shift templates, assignments
- `leave/` - Leave requests, policies
- `training/` - Certifications, renewals
- `safety/` - Incidents, corrective actions
- `asset/` - Asset registry, assignment
- `performance/` - Reviews, goals
- `payroll/` - Export utilities
- `notification/` - In-app/email/SMS
- `integration/` - HRIS/WMS connectors
- `audit/` - Audit logging
- `reporting/` - Reports, analytics
- `mobile/` - Mobile/PWA endpoints
- `onboarding/` - Onboarding/offboarding
- `exception/` - Global exception handling

## Security
- Spring Security with RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- OAuth2/JWT support

## Documentation
- OpenAPI/Swagger auto-generated

## License
MIT
