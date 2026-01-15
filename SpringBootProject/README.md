# Warehouse Employee Management System (WMS)

A comprehensive, production-grade, multi-module Spring Boot 3.x application for managing warehouse employees and operations.

## Modules
- **employee**: Employee management (CRUD, profiles, roles)
- **scheduling**: Shift and work scheduling
- **attendance**: Attendance tracking
- **safety**: Safety compliance and incident management
- **equipment**: Equipment assignment and tracking
- **training**: Employee training records
- **payroll**: Payroll processing
- **notifications**: Email/SMS notifications
- **integration**: External system integrations
- **audit**: Audit logging
- **reporting**: Reports and analytics
- **common**: Shared utilities and base classes

## Tech Stack
- Java 17+
- Spring Boot 3.x
- PostgreSQL
- Flyway (DB migrations)
- Spring Security (RBAC)
- Maven (multi-module)
- RESTful APIs

## Getting Started
1. **Clone the repository**
2. **Build the project**
   ```bash
   mvn clean install
   ```
3. **Configure the database**
   - Update `application.yml` with your PostgreSQL credentials.
4. **Run the application**
   ```bash
   mvn spring-boot:run -pl employee
   ```
   (or any other module)

## RBAC Roles
- `ADMIN`
- `HR`
- `SUPERVISOR`
- `WORKER`

## API Documentation
- Swagger/OpenAPI available at `/swagger-ui.html` (per module)

## Contributing
- Follow standard Java/Spring Boot best practices
- Write unit and integration tests
- Document code with Javadoc and comments

## License
MIT
