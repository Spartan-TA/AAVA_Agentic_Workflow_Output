# Warehouse Employee Management System

This is a Spring Boot application for managing warehouse employees, attendance, shifts, leave, certifications, safety, assets, reviews, payroll, notifications, integrations, audit, reporting, mobile access, onboarding/offboarding, localization, batch jobs, and CI/CD.

## Features
- Employee CRUD (name, badgeId, role, department, shiftGroup, hireDate, status)
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- Attendance, shift, leave, certification, safety, asset, review, payroll, notification, integration, audit, reporting, mobile, onboarding/offboarding, localization, batch jobs, CI/CD
- RESTful APIs with OpenAPI/Swagger
- Flyway DB migrations
- Spring Security
- Actuator health endpoints

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

Application runs on port 8080.

### API Docs
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Health Check
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Test
```bash
mvn test
```

## Database
- Uses H2 in-memory DB for development/testing
- Flyway migrations auto-run on startup

## Authentication
- Default user: admin / admin (role: ADMIN)

## Directory Structure
```
SpringBootProject/
âââ src/main/java/com/warehouse/employee/management/
â   âââ Application.java
â   âââ model/
â   âââ repository/
â   âââ service/
â   âââ controller/
âââ src/main/resources/
â   âââ application.properties
â   âââ db/migration/
âââ README.md
âââ pom.xml
```

## Extending
Add new domain models, repositories, services, controllers, and migrations as per the epics.

## License
MIT
