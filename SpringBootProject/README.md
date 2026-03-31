# Warehouse Employee Management System (EMS)

A production-ready Spring Boot application for managing warehouse employees, attendance, scheduling, safety, assets, and more.

## Features
- Employee CRUD with RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- JWT & OAuth2 authentication
- Attendance, shift, leave, certification, asset, and safety management
- Payroll export, notifications, audit trail, reporting, and BI integration
- PWA-ready frontend (see `/pwa`)
- PostgreSQL, Flyway, Spring Data JPA, OpenAPI docs

## Requirements
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

## Getting Started

### 1. Clone the repository
```bash
git clone <repo-url>
cd SpringBootProject
```

### 2. Configure the database
Edit `src/main/resources/application.yml` with your PostgreSQL credentials.

### 3. Build the project
```bash
mvn clean install
```

### 4. Run the application
```bash
mvn spring-boot:run
```
The app will start on [http://localhost:8080](http://localhost:8080)

### 5. API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### 6. Health Check
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Project Structure
- `src/main/java/com/wms/ems/` - Main Java codebase
- `src/main/resources/db/migration/` - Flyway migration scripts
- `src/main/resources/application.yml` - Configuration

## Security
- JWT authentication enabled by default
- OAuth2 can be toggled via `application.yml`
- Roles: ADMIN, HR, SUPERVISOR, WORKER

## Database Migrations
- Flyway auto-applies migrations on startup

## Testing
```bash
mvn test
```

## License
MIT
