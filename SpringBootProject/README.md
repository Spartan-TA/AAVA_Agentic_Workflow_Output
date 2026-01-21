# Warehouse Employee Management System

A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, payroll, and more.

## Features

- Employee CRUD with RBAC
- Attendance & shift management
- Leave, certification, safety, asset, and review modules
- Payroll, notifications, audit, reporting, document management
- RESTful APIs with OpenAPI docs
- PostgreSQL, Flyway migrations
- JWT/OAuth2 security
- Unit & integration tests

## Build & Run

```bash
mvn clean install
java -jar target/warehouse-employee-mgmt-0.0.1-SNAPSHOT.jar
```

## API Docs

Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Database

- PostgreSQL required (see `application.yml`)
- Migrations auto-run via Flyway

## Health Check

`GET /actuator/health` returns `UP` if running.

## Testing

```bash
mvn test
```