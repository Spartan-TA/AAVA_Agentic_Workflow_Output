# Warehouse Employee Management System

A production-ready Spring Boot 3.x application for managing warehouse employees, shifts, attendance, safety, and more.

## Features

- Employee CRUD with RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- Time & attendance tracking
- Shift scheduling & leave management
- Certification & safety incident tracking
- Equipment/asset assignment
- Performance reviews & payroll export
- Notifications, HRIS/WMS integration, audit trail, reporting
- Mobile PWA support, onboarding/offboarding workflows
- Multi-site/multi-tenant support
- OpenAPI documentation

## Requirements

- Java 17+
- Maven 3.8+
- (Optional) PostgreSQL/MySQL for production

## Build & Run

```bash
mvn clean package
java -jar target/management-1.0.0.jar
```

The app runs on [http://localhost:8080](http://localhost:8080).

## API Docs

See [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Database

- Uses H2 in-memory by default for dev/test.
- Flyway runs migrations on startup.
- Configure `application.yml` for production DB.

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Testing

```bash
mvn test
```