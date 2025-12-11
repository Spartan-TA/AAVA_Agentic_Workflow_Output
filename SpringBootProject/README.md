# Warehouse Employee Management System

A comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and compliance.

## Features
- Employee CRUD with role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- Time & Attendance (clock-in/out, missed punch correction, CSV export)
- Shift & Schedule Management (templates, rotations, conflict detection)
- Leave & Absence Management (PTO, sick, unpaid, accruals)
- Training & Certification Tracking (alerts, document upload)
- Safety Incidents & OSHA Reporting
- Equipment & Asset Assignment (check-in/out, condition tracking)
- Performance Reviews & Goals
- Payroll Export Integration
- Notifications & Announcements (in-app, email, SMS)
- Integration APIs (HRIS, WMS, IDP)
- Audit Trail & Compliance
- Reporting & Analytics (CSV/PDF export)
- Mobile PWA support
- Multi-tenant & localization
- Observability, monitoring, and CI/CD pipeline

## Tech Stack
- Java 17, Spring Boot 3.2+
- Spring Data JPA, PostgreSQL
- Spring Security (JWT/OAuth2)
- Flyway for DB migrations
- OpenAPI/Swagger for API docs
- MapStruct, Lombok
- Prometheus, OpenTelemetry

## Project Structure
- `src/main/java/com/warehouse/employee/management/` - Main source code
- `src/main/resources/` - Configuration and migration scripts
- Layered architecture: Controller â Service â Repository â Entity

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or update `application.yml` for your DB)

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The app will start on [http://localhost:8080](http://localhost:8080)

### API Docs
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Test
```bash
mvn test
```

### Database Migration
- Flyway runs automatically on startup. Place migration scripts in `src/main/resources/db/migration/`.

### Health Check
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Modules
- **employee**: Employee CRUD, RBAC, profile
- **attendance**: Clock-in/out, reports
- **shift**: Shift templates, assignments
- **leave**: Leave requests, approvals
- **certification**: Training, alerts
- **safety**: Incidents, OSHA
- **asset**: Equipment registry
- **performance**: Reviews, goals
- **notification**: Multi-channel alerts
- **audit**: Change logging
- **integration**: HRIS/WMS APIs
- **report**: Analytics, exports

## Multi-Tenancy & Localization
- Data isolation by tenant ID
- Locale and timezone per tenant

## Observability
- Prometheus metrics: `/actuator/prometheus`
- Distributed tracing: OpenTelemetry

## CI/CD
- GitHub Actions pipeline for build, test, Docker, and deployment

## License
MIT
