# Warehouse Employee Management System (WEMS)

A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features

- Employee CRUD with RBAC and JWT/OAuth2 security
- Attendance clock-in/out with geofencing
- Shift and schedule management
- Leave and absence workflows
- Certification tracking and alerts
- Safety incident reporting and OSHA export
- Asset assignment and tracking
- Performance reviews and goals
- Notifications (in-app, email, SMS)
- Integration APIs (HRIS, WMS, Payroll)
- Audit logging and compliance
- Reporting and analytics
- PWA mobile access
- Observability (logging, tracing, metrics)
- Localization (i18n)
- Dockerized deployment
- CI/CD pipeline (GitHub Actions)

## Getting Started

1. **Clone the repo**
2. **Configure database** (PostgreSQL)
3. **Run migrations**: `mvn flyway:migrate`
4. **Build**: `mvn clean package`
5. **Run**: `java -jar target/wems-1.0.0.jar`
6. **Access**: [http://localhost:8080](http://localhost:8080)

## Profiles

- `dev`, `staging`, `prod` (see `application.yml`)

## API Docs

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Docker

```bash
docker build -t wems:latest .
docker run -p 8080:8080 wems:latest
```

## License

MIT