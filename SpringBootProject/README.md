# Warehouse EMS (Employee Management System)

Warehouse EMS is a production-ready, modular Spring Boot 3.x application for managing warehouse employees, attendance, shifts, leave, certifications, safety, assets, reviews, payroll, notifications, audits, reports, and integrations.

## Features
- Employee management (CRUD, search, roles)
- Attendance tracking (clock-in/out, history)
- Shift scheduling and blackout dates
- Leave requests and balances
- Certification tracking
- Safety incident reporting and investigation
- Asset management and assignments
- Performance reviews
- Payroll exports
- Notifications and announcements
- Auditing and reporting
- Integration endpoints
- JWT-based security
- Centralized exception handling
- Database migrations with Flyway
- Profiles for dev/prod

## Architecture Overview
- **Spring Boot 3.x** (Java 17+)
- **JPA/Hibernate** for ORM
- **Spring Security** with JWT authentication
- **RESTful API** (controllers, DTOs, services, repositories)
- **Flyway** for DB migrations
- **Actuator** for monitoring
- **Layered modules**: security, employee, attendance, shift, leave, certification, safety, asset, review, payroll, notification, audit, report, integration, exception

## Directory Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ .gitignore
âââ src/
â   âââ main/
â   â   âââ java/com/warehouse/ems/
â   â   â   âââ WarehouseEmsApplication.java
â   â   â   âââ ... (modules)
â   â   âââ resources/
â   â       âââ application.properties
â   â       âââ application-dev.properties
â   â       âââ application-prod.properties
â   â       âââ db/migration/
â   â           âââ V1__Create_Employee_Table.sql
â   â           âââ ...
```

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (or compatible DB)

### Build
```bash
mvn clean package
```

### Run (Dev Profile)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run (Prod Profile)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### API Docs
- Swagger/OpenAPI available at `/swagger-ui.html` (if enabled)

### Database Migrations
- Flyway auto-runs on startup using scripts in `src/main/resources/db/migration/`

## Security
- JWT-based authentication
- Role-based authorization
- Passwords stored securely (BCrypt)

## Contribution
1. Fork the repo
2. Create a feature branch
3. Commit and push
4. Open a pull request

## License
MIT License
