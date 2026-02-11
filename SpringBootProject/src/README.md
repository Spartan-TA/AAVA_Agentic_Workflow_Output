# Warehouse Employee Management System

This is a comprehensive, production-ready Spring Boot 3.x application for managing warehouse employees, including RBAC, attendance, shift management, leave, certifications, safety, assets, reviews, payroll, notifications, integrations, audit, reporting, mobile PWA, onboarding/offboarding, localization, observability, and CI/CD.

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or H2 for dev)

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Database
- Configure datasource in `src/main/resources/application.yml`
- Flyway migrations are auto-applied

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Health Check
- Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### CI/CD
- See `.github/workflows/ci-cd.yml` for pipeline
- See `kubernetes/` for deployment manifests

## License
MIT
