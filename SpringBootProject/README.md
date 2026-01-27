# Warehouse Employee Management System

A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features

- Employee CRUD with RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- Attendance, shift, leave, certification, safety, asset, performance, payroll, notifications, audit, reporting, mobile PWA, onboarding/offboarding, localization, observability, CI/CD
- RESTful APIs with OpenAPI documentation
- Flyway database migrations
- Spring Security with JWT/OAuth2
- Docker-ready

## Build & Run

```bash
# Build
mvn clean package

# Run (dev profile)
SPRING_PROFILES_ACTIVE=dev java -jar target/employee-management-1.0.0.jar

# Run (prod profile)
SPRING_PROFILES_ACTIVE=prod java -jar target/employee-management-1.0.0.jar
```

## Test

```bash
mvn test
```

## API Docs

Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Database

- Dev: H2 (in-memory)
- Prod: PostgreSQL (configure in `application-prod.yml`)

## Docker

```bash
docker build -t warehouse-employee-mgmt .
docker run -p 8080:8080 warehouse-employee-mgmt
```

## Configuration

Edit `src/main/resources/application.yml` and environment-specific files for DB, security, integrations.

## License

MIT