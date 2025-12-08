# Warehouse Employee Management System

This is a production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features
- Employee master data management
- Role-based access control (RBAC)
- Time & attendance tracking
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incident reporting
- Equipment & asset assignment
- Performance reviews
- Payroll export integration
- Notifications & announcements
- HRIS/WMS API integration
- Audit trail & compliance
- Reporting & analytics
- Mobile PWA support
- Onboarding/offboarding workflows
- Multi-tenant & localization
- Observability (metrics, logging)
- CI/CD pipeline

## Tech Stack
- Spring Boot 2.7+
- Java 11+
- Maven
- Spring Data JPA
- Spring Security
- Flyway
- Spring Boot Actuator
- OpenAPI
- Docker, Kubernetes (optional)

## Build & Run

### Prerequisites
- Java 11+
- Maven 3.6+
- Docker (optional)

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```
Or with Docker:
```bash
docker-compose up --build
```

### Test
```bash
mvn test
```

### API Docs
OpenAPI docs available at: `http://localhost:8080/swagger-ui.html`

### Database Migrations
Flyway runs automatically on startup. Migration scripts are in `src/main/resources/db/migration/`.

## Directory Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ Dockerfile
âââ docker-compose.yml
âââ .github/workflows/ci.yml
âââ src/main/java/com/company/wms/
â   âââ ...
âââ src/main/resources/
â   âââ application.yml
â   âââ messages_en.properties
â   âââ messages_es.properties
â   âââ db/migration/
â       âââ V1__create_employee_table.sql
â       âââ ...
```

## CI/CD
GitHub Actions pipeline defined in `.github/workflows/ci.yml`.

## Contact
For questions, contact the engineering team at engineering@company.com
