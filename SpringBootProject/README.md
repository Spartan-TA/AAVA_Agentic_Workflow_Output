# Warehouse Employee Management System (EMS)

This is a Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, assets, and more. It implements 17 core epics for a comprehensive EMS solution.

## Features
- Employee CRUD with role-based access
- Attendance clock-in/out with geofence
- Shift & schedule management
- Leave & absence workflows
- Training & certification tracking
- Safety incident logging & OSHA reporting
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications & announcements
- Integration APIs (HRIS, WMS, IDP)
- Audit trail & compliance
- Reporting & analytics
- Mobile PWA access
- Onboarding & offboarding automation

## Tech Stack
- Java 17+
- Spring Boot 3.x
- Maven
- PostgreSQL
- Spring Data JPA
- Spring Security
- Flyway/Liquibase
- OpenAPI/Swagger
- Actuator

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL database

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```
Application runs on port 8080 by default.

### Database Setup
- Ensure PostgreSQL is running and accessible.
- Update `src/main/resources/application.yml` with your DB credentials.
- Flyway/Liquibase migrations will run automatically on startup.

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Docs: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Health Check
- Actuator endpoint: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Testing
```bash
mvn test
```

## Project Structure
```
SpringBootProject/
âââ src/main/java/com/warehouse/ems/
â   âââ config/
â   âââ controller/
â   âââ domain/
â   âââ dto/
â   âââ repository/
â   âââ security/
â   âââ service/
â   âââ WarehouseEmsApplication.java
âââ src/main/resources/
â   âââ application.yml
â   âââ db/
â       âââ migration/
â       âââ changelog/
âââ pom.xml
âââ README.md
```

## Contributing
Pull requests are welcome. Please add tests for new features and follow code style guidelines.

## License
MIT
