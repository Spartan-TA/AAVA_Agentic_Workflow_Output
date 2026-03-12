# Warehouse Employee Management System

A production-ready Spring Boot application for managing warehouse employees, covering 20+ modules including Employee CRUD, RBAC, Attendance, Shift, Leave, Training, Safety, Equipment, Performance, Payroll, Notifications, Integration, Audit, Reporting, Mobile PWA, Onboarding/Offboarding, Localization, AI Scheduling, CI/CD, and Observability.

## Technology Stack
- Spring Boot 3.x
- Maven
- PostgreSQL
- Flyway
- Spring Security (OAuth2)
- Springdoc OpenAPI
- Spring Boot Actuator
- Prometheus
- Redis

## Project Structure
```
com.warehouse.management
âââ config (Security, Cache, Async, etc.)
âââ common (Utils, Constants, Base classes)
âââ employee (Entity, Repository, Service, Controller, DTO)
âââ attendance (Entity, Repository, Service, Controller, DTO)
âââ shift (Entity, Repository, Service, Controller, DTO)
âââ leave (Entity, Repository, Service, Controller, DTO)
âââ training (Entity, Repository, Service, Controller, DTO)
âââ safety (Entity, Repository, Service, Controller, DTO)
âââ equipment (Entity, Repository, Service, Controller, DTO)
âââ performance (Entity, Repository, Service, Controller, DTO)
âââ payroll (Entity, Repository, Service, Controller, DTO)
âââ notification (Entity, Repository, Service, Controller, DTO)
âââ integration (Controllers for external APIs)
âââ audit (Entity, Repository, Service, Aspect)
âââ reporting (Service, Controller)
âââ exception (Custom exceptions, Global handler)
```

## Build & Run
1. **Build:**
   ```bash
   mvn clean install
   ```
2. **Run:**
   ```bash
   mvn spring-boot:run
   ```
   or
   ```bash
   java -jar target/management-1.0.0.jar
   ```
3. **Test:**
   ```bash
   mvn test
   ```

## Configuration
- Edit `src/main/resources/application.yml` for DB, Redis, and profiles.
- Flyway migrations are in `src/main/resources/db/migration/`.

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Health & Metrics
- Actuator endpoints: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`

## Security
- OAuth2 RBAC: Roles (ADMIN, HR, SUPERVISOR, WORKER)
- Row-level security for sensitive data

## Features
- Employee CRUD with soft-delete, pagination, filtering
- Attendance, Shift, Leave, Training, Safety, Equipment, Performance, Payroll, Notification modules
- Audit logging with AOP
- Caching with Redis
- Exception handling with @ControllerAdvice
- OpenAPI documentation
- Observability with Prometheus

## License
MIT
