# Warehouse Employee Management System

This is a comprehensive Spring Boot Maven project for managing warehouse employees, covering 17 major epics:

## Features
- Employee CRUD with pagination, filtering, soft-delete
- RBAC with Spring Security (ADMIN, HR, SUPERVISOR, WORKER roles)
- Time & Attendance with clock-in/out, geofence validation
- Shift scheduling with templates, assignments, conflict detection
- Leave management with approval workflow
- Certification tracking with expiry alerts
- Safety incident reporting and OSHA compliance
- Equipment/asset assignment with certification checks
- Performance reviews with acknowledgement workflow
- Payroll export integration
- Notifications (in-app, email, SMS)
- Integration APIs for HRIS/WMS
- Audit trail with immutable logging
- Reporting and analytics
- Mobile PWA support
- Onboarding/offboarding automation

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (default config)

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Test
```bash
mvn test
```

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Database Migration
- Flyway runs automatically on startup. Migration scripts are in `src/main/resources/db/migration/`.

## Security
- Spring Security RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- OAuth2/JWT and API Key support

## Project Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/main/java/com/wms/
â   âââ ...
âââ src/main/resources/
â   âââ application.properties
â   âââ db/migration/
â       âââ V1__Initial_Schema.sql
âââ src/test/java/com/wms/
    âââ WmsApplicationTests.java
```

## Contribution
- Please follow standard Java/Spring Boot best practices.
- Inline comments and documentation are included throughout the codebase.

## License
MIT
