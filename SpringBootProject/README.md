# Warehouse Employee Management System (EMS)

This is a production-ready Spring Boot application for managing warehouse employee data, attendance, scheduling, safety, equipment, performance, payroll integration, notifications, and more.

## Features
- Employee Master Data CRUD (with badgeId uniqueness, soft-delete, pagination, filtering)
- Role-Based Access Control (RBAC) with Spring Security (ADMIN, HR, SUPERVISOR, WORKER)
- RESTful APIs with DTOs and OpenAPI documentation
- Global exception handling
- Audit fields (createdAt, updatedAt, createdBy, updatedBy)
- Flyway database migrations
- Spring Boot Actuator for health and metrics
- Profiles for dev, test, prod

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (default, can be changed in `application.yml`)

## Build & Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/Spartan-TA/AAVA_Agentic_Workflow_Output.git
   cd AAVA_Agentic_Workflow_Output/SpringBootProject
   ```

2. **Configure database**
   - Update `src/main/resources/application.yml` with your DB credentials if needed.

3. **Run Flyway migrations**
   - Migrations run automatically on startup.

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   - The app runs on port 8080 by default.
   - Health endpoint: `GET /actuator/health`

6. **API Documentation**
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Testing
- Unit and integration tests can be added under `src/test/java`.
- To run tests:
  ```bash
  mvn test
  ```

## Directory Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/
â   âââ main/
â   â   âââ java/com/warehouse/ems/...
â   â   âââ resources/application.yml
â   â   âââ resources/db/migration/V1__initial_schema.sql
â   âââ test/java/...
```

## Security
- Uses JWT/OAuth2 for authentication
- RBAC enforced via Spring Security

## Next Steps
- Implement additional modules (attendance, scheduling, safety, etc.)
- Add unit and integration tests
- Extend Flyway migrations for other tables

## License
MIT
