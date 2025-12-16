# Warehouse Employee Management System

This is a Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features
- Employee CRUD with RBAC
- Attendance tracking
- Shift and schedule management
- Leave and absence workflows
- Training and certification tracking
- Safety incident reporting
- Asset assignment and tracking
- Performance reviews
- Payroll export integration
- Notifications and announcements
- Integration layer (HRIS, WMS, IDP)
- Audit trail and compliance
- Reporting and analytics
- Mobile access (PWA)
- Onboarding/offboarding workflows
- Localization and multi-tenant support
- Performance and scalability optimizations
- Deployment and observability (Docker, Kubernetes, Actuator)

## Requirements
- Java 17+
- Maven 3.8+
- PostgreSQL (production)
- H2 (development/testing)

## Build & Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on port **8080**.

## Database Migrations
Flyway is used for database migrations. On startup, migrations in `src/main/resources/db/migration` will be applied automatically.

## API Documentation
OpenAPI/Swagger UI is available at `/swagger-ui.html` when the application is running.

## Testing
Unit and integration tests are located in `src/test/java`.
Run all tests with:
```bash
mvn test
```

## Docker
Build and run the application in Docker:
```bash
docker build -t warehouse-employee-mgmt .
docker run -p 8080:8080 warehouse-employee-mgmt
```

## Kubernetes
See `deployment.yaml` for deployment instructions.

## Configuration
Application configuration files are in `src/main/resources`:
- `application.yml` (default)
- `application-dev.yml` (development)
- `application-prod.yml` (production)

## Health Check
Actuator health endpoint: `GET /actuator/health`

## RBAC Roles
- ADMIN
- HR
- SUPERVISOR
- WORKER

## Contact
For questions or support, contact the development team at dev@company.com.
