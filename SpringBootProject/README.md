# Warehouse Employee Management System

Comprehensive Spring Boot application for warehouse employee management, including RBAC, scheduling, attendance, safety, and more.

## Features
- Employee CRUD with soft delete and filtering
- Role-Based Access Control (ADMIN, HR, SUPERVISOR, WORKER)
- PostgreSQL with Flyway migrations
- RESTful APIs with OpenAPI/Swagger
- Actuator for health monitoring
- Security with API key/OAuth2 toggle
- Ready for unit testing

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

## Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

## Database Setup
- Configure PostgreSQL in `src/main/resources/application.yml`
- Flyway migrations will auto-run on startup

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Health Monitoring
- Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Testing
```bash
mvn test
```

## Directory Structure
- `src/main/java/com/warehousemgmt/domain` - Entities
- `src/main/java/com/warehousemgmt/repository` - Repositories
- `src/main/java/com/warehousemgmt/service` - Services
- `src/main/java/com/warehousemgmt/controller` - Controllers
- `src/main/java/com/warehousemgmt/dto` - DTOs
- `src/main/java/com/warehousemgmt/config` - Configuration
- `src/main/resources/db/migration` - Flyway scripts

## Deployment
- Dockerfile and CI/CD workflow included

## License
MIT
