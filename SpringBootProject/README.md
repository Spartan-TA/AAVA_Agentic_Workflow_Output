# Warehouse Employee Management System

A comprehensive, production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, and compliance.

## Features
- Employee master data CRUD (with soft-delete, pagination, filtering)
- Role-based access control (ADMIN, HR, SUPERVISOR, WORKER)
- Time & attendance tracking
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incidents & OSHA reporting
- Equipment & asset assignment
- Performance reviews
- Payroll export integration
- Notifications & announcements
- Integration layer (HRIS, WMS, SSO)
- Audit trail & compliance
- Reporting & analytics
- Mobile access (PWA-ready)
- Onboarding & offboarding workflows

## Tech Stack
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Spring Security
- Flyway (DB migrations)
- PostgreSQL
- Actuator
- OpenAPI/Swagger
- Lombok

## Project Structure
```
SpringBootProject/
âââ pom.xml
âââ src/main/java/com/warehouse/
â   âââ employee/
â   âââ scheduling/
â   âââ attendance/
â   âââ safety/
â   âââ security/
â   âââ certification/
â   âââ asset/
â   âââ performance/
â   âââ payroll/
â   âââ notification/
â   âââ integration/
â   âââ audit/
â   âââ reporting/
â   âââ config/
â   âââ exception/
â   âââ dto/
â   âââ util/
âââ src/main/resources/
â   âââ application.yml
â   âââ db/migration/
âââ README.md
```

## Build & Run

1. **Configure Database**
   - Update `spring.datasource.*` in `application.yml` for your PostgreSQL instance.
2. **Build**
   - `mvn clean install`
3. **Run**
   - `mvn spring-boot:run` or `java -jar target/warehouse-employee-management-1.0.0.jar`
4. **API Docs**
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
5. **Health Check**
   - [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Testing
- Unit and integration tests can be run with `mvn test`.
- Test coverage for security, business logic, and API endpoints.

## Contributing
- Follow standard Java/Spring Boot best practices.
- Use feature branches and submit PRs for review.

## License
MIT
