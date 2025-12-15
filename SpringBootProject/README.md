# Warehouse Employee Management System

A Spring Boot application for warehouse employee management, including employee, attendance, scheduling, and security modules.

## Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+

## Build Instructions
```sh
mvn clean install
```

## Run Instructions
```sh
mvn spring-boot:run
```

## Database Setup
- Create a PostgreSQL database named `warehouseems`.
- Flyway will auto-migrate all tables (see `db/migration/`).

## Access Points
- API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Default Credentials
- Username: `admin`
- Password: `admin` (BCrypt hash in `data.sql`)

## API Authentication
- JWT token via `/api/auth/login`
- API key in `X-API-Key` header (if enabled)

## Testing
```sh
mvn test
```

## Architecture
- Layered: Controller â Service â Repository â Entity
- Security: Spring Security with JWT/OAuth2, RBAC with 4 roles (ADMIN, HR, SUPERVISOR, WORKER)
- Documentation: OpenAPI 3.0 via SpringDoc

## Documentation
- All endpoints are documented via OpenAPI annotations and available in Swagger UI.
