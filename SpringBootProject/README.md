# Warehouse Employee Management System

A production-ready Spring Boot 3.2.x application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features
- Modular architecture: employee, scheduling, attendance, safety, asset, reporting, integration, common
- PostgreSQL database with Flyway migrations
- Spring Security with JWT/OAuth2
- Spring Data JPA repositories
- RESTful controllers with OpenAPI docs
- Service layer with transaction management
- DTOs with Bean Validation
- Lombok and MapStruct for boilerplate reduction
- Actuator for health checks
- Micrometer/Prometheus for metrics
- Structured JSON logging
- Ready for unit/integration testing

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

## Database Setup
1. Create a PostgreSQL database and user:
   ```sql
   CREATE DATABASE warehouse;
   CREATE USER warehouse_user WITH PASSWORD 'warehouse_pass';
   GRANT ALL PRIVILEGES ON DATABASE warehouse TO warehouse_user;
   ```
2. Flyway will run migrations automatically on startup.

## Build & Run
```bash
mvn clean package
java -jar target/warehouse-employee-management-1.0.0.jar
```

The app runs on [http://localhost:8080](http://localhost:8080)

## API Documentation
- OpenAPI/Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Health & Metrics
- Actuator health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- Prometheus metrics: [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)

## Testing
```bash
mvn test
```

## Project Structure
- `src/main/java/com/company/warehouse/employee` - Employee domain (CRUD, RBAC, etc.)
- `src/main/java/com/company/warehouse/scheduling` - Shift & schedule management
- `src/main/java/com/company/warehouse/attendance` - Time & attendance
- `src/main/java/com/company/warehouse/safety` - Safety, certifications, incidents
- `src/main/java/com/company/warehouse/asset` - Asset assignment
- `src/main/java/com/company/warehouse/reporting` - Reporting & analytics
- `src/main/java/com/company/warehouse/integration` - Integration layer
- `src/main/java/com/company/warehouse/common` - Shared utilities, exceptions

## Configuration
- Edit `src/main/resources/application.yml` for DB, security, and other settings.

## License
MIT
