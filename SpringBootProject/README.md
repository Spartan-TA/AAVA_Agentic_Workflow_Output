# Warehouse Employee Management System (EMS)

A comprehensive Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, payroll, notifications, and more.

## Features
- Employee CRUD with RBAC
- Time & Attendance with geofence and device validation
- Shift & Schedule Management
- Leave & Absence Management
- Training & Certification Tracking
- Safety Incidents & OSHA Reporting
- Equipment & Asset Assignment
- Performance Reviews & Goals
- Payroll Export Integration
- Notifications & Announcements
- Integration Layer (HRIS, WMS, webhooks)
- Audit Trail & Compliance
- Reporting & Analytics
- Mobile Access (PWA)
- Onboarding & Offboarding Workflow

## Technology Stack
- Spring Boot 2.7+
- Java 17+
- Maven 3.8+
- Spring Data JPA
- PostgreSQL 14+
- Flyway for migrations
- Spring Security with JWT
- Springdoc OpenAPI
- Spring Boot Actuator
- Micrometer for metrics

## Project Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/
â   âââ main/
â   â   âââ java/com/warehouse/ems/
â   â   â   âââ config/
â   â   â   âââ controller/
â   â   â   âââ service/
â   â   â   âââ repository/
â   â   â   âââ entity/
â   â   â   âââ dto/
â   â   â   âââ exception/
â   â   â   âââ security/
â   â   â   âââ util/
â   â   âââ resources/
â   â       âââ application.yml
â   â       âââ db/migration/
â   âââ test/java/com/warehouse/ems/
```

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Database Setup
1. Create a PostgreSQL database named `warehouse_ems`.
2. Create a user `ems_user` with password `ems_password` and grant privileges.
3. Flyway will auto-run migrations on startup.

### Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Actuator Endpoints
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Running Tests
```bash
mvn test
```

## Profiles
- `dev` (default): Local development
- `prod`: Production settings

## Security
- JWT-based authentication
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)

## Contribution
1. Fork the repo
2. Create a feature branch
3. Commit and push changes
4. Open a pull request

## License
MIT
