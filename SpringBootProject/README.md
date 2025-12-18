# Warehouse Employee Management System (EMS)

A comprehensive, production-ready Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, assets, payroll, and more.

## Features
- Employee CRUD & master data
- Role-based access control (RBAC) with JWT
- Time & attendance (clock in/out, corrections)
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incidents & OSHA reporting
- Asset assignment & tracking
- Performance reviews & goals
- Payroll export integration
- Notifications & announcements
- Integration layer (HRIS/WMS APIs)
- Audit trail & compliance
- Reporting & analytics
- Mobile access (PWA-ready)
- Onboarding & offboarding workflows
- Localization & multi-warehouse
- Advanced scheduling (AI-assisted)
- Document management

## Tech Stack
- Java 17
- Spring Boot 3.x
- Spring Security (JWT)
- Spring Data JPA (PostgreSQL/MySQL)
- Flyway for DB migrations
- Springdoc OpenAPI
- Maven

## Project Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/main/java/com/warehouse/ems/
â   âââ WarehouseEmsApplication.java
â   âââ config/
â   âââ employee/
â   âââ attendance/
â   âââ scheduling/
â   âââ ... (other modules)
â   âââ exception/
âââ src/main/resources/
â   âââ application.yml
â   âââ db/migration/
âââ src/test/java/com/warehouse/ems/
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL or MySQL

### Build
```
mvn clean install
```

### Run (Dev)
```
mvn spring-boot:run
```

The app will start on [http://localhost:8080](http://localhost:8080)

### Test
```
mvn test
```

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Database Migrations
- Flyway runs automatically on startup. Migration scripts are in `src/main/resources/db/migration/`.

### Security
- JWT-based authentication
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- See `application.yml` for security config

### Profiles
- `application-dev.yml` (default)
- `application-prod.yml`

### Health Check
- Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Contributing
1. Fork the repo
2. Create a feature branch
3. Commit your changes
4. Open a pull request

## License
MIT
