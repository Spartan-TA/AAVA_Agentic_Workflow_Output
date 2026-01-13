# Warehouse Employee Management System

Enterprise-grade Spring Boot application for managing warehouse employees, attendance, scheduling, safety, certifications, and more.

## Features
- Employee master data CRUD
- Role-based access control (RBAC)
- Time & attendance tracking
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incidents & OSHA reporting
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications & announcements
- Integration layer (HRIS/WMS)
- Audit trail & compliance
- Reporting & analytics
- Mobile access (PWA)
- Onboarding & offboarding workflows
- Localization & multi-tenant support
- Observability & monitoring
- CI/CD pipeline & Docker support

## Tech Stack
- Spring Boot 3.2.x (Java 17)
- Maven
- PostgreSQL 15+
- Spring Security (JWT/OAuth2)
- Spring Data JPA
- Flyway
- SpringDoc OpenAPI 3
- Actuator + Prometheus
- JUnit 5 + Mockito
- Docker
- GitHub Actions

## Build Instructions
```bash
mvn clean install
```

## Run Instructions
```bash
mvn spring-boot:run
```

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Testing Instructions
```bash
mvn test
```

## Deployment Guide
- Build Docker image:
  ```bash
  docker build -t warehouse-employee-mgmt .
  ```
- Run Docker container:
  ```bash
  docker run -p 8080:8080 warehouse-employee-mgmt
  ```
- CI/CD pipeline is configured via GitHub Actions in `.github/workflows/ci-cd.yml`

## Configuration
- Main config: `src/main/resources/application.properties`
- Environment overrides: `application-dev.properties`, `application-prod.properties`

## Database
- PostgreSQL 15+
- Flyway migrations in `src/main/resources/db/migration/`

## Contact
For issues or contributions, please open a GitHub issue or pull request.
