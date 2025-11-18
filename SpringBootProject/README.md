# Warehouse Employee Management System

This is a production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features
- Employee CRUD & master data
- Role-based access control (RBAC) with OAuth2/JWT
- Attendance clock-in/out, corrections, and reporting
- Shift & schedule management
- Leave & absence management
- Training & certification tracking
- Safety incident logging & OSHA reporting
- Equipment & asset assignment
- Performance reviews & goals
- Payroll export integration
- Notifications (in-app, email, SMS)
- Integration layer for HRIS/WMS
- Audit trail & compliance
- Reporting & analytics
- Mobile access (PWA)
- Onboarding & offboarding workflows

## Tech Stack
- Java 11+
- Spring Boot 2.7+
- Maven
- PostgreSQL
- Flyway
- Spring Security (OAuth2/JWT)
- Lombok
- OpenAPI (Swagger)

## Build & Run

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd employee-management
   ```
2. **Configure Database**
   - Update `src/main/resources/application.yml` with your PostgreSQL credentials.
3. **Run Flyway Migration**
   - Flyway will auto-run on startup to create all tables.
4. **Build the project**
   ```bash
   mvn clean install
   ```
5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   - The app runs on port `8080`.
   - Health endpoint: `GET /actuator/health`
6. **API Documentation**
   - Swagger UI: `GET /swagger-ui.html`

## Testing
- Unit and integration tests can be added under `src/test/java`.
- Use `mvn test` to run all tests.

## Directory Structure
```
SpringBootProject/
âââ pom.xml
âââ README.md
âââ src/main/java/com/warehouse/employee/
â   âââ config/
â   âââ domain/
â   âââ repository/
â   âââ service/
â   âââ controller/
â   âââ dto/
â   âââ exception/
âââ src/main/resources/
â   âââ application.yml
â   âââ db/migration/
â       âââ V1__Initial_Schema.sql
âââ src/main/resources/static/
    âââ manifest.json
```

## Security
- OAuth2 JWT resource server
- RBAC enforced via roles: ADMIN, HR, SUPERVISOR, WORKER
- Method-level security with `@PreAuthorize`

## Contribution
- Follow Java/Spring Boot best practices
- Use DTOs for all API requests/responses
- Add JavaDoc and inline comments
- Use proper exception handling and validation

## License
MIT
