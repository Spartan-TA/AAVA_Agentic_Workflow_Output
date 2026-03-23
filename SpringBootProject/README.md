# Warehouse Employee Management System (EMS)

A modular Spring Boot application for warehouse employee management, attendance, scheduling, safety, and compliance.

## Features
- Employee CRUD & master data
- Role-based access control (RBAC)
- Time & attendance (clock in/out)
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incidents & OSHA reporting
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications & announcements
- Integration layer (HRIS/WMS APIs)
- Audit trail & compliance
- Reporting & analytics
- Mobile access (PWA)
- Onboarding & offboarding workflow

## Technology Stack
- Java 17
- Spring Boot 3.2.x
- Spring Data JPA, Security, Validation, Actuator
- PostgreSQL 15+
- Flyway for DB migrations
- OpenAPI 3 (Swagger)
- JUnit 5, Mockito, Testcontainers
- Docker

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 15+

### Build
```bash
mvn clean install
```

### Run
```bash
# Start PostgreSQL (if not already running)
# Update application.yml if needed for DB credentials
mvn spring-boot:run
```

The application will start on [http://localhost:8080](http://localhost:8080)

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Docs: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Health Check
- Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Database Migrations
- Flyway runs automatically on startup.

### Test
```bash
mvn test
```

## Project Structure
- `src/main/java/com/warehouse/ems/` - Main application code
- `src/main/resources/db/migration/` - Flyway migration scripts
- `src/main/resources/application.yml` - Configuration

## Modules
- employee, attendance, scheduling, leave, training, safety, asset, performance, payroll, notification, integration, audit, reporting, security

## License
MIT
