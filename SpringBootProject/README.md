# Warehouse Employee Management System (EMS)

A comprehensive Spring Boot 3.x Maven application for managing warehouse employees, attendance, scheduling, safety, training, assets, payroll, and integrations.

## Features
- Employee CRUD & master data
- Role-based access control (RBAC)
- Time & attendance clock in/out
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incidents & OSHA reporting
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications & announcements
- HRIS/WMS API integration
- Audit trail & compliance
- Reporting & analytics
- Mobile PWA access
- Onboarding & offboarding workflows
- Overtime & compliance rules
- Localization & multi-warehouse support
- Unit, integration, and load tests
- Swagger/OpenAPI documentation

## Project Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/
â   âââ main/
â   â   âââ java/
â   â   â   âââ com/
â   â   â       âââ warehouse/
â   â   â           âââ ems/
â   â   â               âââ WarehouseEmsApplication.java
â   â   â               âââ config/
â   â   â               âââ employee/
â   â   â               âââ attendance/
â   â   â               âââ scheduling/
â   â   â               âââ leave/
â   â   â               âââ training/
â   â   â               âââ safety/
â   â   â               âââ asset/
â   â   â               âââ performance/
â   â   â               âââ payroll/
â   â   â               âââ notification/
â   â   â               âââ integration/
â   â   â               âââ audit/
â   â   â               âââ reporting/
â   â   â               âââ common/
â   â   âââ resources/
â   â       âââ application.properties
â   â       âââ db/
â   â           âââ migration/
â   âââ test/
â       âââ java/
```

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (default, can use H2 for dev)

## Build & Run
1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. **Build the project:**
   ```bash
   mvn clean install
   ```
3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   The app runs on `http://localhost:8080`.

## Database Migration
- Flyway runs automatically on startup.
- Migration scripts are in `src/main/resources/db/migration/`.

## API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Health Check
- Actuator endpoint: `http://localhost:8080/actuator/health`

## Testing
- Unit and integration tests: `mvn test`
- Coverage reports: `target/site/jacoco/index.html`

## Configuration
- Main config: `src/main/resources/application.properties`
- Security, DB, and other configs in `config/` package

## Modules
- Each feature is in its own package under `com.warehouse.ems`
- Entities, repositories, services, controllers, DTOs, and configs are organized by module

## Contributing
- Follow Java/Spring Boot best practices
- Write unit tests for all new code
- Document public APIs and business logic

## License
MIT
