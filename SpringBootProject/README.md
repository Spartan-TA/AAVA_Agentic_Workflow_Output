# Warehouse Employee Management System

This is a production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more. It implements 87 user stories across 20 epics.

## Features
- Employee master data CRUD
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- Time & attendance (clock-in/out, corrections, CSV export)
- Shift management and conflict detection
- Leave management and approval workflow
- Certification tracking and expiry alerts
- Safety incident reporting and OSHA exports
- Asset assignment and check-in/out
- Performance reviews and goal tracking
- Payroll export and SFTP delivery
- Notifications (in-app, email, SMS)
- HRIS/WMS integration APIs
- Audit trail and compliance
- Reporting and analytics
- Mobile/PWA support
- Onboarding/offboarding automation
- Localization and multi-tenant support
- AI-powered scheduling
- CI/CD with GitHub Actions

## Build & Run Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (default, can use H2 for dev)

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

Application runs on port 8080 by default.

### Database
- Flyway migration scripts are in `src/main/resources/db/migration/`
- Default profile uses H2 in-memory DB for development
- For production, configure PostgreSQL in `application.yml`

### API Documentation
- Swagger UI available at `/swagger-ui.html`
- OpenAPI spec at `/v3/api-docs`

### Health Check
- Actuator health endpoint: `/actuator/health`

### Testing
```bash
mvn test
```

### Directory Structure
- `src/main/java/com/company/warehouse/` - Java source files
- `src/main/resources/` - configs, messages, migrations
- `src/test/java/com/company/warehouse/` - test classes
- `.github/workflows/ci.yml` - CI/CD workflow
- `manifest.json` - PWA manifest

## Modules
- **employee**: Employee master data
- **scheduling**: Shifts, assignments, AI scheduling
- **attendance**: Clock events, leave, payroll
- **safety**: Incidents, certifications
- **common**: Shared utilities, audit, notifications

## Security
- Spring Security with JWT/OAuth2
- Role hierarchy: ADMIN > HR > SUPERVISOR > WORKER

## Contact
For issues or contributions, please open a GitHub issue or pull request.
