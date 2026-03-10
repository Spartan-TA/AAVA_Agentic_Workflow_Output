# Warehouse Employee Management System

A production-ready, modular Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, assets, and performance reviews.

## Modules

- `employee`: Employee master data and CRUD
- `attendance`: Time & attendance tracking
- `schedule`: Shift and schedule management
- `safety`: Safety incidents and OSHA reporting
- `asset`: Equipment and asset assignment
- `review`: Performance reviews and goals
- `common`: Shared DTOs, exceptions, and utilities

## Requirements

- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Docker (optional for local DB)

## Build & Run

1. **Clone the repo**
   ```bash
   git clone <repo-url>
   cd warehouse-employee-mgmt
   ```

2. **Configure DB**
   - Update `application.yml` with your PostgreSQL credentials.

3. **Build**
   ```bash
   mvn clean install
   ```

4. **Run**
   ```bash
   cd employee
   mvn spring-boot:run
   ```

5. **API Docs**
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

6. **Actuator**
   - Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
   - Metrics: [http://localhost:8080/actuator/metrics](http://localhost:8080/actuator/metrics)

## Testing

- Unit and integration tests can be added under each module's `src/test/java` directory.

## Security

- OAuth2/JWT and API Key toggle supported.
- RBAC enforced via Spring Security.

## Flyway

- DB migrations are auto-applied on startup.

## Contribution

- Follow standard Java and Spring Boot best practices.
- Use Lombok for boilerplate reduction.
- All code must be covered by unit tests.

---

**This is a partial codebase (employee & common modules) as an example. Repeat the above structure for attendance, schedule, safety, asset, and review modules, following the same conventions and requirements.**