# Warehouse Employee Management System

## Overview
This is a production-ready Spring Boot 3.x Maven application for managing warehouse employees, attendance, scheduling, safety, assets, and compliance. It supports multi-tenant, RBAC, mobile PWA, and integrates with HRIS/WMS/IDP systems.

## Modules
- **Employee Master Data**: CRUD for employees, roles, departments, shifts
- **Scheduling & Attendance**: Shift templates, clock-in/out, leave management
- **Safety & Compliance**: Incident reporting, OSHA, certifications
- **Assets & Equipment**: Assignment, condition tracking
- **Performance & Payroll**: Reviews, goals, payroll export
- **Notifications & Integration**: In-app/email/SMS, webhooks, REST APIs
- **Audit & Reporting**: Centralized audit trail, analytics, dashboards
- **Mobile Access**: PWA for core flows

## Build & Run
```bash
mvn clean install
mvn spring-boot:run
```
App runs on [http://localhost:8080](http://localhost:8080)

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Database
- Default: PostgreSQL
- Migrations: Flyway (see `src/main/resources/db/migration`)

## Testing
- Unit tests: `mvn test`
- Coverage: JUnit & Mockito

## Security
- RBAC: ADMIN, HR, SUPERVISOR, WORKER
- OAuth2/JWT and API Key toggle

## Observability
- Actuator: `/actuator/health`, `/actuator/prometheus`
- Tracing: Jaeger/Zipkin

## CI/CD
- GitHub Actions/Jenkins pipeline for build, test, Docker, deploy

## Localization & Multi-Tenant
- Locale-aware, timezone support
- Tenant isolation via tenant ID

## Contribution
See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License
MIT
