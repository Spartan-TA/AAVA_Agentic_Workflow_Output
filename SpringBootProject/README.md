# Warehouse Employee Management System

## Overview

This is a production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, leave, certifications, safety, assets, performance reviews, payroll, notifications, integrations, audit trails, reporting, mobile access, onboarding/offboarding, localization, advanced scheduling, and CI/CD.

## Technology Stack

- Spring Boot 3.2.1 (Java 17)
- PostgreSQL 15.x
- Redis 7.x
- Spring Security 6.2.x (JWT)
- Spring Data JPA 3.2.x
- Flyway 9.x
- Springdoc OpenAPI 2.3.x
- Maven 3.9.x

## Build & Run

1. **Database Setup**
   - Create a PostgreSQL database named `warehouse_db`.
   - Create a user `warehouse_user` with password `warehouse_pass`.
   - Ensure Redis is running on `localhost:6379`.

2. **Build**
   ```bash
   mvn clean install
   ```

3. **Run**
   ```bash
   mvn spring-boot:run
   ```

4. **API Documentation**
   - Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

5. **Health Check**
   - Visit [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Directory Structure

- `src/main/java/com/warehouse/management/` - Source code
- `src/main/resources/application.yml` - Configuration
- `src/main/resources/db/migration/` - Flyway migration scripts

## Features

- Employee CRUD with validation, caching, and OpenAPI docs
- JWT-secured endpoints with RBAC
- Attendance tracking and reporting
- Shift scheduling with conflict detection
- Leave management and accruals
- Certification tracking and alerts
- Safety incident workflow and OSHA exports
- Asset management and history
- Performance reviews and PDF exports
- Payroll integration and audit logs
- Notifications (in-app, email, SMS)
- HRIS/WMS integration APIs
- Centralized audit trail
- Reporting and analytics
- Mobile PWA support
- Automated onboarding/offboarding
- Localization
- Advanced scheduling
- CI/CD pipeline and observability

## Testing

Run unit tests:
```bash
mvn test
```

## License

MIT