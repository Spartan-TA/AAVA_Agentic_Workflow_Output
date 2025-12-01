# Warehouse Employee Management System (EMS)

A production-ready Spring Boot application for managing warehouse employees, attendance, shifts, assets, safety, and more.

## Features
- Employee CRUD with RBAC (ADMIN, HR, SUPERVISOR, WORKER)
- PostgreSQL with Flyway migrations
- OAuth2 and API Key authentication
- Attendance, shift, leave, certification, asset, safety, and reporting modules
- RESTful APIs with OpenAPI/Swagger docs
- Modular, testable, and extensible codebase

## Requirements
- Java 17+
- Maven 3.8+
- PostgreSQL 13+

## Getting Started

### 1. Clone the repository
```
git clone <repo-url>
cd SpringBootProject
```

### 2. Configure the database
Edit `src/main/resources/application.yml` with your PostgreSQL credentials.

### 3. Build the project
```
mvn clean install
```

### 4. Run the application
```
mvn spring-boot:run
```

The app will start on [http://localhost:8080](http://localhost:8080)

### 5. API Documentation
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 6. Running Tests
```
mvn test
```

## Project Structure
- `src/main/java/com/warehouse/ems/` - Main Java source code
- `src/main/resources/` - Configuration and migration scripts
- `db/migration/` - Flyway SQL migrations

## Security
- OAuth2 JWT and API Key support
- RBAC enforced at endpoint and method level

## Contributing
Pull requests welcome! Please ensure code coverage and follow project conventions.

## License
MIT
