# Warehouse Employee Management System (WEMS)

A production-ready Spring Boot 3.x application for managing warehouse employees, attendance, scheduling, safety, and certifications.

## Features
- Employee CRUD management
- Attendance clock in/out
- Shift scheduling
- Leave requests and approvals
- Safety incident reporting
- Certification tracking
- Role-based access control (RBAC)
- PostgreSQL with Flyway migrations
- OpenAPI/Swagger UI documentation
- Spring Boot Actuator endpoints

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

## Database Setup
1. Create a PostgreSQL database:
   ```sql
   CREATE DATABASE wems;
   CREATE USER wems_user WITH PASSWORD 'wems_pass';
   GRANT ALL PRIVILEGES ON DATABASE wems TO wems_user;
   ```
2. Flyway will auto-create tables on first run.

## Build Instructions
```bash
mvn clean install
```

## Run Instructions
```bash
mvn spring-boot:run
```

## API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Docs: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Security
- Default admin user: `admin` / `admin` (change in production!)
- JWT and API Key support (see `application.properties`)
- Roles: ADMIN, HR, SUPERVISOR, WORKER

## Testing
```bash
mvn test
```

## Project Structure
- `src/main/java/com/wems/` - Application source code
- `src/main/resources/db/migration/` - Flyway SQL migrations
- `src/main/resources/application.properties` - Configuration

## Modules
- **Employee**: CRUD, soft-delete, validation, pagination
- **Attendance**: Clock in/out, event tracking
- **Scheduling**: Shift templates, assignments
- **Leave**: Requests, approvals, balances
- **Safety**: Incident reporting, investigation
- **Certification**: Training, expiry alerts
- **Security**: RBAC, JWT, API Key
- **Common**: Exception handling

## License
MIT
