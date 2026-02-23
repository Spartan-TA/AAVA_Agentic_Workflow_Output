# Warehouse Employee Management System (WEMS)

A comprehensive Spring Boot 3.x application for managing warehouse employees, covering 20 business-critical epics including employee CRUD, RBAC, attendance, shift management, safety, payroll, notifications, integrations, audit, reporting, mobile PWA, and more.

## Features (Epics)
- E01: Project Scaffolding & Domain Setup
- E02: Employee Master Data (CRUD)
- E03: Role-Based Access Control (RBAC)
- E04: Time & Attendance (Clock In/Out)
- E05: Shift & Schedule Management
- E06: Leave & Absence Management
- E07: Training & Certification Tracking
- E08: Safety Incidents & OSHA Reporting
- E09: Equipment & Asset Assignment
- E10: Performance Reviews & Goals
- E11: Payroll Export Integration
- E12: Notifications & Announcements
- E13: Integration Layer (HRIS/WMS APIs)
- E14: Audit Trail & Compliance
- E15: Reporting & Analytics
- E16: Mobile Access (PWA)
- E17: Onboarding & Offboarding Workflow
- E18: Localization
- E19: Observability
- E20: Deployment

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or update `application.yml` for your DB)

### Steps
```bash
# Clone the repository
$ git clone <repo-url>
$ cd SpringBootProject

# Build the project
$ mvn clean install

# Run the application
$ mvn spring-boot:run
```

- The app runs on [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Actuator Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Project Structure
- `src/main/java/com/company/wems/` - Main application code
- `src/main/resources/` - Configuration, migrations, static assets
- `src/test/java/com/company/wems/` - Unit and integration tests

## Configuration
- Edit `src/main/resources/application.yml` for DB, security, notifications, etc.

## Testing
```bash
$ mvn test
```

## Documentation
- OpenAPI/Swagger docs auto-generated at `/swagger-ui.html`

## License
Proprietary. All rights reserved.
