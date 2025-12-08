# Warehouse Employee Management System (EMS)

A comprehensive, production-ready Spring Boot application for managing warehouse employees, scheduling, attendance, safety, certifications, assets, performance reviews, payroll integration, and more.

## Features

- Modular monolith architecture
- Employee CRUD with RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- Attendance, scheduling, leave, certification, safety, asset, and review management
- RESTful APIs with OpenAPI/Swagger docs
- Flyway database migrations
- Spring Security with method/endpoint security
- Actuator endpoints for monitoring
- Exception handling, audit logging, and reporting

## Requirements

- Java 17+
- Maven 3.6+
- MySQL 8+

## Build & Run

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd SpringBootProject
   ```

2. **Configure the database**
   - Edit `src/main/resources/application.yml` with your DB credentials.

3. **Run Flyway migrations**
   - Migrations run automatically on startup.

4. **Build and run**
   ```bash
   mvn clean package
   java -jar target/ems-1.0.0.jar
   ```

5. **Access**
   - API: `http://localhost:8080/`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Actuator: `http://localhost:8080/actuator/health`

## Testing

```bash
mvn test
```

## Project Structure

See `/src/main/java/com/warehouse/ems/` for modules:
- `employee`, `attendance`, `scheduling`, `leave`, `certification`, `safety`, `asset`, `review`, `common`

## Security

- Default users/roles are seeded in migration scripts.
- See `application.yml` for authentication config.

## License

MIT