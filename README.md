# Warehouse Employee Management System

This is a comprehensive Spring Boot application for managing warehouse employees, including master data, scheduling, attendance, safety, and integrations.

## Build & Run

1. Ensure Java 17+ and Maven are installed.
2. Configure PostgreSQL database (see `application.yml`).
3. Run Flyway migrations automatically on startup.
4. Build:
   ```bash
   mvn clean install
   ```
5. Run:
   ```bash
   mvn spring-boot:run
   ```
6. Access API docs at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
7. Health check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Features
- Employee CRUD (REST API)
- Role-based access control (Spring Security)
- Flyway database migrations
- Actuator endpoints
- OpenAPI documentation
- Global exception handling
- Extensible for all 20 epics

## Project Structure
- `src/main/java/com/company/wms/` - Java source code
- `src/main/resources/` - configs and migrations
- `src/test/java/` - unit tests

## Contact
For questions, contact the engineering team.
