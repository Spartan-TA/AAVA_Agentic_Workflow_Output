# Warehouse Employee Management System

This is a comprehensive Spring Boot 3.x multi-module application for managing warehouse employees, attendance, scheduling, safety, assets, and integrations with HRIS/WMS systems.

## Modules
- **core**: Domain entities, repositories, services, business logic
- **api**: REST controllers, DTOs, security configuration
- **integration**: External API connectors, HRIS/WMS sync, SSO

## Prerequisites
- Java 17+
- Maven 3.8+
- Docker (optional, for DB)

## Build & Run
1. Clone the repo:
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```
2. Build:
   ```bash
   mvn clean install
   ```
3. Run:
   ```bash
   mvn spring-boot:run -pl api
   ```
   The app runs on port 8080 by default.

## Database
- Uses PostgreSQL by default (see `application.yml`)
- Flyway migrations auto-run on startup
- To run locally:
   ```bash
   docker run --name warehouse-db -e POSTGRES_DB=warehouse -e POSTGRES_USER=warehouse -e POSTGRES_PASSWORD=warehouse -p 5432:5432 -d postgres:15
   ```

## API Docs
- OpenAPI/Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Security
- Spring Security with OAuth2, RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- API key toggle via config

## CI/CD
- See `.github/workflows/ci.yml` for pipeline config

## Health Check
- Actuator endpoint: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Contact
For issues or contributions, open a GitHub issue or pull request.
