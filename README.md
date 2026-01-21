# Warehouse Employee Management System

This is a production-ready Spring Boot 3.1.0 application for managing warehouse employees, shifts, attendance, safety, assets, payroll, and more.

## Technology Stack
- Spring Boot 3.1.0
- Java 17
- PostgreSQL
- Maven
- Flyway
- Spring Security (OAuth2/JWT)
- Spring Data JPA
- Spring Boot Actuator

## Build & Run

### Prerequisites
- Java 17
- Maven 3.8+
- PostgreSQL running on `localhost:5432` with database `warehouse`, user `warehouse_user`, password `warehouse_pass`

### Setup Database
```
CREATE DATABASE warehouse;
CREATE USER warehouse_user WITH PASSWORD 'warehouse_pass';
GRANT ALL PRIVILEGES ON DATABASE warehouse TO warehouse_user;
```

### Build
```
mvn clean package
```

### Run
```
mvn spring-boot:run
```

The application will start on [http://localhost:8080](http://localhost:8080)

### API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Docs: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Health Check
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Flyway
- Database migrations are applied automatically on startup.

## Modules
- Employee, Scheduling, Attendance, Safety, Training, Assets, Performance, Payroll, Notifications, Integration, Audit, Reporting, and more.

## Disaster Recovery
- Backup scripts are in `/scripts` (see documentation).

## Performance Testing
- JMeter test plans are in `/performance-tests`.

## Localization
- See `src/main/resources/messages*.properties`.

## Contact
- For support, contact the IT team.
